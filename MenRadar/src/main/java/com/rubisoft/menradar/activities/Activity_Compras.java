package com.rubisoft.menradar.activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.menradar.Adapters.Drawer_Adapter;
import com.rubisoft.menradar.Classes.Compra;
import com.rubisoft.menradar.Classes.Drawer_Item;
import com.rubisoft.menradar.Classes.Logro;
import com.rubisoft.menradar.Classes.Premium;
import com.rubisoft.menradar.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.menradar.R;
import com.rubisoft.menradar.tools.utils;

import org.json.JSONObject;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;

public class Activity_Compras extends AppCompatActivity  {
    private static final int REQUEST_CODE_COMPRA_ESTRELLAS = 1;
    private static final int REQUEST_CODE_SUSCRIBIR_PREMIUM = 2;
    private static final int BILLING_RESPONSE_RESULT_OK = 0;
    private static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    private static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    private static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    private static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    private static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    private static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    private static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    private static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    private SharedPreferences perfil_usuario;
    private TextView TextView_comprar_estrellas;
    private TextView TextView_suscribir_premium;
    private TextView TextView_ventajas_de_suscribir;
    private TextView TextView_ventajas_de_comprar;
	private FirebaseFirestore db;

    private ProgressBar mProgressBar;
    private RadioButton RadioButton_medio_anyo;
    private RadioButton RadioButton_1_anyo;
    private RadioButton RadioButton_5_estrellas;

    private RadioButton RadioButton_10_estrellas;
    private RadioButton RadioButton_20_estrellas;
    private RadioButton RadioButton_40_estrellas;

    private AppCompatImageView Button_comprar_estrellas;
    private AppCompatImageView Button_suscribirse;

    private String developerPayload;
	private LinearLayout Main_LinearLayout;

    @Nullable
    private IInAppBillingService mService;

    @Nullable
    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }

    };

    //navigation drawer
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout mDrawerLayout;
    private RecyclerView recyclerViewDrawer;
    private ImageView mImageView_PictureMain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            super.onCreate(savedInstanceState);
            if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
                perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
                if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
                    salir();
                } else {
					this.setContentView(R.layout.layout_compras);
					mProgressBar = this.findViewById(R.id.mProgressBar);
                    this.setup_toolbar();// Setup toolbar and statusBar (really FrameLayout)
                    this.preparar_servicio_billing();
					db = FirebaseFirestore.getInstance();

                    //instanciamos las Views, ponemos la tipografía y el texto correspondiente a cada View
                    this.setup_Views();
                    this.setTypeFace();
                    this.setText();
					inicializa_anuncios();
                    Drawable icono = new IconicsDrawable(this).icon(Icon.gmd_done).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));

                    //Creamos un listener para el botón de comprar estrellas
                    Button_comprar_estrellas.setImageDrawable(icono);
                    Button_comprar_estrellas.setOnClickListener(view -> {
						try {
							RadioGroup mRadioGroup = Activity_Compras.this.findViewById(R.id.Layout_compras_RadioGroup_compra);
							int id_radio_selected = mRadioGroup.getCheckedRadioButtonId();
							switch (id_radio_selected) {
								case R.id.Layout_compras_RadioButton_5_estrellas:
									Activity_Compras.this.comprar_estrellas(utils.get_SKU_compra_5_estrellas());
									break;
								case R.id.Layout_compras_RadioButton_10_estrellas:
									Activity_Compras.this.comprar_estrellas(utils.get_SKU_compra_10_estrellas());
									break;
								case R.id.Layout_compras_RadioButton_20_estrellas:
									Activity_Compras.this.comprar_estrellas(utils.get_SKU_compra_20_estrellas());
									break;
								case R.id.Layout_compras_RadioButton_40_estrellas:
									Activity_Compras.this.comprar_estrellas(utils.get_SKU_compra_40_estrellas());
									break;

							}
						}catch (Exception e){
							utils.registra_error(e.toString(), "onCreate (Button_comprar_estrellas) de compras");

						}
					});

                    //Creamos un listener para el botón de suscribirse
                    Button_suscribirse.setImageDrawable(icono);
                    Button_suscribirse.setOnClickListener(view -> {
						try {
							RadioGroup mRadioGroup = Activity_Compras.this.findViewById(R.id.Layout_compras_RadioGroup_suscribe);
							int id_radio_selected = mRadioGroup.getCheckedRadioButtonId();
							switch (id_radio_selected) {
								case R.id.Layout_compras_RadioButton_1_anyo:
									if (perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
										Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_MEJORAR_YA_ES_PREMIUM), Toast.LENGTH_LONG).show();
									} else {
										Activity_Compras.this.suscribir_premium(utils.get_SKU_suscribe_1_anyo_premium());
									}
									break;
								case R.id.Layout_compras_RadioButton_medio_anyo:
									if (perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
										Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_MEJORAR_YA_ES_PREMIUM), Toast.LENGTH_LONG).show();
									} else {
										Activity_Compras.this.suscribir_premium(utils.get_SKU_suscribe_medio_anyo_premium());
									}
									break;
							}
						}catch (Exception e){
							utils.registra_error(e.toString(), "onCreate (Button_suscribirse) de compras");

						}
					});
                }
            } else {
                Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mIntent);
                finish();
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onCreate de compras");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,  Intent data) {
        //Esta función es llamada cuando se ha finalizado la compra o suscripción de un producto
        //Tenemos que hacer los cambios pertinentes en nuestro Datastore
        super.onActivityResult(requestCode, resultCode, data);


		String token_socialauth=perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH),"");

        try {
            if (requestCode == REQUEST_CODE_COMPRA_ESTRELLAS) {
				//Si venimos de comprar estrellas
				procesa_compra_de_estrellas(resultCode,  data,token_socialauth);
			}else if (requestCode == REQUEST_CODE_SUSCRIBIR_PREMIUM) {
                //Si venimos de suscribirnos a la versión premium
				procesa_subscripcion_premium(resultCode,  data,token_socialauth);
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onActivityResult de Activity_Compras");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.menu_options, menu);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
			Appodeal.onResume(this, Appodeal.BANNER_TOP);

			this.setupNavigationDrawer();// Setup navigation drawer
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onResume de compras");
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Intent mIntent = new Intent(this, Activity_Principal.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mIntent);
        finish();
    }

	@Override
	protected void onDestroy() {
		if (null != mService) {
			this.unbindService(mServiceConn);
		}
		super.onDestroy();
	}

    private void setup_toolbar() {
        // Setup toolbar and statusBar (really FrameLayout)
        toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!utils.isTablet(this));
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void procesa_compra_de_estrellas(int resultCode, Intent data,String token_socialauth){
    	try {
			final int RESULT_OK = -1;
			final int RESULT_CANCELLED = 0;
			switch (resultCode) {
				case RESULT_OK:
					String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
					JSONObject mJSONObject = new JSONObject(purchaseData);
					Long estrellas_compradas = estrellas_compradas(mJSONObject.getString("productId"));

					if ((mJSONObject.getString("developerPayload").equals(developerPayload)) && (mJSONObject.getString("orderId").contains("GPA."))) {
						new AsyncTask_consume_purchase().execute(mJSONObject.getString("purchaseToken"));
						get_num_estrellas_y_registra_logro(token_socialauth, estrellas_compradas);
						actualiza_estrellas_en_Firestore(estrellas_compradas, token_socialauth);
						registra_compra_en_Firestore(token_socialauth, mJSONObject.getString("orderId"), mJSONObject.getString("purchaseToken"), (long) BILLING_RESPONSE_RESULT_OK);
						actualiza_estrellas_en_sharedpreferences(estrellas_compradas.intValue());                            //Para que ya se vea el icono de premium

						//Si la transacción era de compra de estrellas informamos al Usuario_para_listar que ha ido bien
						Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_EXITO_COMPRA), Toast.LENGTH_LONG).show();

						this.setupNavigationDrawer();
					} else {
						Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS), Toast.LENGTH_LONG).show();
					}
					break;
				case RESULT_CANCELLED:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_CANCELADO), Toast.LENGTH_LONG).show();
					break;
				default:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS), Toast.LENGTH_LONG).show();
					break;
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "procesa_compra_de_estrellas");
		}
	}

	private void procesa_subscripcion_premium(int resultCode, Intent data,String token_socialauth){
    	try{
			final int RESULT_OK = -1;
			final int RESULT_CANCELLED = 0;
			switch (resultCode) {
				case RESULT_OK:
					String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

					//Si ha ido bien, recojemos el objeto purchaseData con la información de la transacción
					JSONObject mJSONObject = new JSONObject(purchaseData);
					if ((mJSONObject.getString("developerPayload").equals(developerPayload)) && (mJSONObject.getString("orderId").contains("GPA."))) {
						//Registramos la transacción en el datastore
						registra_compra_en_Firestore(token_socialauth,mJSONObject.getString("orderId"),mJSONObject.getString("purchaseToken"),(long) BILLING_RESPONSE_RESULT_OK);

						//Si la transacción era de suscripción informamos al Usuario_para_listar que ha ido bien
						Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_EXITO_SUSCRIPCION), Toast.LENGTH_LONG).show();

						//actualizamos el sharedpreferences para que a partir de ahora permita las opciones premium
						actualiza_premium_en_SharedPreferences();

						Calendar fecha= Calendar.getInstance();

						//Actualizamos su perfil en el datastore guardando su purchaseToken como señal de que es un Usuario_para_listar premium
						if (mJSONObject.getString("productId").equals(utils.get_SKU_suscribe_1_anyo_premium())) {
							fecha.add(Calendar.YEAR,1);
							registra_premium_en_firestore(token_socialauth,fecha.getTimeInMillis());
						} else if (mJSONObject.getString("productId").equals(utils.get_SKU_suscribe_medio_anyo_premium())) {
							fecha.add(Calendar.MONTH,6);
							registra_premium_en_firestore(token_socialauth,fecha.getTimeInMillis());
						}
						actualiza_usuario_en_Firestore(token_socialauth);
						actualiza_estrellas_en_sharedpreferences(this.estrellas_compradas(mJSONObject.getString("productId")).intValue());

						//Para que ya se vea el icono de premium
						this.setupNavigationDrawer();
					} else {
						Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION), Toast.LENGTH_LONG).show();
					}
					break;
				case RESULT_CANCELLED:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_CANCELADO), Toast.LENGTH_LONG).show();
					break;
				default:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION), Toast.LENGTH_LONG).show();
					break;
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "procesa_subscripcion_premium");
		}
	}

	private void preparar_servicio_billing() {
        //preparamos el servicio
        try {
            Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            this.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "preparar_servicio_billing");
        }
    }

	private void setupNavigationDrawer() {
		try {
			if (mDrawerLayout!=null) {

				// Setup Drawer Icon
				drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
				mDrawerLayout.addDrawerListener(drawerToggle);
				drawerToggle.syncState();

				TypedValue typedValue = new TypedValue();
				int color = typedValue.data;
				mDrawerLayout.setStatusBarBackgroundColor(color);
			}
			// Setup RecyclerViews inside drawer
			setupNavigationDrawerRecyclerViews();

			mImageView_PictureMain = findViewById(R.id.mImageView_PictureMain);
			TextView mTextView_Name = findViewById(R.id.Drawer_TextView_Name);
			TextView mTextView_Numero_estrellas = findViewById(R.id.Drawer_TextView_Num_Estrellas);
			TextView mTextView_Premium_User = findViewById(R.id.Drawer_TextView_Premium_User);

			Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

			mTextView_Name.setTypeface(typeFace_roboto_Light);
			mTextView_Numero_estrellas.setTypeface(typeFace_roboto_Light);

			new AsyncTask_Coloca_PictureMain().execute();

			mTextView_Name.setText(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_NICK), ""));
			mTextView_Name.setTextColor(ContextCompat.getColor(this, R.color.white));

			mTextView_Numero_estrellas.setText(String.format(getResources().getString(R.string.numero),perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), 0)));
			mTextView_Numero_estrellas.setTextColor(ContextCompat.getColor(this, R.color.white));

			if (perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)){
				mTextView_Premium_User.setText(getResources().getString(R.string.premium_user));
			}
			Drawable icono_estrella;
			icono_estrella = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_star).color(ContextCompat.getColor(this, R.color.white));
			ImageView mImageView_Estrella = findViewById(R.id.Drawer_ImageView_estrella);
			mImageView_Estrella.setImageDrawable(icono_estrella);

		} catch (Exception e) {
			utils.registra_error(e.toString(), "setupNavigationDrawer");
		}
	}

    private void setupNavigationDrawerRecyclerViews() {
        try {
            recyclerViewDrawer = findViewById(R.id.recyclerViewDrawer);
            recyclerViewDrawer.setLayoutManager(new LinearLayoutManager(this));

            ArrayList<Drawer_Item> drawerItems = new ArrayList<>();
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_track_changes).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[0]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_tune).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[1]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_email).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[2]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_perm_identity).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[3]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_star_border).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[4]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_attach_money).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[5]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_forum).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[6]));
            //drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_whatshot).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[7]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_help_outline).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[7]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[8]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_thumbs_up_down).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[9]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_power_settings_new).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[10]));
            RecyclerView.Adapter drawerAdapter = new Drawer_Adapter(drawerItems);
            recyclerViewDrawer.setAdapter(drawerAdapter);
            recyclerViewDrawer.setMinimumHeight(utils.Dp2Px(144, this));
            recyclerViewDrawer.setHasFixedSize(true);
            crea_ItemTouchListener_menu();

            Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
            for (int i = 0; i < recyclerViewDrawer.getChildCount(); i++) {
                TextView Drawer_item_TextView_title = recyclerViewDrawer.getChildAt(i).findViewById(R.id.Drawer_item_TextView_title);
                Drawer_item_TextView_title.setTypeface(typeFace_roboto_Light);
            }
        } catch (Exception ignored) {
        }
    }

    private void crea_ItemTouchListener_menu() {
        //Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
        //y que lo ha detectado y codificado primero el  RecyclerTouchListener
        recyclerViewDrawer.addOnItemTouchListener(new Activity_Compras.RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Compras.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				 mDrawerLayout.closeDrawers();
			}
		}));
    }

	private void suscribir_premium(String SKU) {
		try {
			RandomString();
			while (null == mService) {
				Thread.sleep(250);
			}
			Bundle buyIntentBundle = mService.getBuyIntent(3, this.getPackageName(), SKU, "inapp", developerPayload);

			switch ((Integer) buyIntentBundle.get("RESPONSE_CODE")) {
				case BILLING_RESPONSE_RESULT_OK:
					PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
					this.startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_SUSCRIBIR_PREMIUM, new Intent(), 0, 0, 0);
					break;
				case BILLING_RESPONSE_RESULT_USER_CANCELED:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_CANCELADO), Toast.LENGTH_LONG).show();
					break;
				case BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION) + " BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					break;
				case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION) + " BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					break;
				case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION) + " BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					break;
				case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION) + " BILLING_RESPONSE_RESULT_DEVELOPER_ERROR", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					break;
				case BILLING_RESPONSE_RESULT_ERROR:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION) + " BILLING_RESPONSE_RESULT_ERROR", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					break;
				case BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION) + " BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					break;
				case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
					Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_SUSCRIPCION) + " BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					break;
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "suscribir_premium");
		}
	}

    private void comprar_estrellas(String SKU) {
        //Esta función llama a las utils necesarias para pagar la compra de estrellas. Como es un pendingintent
        //cuando termine se ejecutara onActivityResult
        try {
            RandomString();
            Bundle buyIntentBundle = mService.getBuyIntent(3, this.getPackageName(), SKU, "inapp", developerPayload);

            switch ((Integer) buyIntentBundle.get("RESPONSE_CODE")) {
                case BILLING_RESPONSE_RESULT_OK:
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    this.startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_COMPRA_ESTRELLAS, new Intent(), 0, 0, 0);
                    break;
                case BILLING_RESPONSE_RESULT_USER_CANCELED:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_CANCELADO), Toast.LENGTH_LONG).show();
                    break;
                case BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS) + " BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    // new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    break;
                case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS) + " BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    break;
                case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS) + " BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    break;
                case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS) + " BILLING_RESPONSE_RESULT_DEVELOPER_ERROR", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    break;
                case BILLING_RESPONSE_RESULT_ERROR:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS) + " BILLING_RESPONSE_RESULT_ERROR", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    break;
                case BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS) + " BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    break;
                case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
                    Toast.makeText(this, this.getResources().getString(R.string.ACTIVITY_MEJORAR_ERROR_COMPRA_ESTRELLAS) + " BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED", Toast.LENGTH_LONG).show();
					registra_error_en_compra(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
					//new AsyncTask_actualizar_errores_en_compras().execute(((Integer) buyIntentBundle.get("RESPONSE_CODE")).longValue());
                    break;
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "comprar_estrellas");
        }
    }

    private void setTypeFace() {
        try {
            Typeface typeFace_roboto_light = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
            Typeface typeFace_roboto_bold = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Bold.ttf");

            RadioButton_1_anyo.setTypeface(typeFace_roboto_light);
            RadioButton_medio_anyo.setTypeface(typeFace_roboto_light);
            RadioButton_5_estrellas.setTypeface(typeFace_roboto_light);
            RadioButton_10_estrellas.setTypeface(typeFace_roboto_light);
            RadioButton_20_estrellas.setTypeface(typeFace_roboto_light);
            RadioButton_40_estrellas.setTypeface(typeFace_roboto_light);

            TextView_suscribir_premium.setTypeface(typeFace_roboto_bold);
            TextView_comprar_estrellas.setTypeface(typeFace_roboto_bold);

            TextView_ventajas_de_comprar.setTypeface(typeFace_roboto_light);
            TextView_ventajas_de_suscribir.setTypeface(typeFace_roboto_light);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "setTypeFace");
        }
    }

    private void setup_Views() {
        try {
			Main_LinearLayout = findViewById(R.id.Main_LinearLayout);
			mDrawerLayout = findViewById(R.id.mDrawerLayout);

            TextView_comprar_estrellas = this.findViewById(R.id.Layout_compras_TextView_comprar_estrellas);
            TextView_suscribir_premium = this.findViewById(R.id.Layout_compras_TextView_suscribir_premium);

            TextView_ventajas_de_suscribir = this.findViewById(R.id.Layout_compras_TextView_ventajas_de_suscribir);
            TextView_ventajas_de_comprar = this.findViewById(R.id.Layout_compras_TextView_ventajas_de_comprar);
            RadioButton_5_estrellas = this.findViewById(R.id.Layout_compras_RadioButton_5_estrellas);
            RadioButton_10_estrellas = this.findViewById(R.id.Layout_compras_RadioButton_10_estrellas);
            RadioButton_20_estrellas = this.findViewById(R.id.Layout_compras_RadioButton_20_estrellas);
            RadioButton_40_estrellas = this.findViewById(R.id.Layout_compras_RadioButton_40_estrellas);

            RadioButton_1_anyo = this.findViewById(R.id.Layout_compras_RadioButton_1_anyo);
            RadioButton_medio_anyo = this.findViewById(R.id.Layout_compras_RadioButton_medio_anyo);

            Button_comprar_estrellas = this.findViewById(R.id.Layout_compras_Button_comprar);
            Button_suscribirse = this.findViewById(R.id.Layout_compras_Button_suscribirse);

			if (perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				RadioButton_1_anyo.setEnabled(false);
				RadioButton_medio_anyo.setEnabled(false);
			} else {
				RadioButton_1_anyo.setEnabled(true);
				RadioButton_medio_anyo.setEnabled(true);
			}
        } catch (Exception e) {
            utils.registra_error(e.toString(), "setup_Views");
        }
    }

    private void setText() {
        try {
            TextView_suscribir_premium.setText(this.getResources().getString(R.string.ACTIVITY_MEJORAR_TITULO_SUSCRIBIR));
            TextView_comprar_estrellas.setText(this.getResources().getString(R.string.ACTIVITY_MEJORAR_TITULO_COMPRAR));
            TextView_ventajas_de_suscribir.setText(String.format(this.getResources().getString(R.string.ventajas_de_suscribir), this.getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM)));
            TextView_ventajas_de_comprar.setText(this.getResources().getString(R.string.ventajas_de_comprar));
            /*if (utils.isFromGooglePlay(this)) {*/
                new AsyncTask_set_prices_from_google().execute();
         /*   }else{
                setup_prices_statically();
            }*/
        } catch (Exception e) {
            utils.registra_error(e.toString(), "setText");
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
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.setMargins(0, 0, 0, 0);
				if (mDrawerLayout!=null){
					mDrawerLayout.setLayoutParams(layoutParams);
				}else{
					Main_LinearLayout.setLayoutParams(layoutParams);
				}
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
			utils.registra_error(e.toString(), "setup_banner de Activity_Compras");
		}
	}

	private void RandomString() {
        SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        developerPayload = Arrays.toString(bytes);
    }

    private Long estrellas_compradas(@NonNull String un_SKU) {
        //esta función devuelve el número de estrellas en función de su identificador SKU
        long num = 0L;
        try {
            if (un_SKU.equals(utils.get_SKU_compra_5_estrellas())) {
                num = (Integer.valueOf(this.getResources().getInteger(R.integer.LOGRO_COMPRA_5_ESTRELLAS))).longValue();
            }else if (un_SKU.equals(utils.get_SKU_compra_10_estrellas())) {
                num = (Integer.valueOf(this.getResources().getInteger(R.integer.LOGRO_COMPRA_10_ESTRELLAS))).longValue();
            } else if (un_SKU.equals(utils.get_SKU_compra_20_estrellas())) {
                num = (Integer.valueOf(this.getResources().getInteger(R.integer.LOGRO_COMPRA_20_ESTRELLAS))).longValue();
            } else if (un_SKU.equals(utils.get_SKU_compra_40_estrellas())) {
                num = (Integer.valueOf(this.getResources().getInteger(R.integer.LOGRO_COMPRA_40_ESTRELLAS))).longValue();
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "estrellas_compradas de Activity_Compras");
        }
        return num;
    }

    private void salir() {
        //Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir

        finish();
    }

    private class AsyncTask_Coloca_PictureMain extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
        }

        @Nullable
        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap mBitmap = null;
            try {

                String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), 0);
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                File file = new File(storageDir, nombre_thumb);
                mBitmap = file.exists() ? utils.decodeSampledBitmapFromFilePath(file.getPath(), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles)) : null;
            } catch (Exception ignored) {
            } return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap mBitmap) {
            try {
                AppCompatImageView mAppCompatImageView = new AppCompatImageView(getApplicationContext());
                LayoutParams mlayoutParams_perfil = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
                mAppCompatImageView.setLayoutParams(mlayoutParams_perfil);
                if (mBitmap != null) {
                    mAppCompatImageView.setImageBitmap(mBitmap);
                    mAppCompatImageView.setScaleType(ScaleType.CENTER_CROP);
                } else {
                    mAppCompatImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
                }
                mImageView_PictureMain.setImageDrawable(mAppCompatImageView.getDrawable());
            }catch (Exception e) {
                utils.registra_error(e.toString(), "AsyncTask_Coloca_PictureMain");
            }
        }
    }

    private void actualiza_premium_en_SharedPreferences(){
    	try {
			SharedPreferences preferencias_usuario = this.getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
			SharedPreferences.Editor editor_perfil_usuario = preferencias_usuario.edit();
			editor_perfil_usuario.putBoolean(this.getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), true);
			editor_perfil_usuario.apply();
		}catch (Exception e){
			utils.registra_error(e.toString(), "actualiza_premium_en_SharedPreferences de Activity_Compras");
		}
	}

	private void actualiza_estrellas_en_Firestore(Long estrellas_compradas,String token_socialauth){
		db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).update(getResources().getString(R.string.USUARIO_ESTRELLAS), FieldValue.increment(estrellas_compradas));
	}

    private void actualiza_usuario_en_Firestore( String token_socialauth){
		db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).update(getResources().getString(R.string.USUARIO_ES_PREMIUM),true);
	}

	private void get_num_estrellas_y_registra_logro(String token_socialauth, Long estrellas_compradas){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				try {
					DocumentSnapshot document = task.getResult();
					if (document.exists()) {
						Long num_estrellas_actuales = (Long) document.get(getResources().getString(R.string.USUARIO_ESTRELLAS));
						registra_logro_en_Firestore(token_socialauth, estrellas_compradas, num_estrellas_actuales);
					}
				}catch (Exception e){
					utils.registra_error(e.toString(), "get_num_estrellas_y_registra_logro de Activity_Compras");
				}
			}
		});
	}

    private void registra_logro_en_Firestore(String token_socialauth,Long num_estrellas_compradas, Long num_estrellas_actual){
    	try {
			Logro un_logro = null;
			Long total_estrellas = num_estrellas_compradas + num_estrellas_actual;
			Long fecha = Calendar.getInstance().getTimeInMillis();
			switch (num_estrellas_compradas.intValue()) {
				case 5:
					int motivo5 = getResources().getInteger(R.integer.MOTIVO_COMPRA_5_ESTRELLAS);
					un_logro = new Logro(Integer.valueOf(motivo5).longValue(), Integer.valueOf(getResources().getInteger(R.integer.LOGRO_COMPRA_5_ESTRELLAS)).longValue(), fecha, total_estrellas);
					break;
				case 10:
					int motivo10 = getResources().getInteger(R.integer.MOTIVO_COMPRA_10_ESTRELLAS);
					un_logro = new Logro(Integer.valueOf(motivo10).longValue(), Integer.valueOf(getResources().getInteger(R.integer.LOGRO_COMPRA_10_ESTRELLAS)).longValue(), fecha, total_estrellas);
					break;
				case 20:
					int motivo20 = getResources().getInteger(R.integer.MOTIVO_COMPRA_20_ESTRELLAS);
					un_logro = new Logro(Integer.valueOf(motivo20).longValue(), Integer.valueOf(getResources().getInteger(R.integer.LOGRO_COMPRA_20_ESTRELLAS)).longValue(), fecha, total_estrellas);
					break;
				case 40:
					int motivo40 = getResources().getInteger(R.integer.MOTIVO_COMPRA_40_ESTRELLAS);
					un_logro = new Logro(Integer.valueOf(motivo40).longValue(), Integer.valueOf(getResources().getInteger(R.integer.LOGRO_COMPRA_40_ESTRELLAS)).longValue(), fecha, total_estrellas);
					break;

			}
			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).collection(getResources().getString(R.string.LOGROS)).add(un_logro);
		}catch (Exception e){
			utils.registra_error(e.toString(), "registra_logro_en_Firestore de Activity_Compras");
		}
	}

	private void registra_compra_en_Firestore(String token_socialauth,String un_order_id,String un_token_compra,Long un_response_code){
		Compra una_compra=new Compra(un_order_id,token_socialauth,un_token_compra,un_response_code);
		db.collection(getResources().getString(R.string.COMPRAS)).add(una_compra);
	}

	private void registra_premium_en_firestore(String token_socialauth,Long fecha_limite) {
		Premium un_premium = new Premium(fecha_limite);
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.PREMIUMS)).document(token_socialauth).set(un_premium);
	}

	private class AsyncTask_set_prices_from_google extends AsyncTask<Void, Void, ArrayList<String>> {
        //Esta función consulta el precio de cada producto que tenemos a la venta para averiguar su precio
        @Override
        protected void onPreExecute() {
            if (mProgressBar != null) {
                utils.setProgressBar_visibility(mProgressBar, View.VISIBLE);
            }

        }

        @Nullable
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            Bundle skuDetails;
            Bundle querySkus;
            ArrayList<String> lista_resultado = new ArrayList<>();
            ArrayList<String> skuList;
            try {
                //Esperamos un poco a que el servicio esté disponible
                while (null == mService) {
                    Thread.sleep(250);
                }
                //Creamos un arraylist con los identificadores SKU de cada producto de compra
                skuList = new ArrayList<>();
                skuList.add(utils.get_SKU_compra_5_estrellas());
                skuList.add(utils.get_SKU_compra_10_estrellas());
                skuList.add(utils.get_SKU_compra_20_estrellas());
                skuList.add(utils.get_SKU_compra_40_estrellas());
                skuList.add(utils.get_SKU_suscribe_1_anyo_premium());
                skuList.add(utils.get_SKU_suscribe_medio_anyo_premium());

                querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);


                //Hacemos la consulta
                skuDetails = mService.getSkuDetails(3, Activity_Compras.this.getPackageName(), "inapp", querySkus);

                //Recogemos el resultado en un list
                if (0 == skuDetails.getInt("RESPONSE_CODE")) {
                    lista_resultado.addAll(skuDetails.getStringArrayList("DETAILS_LIST"));
                }
			}catch (Exception e) {
                return null;
            }
            return lista_resultado;
        }

        @Override
        protected void onPostExecute(@Nullable ArrayList<String> responseList) {
            //Con el listado de precios obtenido rellenamos los textos correspondientes
            try {

                if (null != responseList) {
                    for (String thisResponse : responseList) {

                        JSONObject object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");
                        String price = object.getString("price");
                        if (sku.equals(utils.get_SKU_compra_5_estrellas())) {
                            RadioButton_5_estrellas.setText(String.format(Activity_Compras.this.getResources().getString(R.string.ACTIVITY_MEJORAR_COMPRA_X_ESTRELLAS), 5, price));
                        }else if (sku.equals(utils.get_SKU_compra_10_estrellas())) {
                            RadioButton_10_estrellas.setText(String.format(Activity_Compras.this.getResources().getString(R.string.ACTIVITY_MEJORAR_COMPRA_X_ESTRELLAS), 10, price));
                        } else if (sku.equals(utils.get_SKU_compra_20_estrellas())) {
                            RadioButton_20_estrellas.setText(String.format(Activity_Compras.this.getResources().getString(R.string.ACTIVITY_MEJORAR_COMPRA_X_ESTRELLAS), 20, price));
                        } else if (sku.equals(utils.get_SKU_compra_40_estrellas())) {
                            RadioButton_40_estrellas.setText(String.format(Activity_Compras.this.getResources().getString(R.string.ACTIVITY_MEJORAR_COMPRA_X_ESTRELLAS), 40, price));
                        } else if (sku.equals(utils.get_SKU_suscribe_1_anyo_premium())) {
                            RadioButton_1_anyo.setText(String.format(Activity_Compras.this.getResources().getString(R.string.ACTIVITY_MEJORAR_SUSCRIBE_1_AÑO), price));
                        } else if (sku.equals(utils.get_SKU_suscribe_medio_anyo_premium())) {
                            RadioButton_medio_anyo.setText(String.format(Activity_Compras.this.getResources().getString(R.string.ACTIVITY_MEJORAR_SUSCRIBE_6_MESES), price));
                        }
                    }
                }
			} catch (Exception e) {
                utils.registra_error(e.toString(), "AsyncTask_set_prices_from_google");
            } finally {
                if (mProgressBar != null) {
                    utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);
                }
            }
        }

    }

	//las estrellas las tenemos que consumir inmediatamente. Si no, no podría volver a comprar esa cantidad de estrellas otra vez
    private class AsyncTask_consume_purchase extends AsyncTask<String, Void, Exception> {
        //Esta función consume una compra de estrellas para que vuelva a estar disponible inmediatamente
        @Nullable
        @Override
        protected Exception doInBackground(String... params) {
            Exception resultado = null;
            try {
                mService.consumePurchase(3, Activity_Compras.this.getPackageName(), params[0]);
            } catch (Exception e) {
                resultado = e;
            }
            return resultado;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e != null) {
                utils.registra_error(e.toString(), "AsyncTask_consume_purchase");
            }
        }
    }

    private void registra_error_en_compra(Long codigo_error){
		String token_socialauth= perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH),"");
		Compra una_compra=new Compra(null,token_socialauth,null,codigo_error);
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.COMPRAS)).add(una_compra);
	}

	private void actualiza_estrellas_en_sharedpreferences(Integer nuevas_estrellas){
    	try {
			long num_estrellas_actual = perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), 0);
			long nuevo_num_estrellas = num_estrellas_actual + nuevas_estrellas;
			if (nuevo_num_estrellas < 0) {
				nuevo_num_estrellas = 0;
			}
			SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), nuevo_num_estrellas);
			editor_perfil_usuario.apply();
		}catch (Exception e){
			utils.registra_error(e.toString(), "actualiza_estrellas_en_sharedpreferences de Activity_Compras");
		}
	}

	private class RecyclerTouchListener_menu implements OnItemTouchListener {
        @Nullable
        private final GestureDetector mGestureDetector;
        private final Interface_ClickListener_Menu mInterfaceClickListener;

        RecyclerTouchListener_menu(Context context, @NonNull RecyclerView recyclerView, Interface_ClickListener_Menu un_Interface_ClickListener) {
			mInterfaceClickListener = un_Interface_ClickListener;
			mGestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {

				@Override
				public boolean onSingleTapUp(@NonNull MotionEvent e) {
					View child = recyclerViewDrawer.findChildViewUnder(e.getX(), e.getY());
					if ((child != null) && (mInterfaceClickListener != null)) {
						mInterfaceClickListener.My_onClick(child, recyclerView.getChildLayoutPosition(child));
					}
					return true;
				}

			});
		}

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            //mGestureDetector.onTouchEvent llamará al método mas adecuado
            mGestureDetector.onTouchEvent(e);
            return false;
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }
    }
}