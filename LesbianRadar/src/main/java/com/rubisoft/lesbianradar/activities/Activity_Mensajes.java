package com.rubisoft.lesbianradar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.lesbianradar.Adapters.Drawer_Adapter;
import com.rubisoft.lesbianradar.Adapters.RecyclerView_Messages_Adapter;
import com.rubisoft.lesbianradar.Classes.Drawer_Item;
import com.rubisoft.lesbianradar.Classes.NpaGridLayoutManager;
import com.rubisoft.lesbianradar.Classes.Relacion_para_listar;
import com.rubisoft.lesbianradar.Dialogs.Dialog_Interactuar_Mensajes;
import com.rubisoft.lesbianradar.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.lesbianradar.Interfaces.Interface_ClickListener_Perfiles;
import com.rubisoft.lesbianradar.R;
import com.rubisoft.lesbianradar.databinding.LayoutMensajesBinding;
import com.rubisoft.lesbianradar.tools.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//**************************************************************************************************//
//*
//*  ESTA ACTIVITY ALBERGA DOS FRAGMENTS: FRAGMENT_MIS_AMIGOS Y FRAGMENT_SOLICITUDES_AMISTAD
//*  A LOS DOS FRAGMENTS SE ACCEDE A TRAVÉS DE DOS TABS QUE SE UBICAN EN LA PARTE SUPERIOR Y QUE
//*  CREAMOS CON TabLayout. NECESITAMOS UN VIEWPAGER PARA CONTENER LOS FRAGMENTS Y INTERCAMBIARLOS
//*
//**************************************************************************************************//

public class Activity_Mensajes extends AppCompatActivity {
	private FirebaseFirestore db;

	private List<Relacion_con_id> relaciones_encontradas;
	private RecyclerView_Messages_Adapter mRecyclerViewGridAdapter;
	private FirebaseStorage storage;
	private static final String TAG_INTERACCION = "interaccion";

	//navigation drawer
	private Toolbar toolbar;
	private ActionBarDrawerToggle drawerToggle;
	private RecyclerView recyclerViewDrawer;
	private ImageView mImageView_PictureMain;
	private SharedPreferences perfil_usuario;

	private LayoutMensajesBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Activity_Mensajes se compone de dos fragments en forma de tabs: solicitudes_amistad y mis_amigos
		overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
		super.onCreate(savedInstanceState);

		try {
			//Comprobamos siempre que haya internet
			if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
				perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
				if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
					salir();
				} else {
					binding = LayoutMensajesBinding.inflate(getLayoutInflater());
					setContentView(binding.getRoot());
					storage = FirebaseStorage.getInstance();
					db = FirebaseFirestore.getInstance();
					setup_toolbar();
					//setup_views();

					mRecyclerViewGridAdapter = new RecyclerView_Messages_Adapter(this);
					binding.LayoutCentroMensajesReciclerView.setAdapter(mRecyclerViewGridAdapter);
					get_amistades(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));
					inicializa_anuncios();
				}
			} else {
				//si no hay internet nos vamos a principalactivity como siempre
				Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
				startActivity(mIntent);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				finish();
			}
		}  catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de mensajes");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			Appodeal.onResume(this, Appodeal.BANNER_TOP);
			setupNavigationDrawer();// Setup navigation drawer
		}catch (Exception e){
			utils.registra_error(e.toString(), "onResume de mensajes");
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
	public void onBackPressed() {
		// super.onBackPressed();
		Intent mIntent = new Intent(this, Activity_Principal.class);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mIntent);
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
			utils.registra_error(e.toString(), "inicializa_anuncios de Activity_Mensajes");
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

	private void setup_toolbar() {
		// Setup toolbar and statusBar (really FrameLayout)
		toolbar = findViewById(R.id.mToolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
		getSupportActionBar().setDisplayHomeAsUpEnabled(!utils.isTablet(this));
		getSupportActionBar().setHomeButtonEnabled(true);
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
			utils.registra_error(e.toString(), "setupNavigationDrawer");
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
		recyclerViewDrawer.addOnItemTouchListener(new Activity_Mensajes.RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Mensajes.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				binding.mDrawerLayout.closeDrawers();
			}
		}));
	}

	private void crea_ItemTouchListener() {
		//Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
		//y que lo ha detectado y codificado primero el  RecyclerTouchListener
		binding.LayoutCentroMensajesReciclerView.addOnItemTouchListener(new RecyclerTouchListener_perfiles(this.getApplicationContext(), binding.LayoutCentroMensajesReciclerView, (view, position) -> {
			try {
				lanza_dialogo(position);
			} catch (Exception e) {
				utils.registra_error(e.toString(), "crea_ItemTouchListener");

			}
		}));
	}

	private void salir() {
		//Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir

		finish();
	}

	private void pinta_amigos(){
		try {
			binding.LayoutCentroMensajesTextViewNingunPerfil.setVisibility(View.INVISIBLE);
			binding.LayoutCentroMensajesReciclerView.setVisibility(View.VISIBLE);

			binding.LayoutCentroMensajesReciclerView.setLayoutManager(new NpaGridLayoutManager(this, utils.get_num_columns_grids(getApplicationContext())));

			for (int i = 0; i < relaciones_encontradas.size(); i++) {
				carga_perfil_para_lista(relaciones_encontradas.get(i), i);
			}
			binding.LayoutCentroMensajesReciclerView.invalidate();//para que se ejecute su animacion
		}catch (Exception e){
			utils.registra_error(e.toString(), "pinta_amigos de mensajes");
		}
	}

	private void lanza_dialogo(int posicion) {
		try {
			Dialog_Interactuar_Mensajes mDialog_Interactuar = new Dialog_Interactuar_Mensajes();
			String Nick_del_otro= relaciones_encontradas.get(posicion).getNick_de_la_otra_persona();
			String Token_SocialAuth_del_otro= relaciones_encontradas.get(posicion).getToken_socialauth_de_la_otra_persona();
			// Supply num input as an argument.
			Bundle args = new Bundle();
			args.putString(getResources().getString(R.string.RELACIONES_ID_RELACION),relaciones_encontradas.get(posicion).getId_relacion());
			args.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA),Token_SocialAuth_del_otro  );
			args.putString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA),Nick_del_otro);
			mDialog_Interactuar.setArguments(args);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(mDialog_Interactuar, TAG_INTERACCION);
			ft.commitAllowingStateLoss();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo de mensajes");
		}
	}

	private void carga_perfil_para_lista(Relacion_con_id una_relacion, int i) {
		try {
			String Token_SocialAuth_de_la_otra_persona= una_relacion.getToken_socialauth_de_la_otra_persona();
			String Nick_de_la_otra_persona=una_relacion.getNick_de_la_otra_persona();

			if (this.getApplicationContext() != null) {
				Relacion_para_listar una_relacion_para_listar = new Relacion_para_listar();
				una_relacion_para_listar.setNombre(Nick_de_la_otra_persona);
				una_relacion_para_listar.setToken_socialauth(Token_SocialAuth_de_la_otra_persona);

				if (una_relacion.getTiene_mensajes_sin_leer()) {
					Drawable tiene_mensajes_sin_leer = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mail).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(getResources().getInteger(R.integer.Tam_Notification_icons));
					una_relacion_para_listar.setIcono_mensajes_sin_leer(tiene_mensajes_sin_leer);
				} else {
					una_relacion_para_listar.setIcono_mensajes_sin_leer(null);
				}
				if (mRecyclerViewGridAdapter != null) {
					mRecyclerViewGridAdapter.addItem(i, una_relacion_para_listar);
					mRecyclerViewGridAdapter.notifyItemInserted(i);
					descarga_URL(Token_SocialAuth_de_la_otra_persona, una_relacion_para_listar,i);
				}
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "carga_perfil_para_lista de mensajes");
		}
	}

	private void descarga_URL(String Token_socialAuth, Relacion_para_listar una_relacion, int i) {
		try {
			StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_thumb(Token_socialAuth, 0));

			storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
				try {
					if (!isFinishing() && !isDestroyed() && (mRecyclerViewGridAdapter != null) && (i < mRecyclerViewGridAdapter.getItemCount())) {
						una_relacion.setFoto(uri.toString());

						mRecyclerViewGridAdapter.replaceItem(i, una_relacion);
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "descarga_URL 1 de mensajes");
				}
			}).addOnFailureListener(exception -> {

			});
		}catch (RejectedExecutionException e){
			try {
				Thread.sleep(500);
			}catch (Exception ignored){}
		}  catch (Exception e) {
			utils.registra_error(e.toString(), " descarga_URL 2 de mensajes");
		}
	}

	private void pinta_no_hay_amigos(){
		try {
			Typeface typeFace_roboto_light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
			binding.LayoutCentroMensajesTextViewNingunPerfil.setVisibility(View.VISIBLE);
			binding.LayoutCentroMensajesTextViewNingunPerfil.setTypeface(typeFace_roboto_light);
			binding.LayoutCentroMensajesTextViewNingunPerfil.setText(getResources().getString(R.string.ACTIVITY_MENSAJES_NO_HAY_MENSAJES));
		} catch (Exception e) {
			utils.registra_error(e.toString(), "pinta_no_hay_amigos de mensajes");
		}
	}

	private void setup_fondo_pantalla(int tipo) {
		try {
			if (this.getApplicationContext() != null) { //puede que ya no estemos en la activity
				Drawable fondo=null;
				RelativeLayout mRelativeLayout;

				switch (tipo) {
					case 0:
						fondo = new IconicsDrawable(this.getApplicationContext())
								.icon(GoogleMaterial.Icon.gmd_sentiment_very_satisfied)
								.color(ContextCompat.getColor(this.getApplicationContext(), R.color.gris_transparente));
						binding.LayoutCentroMensajesCardView.setVisibility(View.VISIBLE);
						break;
					case 1:
						fondo = new IconicsDrawable(this.getApplicationContext())
								.icon(GoogleMaterial.Icon.gmd_sentiment_neutral)
								.color(ContextCompat.getColor(this.getApplicationContext(), R.color.gris_transparente));
						break;
				}
				binding.LayoutCentroMensajesRelativeLayout.setBackground(fondo);
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "setup_fondo_pantalla de Activity_Mensajes");
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
				utils.registra_error(e.toString(), "AsyncTask_Coloca_PictureMain de mensajes");
			}
		}
	}

	private void get_amistades(String Token_Socialauth){
		db.collection(getString(R.string.USUARIOS)).document(Token_Socialauth).collection(getString(R.string.RELACIONES))
				.whereEqualTo(getResources().getString(R.string.RELACIONES_ESTADO_DE_LA_RELACION),getResources().getInteger(R.integer.RELACION_ACTIVA)).get()
				.addOnCompleteListener(task -> {
					if (task.isSuccessful() && task.getResult().size()>0) {
						List<Relacion_con_id> lista_relaciones = new ArrayList<>();
						for (QueryDocumentSnapshot document : task.getResult()) {
							Relacion_con_id una_relacion = new Relacion_con_id(document.getData());
							una_relacion.setId_relacion(document.getId());
							lista_relaciones.add(una_relacion);
						}
						procesa_relaciones(lista_relaciones);
					}else{
						procesa_relaciones(null);
					}
				});
	}

	private void procesa_relaciones(List<Relacion_con_id> listado){
		if (this !=null && !this.isFinishing()) {
			if (listado != null && !listado.isEmpty()) {
				relaciones_encontradas = listado;

				pinta_amigos();

				this.setup_fondo_pantalla(0);
				crea_ItemTouchListener();
			} else {
				pinta_no_hay_amigos();
				this.setup_fondo_pantalla(1);
			}
		}
	}

	private class RecyclerTouchListener_menu implements RecyclerView.OnItemTouchListener {
		@NonNull
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
					View child = binding.LayoutCentroMensajesReciclerView.findChildViewUnder(e.getX(), e.getY());
					if ((child != null) && (mInterfaceClickListener != null)) {
						mInterfaceClickListener.My_onClick(child, binding.LayoutCentroMensajesReciclerView.getChildLayoutPosition(child));
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

	//la clase Relacion que usamos para el firestore no tiene id_relacion entre sus campos ya que es su id
	private static class Relacion_con_id {

		private String token_socialauth_de_la_otra_persona;
		private String nick_de_la_otra_persona;
		private final Long estado_de_la_relacion;
		private Boolean tiene_mensajes_sin_leer;
		private String id_relacion; //la usaremos para recoger el id del document en firestore

		Relacion_con_id(Map<String, Object> mapa) {
			token_socialauth_de_la_otra_persona = (String) mapa.get("token_socialauth_de_la_otra_persona");
			nick_de_la_otra_persona = (String) mapa.get("nick_de_la_otra_persona");
			estado_de_la_relacion = (Long) mapa.get("estado_de_la_relacion");
			tiene_mensajes_sin_leer = (Boolean) mapa.get("tiene_mensajes_sin_leer");

		}

		public Relacion_con_id(String un_token_socialauth_de_la_otra_persona, String un_nick_de_la_otra_persona, Long un_estado, Boolean tiene_o_no_tiene) {
			token_socialauth_de_la_otra_persona = un_token_socialauth_de_la_otra_persona;
			nick_de_la_otra_persona = un_nick_de_la_otra_persona;
			estado_de_la_relacion = un_estado;
			tiene_mensajes_sin_leer = tiene_o_no_tiene;
		}

		String getId_relacion() {
			return id_relacion;
		}

		void setId_relacion(String id_relacion) {
			this.id_relacion = id_relacion;
		}

		String getToken_socialauth_de_la_otra_persona() {
			return token_socialauth_de_la_otra_persona;
		}

		public void setToken_socialauth_de_la_otra_persona(String token_socialauth_de_la_otra_persona) {
			this.token_socialauth_de_la_otra_persona = token_socialauth_de_la_otra_persona;
		}

		String getNick_de_la_otra_persona() {
			return nick_de_la_otra_persona;
		}

		public void setNick_de_la_otra_persona(String nick_de_la_otra_persona) {
			this.nick_de_la_otra_persona = nick_de_la_otra_persona;
		}

		Boolean getTiene_mensajes_sin_leer() {
			return tiene_mensajes_sin_leer;
		}

		public void setTiene_mensajes_sin_leer(Boolean tiene_mensajes_sin_leer) {
			this.tiene_mensajes_sin_leer = tiene_mensajes_sin_leer;
		}
	}
}