package com.rubisoft.gayradar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rubisoft.gayradar.R;
import com.rubisoft.gayradar.tools.utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class Activity_Un_Perfil extends AppCompatActivity {
	private final String TAG_FOTO_ACTUAL = "FOTO_ACTUAL";
    private SharedPreferences perfil_usuario;
    private int foto_actual; //para que se siga viendo la misma foto si cambia de landscape/portrait
    private int de_donde_vengo;
    private LinearLayout mLinearLayout_miniaturas;
    private AppCompatImageView expanded_image;
    private String token_socialauth_de_la_otra_persona;
    private static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        //ESTA FUNCIÓN FUSIONA DOS IMAGENES EN UNA
        Bitmap bmOverlay = null;
        try {
            bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(bmp1, new Matrix(), null);
            canvas.drawBitmap(bmp2, bmp1.getWidth() / 2 - bmp2.getWidth() / 2, bmp1.getHeight() / 2 - bmp2.getHeight() / 2, null);
        } catch (Exception e) {
			utils.registra_error(e.toString(), "overlay de un_perfil");
        }
        return bmOverlay;

    }

	private RelativeLayout Main_LinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

        super.onCreate(savedInstanceState);
        try {
            if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {

				setContentView(R.layout.layout_un_perfil);


				setup_sharedprefernces();
                if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
                    salir();
                } else {
                    //El token_socialauth de nuestro interlocutor lo recibimos como parámetro extra de nuestro intent.
                    Bundle bundle = getIntent().getExtras();
                    token_socialauth_de_la_otra_persona = bundle.getString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), "");
                    de_donde_vengo = bundle.getInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO));
                    setup_views();
					inicializa_anuncios();
					foto_actual = savedInstanceState != null ? savedInstanceState.getInt(TAG_FOTO_ACTUAL) : 0;

                    descarga_foto_grande(foto_actual);

                    carga_miniaturas();
                }
            } else {
                Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                finish();
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onCreate de un_perfil");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        utils.gestiona_menu(item.getItemId(), this);

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TAG_FOTO_ACTUAL, foto_actual);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        //Tenemos que registrar el receiver cada vez que pasamos por onResume
        //Este receiver permanece escuchando por si el otro Usuario_para_listar manda un mensaje
        //y así poder mostrarlo en tiempo real
        super.onResume();

        try {

			Appodeal.onResume(this, Appodeal.BANNER_TOP);

        } catch (Exception e) {
            utils.registra_error(e.toString(), "onResume de un_perfil");
        }
    }

    @Override
    public void onBackPressed() {
        Intent mIntent;
        switch (de_donde_vengo) {
            case 1: //VENGO DE PRINCIPAL
                // debemos recordar la pagina donde estaba
                mIntent = new Intent(this, Activity_Principal.class);
                Bundle Bundle_destino = new Bundle();
                Bundle Bundle_origen = getIntent().getExtras();
                Bundle_destino.putInt(getResources().getString(R.string.PAGINA), Bundle_origen.getInt(getResources().getString(R.string.PAGINA)));
                mIntent.putExtras(Bundle_destino);

                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                finish();
                break;
            case 7: //VENGO DE MENSAJES

                // vamos a una Activity_Un_Perfil
                mIntent = new Intent(this, Activity_Mensajes.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                finish();
                break;
            case 3: //VENGO DE CHAT GENERAL
                // vamos a una Activity_Un_Perfil
                mIntent = new Intent(this, Activity_Chat_General.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                finish();
                break;
            default:
                mIntent = new Intent(this, Activity_Principal.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                finish();
                break;
        }
    }

	private void inicializa_anuncios(){
		try{
			if (!perfil_usuario.getBoolean(getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				Consent consent = ConsentManager.getInstance(this).getConsent();
				Appodeal.setTesting(false);
				Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER, consent);
				setup_banner();
			}else {
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.setMargins(0, 0, 0, 0);
				Main_LinearLayout.setLayoutParams(layoutParams);
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "inicializa_anuncios de Activity_Principal");
		}
	}

	private void setup_banner() {
		try {
			Appodeal.setBannerCallbacks(new BannerCallbacks() {
				@Override
				public void onBannerLoaded(int height, boolean isPrecache) {

				}
				@Override
				public void onBannerFailedToLoad() {

				}
				@Override
				public void onBannerShown() {

				}
				@Override
				public void onBannerClicked() {

				}
				@Override
				public void onBannerExpired() {

				}
				@Override
				public void onBannerShowFailed() {

				}
			});
			Appodeal.show(this, Appodeal.BANNER_TOP);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_banner de Activity_Ayuda");
		}
	}

	private void carga_miniaturas() {
        try {
            switch (de_donde_vengo) {
                case 1: //VENGO_DE_PRINCIPAL
                case 4: //VENGO_DE_MIS_SOLICITUDES_EN_ESPERA
                case 5: //VENGO_DE_MIS_SOLICITUDES_RECHAZADAS

                    //fotos_privadas_desde_bundle();
                    for (int i = 0; i < getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM); i++) {
                        carga_miniaturas_con_llavecitas(i);
                    }
                    break;
                case 2: //VENGO_DE_MIS_SOLICITUDES_ACEPTADAS

                    for (int i = 0; i < getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM); i++) {
                        try {
                            LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.tamanyo_miniaturas_perfil), (int) getResources().getDimension(R.dimen.tamanyo_miniaturas_perfil));
                            AppCompatImageView mImageView = new AppCompatImageView(this);
                            mImageView.setLayoutParams(mLayoutParams);
                            mImageView.setPadding(3, 3, 15, 3);
                            descarga_Thumb(mImageView, i);
                            mLinearLayout_miniaturas.addView(mImageView);
                        }catch (RejectedExecutionException e){
                            try {
                                Thread.sleep(500);
                            }catch (Exception ignored){}
                        }
                    }
                    break;
                case 3: //VENGO_DE_CHAT_GENERAL
					//get_fotos_privadas();
                    //new AsyncTask_getFotosPrivadas().execute();
                    break;
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "carga_miniaturas de un_perfil");
        }
    }

    private void carga_miniaturas_con_llavecitas(int i) {
        try {
            LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.tamanyo_miniaturas_perfil), (int) getResources().getDimension(R.dimen.tamanyo_miniaturas_perfil));

            AppCompatImageView mImageView = new AppCompatImageView(this);
            mImageView.setLayoutParams(mLayoutParams);
            mImageView.setPadding(3, 3, 15, 3);

			descarga_Thumb(mImageView, i);

			if (mLinearLayout_miniaturas!=null) {
                mLinearLayout_miniaturas.addView(mImageView);
            }
        }catch (RejectedExecutionException e){
            try {
                Thread.sleep(500);
            }catch (Exception ignored){}
        } catch (Exception e) {
            utils.registra_error(e.toString(), "carga_miniaturas_con_llavecitas");
        }
    }

    private void setup_views() {
        try {
            mLinearLayout_miniaturas = findViewById(R.id.Layout_un_perfil_LinearLayout_miniaturas);
            expanded_image = findViewById(R.id.Layout_un_perfil_ImageView_expanded_image);
			Main_LinearLayout = findViewById(R.id.Main_LinearLayout);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "setup_views de un_perfil");
        }
    }

    private void setup_sharedprefernces() {
        perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
    }

    private Bitmap getBitmap_rectangle() {
        Bitmap bitmap = null;
        try {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.rectangulo_blanco_transparente, null);
            Canvas canvas = new Canvas();
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "getBitmap_rectangle");
        }
        return bitmap;
    }

    private void salir() {
        //Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir
        finish();
    }

    private void descarga_Thumb (AppCompatImageView mImageView, Integer num_foto){
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_thumb(token_socialauth_de_la_otra_persona, num_foto));

            Target target = new Target() {
                @Override
                public void onPrepareLoad(Drawable arg0) {

                }

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                    mImageView.setImageBitmap(bitmap);

                    mImageView.setOnClickListener(v -> {
						foto_actual = num_foto;
						descarga_foto_grande(foto_actual); //ponemos la imagen grande
					});
                    //mLinearLayout_miniaturas.invalidate();
                }

                @Override
                public void onBitmapFailed(Drawable arg0) {
                    //mImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light))); /*BitmapFactory.decodeResource(getResources(), R.drawable.no_pic)*/
                }
            };
            mImageView.setTag(target);
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
				try {

					Picasso.with(getApplicationContext())
							.load(uri)
							.placeholder(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))   // optional
							.error(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))       // optional
							.centerCrop()
							.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))                        // optional
							.into(target);

				} catch (Exception ignored) {
				}
			}).addOnFailureListener(e -> {
				// mImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
				//mImageView.setAlpha(0.4f);
			});
        }catch (RejectedExecutionException e){
            try {
                Thread.sleep(500);
            }catch (Exception ignored){}
        }
    }

    private void descarga_foto_grande(Integer num_foto){
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_foto(token_socialauth_de_la_otra_persona, num_foto));

            Target target = new Target() {
                @Override
                public void onPrepareLoad(Drawable arg0) {

                }

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                    expanded_image.setImageBitmap(bitmap);
                    expanded_image.setAlpha(1f);
                    expanded_image.invalidate();
                }

                @Override
                public void onBitmapFailed(Drawable arg0) {
                    expanded_image.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary))); /*BitmapFactory.decodeResource(getResources(), R.drawable.no_pic)*/
                }
            };
            expanded_image.setTag(target);

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
				try {
					if (getApplicationContext() != null) { //puede que ya no estemos en la activity

						Picasso.with(getApplicationContext())
								.load(uri)
								.placeholder(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary)))   // optional
								.error(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary)))       // optional
								.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))// optional
								.centerCrop()
								.into(target);

					}
				} catch (Exception ignored) {
				}
			}).addOnFailureListener(e -> {
				expanded_image.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary))); /*BitmapFactory.decodeResource(getResources(), R.drawable.no_pic)*/
			});
        }catch (RejectedExecutionException e){
            try {
                Thread.sleep(500);
            }catch (Exception ignored){}
        }catch (Exception e) {
            utils.registra_error(e.toString(), "descarga_foto_grande");
        }

    }
}