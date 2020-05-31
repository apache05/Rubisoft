package com.rubisoft.bisexcuddles.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.bisexcuddles.Adapters.Drawer_Adapter;
import com.rubisoft.bisexcuddles.BuildConfig;
import com.rubisoft.bisexcuddles.Classes.Chat;
import com.rubisoft.bisexcuddles.Classes.Drawer_Item;
import com.rubisoft.bisexcuddles.Classes.Logro;
import com.rubisoft.bisexcuddles.Classes.Relacion;
import com.rubisoft.bisexcuddles.Classes.STATS_RELACIONES;
import com.rubisoft.bisexcuddles.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.bisexcuddles.R;
import com.rubisoft.bisexcuddles.databinding.LayoutChatBinding;
import com.rubisoft.bisexcuddles.tools.utils;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Chat_Individual extends AppCompatActivity {
	private final BroadcastReceiver mBroadcastReceiver_new_chat_individual = new MyReceiver_new_message_chat_individual();
	private static final int REQUEST_LOAD_IMAGE = 1;
	private static final int REQUEST_CAPTURE_IMAGE = 2;
	private static final int PERMISSION_CAPTURAR_FOTO = 1;
	private static final int PERMISSION_SELECCIONAR_FOTO = 2;

	private SharedPreferences perfil_usuario;
	private Integer de_donde_vengo;
	private FirebaseFirestore db;

	private int pagina;
	private String token_socialauth_de_la_otra_persona;
	private String nick_de_la_otra_persona;
	private String token_socialauth_mio;
	private String nick_mio;

	private String id_relacion;  //el que inició la relación
	//private LinearLayout linearlayout_conversacion;
	private Typeface typeFace_roboto_Light;
	private Typeface typeFace_roboto_Regular;

	private String nombre_foto_capturada;
	private String token_FCM_de_la_otra_persona;

	//navigation drawer
	private Toolbar toolbar;
	private ActionBarDrawerToggle drawerToggle;
	private RecyclerView recyclerViewDrawer;
	private ImageView mImageView_PictureMain;

	private LayoutChatBinding binding;

	private static String get_nombre_foto(String token_socialauth) {
		return token_socialauth + "_foto0.jpg";
	}

	private static boolean IsSupportedFile(String filePath) {
		// Check supported file extensions
		List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
		String ext = filePath.substring(filePath.lastIndexOf('.') + 1);

		return FILE_EXTN.contains(ext.toLowerCase(Locale.getDefault()));
	}

	private static byte[] FileToBytes(@NonNull String file_path) {
		// ************************************************************************************************
		// Esta función transforma un File en bytes para poder guardarse en un BeanFoto
		// ************************************************************************************************
		byte[] result = null;
		try {
			FileInputStream fis = new FileInputStream(file_path);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int buffer_size = 1024 * 4;
			byte[] bytes = new byte[buffer_size];
			int bytes_leidos;
			while ((bytes_leidos = fis.read(bytes, 0, buffer_size)) != -1) {
				baos.write(bytes, 0, bytes_leidos);
			}
			result = baos.toByteArray();
			fis.close();
			baos.close();
		} catch (Exception ignored) {
		}
		return result;
	}

	private static String get_path_foto_enviada(String token_socialauth, String fecha) {
		return "fotos_usuarios/" + token_socialauth + '/' + token_socialauth + "_ENVIADA_"+fecha+".jpg";
	}

	private static String get_path_thumb_enviada(String token_socialauth, String fecha) {
		return "fotos_usuarios/" + token_socialauth + '/' + token_socialauth+"_ENVIADA_"+fecha + "_thumb.jpg";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
		super.onCreate(savedInstanceState);

		try {
			//Comprobamos siempre que haya internet
			if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
				perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
				if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
					salir();
				} else {
					binding = LayoutChatBinding.inflate(getLayoutInflater());
					setContentView(binding.getRoot());
					setup_toolbar();// Setup toolbar and statusBar (really FrameLayout)
					inicializa_anuncios();

					db = FirebaseFirestore.getInstance();
					token_socialauth_mio = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
					nick_mio = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_NICK), "");

					binding.mProgressBar.setVisibility(View.VISIBLE);
					get_datos_del_bundle();
					set_Typefaces();
					get_token_FCM_de_la_otra_persona();

					Drawable icono_enviar = new IconicsDrawable(getApplicationContext()).icon(Icon.gmd_send).color(ContextCompat.getColor(getApplicationContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Normal_icons));
					Drawable icono_adjuntar_foto = new IconicsDrawable(getApplicationContext()).icon(Icon.gmd_attach_file).color(ContextCompat.getColor(getApplicationContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Normal_icons));
					Drawable icono_hacer_foto = new IconicsDrawable(getApplicationContext()).icon(Icon.gmd_camera_alt).color(ContextCompat.getColor(getApplicationContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Normal_icons));

					binding.LayoutChatImageViewEnviarMensaje.setImageDrawable(icono_enviar);
					binding.LayoutChatImageViewEnviarMensaje.setEnabled(false);
					binding.LayoutChatImageViewAdjuntarFoto.setImageDrawable(icono_adjuntar_foto);
					binding.LayoutChatImageViewAdjuntarFoto.setEnabled(false);
					binding.LayoutChatImageViewHacerFoto.setImageDrawable(icono_hacer_foto);
					binding.LayoutChatImageViewHacerFoto.setEnabled(false);

					//Oculta el softkeyboard
					getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
					);
				}
			} else {
				Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(mIntent);
				finish();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de chat_individual");
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
	protected void onResume() {
		//Tenemos que registrar el receiver cada vez que pasamos por onResume
		//Este receiver permanece escuchando por si el otro Usuario_para_listar manda un mensaje
		//y así poder mostrarlo en tiempo real
		super.onResume();

		try {
			Appodeal.onResume(this, Appodeal.BANNER_TOP);
			IntentFilter filter_new_request_chat_individual = new IntentFilter(getResources().getString(R.string.GOT_MESSAGE_CHAT_INDIVIDUAL));
			registerReceiver(mBroadcastReceiver_new_chat_individual, filter_new_request_chat_individual);
			setupNavigationDrawer();// Setup navigation drawer
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onResume de Activity chat_individual");
		}
	}

	@Override
	public void onBackPressed() {
		Intent mIntent;

		try {

			if (de_donde_vengo == getResources().getInteger(R.integer.VENGO_DE_PRINCIPAL)){
				Bundle Bundle_destino = new Bundle();
				Bundle_destino.putInt(getResources().getString(R.string.PAGINA), pagina);
				mIntent = new Intent(this, Activity_Principal.class);
				mIntent.putExtras(Bundle_destino);
			}else if (de_donde_vengo == getResources().getInteger(R.integer.VENGO_DE_CHAT_GENERAL)) {
				mIntent = new Intent(this, Activity_Chat_General.class);
			}else if (de_donde_vengo == getResources().getInteger(R.integer.VENGO_DE_MENSAJES)){
				mIntent = new Intent(this, Activity_Mensajes.class);
			}else{
				mIntent = new Intent(this, Activity_Principal.class);
			}
			mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			startActivity(mIntent);
			finish();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onBackPressed de Activity_Chat_Individual");
			Toast.makeText(getApplicationContext(), "Unknow error", Toast.LENGTH_LONG).show();

			Bundle Bundle_destino = new Bundle();
			Bundle_destino.putInt(getResources().getString(R.string.PAGINA), pagina);
			mIntent = new Intent(this, Activity_Principal.class);
			mIntent.putExtras(Bundle_destino);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (drawerToggle != null) {
			drawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	protected void onStop() {
		try {
			super.onStop();
			String nombre_foto = get_nombre_foto(token_socialauth_de_la_otra_persona);
			File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File file = new File(storageDir, nombre_foto);
			Boolean resultado= file.delete();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onStop  de Activity_Chat_Individual");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		//aqui se retoma el control de la app cuando el usuario nos ha dado un permiso (en marshmallow)
		switch (requestCode) {
			case PERMISSION_SELECCIONAR_FOTO:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					//Si nos da permiso, continuamos
					lanza_intent_seleccionar_foto();
				}
				break;
			case PERMISSION_CAPTURAR_FOTO:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					//Si nos da permiso, continuamos
					lanza_intent_hacer_foto();
				}
				break;

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		// ************************************************************************************************
		// ESTE METODO SERA LLAMADO CUANDO EL USUARIO ELIJA UNA FOTO.
		// TENEMOS QUE COPIARLA TANTO EN MEMORIA INTERNA COMO EN EL GOOGLE CLOUD STORAGE
		// ************************************************************************************************
		super.onActivityResult(requestCode, resultCode, data);

		try {
			if (resultCode == RESULT_OK) {
				if ((requestCode == REQUEST_LOAD_IMAGE) && (data != null)) {
					procesa_imagen_seleccionada(data);
				} else if (requestCode == REQUEST_CAPTURE_IMAGE) {
					procesa_imagen_capturada();
				}
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onActivityResult de Activity_Chat_Individual");
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
				if (binding.mDrawerLayout!=null){
					binding.mDrawerLayout.setLayoutParams(layoutParams);
				}else{
					binding.MainLinearLayout.setLayoutParams(layoutParams);
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

	private void descarga_Thumb_de_la_otra_persona(String Token_socialAuth, CircleImageView mAppCompatImageView) {
		FirebaseStorage storage = FirebaseStorage.getInstance();
		StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_thumb(Token_socialAuth, 0));

		storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
			if (getApplicationContext() != null) { //puede que ya no estemos en la activity

				Picasso.with(getApplicationContext())
						.load(uri)
						.placeholder(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))   // optional
						.error(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))       // optional
						.centerCrop()
						.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat))                        // optional
						.into(mAppCompatImageView);

				mAppCompatImageView.invalidate();
			}
		}).addOnFailureListener(e -> mAppCompatImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light))));
	}

	private void pinta_conversacion(String quien_lo_dijo, String que_dijo) {
		try {
			LayoutParams layoutParams_miniaturas;

			layoutParams_miniaturas = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat));

			layoutParams_miniaturas.weight = 0.2f;
			CircleImageView mAppCompatImageView = new CircleImageView(this);

			mAppCompatImageView.setLayoutParams(layoutParams_miniaturas);

			TextView TextView_texto = new TextView(getApplicationContext());
			TextView_texto.setText(que_dijo);
			TextView_texto.setTypeface(typeFace_roboto_Regular);
			TextView_texto.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

			String pais = utils.get_locale(this);

			Calendar cal = Calendar.getInstance(new Locale(pais));

			DateFormat sdf_fecha = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
			DateFormat sdf_hora = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM,Locale.getDefault());


			LinearLayout LinearLayout_vertical = new LinearLayout(getApplicationContext());
			LinearLayout LinearLayout_horizontal = new LinearLayout(getApplicationContext());
			LinearLayout LinearLayout_horizontal_interno = new LinearLayout(getApplicationContext());

			float SCREEN_DENSITY = getResources().getDisplayMetrics().density;

			//Dependiendo de quien lo dijo lo pondremos pegado a la izquierda o a la derecha
			if (quien_lo_dijo.equals(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""))) {
				//AppCompatImageView mAppCompatImageView_leido= new AppCompatImageView(this) ;
				//Drawable icono_leido;
				//icono_leido = leido ? new IconicsDrawable(this).icon(Icon.gmd_done_all).color(ContextCompat.getColor(this, R.color.gris_semi_transparente)).sizeDp(getResources().getInteger(R.integer.Tam_leido)) : new IconicsDrawable(this).icon(Icon.gmd_done).color(ContextCompat.getColor(this, R.color.gris_semi_transparente)).sizeDp(getResources().getInteger(R.integer.Tam_leido));
				//mAppCompatImageView_leido.setImageDrawable(icono_leido);
				LayoutParams LayoutParams_horizontal_interno = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				LayoutParams_horizontal_interno.setMargins(0, utils.Dp2Px(20, this), 0, 0);

				TextView TextView_nick_y_hora = new TextView(getApplicationContext());
				TextView_nick_y_hora.setTypeface(typeFace_roboto_Light);
				TextView_nick_y_hora.setText(getResources().getString(R.string.NICK_FECHA_HORA,perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_NICK),"") ,sdf_fecha.format(cal.getTime()),sdf_hora.format(cal.getTime())));
				TextView_texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_s_plus) /SCREEN_DENSITY);
				TextView_nick_y_hora.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_xs)  /SCREEN_DENSITY);

				TextView_nick_y_hora.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris));
				LinearLayout.LayoutParams LayoutParams_TextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				LayoutParams_TextView.setMargins(0, 0, 10, 0);
				TextView_nick_y_hora.setLayoutParams(LayoutParams_TextView);

				LayoutParams LayoutParams_horizontal = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				LayoutParams_horizontal.setMargins(0, utils.Dp2Px(8, this), 0, 0);

				LayoutParams LayoutParams_vertical = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				LayoutParams_vertical.setMargins(20, 10, 20, 10);
				LayoutParams_vertical.weight = 0.8f;

				new AsyncTask_Carga_Miniatura_mia_de_memoria().execute(mAppCompatImageView);
				TextView_texto.setGravity(Gravity.START);
				TextView_nick_y_hora.setGravity(Gravity.START);


				LinearLayout_horizontal.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_rectangle_izq));
				LayoutParams_horizontal.gravity = Gravity.START;

				LinearLayout_vertical.setOrientation(LinearLayout.VERTICAL);

				LinearLayout_horizontal.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout_horizontal.setPadding(5, 5, 5, 5);


				LinearLayout_vertical.setLayoutParams(LayoutParams_vertical);
				LinearLayout_horizontal.setLayoutParams(LayoutParams_horizontal);
				LinearLayout_horizontal_interno.setLayoutParams(LayoutParams_horizontal_interno);

				LinearLayout_horizontal_interno.addView(TextView_nick_y_hora);
				//LinearLayout_horizontal_interno.addView(mAppCompatImageView_leido);

				LinearLayout_vertical.addView(TextView_texto);
				LinearLayout_vertical.addView(LinearLayout_horizontal_interno);

				LinearLayout_horizontal.addView(LinearLayout_vertical);
				LinearLayout_horizontal.addView(mAppCompatImageView);
			} else {

				TextView TextView_nick_y_hora = new TextView(getApplicationContext());
				TextView_nick_y_hora.setTypeface(typeFace_roboto_Light);
				TextView_nick_y_hora.setText(getResources().getString(R.string.NICK_FECHA_HORA,nick_de_la_otra_persona ,sdf_fecha.format(cal.getTime()),sdf_hora.format(cal.getTime())));

				TextView_nick_y_hora.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris));

				TextView_texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_s_plus) /SCREEN_DENSITY);
				TextView_nick_y_hora.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_xs) /SCREEN_DENSITY);

				LayoutParams LayoutParams_horizontal = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				LayoutParams_horizontal.setMargins(0, utils.Dp2Px(8, this), 0, 0);

				LayoutParams LayoutParams_vertical = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				LayoutParams_vertical.setMargins(10, 10, 10, 10);
				LayoutParams_vertical.weight = 0.8f;

				descarga_Thumb_de_la_otra_persona(token_socialauth_de_la_otra_persona, mAppCompatImageView);
				TextView_texto.setGravity(Gravity.END);
				TextView_nick_y_hora.setGravity(Gravity.END);
				LinearLayout_horizontal.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_rectangle_dcha));
				LayoutParams_horizontal.gravity = Gravity.END;

				LinearLayout_vertical.setOrientation(LinearLayout.VERTICAL);

				LinearLayout_horizontal.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout_horizontal.setPadding(5, 5, 5, 5);

				LinearLayout_vertical.setLayoutParams(LayoutParams_vertical);
				LinearLayout_horizontal.setLayoutParams(LayoutParams_horizontal);

				LinearLayout_vertical.addView(TextView_texto);
				LinearLayout_vertical.addView(TextView_nick_y_hora);

				LinearLayout_horizontal.addView(mAppCompatImageView);
				LinearLayout_horizontal.addView(LinearLayout_vertical);

			}

			binding.LayoutChatLinearLayoutConversacion.addView(LinearLayout_horizontal);

			binding.LayoutChatLinearLayoutConversacion.invalidate();
			//esto es para que se vea la parte final del scrollview
			binding.LayoutChatScrollView.postDelayed(new Activity_Chat_Individual.MyRunnable(binding.LayoutChatScrollView), 100);
		} catch (RejectedExecutionException e){
			try {
				Thread.sleep(500);
			}catch (Exception ignored){}
		}catch (Exception e) {
			utils.registra_error(e.toString(), "pinta_conversacion de chat_individual");
		}
	}

	private void pinta_thumb_enviado_por_el_otro_usuario(String uri) {
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
			LayoutParams layoutParams_miniaturas;

			layoutParams_miniaturas = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat));

			layoutParams_miniaturas.weight = 0.2f;
			CircleImageView mAppCompatImageView = new CircleImageView(this);
			mAppCompatImageView.setLayoutParams(layoutParams_miniaturas);

			AppCompatImageView Thumb = new AppCompatImageView(getApplicationContext());
			Thumb.setOnClickListener(v -> {
				try {
					Intent mIntent = new Intent(getApplicationContext(), Activity_una_foto.class);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Bundle mBundle = new Bundle();
					mBundle.putString(getResources().getString(R.string.URI_DE_LA_FOTO), uri);
					mIntent.putExtras(mBundle);

					startActivity(mIntent);
				} catch (Exception e) {
					utils.registra_error(e.toString(), "pinta_thumb_enviado de Activity_Chat_Individual");
				}
			});


			if (getApplicationContext() != null) { //puede que ya no estemos en la activity
				Picasso.with(getApplicationContext())
						.load(uri)
						.placeholder(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))   // optional
						.error(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))       // optional
						.centerCrop()
						.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat))                        // optional
						.into(Thumb);

				Thumb.invalidate();
				binding.LayoutChatLinearLayoutConversacion.invalidate();
			}

			String pais = utils.get_locale(this);

			Calendar cal = Calendar.getInstance(new Locale(pais));

			DateFormat sdf_fecha = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
			DateFormat sdf_hora = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault());

			LinearLayout LinearLayout_vertical = new LinearLayout(getApplicationContext());
			LinearLayout LinearLayout_horizontal = new LinearLayout(getApplicationContext());

			float SCREEN_DENSITY = getResources().getDisplayMetrics().density;
			TextView TextView_nick_y_hora = new TextView(getApplicationContext());
			TextView_nick_y_hora.setTypeface(typeFace_roboto_Light);
			TextView_nick_y_hora.setText(getResources().getString(R.string.NICK_FECHA_HORA, nick_de_la_otra_persona, sdf_fecha.format(cal.getTime()), sdf_hora.format(cal.getTime())));

			TextView_nick_y_hora.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris));

			TextView_nick_y_hora.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_xs) / SCREEN_DENSITY);

			LayoutParams LayoutParams_horizontal = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LayoutParams_horizontal.setMargins(0, utils.Dp2Px(8, this), 0, 0);

			LayoutParams LayoutParams_vertical = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LayoutParams_vertical.setMargins(10, 10, 10, 10);
			LayoutParams_vertical.weight = 0.8f;

			descarga_Thumb_de_la_otra_persona(token_socialauth_de_la_otra_persona, mAppCompatImageView);
			TextView_nick_y_hora.setGravity(Gravity.END);
			LinearLayout_horizontal.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_rectangle_dcha));
			LayoutParams_horizontal.gravity = Gravity.END;

			LinearLayout_vertical.setOrientation(LinearLayout.VERTICAL);

			LinearLayout_horizontal.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout_horizontal.setPadding(5, 5, 5, 5);

			LinearLayout_vertical.setLayoutParams(LayoutParams_vertical);
			LinearLayout_horizontal.setLayoutParams(LayoutParams_horizontal);

			LinearLayout_vertical.addView(Thumb);
			LinearLayout_vertical.addView(TextView_nick_y_hora);

			LinearLayout_horizontal.addView(LinearLayout_vertical);
			LinearLayout_horizontal.addView(mAppCompatImageView);


			binding.LayoutChatLinearLayoutConversacion.addView(LinearLayout_horizontal);

			binding.LayoutChatLinearLayoutConversacion.invalidate();
			//esto es para que se vea la parte final del scrollview
			binding.LayoutChatScrollView.postDelayed(new Activity_Chat_Individual.MyRunnable(binding.LayoutChatScrollView), 100);

		} catch(RejectedExecutionException e){
			try {
				Thread.sleep(500);
			} catch (Exception ignored) {
			}
		}catch(Exception e){
			utils.registra_error(e.toString(), "pinta_thumb_enviado de chat_individual");
		}

	}

	private void pinta_thumb_enviado_por_mi(File file) {
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
			LayoutParams layoutParams_miniaturas;

			layoutParams_miniaturas = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat));

			layoutParams_miniaturas.weight = 0.2f;
			CircleImageView mAppCompatImageView = new CircleImageView(this);
			mAppCompatImageView.setLayoutParams(layoutParams_miniaturas);

			AppCompatImageView Thumb = new AppCompatImageView(getApplicationContext());

			Picasso.with(getApplicationContext())
					.load(file)
					.placeholder(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))   // optional
					.error(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)))       // optional
					.centerCrop()
					.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat))                        // optional
					.into(Thumb);


			String pais = utils.get_locale(this);

			Calendar cal = Calendar.getInstance(new Locale(pais));

			DateFormat sdf_fecha = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
			DateFormat sdf_hora = SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault());

			LinearLayout LinearLayout_vertical = new LinearLayout(getApplicationContext());
			LinearLayout LinearLayout_horizontal = new LinearLayout(getApplicationContext());
			LinearLayout LinearLayout_horizontal_interno = new LinearLayout(getApplicationContext());

			float SCREEN_DENSITY = getResources().getDisplayMetrics().density;

			LayoutParams LayoutParams_horizontal_interno = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LayoutParams_horizontal_interno.setMargins(0, utils.Dp2Px(8, this), 0, 0);
			TextView TextView_nick_y_hora = new TextView(getApplicationContext());
			TextView_nick_y_hora.setTypeface(typeFace_roboto_Light);
			TextView_nick_y_hora.setText(getResources().getString(R.string.NICK_FECHA_HORA, perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_NICK), ""), sdf_fecha.format(cal.getTime()), sdf_hora.format(cal.getTime())));
			TextView_nick_y_hora.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_xs) / SCREEN_DENSITY);

			TextView_nick_y_hora.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris));
			LinearLayout.LayoutParams LayoutParams_TextView = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LayoutParams_TextView.setMargins(0, 0, 10, 0);
			TextView_nick_y_hora.setLayoutParams(LayoutParams_TextView);

			LayoutParams LayoutParams_horizontal = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LayoutParams_horizontal.setMargins(0, utils.Dp2Px(8, this), 0, 0);

			LayoutParams LayoutParams_vertical = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LayoutParams_vertical.setMargins(10, 10, 10, 10);
			LayoutParams_vertical.weight = 0.8f;

			new AsyncTask_Carga_Miniatura_mia_de_memoria().execute(mAppCompatImageView);
			TextView_nick_y_hora.setGravity(Gravity.START);
			LinearLayout_horizontal.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_rectangle_izq));
			LayoutParams_horizontal.gravity = Gravity.START;

			LinearLayout_vertical.setOrientation(LinearLayout.VERTICAL);

			LinearLayout_horizontal.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout_horizontal.setPadding(5, 5, 5, 5);

			LinearLayout_vertical.setLayoutParams(LayoutParams_vertical);
			LinearLayout_horizontal.setLayoutParams(LayoutParams_horizontal);
			LinearLayout_horizontal_interno.setLayoutParams(LayoutParams_horizontal_interno);

			LinearLayout_horizontal_interno.addView(TextView_nick_y_hora);

			LinearLayout_vertical.addView(Thumb);
			LinearLayout_vertical.addView(LinearLayout_horizontal_interno);

			LinearLayout_horizontal.addView(LinearLayout_vertical);
			LinearLayout_horizontal.addView(mAppCompatImageView);

			binding.LayoutChatLinearLayoutConversacion.addView(LinearLayout_horizontal);

			binding.LayoutChatLinearLayoutConversacion.invalidate();
			//esto es para que se vea la parte final del scrollview

			binding.LayoutChatScrollView.postDelayed(new Activity_Chat_Individual.MyRunnable(binding.LayoutChatScrollView), 100);
		} catch (RejectedExecutionException e) {
			try {
				Thread.sleep(500);
			} catch (Exception ignored) {
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "pinta_thumb_enviado de chat_individual");
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
			utils.registra_error(e.toString(), "setup_toolbar de Activity_Chat_Individual");
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
			utils.registra_error(e.toString(), "setupNavigationdrawer  de Activity_Chat_Individual");
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
		recyclerViewDrawer.addOnItemTouchListener(new RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Chat_Individual.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				binding.mDrawerLayout.closeDrawers();
			}
		}));
	}

	private void salir() {
		//Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir

		finish();
	}

	private void get_datos_del_bundle(){
		Bundle bundle = getIntent().getExtras();                 //El token_socialauth de nuestro interlocutor lo recibimos como parámetro extra de nuestro intent.
		de_donde_vengo= bundle.getInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO));
		pagina= bundle.getInt(getResources().getString(R.string.PAGINA));
		token_socialauth_de_la_otra_persona = bundle.getString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), "");
		nick_de_la_otra_persona = bundle.getString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA), "");

	}

	private void lanza_intent_seleccionar_foto() {
		try {
			Intent mIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

			startActivityForResult(mIntent, REQUEST_LOAD_IMAGE);

		} catch (Exception ignored) {
		}
	}

	private void lanza_intent_hacer_foto() {
		try {
			// create Intent to take a picture and return control to the calling application
			Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (mIntent.resolveActivity(getPackageManager()) != null) {
				File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
				Calendar rightNow = Calendar.getInstance();

				nombre_foto_capturada = "captura_"+rightNow.getTimeInMillis()+".jpg";

				File file = new File(storageDir, nombre_foto_capturada);

				mIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file));
				startActivityForResult(mIntent, REQUEST_CAPTURE_IMAGE);
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_intent_hacer_foto  de chat_individual");
		}
	}

	private void lanza_dialogo_no_tienes_suficientes_estrellas() {
		try {
			new MaterialDialog.Builder(this)
					.theme(Theme.LIGHT)
					.title(String.format(getResources().getString(R.string.DIALOGO_AVISO_MENSAJE_NO_TIENES_ESTRELLAS),(-1)*getResources().getInteger(R.integer.LOGRO_COBRO_PETICION_CHAT_GENERAL)))
					.typeface("Roboto-Light.ttf", "Roboto-Regular.ttf")
					.positiveText(getResources().getString(R.string.ok))
					.neutralText(getResources().getString(R.string.DIALOGO_COMPRAR_ESTRELLAS))
					.negativeText(getResources().getString(R.string.ACTIVITY_CHAT_ESTRELLAS_GRATIS))
					.onPositive((dialog, which) -> dialog.dismiss())
					.onNeutral((dialog, which) -> {
						Intent mIntent = new Intent(getApplicationContext(), Activity_Compras.class);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(mIntent);
						finish();
					})
					.onNegative((dialog, which) -> {
						Intent mIntent = new Intent(getApplicationContext(), Activity_estrellas_gratis.class);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(mIntent);
						finish();
					})
					.show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo_no_tienes_suficientes_estrellas de chat_individual");
		}
	}

	private void lanza_dialogo_se_te_va_a_cobrar_por_seleccionar_foto() {
		try {

			new MaterialDialog.Builder(this)
					.theme(Theme.LIGHT)
					.title(String.format(getResources().getString(R.string.DIALOGO_AVISO_MENSAJE_SE_TE_VA_A_COBRAR), (-1) * getResources().getInteger(R.integer.LOGRO_COBRO_PETICION_CHAT_GENERAL)))
					.typeface("Roboto-Light.ttf", "Roboto-Regular.ttf")
					.neutralText(R.string.Cancelar)
					.positiveText(getResources().getString(R.string.ok))
					.onPositive((dialog, which) -> {
						try {
							lanza_intent_seleccionar_foto();
						} catch (Exception e) {
							utils.registra_error(e.toString(), "lanza_dialogo_se_te_va_a_cobrar_por_seleccionar_foto (material builder) de Activity_Chat_Individual");
						}
					})
					.onNeutral((dialog, which) -> dialog.dismiss())
					.show();

		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo_se_te_va_a_cobrar_por_seleccionar_foto de chat_individual");
		}
	}

	private void lanza_dialogo_se_te_va_a_cobrar_por_hacer_foto() {
		try {

			new MaterialDialog.Builder(this)
					.theme(Theme.LIGHT)
					.title(String.format(getResources().getString(R.string.DIALOGO_AVISO_MENSAJE_SE_TE_VA_A_COBRAR), (-1) * getResources().getInteger(R.integer.LOGRO_COBRO_PETICION_CHAT_GENERAL)))
					.typeface("Roboto-Light.ttf", "Roboto-Regular.ttf")
					.neutralText(R.string.Cancelar)
					.positiveText(getResources().getString(R.string.ok))
					.onPositive((dialog, which) -> {
						try {
							lanza_intent_hacer_foto();
						} catch (Exception e) {
							utils.registra_error(e.toString(), "lanza_dialogo_se_te_va_a_cobrar_por_hacer_foto (material builder) de Activity_Chat_Individual");

						}
					})
					.onNeutral((dialog, which) -> dialog.dismiss())
					.show();

		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo_se_te_va_a_cobrar_por_hacer_foto de chat_individual");
		}
	}

	private void procesa_imagen_capturada() {
		try {
			//llegados a este punto hay una foto recien hecha con el nombre correcto en el directorio
			//donde se guardan las fotos. Lo unico que queda es subirla al GCS si no es muy pesada
			File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File file_src = new File(storageDir, nombre_foto_capturada);

			if (file_src.length() < getResources().getInteger(R.integer.MAX_PHOTO_LENGTH)) {
				if (file_src.getPath() != null) {
					String texto_a_enviar=proceso_subir_foto_a_FCS(file_src);
					pinta_thumb_enviado_por_mi(file_src);
					envia_mensaje_por_FCM(texto_a_enviar);
				} else {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_MUY_PESADA), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "procesa_imagen_capturada de chat_individual");
		}
	}

	private void procesa_imagen_seleccionada(@NonNull Intent data ) {
		try {
			Uri selectedImage = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

			if (cursor != null) {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String src = cursor.getString(columnIndex);
				if (src != null) {
					File file_src = new File(src);
					cursor.close();
					if (IsSupportedFile(file_src.getName())) {
						if (src.length() < getResources().getInteger(R.integer.MAX_PHOTO_LENGTH)) {
							//Cargamos la foto a memoria interna
							if (file_src.getPath() != null) {
								String texto_a_enviar=proceso_subir_foto_a_FCS(file_src);
								pinta_thumb_enviado_por_mi(file_src);
								envia_mensaje_por_FCM(texto_a_enviar);
							} else {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
							}
						} else {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_MUY_PESADA), Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_EROR_FORMAT_NOT_SUPPORTED), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_EROR_FORMAT_NOT_SUPPORTED), Toast.LENGTH_LONG).show();
				}
			}
		}
		catch (Exception e) {
			utils.registra_error(e.toString(), "procesa_imagen_seleccionada de chat_individual");
		}
	}

	private void get_token_FCM_de_la_otra_persona(){
		DocumentReference docRef = db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_de_la_otra_persona);
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document.exists()) {
					token_FCM_de_la_otra_persona= document.getString(getResources().getString(R.string.USUARIO_TOKEN_FCM));
					get_Relacion();
				}
			}
		});

	}

	private void envia_mensaje_por_FCM(String mensaje ){
		long ahora= Calendar.getInstance().getTimeInMillis();
		FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
		Map<String, Object> data = new HashMap<>();
		data.put("title", getResources().getString(R.string.DEFAULT_NOTIFICATION_TITLE));
		data.put("body",  getResources().getString(R.string.DEFAULT_NOTIFICATION_TEXT));

		data.put("que_dijo", mensaje);
		data.put("token_FCM", token_FCM_de_la_otra_persona);
		data.put("quien_lo_dijo", token_socialauth_mio);
		data.put("nick_de_quien_lo_dijo", perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_NICK),""));
		data.put("cuando_lo_dijo",Long.valueOf(ahora).toString());

		mFunctions.getHttpsCallable("sendIndividualMessage")
				.call(data)
				.continueWith(task -> {
					// This continuation runs on either success or failure, but if the task
					// has failed then getResult() will throw an Exception which will be
					// propagated down.
					return (String) task.getResult().getData();
				});
	}

	private String proceso_subir_foto_a_FCS(File file_src){
		String texto_a_enviar="";
		try {
			new AsyncTask_sube_foto_a_FCS().execute( FileToBytes(file_src.getPath()));

			long fecha = Calendar.getInstance().getTimeInMillis();
			texto_a_enviar= getResources().getString(R.string.my_bucket) + "/" + get_path_thumb_enviada(token_socialauth_mio, Long.toString(fecha));
			guarda_mensaje_en_Firestore(texto_a_enviar);

			if (!perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				//Cobramos por subir fotos al datastore
				actualiza_estrellas_en_Firestore( token_socialauth_mio, getResources().getInteger(R.integer.LOGRO_COBRO_PETICION_CHAT_GENERAL));
				actualiza_estrellas_en_sharedpreferences(getResources().getInteger(R.integer.LOGRO_COBRO_PETICION_CHAT_GENERAL));
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "proceso_subir_foto_a_FCS de chat_individual");
		}
		return texto_a_enviar;
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
				utils.registra_error(e.toString(), "asynctask_coloca_picturemain de Activity_Chat_Individual");
			}
		}
	}

	private void set_Typefaces(){
		typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		typeFace_roboto_Regular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
	}

	private void get_Relacion(){
		db.collection(getString(R.string.USUARIOS)).document(token_socialauth_mio).collection(getString(R.string.RELACIONES))
				.whereEqualTo("token_socialauth_de_la_otra_persona",token_socialauth_de_la_otra_persona).get()
				.addOnCompleteListener(task -> {
					if (task.isSuccessful() && task.getResult().size()>0) {
						DocumentSnapshot document =task.getResult().getDocuments().get(0) ;
						id_relacion = document.getId();
						cambiar_tiene_mensajes_leidos();
						carga_conversacion();
						Prepara_chat(document.getData());
					} else{
						Prepara_chat(null);
					}
				});
	}

	private void guarda_mensaje_en_Firestore(String texto){
		Chat un_chat = new Chat(id_relacion, token_socialauth_mio, token_socialauth_de_la_otra_persona,nick_mio, texto);
		db.collection(getResources().getString(R.string.CHAT)).add(un_chat);
	}

	private void crear_relacion(){
		try {
			id_relacion = Long.toString(Calendar.getInstance().getTimeInMillis());
			Relacion mi_relacion = new Relacion(token_socialauth_de_la_otra_persona, nick_de_la_otra_persona, Integer.valueOf(getResources().getInteger(R.integer.RELACION_ACTIVA)).longValue(), false);
			Relacion la_otra_relacion = new Relacion(token_socialauth_mio, nick_mio, Integer.valueOf(getResources().getInteger(R.integer.RELACION_ACTIVA)).longValue(), true);

			Calendar hoy = Calendar.getInstance();
			String semana_del_anyo = Integer.valueOf(hoy.get(Calendar.WEEK_OF_YEAR)).toString();

			FirebaseFirestore db = FirebaseFirestore.getInstance();
			db.collection(getResources().getString(R.string.STATS_RELACIONES)).document(semana_del_anyo).get()
					.addOnCompleteListener(task -> {
						if (task.isSuccessful()) {
							DocumentSnapshot document = task.getResult();
							if (document.exists()) {
								DocumentReference Ref= db.collection(getResources().getString(R.string.STATS_RELACIONES)).document(semana_del_anyo);
								WriteBatch batch = db.batch();
								batch.update(Ref,utils.decodifica_app(utils.get_app_code(getApplicationContext().getPackageName())), FieldValue.increment(1));
								batch.update(Ref,"total", FieldValue.increment(1));
								batch.commit();
							} else {
								STATS_RELACIONES nueva_semana_relaciones= new STATS_RELACIONES(0L,0L,0L,0L,0L,0L,0L,1L);
								db.collection(getResources().getString(R.string.STATS_RELACIONES)).document(semana_del_anyo).set(nueva_semana_relaciones)
										.addOnSuccessListener(aVoid -> db.collection(getResources().getString(R.string.STATS_RELACIONES)).document(semana_del_anyo).update(utils.decodifica_app(utils.get_app_code(getApplicationContext().getPackageName())), FieldValue.increment(1)));

							}
						}
					});

			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_mio).collection(getResources().getString(R.string.RELACIONES)).document((id_relacion)).set(mi_relacion);
			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_de_la_otra_persona).collection(getResources().getString(R.string.RELACIONES)).document((id_relacion)).set(la_otra_relacion);
		}catch (Exception e){
			utils.registra_error(e.toString(), "crear_relacion");
		}
	}

	private void crea_boton_enviar_listener(){
		binding.LayoutChatImageViewEnviarMensaje.setOnClickListener(v -> {
			try{
				String texto = binding.LayoutChatEditTextTextoAEnviar.getText().toString();
				if (!texto.isEmpty()){
					if (token_FCM_de_la_otra_persona!=null){
						if (id_relacion==null) {
							crear_relacion(); // y cuando tengamos el id_relacion guardamos el mensaje
						}/*else{
							cambiar_tiene_mensajes_leidos(token_socialauth_de_la_otra_persona,true);
						}*/
						guarda_mensaje_en_Firestore(texto); //lo guardamos directamente
						pinta_conversacion(token_socialauth_mio, texto);
						envia_mensaje_por_FCM(texto);
						binding.LayoutChatEditTextTextoAEnviar.getText().clear();
					}else{
						Toast.makeText(getApplicationContext(), "Unknow error", Toast.LENGTH_LONG).show();
					}
				}
			}
			catch (Exception e)
			{
				utils.registra_error(e.toString(), "boton_enviar");
			}
		});
	}

	private void crea_boton_hacer_foto_listener(){
		binding.LayoutChatImageViewHacerFoto.setOnClickListener(v -> {
			try {
				//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
				if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
						(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
					if (Build.VERSION.SDK_INT >= 23) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAPTURAR_FOTO);
					}
				} else {
					if (perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false) ) {
						lanza_intent_hacer_foto();
					}else {
						if (perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), 0) >= 2L) {
							lanza_dialogo_se_te_va_a_cobrar_por_hacer_foto();
						} else {
							lanza_dialogo_no_tienes_suficientes_estrellas();
						}
					}
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "boton_adjuntar_foto");
			}
		});

	}

	private void crea_boton_seleccionar_foto_listener(){
		binding.LayoutChatImageViewAdjuntarFoto.setOnClickListener(v -> {
			try {
				//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
				if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
						(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
					if (Build.VERSION.SDK_INT >= 23) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_SELECCIONAR_FOTO);
					}
				} else {
					if (perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false) ) {
						lanza_intent_seleccionar_foto();
					}else {
						if (perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), 0) >= 2L) {
							lanza_dialogo_se_te_va_a_cobrar_por_seleccionar_foto();
						} else {
							lanza_dialogo_no_tienes_suficientes_estrellas();
						}
					}
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "boton_hacer_foto");
			}
		});
	}

	private void Prepara_chat(Map<String,Object> una_relacion){
		try {
			boolean permitimos_chatear;
			if (una_relacion !=null) {
				int estado_de_la_relacion = ((Long) una_relacion.get("estado_de_la_relacion")).intValue();
				permitimos_chatear = (estado_de_la_relacion == getResources().getInteger(R.integer.RELACION_ACTIVA))
						|| (estado_de_la_relacion == getResources().getInteger(R.integer.RELACION_BORRADA));
			}else{
				permitimos_chatear=true;
			}
			binding.LayoutChatImageViewEnviarMensaje.setEnabled(permitimos_chatear);
			binding.LayoutChatImageViewHacerFoto.setEnabled(permitimos_chatear);
			binding.LayoutChatImageViewAdjuntarFoto.setEnabled(permitimos_chatear);
			binding.mProgressBar.setVisibility(View.INVISIBLE);

			if (permitimos_chatear) {
				crea_boton_enviar_listener();
				crea_boton_hacer_foto_listener();
				crea_boton_seleccionar_foto_listener();
			}
			else  {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_CHAT_AMISTAD_TERMINADA), Toast.LENGTH_LONG).show();
				binding.LayoutChatImageViewEnviarMensaje.setOnClickListener(v -> Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_CHAT_AMISTAD_TERMINADA), Toast.LENGTH_LONG).show());
				binding.LayoutChatImageViewHacerFoto.setOnClickListener(v -> Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_CHAT_AMISTAD_TERMINADA), Toast.LENGTH_LONG).show());
				binding.LayoutChatImageViewAdjuntarFoto.setOnClickListener(v -> Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_CHAT_AMISTAD_TERMINADA), Toast.LENGTH_LONG).show());
			}
		} catch (Exception e) {
			//utils.registra_error("", "Exception en onBindViewHolder de RecyclerView_Principal_Adapter: " + e));
			utils.registra_error(e.toString(), "AsyncTask_getRelacion de chat_individual");
		}
	}

	private class AsyncTask_sube_foto_a_FCS extends AsyncTask<byte[], Void, Void> {

		private  byte[] BitmapToBytes(Bitmap bitmap) {

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
			} catch (Exception ignored) {
			}
			return byteArrayOutputStream.toByteArray();
		}

		@Override
		protected void onPreExecute() {

		}


		@Override
		protected final Void doInBackground(byte[]... params) {
			try {
				long fecha = Calendar.getInstance().getTimeInMillis();
				byte[] foto_en_bytes = params[0];
				int tamanyo_foto_perfiles=getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles);
				String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");

				FirebaseStorage storage = FirebaseStorage.getInstance();
				StorageReference mStorageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket));

				Bitmap bitmap_foto = BitmapFactory.decodeByteArray(foto_en_bytes, 0, foto_en_bytes.length);
				StorageReference fotoRef = mStorageRef.child(get_path_foto_enviada(token_socialauth, Long.toString(fecha)));
				UploadTask upload_foto_Task = fotoRef.putBytes(BitmapToBytes(ThumbnailUtils.extractThumbnail(bitmap_foto, 520, 520)));
				upload_foto_Task.addOnFailureListener(exception -> {
					// Handle unsuccessful uploads
				}).addOnSuccessListener(taskSnapshot -> {
				});

				Bitmap bitmap_thumb = ThumbnailUtils.extractThumbnail(bitmap_foto, tamanyo_foto_perfiles, tamanyo_foto_perfiles);
				StorageReference thumbRef = mStorageRef.child(get_path_thumb_enviada(token_socialauth, Long.toString(fecha)));
				UploadTask upload_thumb_Task = thumbRef.putBytes(BitmapToBytes(bitmap_thumb));
				upload_thumb_Task.addOnFailureListener(e -> {
				}).addOnSuccessListener(taskSnapshot -> {
				});

			} catch (Exception ignored) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void algo) {
		}

	}

	private void cambiar_tiene_mensajes_leidos(){
		db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_mio)
				.collection(getResources().getString(R.string.RELACIONES)).document(id_relacion)
				.update(getResources().getString(R.string.RELACIONES_TIENE_MENSAJES_SIN_LEER),false);
	}

	private void carga_conversacion(){
		db.collection(getResources().getString(R.string.CHAT))
				.whereEqualTo("id_relacion",id_relacion)
				.orderBy("fecha", Query.Direction.ASCENDING)
				.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				try {
					for (QueryDocumentSnapshot document : task.getResult()) {
						String que_dijo = (String) document.get("que_dijo");
						String quien_lo_dijo = (String) document.get("de_quien");
						if (!que_dijo.isEmpty()) {
							if (que_dijo.contains(getResources().getString(R.string.my_bucket) + "/fotos_usuarios/")) {
								if (quien_lo_dijo.equals(token_socialauth_mio)) {
									FirebaseStorage storage = FirebaseStorage.getInstance();
									StorageReference gsReference = storage.getReferenceFromUrl(que_dijo);
									File localFile = File.createTempFile("images", "jpg");

									gsReference.getFile(localFile).addOnSuccessListener(taskSnapshot -> pinta_thumb_enviado_por_mi(localFile));
								} else {
									pinta_thumb_enviado_por_el_otro_usuario(que_dijo);
								}
							} else {
								pinta_conversacion(quien_lo_dijo, que_dijo);
							}
						}
					}
				}catch (Exception e){
					utils.registra_error(e.toString(), "carga_conversacion");
				}
			}
		});
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
				mBitmap = file.exists() ? utils.decodeSampledBitmapFromFilePath(file.getPath(), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat), getResources().getDimensionPixelSize(R.dimen.tamanyo_miniaturas_chat)) : null;

                /*if (file.exists()) {
                    mBitmap = utils.decodeSampledBitmapFromFilePath(file.getPath(), (int) getResources().getDimension(R.dimen.tamanyo_miniaturas_chat), (int) getResources().getDimension(R.dimen.tamanyo_miniaturas_chat));
                } else {
                    Drawable drawable_no_pic =utils.get_no_pic(getApplicationContext(),ContextCompat.getColor(getApplicationContext(), R.color.primary_light));
                    mBitmap = ((BitmapDrawable) drawable_no_pic).getBitmap();
                }*/
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
					mAppCompatImageView.setScaleType(ScaleType.CENTER_CROP);
				}else{
					mAppCompatImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(),ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "AsyncTask_Carga_Miniatura_mia_de_memoria de chat_individual");
			}
		}
	}

	private void actualiza_estrellas_en_sharedpreferences(int nuevas_estrellas){
		long num_estrellas_actual=perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS),0);
		long nuevo_num_estrellas=num_estrellas_actual+nuevas_estrellas;
		if (nuevo_num_estrellas<0){nuevo_num_estrellas=0;}
		SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), nuevo_num_estrellas);
		editor_perfil_usuario.apply();
	}

	private  void actualiza_estrellas_en_Firestore(String token_socialauth_mio,Integer nuevas_estrellas) {
		try {
			long num_estrellas_actual = perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), 0);
			long nuevo_num_estrellas = num_estrellas_actual + nuevas_estrellas >= 0 ? num_estrellas_actual + nuevas_estrellas : 0;
			Long un_motivo=Integer.valueOf( getResources().getInteger(R.integer.MOTIVO_COBRO_PETICION_CHAT_GENERAL)).longValue();
			Long estrellas_ganadas= Integer.valueOf( getResources().getInteger(R.integer.LOGRO_COBRO_PETICION_CHAT_GENERAL)).longValue();
			Long fecha=Calendar.getInstance().getTimeInMillis();
			Logro un_logro= new Logro(un_motivo,estrellas_ganadas,fecha,nuevo_num_estrellas);

			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_mio).update(getResources().getString(R.string.USUARIO_ESTRELLAS), nuevo_num_estrellas);
			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_mio).collection(getResources().getString(R.string.LOGROS)).add(un_logro);
		}catch (Exception e){
			utils.registra_error(e.toString(), "actualiza_estrellas_en_Firestore de chat_individual");
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

	private class MyReceiver_new_message_chat_individual extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle mBundle = intent.getExtras();

				String quien_lo_dijo = mBundle.getString("quien_lo_dijo");

				if (!token_socialauth_mio.equals(quien_lo_dijo) && token_socialauth_de_la_otra_persona.equals(quien_lo_dijo)) {
					String que_dijo = mBundle.getString("que_dijo");
					pinta_conversacion(quien_lo_dijo, que_dijo);
				}
				abortBroadcast();
			} catch (Exception e) {
				utils.registra_error(e.toString(), "MyReceiver_new_message_chat_general de Chat_general");
			}
		}
	}
}