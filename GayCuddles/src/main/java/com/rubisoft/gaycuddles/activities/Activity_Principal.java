package com.rubisoft.gaycuddles.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.gaycuddles.Adapters.Drawer_Adapter;
import com.rubisoft.gaycuddles.Adapters.RecyclerView_Principal_Adapter;
import com.rubisoft.gaycuddles.Classes.Drawer_Item;
import com.rubisoft.gaycuddles.Classes.NpaLinearLayoutManager;
import com.rubisoft.gaycuddles.Classes.Usuario_para_listar;
import com.rubisoft.gaycuddles.Dialogs.Dialog_Interactuar_Principal;
import com.rubisoft.gaycuddles.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.gaycuddles.Interfaces.Interface_ClickListener_Perfiles;
import com.rubisoft.gaycuddles.R;
import com.rubisoft.gaycuddles.databinding.LayoutPrincipalBinding;
import com.rubisoft.gaycuddles.tools.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;
import io.huq.sourcekit.HISourceKit;

public class Activity_Principal extends AppCompatActivity  {
	private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 2;
	private static final int MY_PERMISSIONS_REQUEST_LAST_LOCATION = 3;
	private final int perfiles_por_pagina=12;
	private FirebaseFirestore db;
	private SharedPreferences perfil_usuario;
	private SharedPreferences busqueda_usuario;
	private SharedPreferences preferencias_usuario;
	private ArrayList<Usuario_para_listar> perfiles_encontrados_global;

	private RecyclerView_Principal_Adapter mRecyclerViewListAdapter;
	private Location mLastLocation;
	private FusedLocationProviderClient mFusedLocationClient;
	private Toolbar toolbar;
	private ActionBarDrawerToggle drawerToggle;

	private RecyclerView recyclerViewDrawer;
	private ImageView mImageView_PictureMain;
	private FirebaseStorage storage;

	private LayoutPrincipalBinding binding;
	private int pagina;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Drawable icono_retroceder;
		Drawable icono_seguir;
		try {
			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
			super.onCreate(savedInstanceState);
		}
		catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate 1 de Activity_Principal");
		}
		if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
			binding = LayoutPrincipalBinding.inflate(getLayoutInflater());
			setContentView(binding.getRoot());
			setup_sharedprefernces(); //inicializamos los sharedpreferences

			if (perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
				// es posible que haciendo backpress en alguna activity hayamos vuelto a activity_principal sin estar registrado
				salir();
			}
			else {
					mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

					setup_RecyclerView();  //inicializamos el recyclerview que contendrá los perfiles encontrados

					inicializa_anuncios();

					inicializa_firebase();

					chequea_num_estrellas_y_premium();

					buscar_gente_si_se_puede();

					setup_toolbar();
					Bundle mBundle = getIntent().getExtras();
					pagina= mBundle==null?0:mBundle.getInt(getResources().getString(R.string.PAGINA));

					Configuration config = getResources().getConfiguration();
					if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
						icono_seguir = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_arrow_forward).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));

						icono_retroceder = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_arrow_back).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
					}
					else {
						icono_seguir = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_arrow_back).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));

						icono_retroceder = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_arrow_forward).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
					}

					binding.LayoutPrincipalButtonRetroceder.setImageDrawable(icono_retroceder);
					binding.LayoutPrincipalButtonAvanzar.setImageDrawable(icono_seguir);

					binding.LayoutPrincipalButtonRetroceder.setOnClickListener(view -> {
						if (perfiles_encontrados_global != null && pagina>0) {
							pagina--;
							pinta_perfiles(perfiles_encontrados_global);
						}
					});
					binding.LayoutPrincipalButtonAvanzar.setOnClickListener(view -> {
						if (perfiles_encontrados_global != null && pagina <perfiles_encontrados_global.size()/12){
							pagina++;
							pinta_perfiles(perfiles_encontrados_global);
						}
					});

			}
			actualiza_fecha_ultimo_acceso();

			if (HISourceKit.getInstance()!=null) {
				HISourceKit.getInstance().recordWithAPIKey(getString(R.string.huqh_API_KEY), getApplication());
			}
		}
		else {
			Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
			startActivity(mIntent);
			finish();
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
	public void onStart() {
		super.onStart();
		getLastLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			if (perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
				// es posible que haciendo backpress en alguna activity hayamos vuelto a activity_principal sin estar registrado
				salir();
			}
			Appodeal.onResume(this, Appodeal.BANNER_TOP);

			setupNavigationDrawer();
			crea_ItemTouchListener();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onResume de Activity_Principal");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			//SI NO NOS DA SU PERMISO VOLVEMOS AL INICIO
			//SI NOS DA PERMISO, AHORA COMPROBAMOS QUE LOCATION ESTÁ ACTIVADO
			if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				getLastLocation();
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	private void getLastLocation() {
		try {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
				mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
					mLastLocation = location;
					if (location != null) {
						comprueba_si_ha_cambiado_de_posicion();
					}
				});
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LAST_LOCATION);
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "getLastLocation de Activity_Principal");
		}
	}

	private void comprueba_si_ha_cambiado_de_posicion(){
		try {
			String Token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
			if (!Token_socialauth.isEmpty()) {
				double new_latitud = utils.convertToDecimal(mLastLocation.getLatitude(), 2);
				double new_longitud = utils.convertToDecimal(mLastLocation.getLongitude(), 2);
				double old_latitud = utils.convertToDecimal(perfil_usuario.getFloat(getResources().getString(R.string.PERFIL_USUARIO_LATITUD), 0f), 2);
				double old_longitud = utils.convertToDecimal(perfil_usuario.getFloat(getResources().getString(R.string.PERFIL_USUARIO_LONGITUD), 0f), 2);

				if ((old_latitud != new_latitud) || (old_longitud != new_longitud)) {
					Location new_location = new Location("dummyprovider");
					new_location.setLatitude(new_latitud);
					new_location.setLongitude(new_longitud);

					Editor editor = perfil_usuario.edit();
					editor.putFloat(getResources().getString(R.string.PERFIL_USUARIO_LATITUD), Double.valueOf(mLastLocation.getLatitude()).floatValue());
					editor.putFloat(getResources().getString(R.string.PERFIL_USUARIO_LONGITUD), Double.valueOf(mLastLocation.getLongitude()).floatValue());
					editor.apply();

					actualiza_ubicacion_en_Firestore( Token_socialauth,mLastLocation);
					//new AsyncTask_actualiza_ubicacion_en_Firestore().execute(new Pair<>(mLastLocation, Token_socialauth));

					String pais_actual = utils.get_codigo_pais(getApplicationContext(), mLastLocation.getLatitude(), mLastLocation.getLongitude());
					if (!perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_PAIS), "").equals(pais_actual)) {
						desuscribir_de_grupo();
						actualiza_pais_en_Firestore(pais_actual);
						editor.putString(getResources().getString(R.string.PERFIL_USUARIO_PAIS), pais_actual);
						editor.apply();
						suscribir_a_grupo();
					}

					//reload the activity
					finish();
					startActivity(getIntent());
					//buscar_gente_si_se_puede();
				}

			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "comprueba_si_ha_cambiado_de_posicion de Activity_Principal");
		}
	}

	private void pinta_perfiles( ArrayList<Usuario_para_listar> perfiles_encontrados) {
		// **********************************************************************************************************
		//* La estructura se puede ver en cardview_list_old
		// **********************************************************************************************************
		try {

			utils.setProgressBar_visibility(binding.mProgressBar, View.VISIBLE);

			if (perfiles_encontrados != null && pagina>0) {
				binding.LayoutPrincipalButtonRetroceder.setVisibility(View.VISIBLE);
				binding.LayoutPrincipalCardview.setVisibility(View.VISIBLE);
			}else{
				binding.LayoutPrincipalButtonRetroceder.setVisibility(View.INVISIBLE);

			}
			if (perfiles_encontrados != null && pagina <perfiles_encontrados.size()/12){
				binding.LayoutPrincipalButtonAvanzar.setVisibility(View.VISIBLE);
				binding.LayoutPrincipalCardview.setVisibility(View.VISIBLE);
			}else{
				binding.LayoutPrincipalButtonAvanzar.setVisibility(View.INVISIBLE);
			}
			binding.LayoutPrincipalTextViewNingunPerfil.setVisibility(View.INVISIBLE);

			binding.LayoutPrincipalReciclerView.setVisibility(View.VISIBLE);

			mRecyclerViewListAdapter = new RecyclerView_Principal_Adapter(getApplicationContext());
			binding.LayoutPrincipalReciclerView.setAdapter(mRecyclerViewListAdapter);
			binding.LayoutPrincipalReciclerView.setLayoutManager(new NpaLinearLayoutManager(getApplicationContext()));
			int j=0;
			for (int i=pagina*perfiles_por_pagina; i<((pagina+1)*perfiles_por_pagina)&&perfiles_encontrados.size()>i;i++) {
				carga_perfil_para_lista(perfiles_encontrados.get(i), j);
				j++;
			}
			binding.LayoutPrincipalReciclerView.invalidate();//para que se ejecute su animacion
		} catch (Exception e) {
			utils.registra_error(e.toString(), "pinta_perfiles de Activity_Principal");
		} finally {
			utils.setProgressBar_visibility(binding.mProgressBar, View.INVISIBLE);
		}
	}

	private void carga_perfil_para_lista(Usuario_para_listar un_usuario, int i) {
		try {
			if (this.getApplicationContext() != null) {
				Usuario_para_listar un_usuario_para_listar = utils.prepara_datos_usuario_para_listar(getApplicationContext(),un_usuario,perfil_usuario);
				////un_usuario = utils.get_esta_online(getApplicationContext(), mBeanPerfil, un_usuario);
				if (mRecyclerViewListAdapter != null) {
					mRecyclerViewListAdapter.addItem(i, un_usuario_para_listar);
					mRecyclerViewListAdapter.notifyItemInserted(i);
					descarga_URL(un_usuario.getToken_socialauth(), un_usuario_para_listar,i);
				}
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "carga_perfil_para_lista de Activity_Principal");
		}
	}

	private void descarga_URL(String Token_socialAuth, Usuario_para_listar un_usuario, int i) {
		try {
			StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_thumb(Token_socialAuth, 0));

			storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
				try {
					if (!isFinishing() && !isDestroyed() && (mRecyclerViewListAdapter != null) && (i < mRecyclerViewListAdapter.getItemCount())) {
						un_usuario.setFoto(uri.toString());

						mRecyclerViewListAdapter.replaceItem(i, un_usuario);
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "descarga_url 1 de Activity_Principal");
				}
			}).addOnFailureListener(exception -> {

			});
		}catch (RejectedExecutionException e){
			try {
				Thread.sleep(500);
			}catch (Exception ignored){}
		}  catch (Exception e) {
			utils.registra_error(e.toString(), "descarga_url 2  de Activity_Principal");
		}
	}

	private void pinta_no_hay_perfiles() {
		try {
			binding.LayoutPrincipalReciclerView.setVisibility(View.INVISIBLE);
			binding.LayoutPrincipalButtonRetroceder.setVisibility(View.INVISIBLE);
			binding.LayoutPrincipalButtonAvanzar.setVisibility(View.INVISIBLE);
			Typeface typeFace_roboto_light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
			binding.LayoutPrincipalTextViewNingunPerfil.setVisibility(View.VISIBLE);
			binding.LayoutPrincipalTextViewNingunPerfil.setTypeface(typeFace_roboto_light);
			binding.LayoutPrincipalTextViewNingunPerfil.setText(getResources().getString(R.string.ACTIVITY_PRINCIPAL_NINGUN_PERFIL_ENCONTRADO));
		} catch (Exception e) {
			utils.registra_error(e.toString(), "pinta_no_hay_PErfiles de Activity_Principal");
		}
	}

	private void pinta_no_va_el_gps() {
		try {
			binding.LayoutPrincipalButtonRetroceder.setVisibility(View.INVISIBLE);
			binding.LayoutPrincipalButtonAvanzar.setVisibility(View.INVISIBLE);
			Typeface typeFace_roboto_light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
			binding.LayoutPrincipalTextViewNingunPerfil.setVisibility(View.VISIBLE);
			binding.LayoutPrincipalTextViewNingunPerfil.setTypeface(typeFace_roboto_light);
			binding.LayoutPrincipalTextViewNingunPerfil.setText(getResources().getString(R.string.ACTIVITY_PRINCIPAL_NO_VA_EL_GPS));

			this.setup_fondo_pantalla(2);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "pinta_no_va_el_gps de Activity_Principal");
		}
	}

	private boolean esta_localizado() {
		boolean esta_localizado=false;
		try {
			esta_localizado = (this.perfil_usuario.getFloat(this.getResources().getString(R.string.PERFIL_USUARIO_LATITUD), 0F) != 0F) || (this.perfil_usuario.getFloat(this.getResources().getString(R.string.PERFIL_USUARIO_LONGITUD), 0F) != 0F);
		}catch (Exception e){
			utils.registra_error(e.toString(), "esta_localizado de Activity_Principal");
		}
		return esta_localizado;
	}

	private void buscar_gente_si_se_puede() {
		try {
			if (esta_localizado()) {
				//¿QUE ESTÁ BUSCANDO: PERSONAS O PAREJAS?
				//DEPENDE DE QUÉ BUSQUE TENEMOS QUE LLAMAR A UNA FUNCIÓN DISTINTA EN EL BACKEND
				//	new AsyncTask_buscar_personas().execute();  //Buscamos personas
				buscar_personas();
			} else {
				//mImageButton_reload.setVisibility(View.VISIBLE);
				this.pinta_no_va_el_gps();
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "buscar_gente_si_se_puede de Activity_Principal");
		}
	}

	private void setup_fondo_pantalla(int tipo) {
		try {
			if (this.getApplicationContext() != null) { //puede que ya no estemos en la activity
				Drawable fondo;

				switch (tipo) {
					case 0:
						fondo = new IconicsDrawable(this.getApplicationContext())
								.icon(GoogleMaterial.Icon.gmd_sentiment_very_satisfied)
								.color(ContextCompat.getColor(this.getApplicationContext(), R.color.gris_transparente));
						binding.LayoutPrincipalRelativeLayout.setBackground(fondo);
						break;
					case 1:
						fondo = new IconicsDrawable(this.getApplicationContext())
								.icon(GoogleMaterial.Icon.gmd_sentiment_neutral)
								.color(ContextCompat.getColor(this.getApplicationContext(), R.color.gris_transparente));
						binding.LayoutPrincipalRelativeLayout.setBackground(fondo);
						break;
					case 2:
						fondo = new IconicsDrawable(this.getApplicationContext())
								.icon(GoogleMaterial.Icon.gmd_gps_off)
								.color(ContextCompat.getColor(this.getApplicationContext(), R.color.gris_transparente));
						binding.LayoutPrincipalRelativeLayout.setBackground(fondo);
						break;
				}
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "setup_fondo_pantalla  de Activity_Principal");
		}
	}

	private void crea_ItemTouchListener_menu() {
		//Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
		//y que lo ha detectado y codificado primero el  RecyclerTouchListener
		recyclerViewDrawer.addOnItemTouchListener(new RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Principal.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				binding.mDrawerLayout.closeDrawers();
			}
		}));
	}

	private void actualiza_fecha_ultimo_acceso(){
		try {
			String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).update(getResources().getString(R.string.USUARIO_FECHA_ULTIMO_ACCESO), Calendar.getInstance().getTimeInMillis());
		}catch (Exception ignored){

		}
	}

	private void inicializa_anuncios(){
		try{
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

			if (!perfil_usuario.getBoolean(getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				Consent consent = ConsentManager.getInstance(this).getConsent();
				Appodeal.setTesting(false);
				Appodeal.setLogLevel(com.appodeal.ads.utils.Log.LogLevel.verbose);
				Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER|Appodeal.INTERSTITIAL, consent);
				setup_banner();
				lanza_interstitial();
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
			utils.registra_error(e.toString(), "inicializa_anuncios de Activity_Principal");
		}
	}

	private void setup_banner() {
		try {
			Appodeal.setBannerCallbacks(new BannerCallbacks() {
				@Override
				public void onBannerLoaded(int height, boolean isPrecache) {
					Log.d("Appodeal", "onInterstitialFailedToLoad");

				}
				@Override
				public void onBannerFailedToLoad() {
					Log.d("Appodeal", "onInterstitialFailedToLoad");
				}
				@Override
				public void onBannerShown() {
					Log.d("Appodeal", "onInterstitialFailedToLoad");

				}
				@Override
				public void onBannerClicked() {

				}
				@Override
				public void onBannerExpired() {

				}
				@Override
				public void onBannerShowFailed() {
					Log.d("Appodeal", "onBannerShowFailed");

				}
			});
			Appodeal.show(this, Appodeal.BANNER_TOP);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_banner de Activity_Principal");
		}
	}

	private void lanza_interstitial() {
		try {
			SharedPreferences sharedPreferences_principal = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PRINCIPAL), Context.MODE_PRIVATE);
			int num_veces_visto_anuncio = sharedPreferences_principal.getInt("veces_visto_el_anuncio", 0);
			if (num_veces_visto_anuncio % 4 == 0) {
				Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
					@Override
					public void onInterstitialLoaded(boolean isPrecache) {
						Log.d("Appodeal", "onInterstitialFailedToLoad");

					}
					@Override
					public void onInterstitialFailedToLoad() {
						Log.d("Appodeal", "onInterstitialFailedToLoad");

					}
					@Override
					public void onInterstitialShown() {
					}
					@Override
					public void onInterstitialClicked() {
					}
					@Override
					public void onInterstitialClosed() {
					}
					@Override
					public void onInterstitialExpired() {
					}
					@Override
					public void onInterstitialShowFailed() {
					}
				});
				Appodeal.show(this, Appodeal.INTERSTITIAL);
			}
			num_veces_visto_anuncio++;
			SharedPreferences.Editor editor_chat_general = sharedPreferences_principal.edit();
			editor_chat_general.putInt("veces_visto_el_anuncio", num_veces_visto_anuncio);
			editor_chat_general.apply();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_interstitial de Activity_principal");
		}
	}

	private void setup_sharedprefernces() {
		try {
			perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
			busqueda_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_BUSQUEDAS_USUARIO), Context.MODE_PRIVATE);
			preferencias_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_sharedpreferences de Activity_Principal");
		}
	}

	private void setup_RecyclerView() {
		try {
			RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			this.binding.LayoutPrincipalReciclerView.setLayoutParams(mLayoutParams);

			if (perfil_usuario.getBoolean(getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				binding.LayoutPrincipalReciclerView.setPadding(0, 0, 0, 0);
			}
			utils.set_RecyclerView_Animator(binding.LayoutPrincipalReciclerView);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_recyclerview de Activity_Principal");
		}
	}

	private void setup_toolbar() {
		try {
			// Setup toolbar and statusBar (really FrameLayout)
			toolbar = findViewById(R.id.mToolbar);
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
			getSupportActionBar().setDisplayHomeAsUpEnabled(!utils.isTablet(this));
			getSupportActionBar().setHomeButtonEnabled(true);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_toolbar de Activity_Principal");
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
			utils.registra_error(e.toString(), "setupNavigationdrawer de Activity_Principal");
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

	private void salir() {
		//Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir

		finish();
	}

	private void lanza_dialogo(int posicion, String nick) {
		try {
			Dialog_Interactuar_Principal mDialog_Interactuar = new Dialog_Interactuar_Principal();
			Bundle args = new Bundle();
			args.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA),  perfiles_encontrados_global.get(posicion+pagina*perfiles_por_pagina).getToken_socialauth());
			args.putInt(getResources().getString(R.string.PAGINA),pagina);
			args.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH),""));
			args.putString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA),nick);
			args.putInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO),getResources().getInteger(R.integer.VENGO_DE_PRINCIPAL));
			mDialog_Interactuar.setArguments(args);
			mDialog_Interactuar.show(getSupportFragmentManager(),"d");
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo de Activity_Principal");
		}
	}

	private void crea_ItemTouchListener() {
		//Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
		//y que lo ha detectado y codificado primero el  RecyclerTouchListener
		binding.LayoutPrincipalReciclerView.addOnItemTouchListener(new RecyclerTouchListener_perfiles(this.getApplicationContext(), binding.LayoutPrincipalReciclerView, (view, position) -> {
			try {
				lanza_dialogo(position,perfiles_encontrados_global.get(position+pagina*perfiles_por_pagina).getNick());
				// utils.ir_a_Activity_Un_Perfil(Activity_Principal.this, perfiles_encontrados.get(position+pagina*perfiles_por_pagina), getResources().getInteger(R.integer.VENGO_DE_PRINCIPAL),pagina);
			} catch (Exception e) {
				utils.registra_error(e.toString(), "crea_ItemTouchListener de Activity_Principal");

			}
		}));
	}

	private void lanza_dialogo_tip() {
		try {
			if (!isFinishing() && !isDestroyed() && (this.hasWindowFocus())) {

				new MaterialDialog.Builder(this)
						.theme(Theme.LIGHT)
						.title(this.getResources().getString(R.string.DIALOGO_CONSEJO_TITULO))

						.content(this.getResources().getString(R.string.DIALOGO_CONSEJO_MENSAJE))
						.typeface("Roboto-Bold.ttf", "Roboto-Regular.ttf")
						.positiveText(R.string.ok)
						.onPositive((dialog, which) -> dialog.dismiss())

						.negativeText(R.string.DIALOGO_CONSEJO_YA_NO_MAS)
						.onNegative((dialog, which) -> {
							dialog.dismiss();
							Editor preferencias_editor = preferencias_usuario.edit();
							preferencias_editor.putBoolean(getResources().getString(R.string.AYUDA_ACTIVITY_PRINCIPAL), false);
							preferencias_editor.apply();
						}).canceledOnTouchOutside(false)
						.show();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo_tip de Activity_Principal");
		}
	}

	private void JSON_to_File(List<Usuario_para_listar> listado_perfiles_encontrados ,String path){
		try {
			String json = new Gson().toJson(listado_perfiles_encontrados);
			File file =  new File(path);
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.write(json);
			bw.close();
		}catch ( IOException ignore) {
		}catch (Exception e){
			utils.registra_error(e.toString(), "JSON_to_File de Activity_Principal");
		}
	}

	private boolean isFilePresent(String path) {
		File file = new File(path);
		return file.exists();
	}

	private String File_To_JSON(String path ) {
		try {
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String buffer;
			StringBuilder stringBuilder = new StringBuilder();

			while ((buffer = bufferedReader.readLine()) != null) {
				stringBuilder.append(buffer);
			}

			return stringBuilder.toString();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "File_To_JSON de Activity_Principal");
			return null;
		}
	}

	private ArrayList<Usuario_para_listar> promocionar_premiums(List<Usuario_para_listar> una_lista){
		ArrayList<Usuario_para_listar> lista_con_perfiles_premiums = new ArrayList<>();
		ArrayList<Usuario_para_listar> lista_con_perfiles_normales = new ArrayList<>();

		for (Usuario_para_listar un_usuario_para_listar: una_lista){
			if (un_usuario_para_listar.is_premium()) {
				lista_con_perfiles_premiums.add(un_usuario_para_listar);
			}else{
				lista_con_perfiles_normales.add(un_usuario_para_listar);
			}

		}
		lista_con_perfiles_premiums.addAll(lista_con_perfiles_normales);
		return lista_con_perfiles_premiums;
	}

	private  boolean filtro_online(Boolean opcion_online, Long Fecha_ultimo_acceso) {
		boolean resultado=true;
		try {
			long diferencia_en_milisegundos = Calendar.getInstance().getTimeInMillis() - Fecha_ultimo_acceso;
			long diferencia_en_horas = ((diferencia_en_milisegundos / 1000) / 60) / 60;

			resultado = !(opcion_online != null && (opcion_online && diferencia_en_horas > getResources().getInteger(R.integer.UMBRAL_ONLINE)));
		}catch (Exception e){
			utils.registra_error(e.toString(), "filtro_online de Activity_Principal");
		}
		return resultado;

	}

	private  boolean filtro_nuevo(Boolean opcion_nuevo, Long Fecha_registro) {
		boolean resultado=true;
		try {
			long diferencia_en_milisegundos = Calendar.getInstance().getTimeInMillis() - Fecha_registro;
			long diferencia_en_horas = ((diferencia_en_milisegundos / 1000) / 60) / 60;

			resultado = !(opcion_nuevo != null && (opcion_nuevo && diferencia_en_horas > getResources().getInteger(R.integer.UMBRAL_NUEVO)));
		}catch (Exception e){
			utils.registra_error(e.toString(), "filtro_nuevo de Activity_Principal");
		}
		return resultado;

	}

	private  boolean comprueba_edad_minima(Long edad_minima, Long fecha_nacimiento) {

		Calendar hoy = Calendar.getInstance();
		Calendar fecha_limite = Calendar.getInstance();
		fecha_limite.set((int) (hoy.get(Calendar.YEAR) - edad_minima), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));

		return fecha_limite.getTimeInMillis() >= fecha_nacimiento;
	}

	private  boolean comprueba_edad_maxima(Long edad_maxima, Long fecha_nacimiento) {

		Calendar hoy = Calendar.getInstance();
		Calendar fecha_limite = Calendar.getInstance();
		fecha_limite.set((int) (hoy.get(Calendar.YEAR) - edad_maxima), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));

		return fecha_limite.getTimeInMillis() <= fecha_nacimiento;
	}

	private void borra_cached_search(File file){
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory()) {
						borra_cached_search(f);
					} else {
						f.delete();
					}
				}
			}
		}
	}

	private void inicializa_firebase(){
		storage = FirebaseStorage.getInstance();
		db = FirebaseFirestore.getInstance();
	}

	private void buscar_personas(){
		try{
			/*String fileName = "cached_search.tmp";
			String path = getApplicationContext().getCacheDir() + "/" + fileName;

			if (isFilePresent(path)) {
				String jsonString = File_To_JSON(path );
				ObjectMapper mapper = new ObjectMapper();
				ObjectReader reader = mapper.readerFor(new TypeReference<List<Usuario_para_listar>>(){});

				perfiles_encontrados_global = reader.with(DeserializationFeature.USE_LONG_FOR_INTS).readValue(jsonString);
				//JSON_to_File(perfiles_encontrados_global,path);
				pinta_perfiles(perfiles_encontrados_global);
			}else{*/
			db.collection(getResources().getString(R.string.USUARIOS))
					.whereEqualTo(getResources().getString(R.string.USUARIO_PAIS), perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""))
					.whereEqualTo(getResources().getString(R.string.USUARIO_ORIENTACION),busqueda_usuario.getLong(this.getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), 1L))
					.whereEqualTo(getResources().getString(R.string.USUARIO_SEXO),busqueda_usuario.getLong(this.getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), 1L))
					.get().addOnCompleteListener(task -> {
				try {
					if (task.isSuccessful()) {
						double max_distance = busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_RADIO), getResources().getInteger(R.integer.DEFAULT_RADIO));
						List<Usuario_para_listar> listado_perfiles_encontrados = new ArrayList() ;

						for (QueryDocumentSnapshot document : task.getResult()) {
							Double distanceBetweenTwoPoints = utils.getDistancia(getApplicationContext(), document.getDouble(getString(R.string.USUARIO_LATITUD)), document.getDouble(getString(R.string.USUARIO_LONGITUD)));
							if (distanceBetweenTwoPoints < max_distance) {

								if (comprueba_edad_minima(busqueda_usuario.getLong(getString(R.string.BUSQUEDA_EDAD_MINIMA), 18L), (Long) document.get(getResources().getString(R.string.USUARIO_FECHA_NACIMIENTO)))) {
									if (comprueba_edad_maxima(busqueda_usuario.getLong(getString(R.string.BUSQUEDA_EDAD_MAXIMA), 99L), (Long) document.get(getResources().getString(R.string.USUARIO_FECHA_NACIMIENTO)))) {

										if (busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) > (Long) document.get(getResources().getString(R.string.USUARIO_PESO))) {
											if (busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) < (Long) document.get(getResources().getString(R.string.USUARIO_PESO))) {

												if (busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) > (Long) document.get(getResources().getString(R.string.USUARIO_ALTURA))) {
													if (busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) < (Long) document.get(getResources().getString(R.string.USUARIO_ALTURA))) {

														if (filtro_online(busqueda_usuario.getBoolean(getResources().getString(R.string.BUSQUEDA_QUE_ESTE_ONLINE), false), (Long) document.get(getResources().getString(R.string.USUARIO_FECHA_ULTIMO_ACCESO)))) {
															if (filtro_nuevo(busqueda_usuario.getBoolean(getResources().getString(R.string.BUSQUEDA_QUE_SEA_NUEVO), false), (Long) document.get(getResources().getString(R.string.USUARIO_FECHA_REGISTRO)))) {

																if (!perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").equals(document.getId())) {
																	Usuario_para_listar un_usuario_para_listar = new Usuario_para_listar(document.getData());
																	un_usuario_para_listar.setDouble_distancia(distanceBetweenTwoPoints);
																	un_usuario_para_listar.setLong_edad(Integer.valueOf(utils.getEdad(document.getLong(getString(R.string.USUARIO_FECHA_NACIMIENTO)))).longValue());
																	un_usuario_para_listar.setToken_socialauth(document.getId());
																	listado_perfiles_encontrados.add(un_usuario_para_listar);
																	if (! (Boolean) document.get(getResources().getString(R.string.USUARIO_ES_PREMIUM))) {
																		utils.actualizacion_semanal_de_estrellas(this, un_usuario_para_listar.getToken_socialauth(), un_usuario_para_listar.getFecha_cobro_estrellas());
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
						if (listado_perfiles_encontrados != null && !listado_perfiles_encontrados.isEmpty()) {
							ArrayList<Usuario_para_listar> listado_perfiles_promocionados = promocionar_premiums(listado_perfiles_encontrados);
							pinta_perfiles(listado_perfiles_promocionados);
							perfiles_encontrados_global = listado_perfiles_promocionados;
							//borra_cached_search(new File(path));
							//JSON_to_File(listado_perfiles_promocionados, path);
						}
						else {
							pinta_no_hay_perfiles();
						}
					} else {
						pinta_no_hay_perfiles();
						utils.registra_error(task.getException().toString(), "buscar_personas (onComplete) de Activity_Principal");

					}
				}catch(Exception e){
					pinta_no_hay_perfiles();
					utils.registra_error(e.toString(), "buscar_personas (onComplete) de Activity_Principal");
				}
			});

		} catch (Exception e) {
			pinta_no_hay_perfiles();
			utils.registra_error(e.toString(), "buscar_personas de Activity_Principal");
		}
	}

	private void actualiza_ubicacion_en_Firestore(String Token_SocialAuth,Location mLocation){
		try{
			DocumentReference Ref= db.collection(getResources().getString(R.string.USUARIOS)).document(Token_SocialAuth);
			WriteBatch batch = db.batch();
			batch.update(Ref,getResources().getString(R.string.USUARIO_LATITUD),mLocation.getLatitude());
			batch.update(Ref,getResources().getString(R.string.USUARIO_LONGITUD),mLocation.getLongitude());

			batch.commit();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "actualiza_ubicacion_en_Firestore de Activity_Principal");
		}
	}

	private void actualiza_pais_en_Firestore (String nuevo_pais){
		try{
			String token_socialauth= perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH),"");
			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).update(getResources().getString(R.string.USUARIO_PAIS),nuevo_pais);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "actualiza_pais_en_Firestore de Activity_Principal");
		}

	}

	private void desuscribir_de_grupo(){
		try{
			String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
			FirebaseMessaging.getInstance().unsubscribeFromTopic(grupo_al_que_pertenece);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "desuscribir_de_grupo de Activity_Principal");
		}
	}

	private void suscribir_a_grupo(){
		try{
			String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
			FirebaseMessaging.getInstance().subscribeToTopic(grupo_al_que_pertenece);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "suscribir_a_grupo de Activity_Principal");
		}
	}

	private void chequea_num_estrellas_y_premium(){
		try {
			if (ha_pasado_24h(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_FECHA_ULTIMO_ACCESO), 0))) {
				String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
				db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).get()
						.addOnCompleteListener(task -> {
							if (task.isSuccessful()) {
								DocumentSnapshot document = task.getResult();
								if (document.exists()) {
									try {
										boolean es_premium =(Boolean) document.get(getResources().getString(R.string.USUARIO_ES_PREMIUM));
										Long num_estrellas = (Long) document.get("estrellas");

										Editor editor_perfil_usuario = perfil_usuario.edit();
										editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), num_estrellas);
										editor_perfil_usuario.putBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM),es_premium);
										editor_perfil_usuario.apply();

										setupNavigationDrawer(); //Para que se actualice
										crea_ItemTouchListener();
										utils.setProgressBar_visibility(binding.mProgressBar, View.INVISIBLE);
									} catch (Exception e) {
										utils.registra_error(e.toString(), "chequea_num_estrellas_y_premium (onComplete) de Activity_Principal");
									}
								}
							}
						});
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "chequea_num_estrellas_y_premium de Activity_Principal");

		}
	}

	private boolean ha_pasado_24h(long ultimo_actualizacion) {
		boolean respuesta;
		Calendar hoy = Calendar.getInstance();
		long milisegundos_pasados = (hoy.getTimeInMillis() - ultimo_actualizacion);
		respuesta = (((((milisegundos_pasados / 1000) / 60) / 60) / 24) >= 1);
		if (respuesta) {
			Editor editor_perfil_usuario = perfil_usuario.edit();
			editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_FECHA_ULTIMO_ACCESO), hoy.getTimeInMillis());
			editor_perfil_usuario.apply();
		}
		return respuesta;
	}

	private class AsyncTask_descarga_mi_Thumb extends AsyncTask<String, Void, Void> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				String Token_socialAuth = params[0];
				StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket));

				StorageReference imagesRef = storageRef.child(utils.get_path_thumb(Token_socialAuth, 0));

				imagesRef.getBytes(getResources().getInteger(R.integer.MAX_PHOTO_LENGTH)).addOnSuccessListener(bytes -> {
					if (getApplicationContext() != null) { //puede que ya no estemos en la activity
						new AsyncTask_decode_mi_Thumb().execute(new Pair<>(bytes, Token_socialAuth));
					}
				}).addOnFailureListener(e -> {
				});
			} catch (Exception ignored) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void algo) {

		}

	}

	private class AsyncTask_decode_mi_Thumb extends AsyncTask<Pair<byte[], String>, Void, Bitmap> {
		@Override
		protected void onPreExecute() {
		}

		@SafeVarargs
		@Override
		protected final Bitmap doInBackground(Pair<byte[], String>... params) {
			byte[] bytes = params[0].first;
			String Token_socialAuth = params[0].second;

			Bitmap mBitmap = utils.decodeSampledBitmapFromBytes(bytes, getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles));

			File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			String nombre_foto = utils.get_nombre_thumb(Token_socialAuth, 0);
			File file = new File(storageDir, nombre_foto);
			if (!utils.guarda_foto_en_memoria_interna(bytes, file.getPath())){
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
			}
			return mBitmap;

		}

		@Override
		protected void onPostExecute(Bitmap mBitmap) {
			try {
				if ((mBitmap != null) && (getApplicationContext() != null)) { //puede que ya no estemos en la activity
					mImageView_PictureMain.setImageBitmap(mBitmap);
				}

			} catch (Exception e) {
				utils.registra_error(e.toString(), "asynctask_decode_mi_thumb de Activity_Principal");
			}
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
					new AsyncTask_descarga_mi_Thumb().execute(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));

				}
				mImageView_PictureMain.setImageDrawable(mAppCompatImageView.getDrawable());
			}catch (Exception e) {
				utils.registra_error(e.toString(), "asynctask_coloca_picturemain de Activity_Principal");
			}
		}
	}

	private class RecyclerTouchListener_perfiles implements OnItemTouchListener {
		@Nullable
		private final GestureDetector mGestureDetector;
		private final Interface_ClickListener_Perfiles mInterfaceClickListener;

		private RecyclerTouchListener_perfiles(Context context, @NonNull RecyclerView recyclerView, Interface_ClickListener_Perfiles un_Interface_ClickListener) {
			mInterfaceClickListener = un_Interface_ClickListener;
			mGestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
				@Override
				public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                  /*  try {
                        if ((e1 != null) && (e2 != null)) {
                            View child = binding.LayoutPrincipalReciclerView.findChildViewUnder(e1.getX(), e1.getY());
                            if ((child != null) && (mInterfaceClickListener != null)) {
                                // right to left swipe
                                if (((e1.getX() - e2.getX()) > getResources().getInteger(R.integer.SWIPE_MIN_DISTANCE)) && (Math.abs(velocityX) > getResources().getInteger(R.integer.SWIPE_THRESHOLD_VELOCITY))) {
                                    mInterfaceClickListener.My_onFling_Left(child, recyclerView.getChildLayoutPosition(child));
                                }
                                // left to right swipe
                                else if (((e2.getX() - e1.getX()) > getResources().getInteger(R.integer.SWIPE_MIN_DISTANCE)) && (Math.abs(velocityX) > getResources().getInteger(R.integer.SWIPE_THRESHOLD_VELOCITY))) {
                                    mInterfaceClickListener.My_onFling_Right(child, recyclerView.getChildLayoutPosition(child));
                                }
                            }
                        }
                    } catch (Exception e) {
                        new AsyncTask_registra_error().execute(new Pair<>("", "Exception en RecyclerTouchListener_perfiles (onFling) de Activity_Principal " + e));
                    }*/
					return true;
				}

				@Override
				public boolean onSingleTapUp(@NonNull MotionEvent e) {
					View child = binding.LayoutPrincipalReciclerView.findChildViewUnder(e.getX(), e.getY());
					if ((child != null) && (mInterfaceClickListener != null)) {
						mInterfaceClickListener.My_onClick(child, recyclerView.getChildLayoutPosition(child));
					}
					return true;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
                    /*View child = binding.LayoutPrincipalReciclerView.findChildViewUnder(e.getX(), e.getY());
                    if ((child != null) && (mInterfaceClickListener != null)) {
                        mInterfaceClickListener.My_onDoubleClick(child, recyclerView.getChildLayoutPosition(child));
                    }*/
					return true;
				}

				@Override
				public void onLongPress(@NonNull MotionEvent e) {
                    /*View child = binding.LayoutPrincipalReciclerView.findChildViewUnder(e.getX(), e.getY());
                    if ((child != null) && (mInterfaceClickListener != null)) {
                        mInterfaceClickListener.My_onLongPress(child, recyclerView.getChildLayoutPosition(child));
                    }*/
				}
			});
		}

		@Override
		public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
			//View child= rv.findChildViewUnder(e.getX(),e.getY());
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
			//View child= rv.findChildViewUnder(e.getX(),e.getY());
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