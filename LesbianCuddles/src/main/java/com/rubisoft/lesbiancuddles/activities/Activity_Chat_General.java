package com.rubisoft.lesbiancuddles.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.lesbiancuddles.Adapters.Drawer_Adapter;
import com.rubisoft.lesbiancuddles.Adapters.RecyclerView_Chat_Adapter;
import com.rubisoft.lesbiancuddles.Classes.Drawer_Item;
import com.rubisoft.lesbiancuddles.Classes.Usuario_para_listar;
import com.rubisoft.lesbiancuddles.Dialogs.Dialog_Interactuar_Chat_General;
import com.rubisoft.lesbiancuddles.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.lesbiancuddles.Interfaces.Interface_ClickListener_Perfiles;
import com.rubisoft.lesbiancuddles.R;
import com.rubisoft.lesbiancuddles.RecyclersViews.RecyclerView_AutoFit;
import com.rubisoft.lesbiancuddles.tools.utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Chat_General extends AppCompatActivity {
	private final BroadcastReceiver mBroadcastReceiver_new_message_chat_general = new MyReceiver_new_message_chat_general();

	private SharedPreferences perfil_usuario;
	private SharedPreferences chat_general;
	private EditText texto_a_enviar;
	private String token_socialauth_usuario;
	private String nick_usuario;
	private LinearLayout linearlayout_conversacion;
	private TextView textView_consejo;
	private RecyclerView_AutoFit mRecyclerView;
	private RecyclerView_Chat_Adapter mRecyclerViewListAdapter;
	private FirebaseStorage storage;
	private AppCompatImageView boton_enviar;
	private FirebaseFirestore db;

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
			//Comprobamos siempre que haya internet
			if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
				inicializa_sharedPreferences();
				if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
					salir();
				} else {
					setContentView(R.layout.layout_chat_general);
					db = FirebaseFirestore.getInstance();
					setup_toolbar();
					setup_views();
					inicializa_anuncios();
					setup_typefaces();

					token_socialauth_usuario = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
					nick_usuario = perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_NICK), "");
					textView_consejo.setText(getString(R.string.DIALOGO_CONSEJO_MENSAJE_CHAT_GENERAL));
					storage = FirebaseStorage.getInstance();

					Cargar_chat();//cargamos toda la conversación previa hasta el momento
					get_usuarios_online(perfil_usuario.getLong(getString(R.string.PERFIL_USUARIO_ORIENTACION), 0L),perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_PAIS), ""));

					Drawable icono_enviar = new IconicsDrawable(getApplicationContext()).icon(GoogleMaterial.Icon.gmd_send).color(ContextCompat.getColor(getApplicationContext(), R.color.accent)).sizeDp(getResources().getInteger(R.integer.Tam_Normal_icons));
					boton_enviar.setImageDrawable(icono_enviar);
					boton_enviar.setOnClickListener(v -> procesa_enviar_mensaje());
					//Oculta el softkeyboard
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				}
			} else {
				Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(mIntent);
				finish();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de chat_general");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			Appodeal.onResume(this, Appodeal.BANNER_TOP);
			IntentFilter filter_new_request_chat_general = new IntentFilter(getResources().getString(R.string.GOT_MESSAGE_CHAT_GENERAL));
			registerReceiver(mBroadcastReceiver_new_message_chat_general, filter_new_request_chat_general);

			setupNavigationDrawer();// Setup navigation drawer
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onResume de Activity chat_general");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(mBroadcastReceiver_new_message_chat_general);
		} catch (Exception ignored) {

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

	private void pinta_conversacion(String quien_lo_dijo, String que_dijo, String nick_de_quien_lo_dijo, Long hora) {
		//*******************************************************************************
		//*
		//* La estructura es la siguiente:
		//*
		//* ScrollView
		//*     - LinearLayout un_bocadillo
		//*             - TextView  texto
		//*             - TextView  hora
		//*     - LinearLayout un_bocadillo
		//*                 .
		//*                 .
		//*                 .
		//*******************************************************************************
		try {
			Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
			Typeface typeFace_roboto_Regular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
			LinearLayout.LayoutParams layoutParams_miniaturas;

			layoutParams_miniaturas = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat));

			layoutParams_miniaturas.weight = 0.2f;
			CircleImageView mAppCompatImageView = new CircleImageView(this);
			mAppCompatImageView.setLayoutParams(layoutParams_miniaturas);

			TextView TextView_texto = new TextView(getApplicationContext());
			TextView_texto.setText(que_dijo);
			TextView_texto.setTypeface(typeFace_roboto_Regular);
			TextView_texto.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

			DateFormat sdf_fecha = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
			DateFormat sdf_hora = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM,Locale.getDefault());

			TextView TextView_nick_y_hora = new TextView(getApplicationContext());
			TextView_nick_y_hora.setTypeface(typeFace_roboto_Light);
			TextView_nick_y_hora.setText(getResources().getString(R.string.NICK_FECHA_HORA,nick_de_quien_lo_dijo ,sdf_fecha.format(hora),sdf_hora.format(hora)));

			// if(utils.isTablet(this)) {
			float SCREEN_DENSITY = getResources().getDisplayMetrics().density;

			TextView_texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_s_plus) /SCREEN_DENSITY);
			TextView_nick_y_hora.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_xs)/SCREEN_DENSITY);
            /*}else{
                TextView_texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_s_plus) * 0.4f);
                TextView_nick_y_hora.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_xs) * 0.4f);

            }*/
			TextView_nick_y_hora.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris));

			LinearLayout LinearLayout_vertical = new LinearLayout(getApplicationContext());
			LinearLayout LinearLayout_horizontal = new LinearLayout(getApplicationContext());

			//Dependiendo de quien lo dijo lo pondremos pegado a la izquierda o a la derecha
			LinearLayout.LayoutParams LayoutParams_horizontal = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			LayoutParams_horizontal.setMargins(0, utils.Dp2Px(8, this), 0, 0);

			LinearLayout.LayoutParams LayoutParams_vertical = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			LayoutParams_vertical.setMargins(10, 10, 10, 10);
			LayoutParams_vertical.weight = 0.8f;
			LinearLayout_vertical.setOrientation(LinearLayout.VERTICAL);
			LinearLayout_vertical.setGravity(Gravity.START);

			TextView_texto.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
			TextView_nick_y_hora.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);

			LinearLayout_vertical.addView(TextView_texto);
			LinearLayout_vertical.addView(TextView_nick_y_hora);

			if (quien_lo_dijo.equals(token_socialauth_usuario)) {
				LayoutParams_horizontal.gravity = Gravity.END;

				new Activity_Chat_General.AsyncTask_Carga_Miniatura_mia_de_memoria().execute(mAppCompatImageView);

				LinearLayout_horizontal.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_rectangle_izq));

				//  LinearLayout_horizontal.setGravity(Gravity.LEFT);

			} else {
				LayoutParams_horizontal.gravity = Gravity.START;

				new AsyncTask_descarga_Thumb_de_la_otra_persona().execute(new Pair<>(mAppCompatImageView, new Pair<>(quien_lo_dijo,nick_de_quien_lo_dijo)));

				LinearLayout_horizontal.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_rectangle_dcha));

			}
			LinearLayout_horizontal.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout_horizontal.setPadding(5, 5, 5, 5);
			LinearLayout_horizontal.setLayoutParams(LayoutParams_horizontal);
			LinearLayout_vertical.setLayoutParams(LayoutParams_vertical);
			LinearLayout_horizontal.addView(mAppCompatImageView);
			LinearLayout_horizontal.addView(LinearLayout_vertical);

			linearlayout_conversacion.addView(LinearLayout_horizontal);
			linearlayout_conversacion.invalidate();
			//esto es para que se vea la parte final del scrollview
			ScrollView scrollview = findViewById(R.id.Layout_chat_general_ScrollView);
			scrollview.postDelayed(new Activity_Chat_General.MyRunnable(scrollview), 100);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "pinta_conversacion de chat_general");
		}
	}

	private void inicializa_sharedPreferences(){
		perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
		chat_general = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PRINCIPAL), Context.MODE_PRIVATE);

	}

	private void procesa_enviar_mensaje(){
		try {
			String texto = texto_a_enviar.getText().toString();
			if (!texto.isEmpty() && token_socialauth_usuario!=null) {
				Long hora = Calendar.getInstance().getTimeInMillis();

				pinta_conversacion(token_socialauth_usuario, texto_a_enviar.getText().toString(), nick_usuario, hora);
				utils.acomodar_conversacion_en_sharedpreferences(getApplicationContext(), token_socialauth_usuario, texto_a_enviar.getText().toString(), hora, nick_usuario);
				String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
				difunde_mensaje(texto_a_enviar.getText().toString(),grupo_al_que_pertenece);

				texto_a_enviar.getText().clear();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "procesa_enviar_mensaje de chat_general");
		}
	}

	private void setup_typefaces(){
		Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		Typeface typeFace_roboto_Bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

		texto_a_enviar.setTypeface(typeFace_roboto_Light);
		textView_consejo.setTypeface(typeFace_roboto_Bold);
	}

	private void setup_views(){
		linearlayout_conversacion = findViewById(R.id.Layout_chat_general_LinearLayout_conversacion);
		texto_a_enviar = findViewById(R.id.Layout_chat_general_EditText_texto_a_enviar);
		 textView_consejo = findViewById(R.id.Layout_chat_general_TextView_ayuda);
		 boton_enviar = findViewById(R.id.Layout_chat_general_ImageView_enviar_mensaje);
		Main_LinearLayout = findViewById(R.id.Main_LinearLayout);
		mDrawerLayout = findViewById(R.id.mDrawerLayout);

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
				Appodeal.setTesting(false);
				Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER|Appodeal.INTERSTITIAL, consent);
				setup_banner();
				lanza_interstitial();
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
					Log.d("Appodeal", "onInterstitialFailedToLoad");

				}
				@Override
				public void onBannerFailedToLoad() {
					Log.d("Appodeal", "onInterstitialFailedToLoad");

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
					Log.d("Appodeal", "onInterstitialFailedToLoad");

				}
			});
			Appodeal.show(this, Appodeal.BANNER_TOP);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_banner de Activity_Ayuda");
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
						Log.d("Appodeal", "onInterstitialLoaded");
					}
					@Override
					public void onInterstitialFailedToLoad() {
						Log.d("Appodeal", "onInterstitialFailedToLoad");
					}
					@Override
					public void onInterstitialShown() {
						Log.d("Appodeal", "onInterstitialShown");
					}
					@Override
					public void onInterstitialClicked() {
						Log.d("Appodeal", "onInterstitialClicked");
					}
					@Override
					public void onInterstitialClosed() {
						Log.d("Appodeal", "onInterstitialClosed");
					}
					@Override
					public void onInterstitialExpired() {
						Log.d("Appodeal", "onInterstitialExpired");
					}
					@Override
					public void onInterstitialShowFailed() {
						Log.d("Appodeal", "onInterstitialFailedToLoad");
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

	private void setup_toolbar() {
		try {
			// Setup toolbar and statusBar (really FrameLayout)
			toolbar = findViewById(R.id.mToolbar);
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
			getSupportActionBar().setDisplayHomeAsUpEnabled(!utils.isTablet(this));
			getSupportActionBar().setHomeButtonEnabled(true);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_toolbar de chat_general");
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
			utils.registra_error(e.toString(), "setupNavigationDrawer de chat_general");
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
		recyclerViewDrawer.addOnItemTouchListener(new Activity_Chat_General.RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Chat_General.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				mDrawerLayout.closeDrawers();
			}
		}));
	}

	private void Cargar_chat() {
		try {
			String que_dijo;
			String quien_lo_dijo;
			String nick_de_quien_lo_dijo;
			Long cuando_lo_dijo;
			int i = 0;
			while (chat_general.getString(getResources().getString(R.string.CHAT_GENERAL_QUIEN_LO_DIJO) + i, null) != null) {
				que_dijo = chat_general.getString(getResources().getString(R.string.CHAT_GENERAL_QUE_DIJO) + i, null);
				quien_lo_dijo = chat_general.getString(getResources().getString(R.string.CHAT_GENERAL_QUIEN_LO_DIJO) + i, null);
				cuando_lo_dijo = chat_general.getLong(getResources().getString(R.string.CHAT_GENERAL_CUANDO_LO_DIJO) + i, -1L);
				nick_de_quien_lo_dijo = chat_general.getString(getResources().getString(R.string.CHAT_GENERAL_NICK_DE_QUIEN_LO_DIJO) + i, null);

				pinta_conversacion(quien_lo_dijo, que_dijo, nick_de_quien_lo_dijo,cuando_lo_dijo);
				i++;
			}

		} catch (Exception e) {
			utils.registra_error(e.toString(), "Cargar_chat");
		}
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

	private void descarga_URL(String Token_socialAuth, Usuario_para_listar un_usuario, int i) {
		try {
			StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_thumb(Token_socialAuth, 0));

			storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
				try {
					un_usuario.setFoto(uri.toString());

					mRecyclerViewListAdapter.replaceItem(i, un_usuario);

				} catch (Exception e) {
					utils.registra_error(e.toString(), "descarga_URL (onSuccess) de chat_general");
				}
			}).addOnFailureListener(exception -> {
			});
		}catch (RejectedExecutionException e){
			try {
				Thread.sleep(500);
			}catch (Exception ignored){}
		}  catch (Exception e) {
			utils.registra_error(e.toString(), "descarga_URL de chat_general");
		}
	}

	private void lanza_dialogo(String token_socialauth_de_la_otra_persona, String nick_de_la_otra_persona) {
		try {
			Dialog_Interactuar_Chat_General mDialog_Interactuar = new Dialog_Interactuar_Chat_General();
			Bundle args = new Bundle();
			args.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), token_socialauth_de_la_otra_persona);
			args.putString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA),nick_de_la_otra_persona);
			args.putInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO),getResources().getInteger(R.integer.VENGO_DE_CHAT_GENERAL));
			args.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH),token_socialauth_usuario);
			mDialog_Interactuar.setArguments(args);
			mDialog_Interactuar.show(getSupportFragmentManager(),"d");
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo");
		}
	}

	private void crea_ItemTouchListener(final List<String> usuarios_online) {
		//Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
		//y que lo ha detectado y codificado primero el  RecyclerTouchListener
		mRecyclerView.addOnItemTouchListener(new Activity_Chat_General.RecyclerTouchListener_perfiles(this.getApplicationContext(), mRecyclerView, (view, position) -> {
			try {
				get_perfil(usuarios_online.get(position));
				//new Activity_Chat_General.AsyncTask_get_perfil().execute(Tokens_SocialAuth_OnLine.get(position));

			} catch (Exception e) {
				utils.registra_error(e.toString(), "crea_ItemTouchListener de chat_general");
			}
		}));
	}

	private class RecyclerTouchListener_perfiles implements RecyclerView.OnItemTouchListener {
		@Nullable
		private final GestureDetector mGestureDetector;
		private final Interface_ClickListener_Perfiles mInterfaceClickListener;

		private RecyclerTouchListener_perfiles(Context context, @NonNull RecyclerView recyclerView, Interface_ClickListener_Perfiles un_Interface_ClickListener) {
			mInterfaceClickListener = un_Interface_ClickListener;
			mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {

					return true;
				}

				@Override
				public boolean onSingleTapUp(@NonNull MotionEvent e) {
					View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
					if ((child != null) && (mInterfaceClickListener != null)) {
						mInterfaceClickListener.My_onClick(child, recyclerView.getChildLayoutPosition(child));
					}
					return true;
				}

				@Override
				public boolean onDoubleTap(MotionEvent e) {
                    /*View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if ((child != null) && (mInterfaceClickListener != null)) {
                        mInterfaceClickListener.My_onDoubleClick(child, recyclerView.getChildLayoutPosition(child));
                    }*/
					return true;
				}

				@Override
				public void onLongPress(@NonNull MotionEvent e) {
                    /*View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
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

	private void get_usuarios_online(Long orientacion,String pais){
		try {
			db.collection(getResources().getString(R.string.USUARIOS))
					.whereEqualTo(getString(R.string.USUARIO_ORIENTACION), orientacion)
					.whereEqualTo(getString(R.string.USUARIO_PAIS), pais)
					.get().addOnCompleteListener(task -> {
				if (task.isSuccessful()) {
					List<String> usuarios_online = new ArrayList();
					List<String> nicks_online = new ArrayList();
					for (QueryDocumentSnapshot document : task.getResult()) {
							usuarios_online.add(document.getId());
							nicks_online.add((String) document.get(getString(R.string.USUARIO_NICK)));

					}
					procesa_usuarios_online(usuarios_online, nicks_online);
				} else {
					Toast.makeText(Activity_Chat_General.this, String.format(getString(R.string.ACTIVITY_CHAT_GENERAL_CUANTOS_HAY), 0), Toast.LENGTH_LONG).show();
				}
			});
		}catch (Exception e){
			utils.registra_error(e.toString(), "get_usuarios_online de Chat_general");
		}
	}

	private void procesa_usuarios_online(List<String> usuarios_online,List<String> nicks_online){
		mRecyclerViewListAdapter = new RecyclerView_Chat_Adapter(getApplicationContext());
		mRecyclerView = findViewById(R.id.Layout_chat_general_ReciclerView);

		mRecyclerView.setAdapter(mRecyclerViewListAdapter);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

		int posicion_de_mi_nick=usuarios_online.indexOf(token_socialauth_usuario);
		if (usuarios_online.remove(token_socialauth_usuario)) {
			nicks_online.remove(posicion_de_mi_nick);
		}
		int i=0;
		for (String un_Token_SocialAuth : usuarios_online) {
			try {
				Usuario_para_listar un_usuario = new Usuario_para_listar();
				un_usuario.setNick(nicks_online.get(i));
				mRecyclerViewListAdapter.addItem(i, un_usuario);
				descarga_URL(un_Token_SocialAuth, un_usuario, i);
				i++;
			} catch (Exception e) {
				utils.registra_error(e.toString(), "AsyncTask_Get_Usuarios_OnLine (1) de Activity_chat_general");
			}

		}
		mRecyclerViewListAdapter.notifyDataSetChanged();
		mRecyclerView.invalidate();

		crea_ItemTouchListener(usuarios_online);
	}

	private void get_perfil(String Token_SocialAuth){

		DocumentReference docRef = db.collection(getResources().getString(R.string.USUARIOS)).document(Token_SocialAuth);
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document.exists()) {
					Map<String,Object> un_usuario= document.getData();
					//Document exists  --> entramos
					double latitud = utils.convertToDecimal((double) (perfil_usuario.getFloat(getString(R.string.PERFIL_USUARIO_LATITUD), 0.0F)), 3);
					double longitud = utils.convertToDecimal((double) (perfil_usuario.getFloat(getString(R.string.PERFIL_USUARIO_LONGITUD), 0.0F)), 3);
					double distancia= utils.distFrom(latitud, longitud,(double) un_usuario.get(getResources().getString(R.string.USUARIO_LATITUD)),(double) un_usuario.get(getResources().getString(R.string.USUARIO_LONGITUD)));
					un_usuario.put(getResources().getString(R.string.PERFIL_USUARIO_DISTANCIA),distancia);
					lanza_dialogo(document.getId(), (String)un_usuario.get(getResources().getString(R.string.USUARIO_NICK)));

				}  //Document doesn't exist

			} else {
				//Query failed
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_INICIO_ERROR_LOGGIN), Toast.LENGTH_LONG).show();
			}
		});
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
				utils.registra_error(e.toString(), "AsyncTask_Coloca_PictureMain de chat_general");
			}
		}
	}

	private class AsyncTask_Carga_Miniatura_mia_de_memoria extends AsyncTask<CircleImageView, Void, Pair<CircleImageView, Bitmap>> {
		@Override
		protected void onPreExecute() {
		}

		@Nullable
		@Override
		protected Pair<CircleImageView, Bitmap> doInBackground(CircleImageView... params) {
			Bitmap mBitmap = null;
			try {
				String nombre_foto = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), 0);
				File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

				File file = new File(storageDir, nombre_foto);
				if (file.exists()) {
					mBitmap = utils.decodeSampledBitmapFromFilePath(file.getPath(), (int) getResources().getDimension(R.dimen.tamanyo_miniaturas_chat), (int) getResources().getDimension(R.dimen.tamanyo_miniaturas_chat));
				} else {
					Drawable drawable_no_pic =utils.get_no_pic(getApplicationContext(),ContextCompat.getColor(getApplicationContext(), R.color.primary_light));
					mBitmap = ((BitmapDrawable) drawable_no_pic).getBitmap();
				}
			} catch (Exception ignored) {
			}
			return new Pair<>(params[0], mBitmap);

		}

		@Override
		protected void onPostExecute(Pair mi_par) {
			try {
				CircleImageView mAppCompatImageView = (CircleImageView) mi_par.first;
				Bitmap mBitmap = (Bitmap) mi_par.second;

				if (mBitmap != null) {
					mAppCompatImageView.setImageBitmap(mBitmap);
					mAppCompatImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				}else {
					mAppCompatImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(),ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "AsyncTask_Carga_Miniatura_mia_de_memoria");
			}
		}
	}

	private void  difunde_mensaje(String mensaje, String group_id ){
		long ahora=Calendar.getInstance().getTimeInMillis();

		FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
		Map<String, Object> data = new HashMap<>();
		data.put("title", getResources().getString(R.string.DEFAULT_NOTIFICATION_TITLE));
		data.put("body",  getResources().getString(R.string.DEFAULT_NOTIFICATION_TEXT));
		data.put("que_dijo", mensaje);
		data.put("group_id", group_id);
		data.put("quien_lo_dijo", token_socialauth_usuario);
		data.put("nick_de_quien_lo_dijo", perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_NICK),""));
		data.put("cuando_lo_dijo", Long.toString(ahora));

		 mFunctions.getHttpsCallable("sendGroupMessage")
				.call(data)
				.continueWith(task -> {
					// This continuation runs on either success or failure, but if the task
					// has failed then getResult() will throw an Exception which will be
					// propagated down.
					return (String) task.getResult().getData();
				});
	}

	private class AsyncTask_descarga_Thumb_de_la_otra_persona extends AsyncTask<Pair<CircleImageView,  Pair<String,String>>, Void, Void> {
		@Override
		protected void onPreExecute() {
		}

		@SafeVarargs
		@Override
		protected final Void doInBackground(Pair<CircleImageView, Pair<String, String>>... params) {
			CircleImageView mAppCompatImageView = params[0].first;
			String token_socialauth_de_la_otra_persona = params[0].second.first;
			String nick_de_la_otra_persona = params[0].second.second;

			try {
				FirebaseStorage storage = FirebaseStorage.getInstance();
				StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_thumb(token_socialauth_de_la_otra_persona, 0));

				storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
					if (getApplicationContext() != null) { //puede que ya no estemos en la activity
						Picasso.with(getApplicationContext())
								.load(uri)
								.placeholder(utils.get_no_pic(getApplicationContext(),ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))   // optional
								.error(utils.get_no_pic(getApplicationContext(),ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))       // optional
								.centerCrop()
								.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat))                        // optional
								.into(mAppCompatImageView);
						mAppCompatImageView.setOnClickListener(v -> {
							try {
								lanza_dialogo(token_socialauth_de_la_otra_persona, nick_de_la_otra_persona);

							} catch (Exception e) {
								utils.registra_error(e.toString(), "AsyncTask_descarga_Thumb_de_la_otra_persona (onSuccess) de chat_general");
							}
						});
						mAppCompatImageView.invalidate();
					}
				}).addOnFailureListener(e -> {
					mAppCompatImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(),ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
					mAppCompatImageView.setOnClickListener(v -> {
						try {
							lanza_dialogo(token_socialauth_de_la_otra_persona, nick_de_la_otra_persona);

						} catch (Exception e1) {
							utils.registra_error(e1.toString(), "AsyncTask_descarga_Thumb_de_la_otra_persona (onFailure) de chat_general");
						}
					});
				});
			} catch (Exception ignored) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void algo) {

		}

	}

	private class MyReceiver_new_message_chat_general extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {

				Bundle mBundle = intent.getExtras();

				String quien_lo_dijo = mBundle.getString("quien_lo_dijo");

				if (!token_socialauth_usuario.equals(quien_lo_dijo)) {
					String nick_de_la_otra_persona = mBundle.getString("nick_de_quien_lo_dijo");
					String que_dijo = mBundle.getString("que_dijo");
					Long hora = Calendar.getInstance().getTimeInMillis();
					utils.acomodar_conversacion_en_sharedpreferences(getApplicationContext(), quien_lo_dijo, que_dijo, hora, nick_de_la_otra_persona);
					pinta_conversacion(quien_lo_dijo, que_dijo, nick_de_la_otra_persona,hora);
				}
				abortBroadcast();
			} catch (Exception e) {
				utils.registra_error(e.toString(), "MyReceiver_new_message_chat_general de Chat_general");
			}
		}
	}
}