package com.rubisoft.gayradar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.gayradar.Adapters.Drawer_Adapter;
import com.rubisoft.gayradar.BuildConfig;
import com.rubisoft.gayradar.Classes.Drawer_Item;
import com.rubisoft.gayradar.Classes.Feedback;
import com.rubisoft.gayradar.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.gayradar.R;
import com.rubisoft.gayradar.databinding.LayoutFeedbackBinding;
import com.rubisoft.gayradar.tools.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//*************************************************************************************************************/
//*
//* EN ESTA ACTIVITY HAY ALBERGADOS 2 FRAGMENTS: FRAGMENT_LOCALIZACION, FRAGMENT_PREFERENCIAS
//* . A LOS 2 SE ACCEDE POR TABS QUE SE ENCUENTRAN EN LA PARTE SUPERIOR. PARA IMPLEMENTAR ESTO
//* UTILIZAMOS LA CLASE TabLayout. TAMBIÉN NECESITAMO UN VIEWPAGER PARA PODER CAMBIAR DE UN FRAGMENT A OTRO
//*
//*************************************************************************************************************/

public class Activity_Feedback extends AppCompatActivity {
	private SharedPreferences perfil_usuario;
	private FirebaseFirestore db;

    //navigation drawer
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerViewDrawer;
    private ImageView mImageView_PictureMain;

    private String eleccion="";
    private ScrollView scrollview;

	private LayoutFeedbackBinding binding;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        super.onCreate(savedInstanceState);
        try {
            perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

            if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
                salir();
            } else {
				binding = LayoutFeedbackBinding.inflate(getLayoutInflater());
				setContentView(binding.getRoot());
                setTypefaces();
                setTexts();
				inicializa_anuncios();
                setup_toolbar();// Setup toolbar and statusBar (really FrameLayout)
				db = FirebaseFirestore.getInstance();

				binding.LayoutFeedbackRadioGroupMeGusta.setOnCheckedChangeListener((radioGroup, i) -> {
					switch (i) {
						case R.id.Layout_feedback_RadioButton_si_me_gusta:
							binding.LayoutFeedbackCardViewWriteSuggestion.setVisibility(View.INVISIBLE);
							binding.LayoutFeedbackCardViewRateApp.setVisibility(View.VISIBLE);

							break;
						case R.id.Layout_feedback_RadioButton_no_me_gusta:
							binding.LayoutFeedbackCardViewWriteSuggestion.setVisibility(View.VISIBLE);
							binding.LayoutFeedbackCardViewRateApp.setVisibility(View.INVISIBLE);
							break;
					}
				});

                //Creamos un listener para el botón de rate app
                binding.LayoutFeedbackButtonRateApp.setOnClickListener(view -> {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://details?id="+getApplication().getPackageName()));
						startActivity(intent);
					} catch (Exception e) { //google play app is not installed
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+getApplication().getPackageName()));
						startActivity(intent);
					}
				});

                //Creamos un listener para el botón de feedback
                binding.LayoutFeedbackRadioGroupEncuesta.setOnCheckedChangeListener((radioGroup, i) -> {
					switch (i) {
						case R.id.Layout_feedback_RadioButton_poca_gente:
							eleccion = "Poca gente";
							break;
						case R.id.Layout_feedback_RadioButton_mal_disenyo:
							eleccion = "Mal diseño";
							break;
						case R.id.Layout_feedback_RadioButton_mal_traducida:
							eleccion = "Mal traducida";
							break;
						case R.id.Layout_feedback_RadioButton_no_funciona_bien:
							eleccion = "No funciona bien";
							break;
						case R.id.Layout_feedback_RadioButton_no_se_como_va:
							eleccion = "Soy tonto y no sé cómo va";
							break;
						case R.id.Layout_feedback_RadioButton_va_lenta:
							eleccion = "Va lenta";
							break;
						case R.id.Layout_feedback_RadioButton_otro:
							eleccion = "Otros";
							break;
					}
					scrollview.postDelayed(new MyRunnable(scrollview), 100);
				});
                binding.LayoutFeedbackButtonEnviarFeedback.setOnClickListener(v -> {
					if (!eleccion.isEmpty()) {
						Registra_Feedback_en_Firestore(binding.LayoutFeedbackEditTextFeedback.getText().toString(), eleccion);
						ir_a_principal();
					}
				});
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onCreate de principal");
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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

			setupNavigationDrawer();// Setup navigation drawer
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onResume de Activity_Feedback");
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

    private static class MyRunnable implements Runnable {
        private final ScrollView scrollview;

        MyRunnable(ScrollView un_scrollview) {
			scrollview = un_scrollview;
		}

        @Override
        public void run() {
            scrollview.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void setup_toolbar() {
        // Setup toolbar and statusBar (really FrameLayout)
        try {
            toolbar = findViewById(R.id.mToolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(!utils.isTablet(this));
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "setup_toolbar de feedback");
        }
    }

	private void setupNavigationDrawer() {
		try {
			if (binding.mDrawerLayout!=null) {

				// Setup Drawer Icon
				drawerToggle = new ActionBarDrawerToggle(this, binding.mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
				binding.mDrawerLayout.addDrawerListener(drawerToggle);
				drawerToggle.syncState();

				TypedValue typedValue = new TypedValue();
				int color = typedValue.data;
				binding.mDrawerLayout.setStatusBarBackgroundColor(color);
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
			utils.registra_error(e.toString(), "setupNavigationDrawer de feedback");
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
        recyclerViewDrawer.addOnItemTouchListener(new Activity_Feedback.RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Feedback.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				binding.mDrawerLayout.closeDrawers();
			}
		}));
    }

    private void salir() {
        //Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir

        finish();
    }

	private void inicializa_anuncios(){
		try{
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

			if (!perfil_usuario.getBoolean(getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				Consent consent = ConsentManager.getInstance(this).getConsent();
				Appodeal.setTesting(false);
				Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER, consent);
				setup_banner();
				int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
				layoutParams.setMargins(0, px, 0, 0);
			}else {
				layoutParams.setMargins(0, 0, 0, 0);
			}
			if (binding.mDrawerLayout!=null){
				binding.mDrawerLayout.setLayoutParams(layoutParams);
			}else{
				binding.MainLinearLayout.setLayoutParams(layoutParams);
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "inicializa_anuncios de Activity_Feedback");
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

	private void setTexts(){
        binding.LayoutFeedbackTextViewTitulo.setText(getResources().getString(R.string.ACTIVITY_FEEDBACK_TITULO));
    }

    private void setTypefaces() {
        Typeface typeFace_roboto_light = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
        Typeface typeFace_roboto_bold = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Bold.ttf");

		binding.LayoutFeedbackTextViewTitulo.setTypeface(typeFace_roboto_bold);

		binding.LayoutFeedbackEditTextFeedback.setTypeface(typeFace_roboto_light);

		binding.LayoutFeedbackButtonEnviarFeedback.setTypeface(typeFace_roboto_light);
        binding.LayoutFeedbackButtonRateApp.setTypeface(typeFace_roboto_light);

		binding.LayoutFeedbackRadioButtonSiMeGusta.setTypeface(typeFace_roboto_light);
		binding.LayoutFeedbackRadioButtonNoMeGusta.setTypeface(typeFace_roboto_light);
		binding.LayoutFeedbackRadioButtonMalDisenyo.setTypeface(typeFace_roboto_light);
		binding.LayoutFeedbackRadioButtonVaLenta.setTypeface(typeFace_roboto_light);
		binding.LayoutFeedbackRadioButtonNoFuncionaBien.setTypeface(typeFace_roboto_light);
		binding.LayoutFeedbackRadioButtonNoSeComoVa.setTypeface(typeFace_roboto_light);
		binding.LayoutFeedbackRadioButtonMalTraducida.setTypeface(typeFace_roboto_light);
		binding.LayoutFeedbackRadioButtonPocaGente.setTypeface(typeFace_roboto_light);
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
                LinearLayout.LayoutParams mlayoutParams_perfil = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
                mAppCompatImageView.setLayoutParams(mlayoutParams_perfil);
                if (mBitmap != null) {
                    mAppCompatImageView.setImageBitmap(mBitmap);
                    mAppCompatImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    mAppCompatImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
                }
                mImageView_PictureMain.setImageDrawable(mAppCompatImageView.getDrawable());
            }catch (Exception ignored) {

            }
        }
    }

    private void Registra_Feedback_en_Firestore(String feedback_usuario,String motivo){
		Calendar hoy = Calendar.getInstance();

		Feedback un_feedback= new Feedback(
    			perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""),
				hoy.getTimeInMillis(),
				perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS), ""),
				feedback_usuario,
				utils.get_Version_Name(getApplicationContext()),
				BuildConfig.APPLICATION_ID,
				motivo);
		db.collection(getResources().getString(R.string.FEEDBACK)).add(un_feedback);

	}

	private void ir_a_principal(){
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_FEEDBACK_SENT), Toast.LENGTH_LONG).show();
		Intent mIntent = new Intent(getApplicationContext(), Activity_Principal.class);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mIntent);
		finish();
	}

    private class RecyclerTouchListener_menu implements RecyclerView.OnItemTouchListener {
        @Nullable
        private final GestureDetector mGestureDetector;
        private final Interface_ClickListener_Menu mInterfaceClickListener;

        RecyclerTouchListener_menu(Context context, @NonNull RecyclerView recyclerView, Interface_ClickListener_Menu un_Interface_ClickListener) {
			mInterfaceClickListener = un_Interface_ClickListener;
			mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

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

