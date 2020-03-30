package com.rubisoft.menradar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.menradar.Adapters.Drawer_Adapter;
import com.rubisoft.menradar.Classes.Drawer_Item;
import com.rubisoft.menradar.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.menradar.R;
import com.rubisoft.menradar.tools.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Activity_Configuracion extends AppCompatActivity {
    private TextView mTextView_unidades;
    private TextView mTextView_desactivar_chat;
    private SwitchCompat mSwitch_desactivar_chat;
	private FirebaseFirestore db;

    private Spinner mSpinner_unidades;
    private SharedPreferences preferencias_usuario;
    private SharedPreferences perfil_usuario;

    //navigation drawer
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout mDrawerLayout;
    private RecyclerView recyclerViewDrawer;
    private ImageView mImageView_PictureMain;

	private LinearLayout Main_LinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        super.onCreate(savedInstanceState);
        try {
            preferencias_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);
            perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

            if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
                salir();
            } else {
				setContentView(R.layout.layout_configuracion);
                setup_Views();
                setTypefaces();
                mTextView_unidades.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_UNIDADES));
				db = FirebaseFirestore.getInstance();
				inicializa_anuncios();
                setup_spinners();
                setup_textviews_y_switches();

                mSpinner_unidades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int posicion_actual, long id) {
                        SharedPreferences.Editor editor = preferencias_usuario.edit();
                        editor.putLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), posicion_actual);
                        editor.apply();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                mSwitch_desactivar_chat.setOnCheckedChangeListener((buttonView, isChecked) -> {
					SharedPreferences.Editor editor = preferencias_usuario.edit();
					editor.putBoolean(getResources().getString(R.string.PREFERENCIAS_ENABLE_CHAT), isChecked);
					editor.apply();

					if (isChecked) {
						suscribir_a_grupo();
						mTextView_desactivar_chat.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_CHAT_ACTIVADO));
					} else {
						desuscribir_de_grupo();
						mTextView_desactivar_chat.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_SONIDO_CHAT_DESACTIVADO));
					}
				});

                AppCompatButton boton_eliminar_cuenta = findViewById(R.id.Layout_configuracion_AppCompatButton_eliminar_cuenta);
                boton_eliminar_cuenta.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_ELIMINAR_CUENTA));
                boton_eliminar_cuenta.setOnClickListener(v -> confirmacion());
                setup_toolbar();// Setup toolbar and statusBar (really FrameLayout)
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onCreate de configuracion");
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

			setupNavigationDrawer();// Setup navigation drawer
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onResume de Activity_Configuracion");        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Intent mIntent = new Intent(this, Activity_Principal.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mIntent);
        finish();
    }

	private void confirmacion() {
		try {
			new MaterialDialog.Builder(this)
					.theme(Theme.LIGHT)
					.title(R.string.DIALOGO_BORRAR_CUENTA_TITULO)
					.content(getResources().getString(R.string.DIALOGO_BORRAR_CUENTA_ADVERTENCIA))
					.typeface("Roboto-Bold.ttf", "Roboto-Regular.ttf")
					.positiveText(R.string.ACTIVITY_UNINSTALL_UNINSTALL)
					.negativeText(R.string.Cancelar)
					.onPositive((dialog, which) -> {
						eliminar_cuenta(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));
						Intent mIntent = new Intent(Activity_Configuracion.this, Activity_Inicio.class);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						startActivity(mIntent);
						finish();
					})
					.onNegative((dialog, which) -> dialog.dismiss()).canceledOnTouchOutside(false)
					.show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "uninstall");
		}
	}

	private void borrar_foto_de_memoria_interna(String token_socialauth, int num_foto) {
		try {
			String nombre_thumb = utils.get_nombre_thumb(token_socialauth, num_foto);

			File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

			File file = new File(storageDir, nombre_thumb);
			if (file.exists()) {
				boolean resultado=file.delete();
				if (!resultado){
					Toast.makeText(getApplicationContext(),getResources().getString(R.string.error_deleting_files), Toast.LENGTH_LONG).show();
				}
			}
		} catch (Exception ignored) {
		}
	}

	private void borra_perfil( String token_socialauth){
		Map<String, Object> data = new HashMap<>();
		data.put("token_socialauth", token_socialauth);

		FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
		mFunctions.getHttpsCallable("borra_perfil")
				.call(data)
				.continueWith(task2 -> {
					// This continuation runs on either success or failure, but if the task
					// has failed then getResult() will throw an Exception which will be
					// propagated down.
					return (String) task2.getResult().getData();
				});
    }

    private void borrar_chat( String id_relacion){
		db.collection(getResources().getString(R.string.CHAT))
				.whereEqualTo(getResources().getString(R.string.CHAT_ID_RELACION),id_relacion)
				.get().addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						for (QueryDocumentSnapshot document : task.getResult()) {
							Map<String, Object> data = new HashMap<>();
							data.put("id_chat", document.getId());

							FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
							mFunctions.getHttpsCallable("borra_chat")
									.call(data)
									.continueWith(task2 -> {
										// This continuation runs on either success or failure, but if the task
										// has failed then getResult() will throw an Exception which will be
										// propagated down.
										return (String) task2.getResult().getData();
									});
						}
					}
				});
	}

	private void desuscribir_de_grupo(){
		String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
		FirebaseMessaging.getInstance().unsubscribeFromTopic(grupo_al_que_pertenece);

	}

	private void suscribir_a_grupo(){
		String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
		FirebaseMessaging.getInstance().subscribeToTopic(grupo_al_que_pertenece);
	}

	private void actualiza_relaciones_y_luego_borra_perfil( String token_socialauth){
		db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).collection(getResources().getString(R.string.RELACIONES)).get()
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						for (QueryDocumentSnapshot document : task.getResult()) {
							String id_relacion=document.getId();
							String token_socialauth_de_la_otra_persona=(String)document.get(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA));
							Long estado_de_la_relacion=(Long)document.get(getResources().getString(R.string.RELACIONES_ESTADO_DE_LA_RELACION));
							if (estado_de_la_relacion==getResources().getInteger(R.integer.RELACION_DENUNCIADA)
							|| estado_de_la_relacion==getResources().getInteger(R.integer.RELACION_VIUDA)){
								borrar_chat(id_relacion);
							}else{
								db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_de_la_otra_persona)
										.collection(getResources().getString(R.string.RELACIONES)).document(id_relacion)
										.update(getResources().getString(R.string.RELACIONES_ESTADO_DE_LA_RELACION),getResources().getInteger(R.integer.RELACION_VIUDA));
							}
						}
					}
					borra_perfil(token_socialauth);
				});
	}

	private void elimina_SharedPreferencies() {
		try {
			//************* Lo borramos de SharedPreferences  ****************
			SharedPreferences perfil_usuario = getSharedPreferences(getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
			SharedPreferences busquedas_usuario = getSharedPreferences(getString(R.string.SHAREDPREFERENCES_BUSQUEDAS_USUARIO), Context.MODE_PRIVATE);
			SharedPreferences preferencias_usuario = getSharedPreferences(getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);
			SharedPreferences chat_general = getSharedPreferences(getString(R.string.SHAREDPREFERENCES_PRINCIPAL), Context.MODE_PRIVATE);

			SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
			editor_perfil_usuario.clear();
			editor_perfil_usuario.apply();
			SharedPreferences.Editor editor_preferencias_usuario = preferencias_usuario.edit();
			editor_preferencias_usuario.clear();
			editor_preferencias_usuario.apply();
			SharedPreferences.Editor editor_busquedas_usuario = busquedas_usuario.edit();
			editor_busquedas_usuario.clear();
			editor_busquedas_usuario.apply();
			SharedPreferences.Editor editor_chat_general = chat_general.edit();
			editor_chat_general.clear();
			editor_chat_general.apply();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "elimina_SharedPreferencies");

		}
	}

	private void eliminar_cuenta(String token_socialauth){
    	try {
			for (int i = 0; i < getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM); i++) {
				//borrar_foto_de_FCM(token_socialauth, i);
				borrar_foto_de_memoria_interna(token_socialauth, i);
			}
			String app = utils.decodifica_app(utils.get_app_code(getApplicationContext().getPackageName()));
			String pais = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS), "");
			//Long raza = perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_RAZA), 0L);
			Long sexo = perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_SEXO), 0L);
			Long orientacion = perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION), 0L);

			actualiza_relaciones_y_luego_borra_perfil(token_socialauth);
			FirebaseAuth.getInstance().getCurrentUser().delete();
			desuscribir_de_grupo();
			elimina_SharedPreferencies();
			actualiza_stats(pais, utils.decodifica_sexo(sexo), utils.decodifica_orientacion(orientacion), app);
		}catch (Exception e){
			utils.registra_error(e.toString(), "eliminar_cuenta de Activity_Configuracion");
		}
	}

	private void actualiza_stats( String pais, String sexo, String orientacion, String app){
		Calendar hoy = Calendar.getInstance();
		String semana_del_anyo = Integer.valueOf(hoy.get(Calendar.WEEK_OF_YEAR)).toString();

		DocumentReference ref_paises= db.collection(getResources().getString(R.string.STATS_PAISES)).document(pais);
		DocumentReference ref_usuarios= db.collection(getResources().getString(R.string.STATS_GLOBAL)).document(semana_del_anyo);

		WriteBatch batch = db.batch();
		batch.update(ref_paises, "total_usuarios", FieldValue.increment(-1));
		batch.update(ref_paises, sexo+"_"+orientacion, FieldValue.increment(-1));
		batch.update(ref_paises, app, FieldValue.increment(-1));
		batch.commit();

		WriteBatch batch2 = db.batch();
		batch2.update(ref_usuarios, "total_usuarios", FieldValue.increment(-1));
		batch2.update(ref_usuarios, sexo+"_"+orientacion, FieldValue.increment(-1));
		batch2.update(ref_usuarios, app, FieldValue.increment(-1));
		batch2.commit();
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
            utils.registra_error(e.toString(), "setup_toolbar de Activity_Configuracion");
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
			utils.registra_error(e.toString(), "setupNavigationDrawer de Activity_Configuracion");
		}
	}

    private void setupNavigationDrawerRecyclerViews() {
        try {
            recyclerViewDrawer = findViewById(R.id.recyclerViewDrawer);
            recyclerViewDrawer.setLayoutManager(new LinearLayoutManager(this));

            ArrayList<Drawer_Item> drawerItems = new ArrayList<>();
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_track_changes).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[0]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_tune).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[1]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_email).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[2]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_perm_identity).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[3]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_star_border).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[4]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attach_money).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[5]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_forum).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[6]));
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
        recyclerViewDrawer.addOnItemTouchListener(new Activity_Configuracion.RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Configuracion.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				mDrawerLayout.closeDrawers();
			}
		}));
    }

    private void salir() {
        //Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir

        finish();
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
			utils.registra_error(e.toString(), "setup_banner de Activity_Ayuda");
		}
	}

	private void setTypefaces() {
        Typeface typeFace_roboto_light = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");


        //mSwitch_aceptar_peticiones.setTypeface(typeFace_roboto_light);
        mTextView_unidades.setTypeface(typeFace_roboto_light);
    //    mTextView_modo_invisible.setTypeface(typeFace_roboto_light);
        //mTextView_eliminar_cuenta.setTypeface(typeFace_roboto_light);
        mTextView_desactivar_chat.setTypeface(typeFace_roboto_light);

    }

    private void setup_Views() {
        mTextView_desactivar_chat = findViewById(R.id.Layout_configuracion_TextView_desactivar_sonido_chat);
		Main_LinearLayout = findViewById(R.id.Main_LinearLayout);
        mTextView_unidades = findViewById(R.id.Layout_configuracion_TextView_unidades);
        mSwitch_desactivar_chat = findViewById(R.id.Layout_configuracion_Switch_desactivar_sonido_chat);
		mDrawerLayout = findViewById(R.id.mDrawerLayout);

	}

    private void setup_spinners() {
        mSpinner_unidades = findViewById(R.id.Layout_configuracion_Spinner_unidades);
        ArrayAdapter<CharSequence> adapter_unidades;
		adapter_unidades = ArrayAdapter.createFromResource(this, R.array.array_unidades, R.layout.spinner_item);
        adapter_unidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_unidades.setAdapter(adapter_unidades);
        mSpinner_unidades.setSelection((int) preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0));
    }

    private void setup_textviews_y_switches() {
       // mTextView_eliminar_cuenta.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_ELIMINAR_CUENTA));

        //mSwitch_aceptar_peticiones.setChecked(preferencias_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ACEPTA_PETICIONES), true));

        mSwitch_desactivar_chat.setChecked(preferencias_usuario.getBoolean(getResources().getString(R.string.PREFERENCIAS_ENABLE_CHAT), true));
/*

        if (mSwitch_aceptar_peticiones.isChecked()) {
            mTextView_modo_invisible.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_MODO_INVISIBLE_on));
        } else {
            mTextView_modo_invisible.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_MODO_INVISIBLE_off));
        }
*/

        if (mSwitch_desactivar_chat.isChecked()) {
            mTextView_desactivar_chat.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_CHAT_ACTIVADO));
        } else {
            mTextView_desactivar_chat.setText(getResources().getString(R.string.ACTIVITY_CONFIGURACION_SONIDO_CHAT_DESACTIVADO));
        }
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
            }catch (Exception e) {
                
                utils.registra_error(e.toString(), "AsyncTask_Coloca_PictureMain de Activity_Configuracion");
            }
        }
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
