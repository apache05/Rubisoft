package com.rubisoft.gaycuddles.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.utils.PermissionsHelper;
import com.appyvet.rangebar.RangeBar;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.gaycuddles.Classes.Logro;
import com.rubisoft.gaycuddles.Classes.STATS_PAISES;
import com.rubisoft.gaycuddles.Classes.STATS_GLOBAL;
import com.rubisoft.gaycuddles.Classes.Usuario;
import com.rubisoft.gaycuddles.R;
import com.rubisoft.gaycuddles.tools.utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

public class Activity_Registro_Persona extends AppCompatActivity  {
	private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 2;
	private static final int MY_PERMISSIONS_REQUEST_LAST_LOCATION = 3;
	private FusedLocationProviderClient mFusedLocationClient;
	private EditText mEditText_nick;
	private Spinner mSpinner_dia_nacimiento;
	private Spinner mSpinner_mes_nacimiento;
	private Spinner mSpinner_anyo_nacimiento;
	private Spinner mSpinner_sexo;
	private Spinner mSpinner_orientacion;
	private CardView mCardView_sexualidad;

	private TextView mTextView_nick;
	private TextView mTextView_registro_birth;
	private RangeBar mRanger_altura;
	private RangeBar mRanger_peso;
	private Spinner mSpinner_raza;
	private TextView mTextView_mi_altura;
	private TextView mTextView_mi_peso;
	private TextView mTextView_sexualidad;
	private TextView mTextView_datos_fisicos;
	private TextView mTextView_terms;
	private EditText mEditText_quiero_dejar_claro;
	private ImageView mImageView_ok;
	private TextView mTextView_quiero_dejar_claro;
	private TextView mTextView_opcional;

	private TextView mTextView_altura;
	private TextView mTextView_peso;
	private TextView mTextView_raza;
	//private TextView mTextView_otros_datos;
	private CheckBox mCheckbox;
	private ProgressBar mProgressBar;
	private FirebaseFirestore db;

	private Location mLastLocation;
	private String User_country = "";
	private String User_locale = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
		super.onCreate(savedInstanceState);
		try {

			if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
				setContentView(R.layout.layout_registro_personas);

				mProgressBar = findViewById(R.id.mProgressBar);

				setup_Views();
				User_locale= utils.get_locale(this);
				mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

				setup_Typefaces();
				setup_Texts();
				setup_Spinners();
				setup_RangerBars();
				inicializa_anuncios();
				db = FirebaseFirestore.getInstance();

				mTextView_terms.setOnClickListener(v -> {
					Intent mIntent = new Intent(Activity_Registro_Persona.this, Activity_Condiciones_Uso.class);
					startActivity(mIntent);
				});
				ImageView mImageView_retroceder = findViewById(R.id.Layout_registro_personas_Button_retroceder);
				Drawable icono_retroceder;
				Configuration config = getResources().getConfiguration();
				icono_retroceder = config.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR ? new IconicsDrawable(this).icon(Icon.gmd_arrow_back).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons)) : new IconicsDrawable(this).icon(Icon.gmd_arrow_forward).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
				mImageView_retroceder.setImageDrawable(icono_retroceder);
				mImageView_retroceder.setOnClickListener(view -> {
					Intent mIntent = new Intent(Activity_Registro_Persona.this, Activity_Inicio.class);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mIntent.putExtras(getIntent().getExtras());  //le pasamos el bundle que contiene el token_socialauth
					startActivity(mIntent);
					Activity_Registro_Persona.this.finish();
				});

				mImageView_ok = findViewById(R.id.Layout_registro_personas_Button_registrarse);
				Drawable icono_seguir;
				config = getResources().getConfiguration();
				icono_seguir = config.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR ? new IconicsDrawable(this).icon(Icon.gmd_arrow_forward).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons)) : new IconicsDrawable(this).icon(Icon.gmd_arrow_back).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
				mImageView_ok.setImageDrawable(icono_seguir);
				mImageView_ok.setOnClickListener(v -> {
					if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
						if (check_nick()) {
							if (check_birth()) {
								if (check_sexuality()) {
									if (check_terms()) {
										mImageView_ok.setEnabled(false); //para que no pulsen más de una vez

										Bundle bundle = getIntent().getExtras();

										String token_socialauth = bundle.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH));

										FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( Activity_Registro_Persona.this, instanceIdResult -> {
											String nuevo_token_FCM = instanceIdResult.getToken();
											Location localizacion_inicial = get_location_inicial();
											User_country = utils.get_codigo_pais(getApplicationContext(), localizacion_inicial.getLatitude(), localizacion_inicial.getLongitude());
											int motivo = getResources().getInteger(R.integer.MOTIVO_SIGNED_UP);
											Usuario un_Perfil = recopila_info(localizacion_inicial, nuevo_token_FCM);
											Logro un_logro = new Logro( Integer.valueOf(motivo).longValue(), Integer.valueOf(getResources().getInteger(R.integer.LOGRO_SIGNED_UP)).longValue(), Calendar.getInstance().getTimeInMillis(), 0L);

											actualiza_Firestore(token_socialauth, un_Perfil, un_logro);

											inicializa_sharedpreferences_perfil(token_socialauth, un_Perfil);
											inicializa_sharedpreferences_busqueda(un_Perfil);
											inicializa_sharedpreferences_preferencias(getApplicationContext(), User_locale);

											suscribir_a_grupo();
											ir_a_primeras_fotos();
										});
									}
								}
							}
						}
					} else {
						Intent mIntent = new Intent(Activity_Registro_Persona.this, Activity_Sin_Conexion.class);
						startActivity(mIntent);
					}
				});
			} else {
				Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
				startActivity(mIntent);
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de registro_persona");
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		check_location_enabled();
	}

	@Override
	public void onResume() {
		super.onResume();
		Appodeal.onResume(this, Appodeal.BANNER_TOP);
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			//SI NO NOS DA SU PERMISO VOLVEMOS AL INICIO
			//SI NOS DA PERMISO, AHORA COMPROBAMOS QUE LOCATION ESTÁ ACTIVADO
			if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				getLastLocation();
			} else {
				Intent mIntent2 = new Intent(this, Activity_Inicio.class);
				startActivity(mIntent2);
				finish();
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent mIntent = new Intent(this, Activity_Inicio.class);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mIntent);
		finish();
	}

	private void inicializa_anuncios(){
		try{
			Appodeal.requestAndroidMPermissions( this, new PermissionsHelper.AppodealPermissionCallbacks(){
				@Override
				public void writeExternalStorageResponse(int result) { }

				@Override
				public void accessCoarseLocationResponse(int result) { }
			});

			Consent consent = ConsentManager.getInstance(this).getConsent();
			Appodeal.setTesting(false);
			Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER, consent);
			setup_banner();
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

	private void check_location_enabled() {
		try {
			LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			boolean gps_enabled;
			boolean network_enabled;

			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

			network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!gps_enabled && !network_enabled) {
				Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.LOCATION_NOT_READY), Toast.LENGTH_LONG).show();
				Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);

			}else{
				getLastLocation();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "check_location_enabled de registro_persona");

		}
			/*int LOCATION_INTERVAL_SECONDS = 30;
			int MILLISECONDS_PER_SECOND = 1000;
			int LOCATION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND * LOCATION_INTERVAL_SECONDS;

			LocationRequest mLocationRequestHighAccuracy;
			mLocationRequestHighAccuracy = new LocationRequest();
			mLocationRequestHighAccuracy.setInterval(LOCATION_INTERVAL_MILLISECONDS);
			mLocationRequestHighAccuracy.setFastestInterval(LOCATION_INTERVAL_MILLISECONDS / 10);
			mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

			LocationRequest mLocationRequestBalancedPowerAccuracy;
			mLocationRequestBalancedPowerAccuracy = new LocationRequest();
			mLocationRequestBalancedPowerAccuracy.setInterval(LOCATION_INTERVAL_MILLISECONDS);
			mLocationRequestBalancedPowerAccuracy.setFastestInterval(LOCATION_INTERVAL_MILLISECONDS / 100);
			mLocationRequestBalancedPowerAccuracy.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
			LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
					.addLocationRequest(mLocationRequestHighAccuracy)
					.addLocationRequest(mLocationRequestBalancedPowerAccuracy);

			builder.setNeedBle(true);
			Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
			result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
				@Override
				public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
					try {
						LocationSettingsResponse response = task.getResult(ApiException.class);
						// All location settings are satisfied. The client can initialize location
						// requests here.
						if (mFusedLocationClient != null) {
							getLastLocation();
						}
					} catch (ApiException exception) {
						switch (exception.getStatusCode()) {
							case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
								// Location settings are not satisfied. But could be fixed by showing the
								// user a dialog.
								try {
									// Cast to a resolvable exception.
									ResolvableApiException resolvable = (ResolvableApiException) exception;
									// Show the dialog by calling startResolutionForResult(),
									// and check the result in onActivityResult().
									resolvable.startResolutionForResult(Activity_Registro_Persona.this, REQUEST_CHECK_SETTINGS);
								} catch (IntentSender.SendIntentException | ClassCastException ignore) {
									// Ignore, should be an impossible error.
								}
								break;
							case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
								// Location settings are not satisfied. However, we have no way to fix the
								// settings so we won't show the dialog.
								Toast.makeText(Activity_Registro_Persona.this.getApplicationContext(), Activity_Registro_Persona.this.getResources().getString(R.string.LOCATION_NOT_READY), Toast.LENGTH_LONG).show();

								break;
						}
					}
				}
			});
		}catch (Exception e){
			utils.registra_error(e.toString(), "check_location_enabled de registro_persona"));

		}
	}*/
	}

	private void getLastLocation() {
		try {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
				mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> mLastLocation = location);
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LAST_LOCATION);
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "getLastLocation de Activity_Registro_persona");
		}
	}

	private Location get_location_inicial() {
		//Guardamos la posición actual
		Location mLocation = new Location("");

		try {
			if (null != mLastLocation) {
				mLocation.setLatitude(mLastLocation.getLatitude());
				mLocation.setLongitude(mLastLocation.getLongitude());
			} else {
				//si no esta el servicio de localización ponemos que está en el 0,0

				mLocation.setLatitude(0.0D);
				mLocation.setLongitude(0.0D);
			}
		} catch (SecurityException e) {
			utils.registra_error(e.toString(), "get_location_inicial");
		}


		return mLocation;
	}

	private void setup_Views() {
		mCheckbox = findViewById(R.id.Layout_registro_personas_CheckBox_I_Agree);
		mTextView_terms = findViewById(R.id.Layout_registro_personas_TextView_Terms);
		mTextView_altura = findViewById(R.id.Layout_registro_personas_TextView_Altura_registro);
		mTextView_peso = findViewById(R.id.Layout_registro_personas_TextView_Peso_registro);
		mTextView_raza = findViewById(R.id.Layout_registro_personas_TextView_mi_Raza_registro);
		mRanger_altura = findViewById(R.id.Layout_registro_personas_RangeBar_mi_altura);
		mRanger_peso = findViewById(R.id.Layout_registro_personas_RangeBar_mi_peso);
		mTextView_mi_altura = findViewById(R.id.Layout_registro_personas_TextView_mi_Altura_registro);
		mTextView_mi_peso = findViewById(R.id.Layout_registro_personas_TextView_mi_Peso_registro);
		mTextView_nick = findViewById(R.id.Layout_registro_personas_TextView_nick);
		mTextView_registro_birth = findViewById(R.id.Layout_registro_personas_TextView_registro_birth);
		mSpinner_dia_nacimiento = findViewById(R.id.Layout_registro_personas_Spinner_mi_dia_nacimiento);
		mSpinner_mes_nacimiento = findViewById(R.id.Layout_registro_personas_Spinner_mi_mes_nacimiento);
		mSpinner_anyo_nacimiento = findViewById(R.id.Layout_registro_personas_Spinner_mi_anyo_nacimiento);
		mSpinner_sexo = findViewById(R.id.Layout_registro_personas_Spinner_mi_sexo);
		mSpinner_orientacion = findViewById(R.id.Layout_registro_personas_Spinner_mi_orientacion);
		//mTextView_orientacion_cambiable = findViewById(R.id.Layout_registro_personas_TextView_orientacion_cambiable);
		mSpinner_raza = findViewById(R.id.Layout_registro_personas_Spinner_mi_Raza_registro);
		mTextView_quiero_dejar_claro = findViewById(R.id.Layout_registro_personas_TextView_quiero_dejar_claro);
		mTextView_opcional = findViewById(R.id.Layout_registro_personas_TextView_opcional);
		//mTextView_otros_datos = findViewById(R.id.Fragment_datos_personales_TextView_Quiero_dejar_claro_que);
		mTextView_sexualidad = findViewById(R.id.Layout_registro_personas_sexualidad);
		mTextView_datos_fisicos = findViewById(R.id.Layout_registro_personas_datos_fisicos);

		mEditText_nick = findViewById(R.id.Layout_registro_personas_EditText_nick);
		mEditText_quiero_dejar_claro = findViewById(R.id.Layout_registro_personas_EditText_quiero_dejar_claro);

		this.mCardView_sexualidad = findViewById(R.id.Layout_registro_personas_CardView_Sexualidad);

	}

	private void setup_RangerBars() {
		mRanger_altura.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
			if (utils.es_anglosajon(User_locale)) {
				mTextView_mi_altura.setText(utils.cm_a_feet_and_inches(Integer.valueOf(rightPinValue)));
			} else {
				mTextView_mi_altura.setText(String.format(getResources().getString(R.string.m),Float.valueOf(rightPinValue)/100));
			}
		});
		mRanger_peso.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
			if (utils.es_uk(User_locale)) {
				Pair un_par = utils.kg_a_st_and_lb(Integer.valueOf(rightPinValue));
				mTextView_mi_peso.setText(getResources().getString(R.string.st_y_lb, (double)un_par.first, (double)un_par.second));
			} else if (utils.es_usa(User_locale)) {
				mTextView_mi_peso.setText(utils.kg_a_lb(Integer.valueOf(rightPinValue)));
			} else {
				mTextView_mi_peso.setText(String.format(getResources().getString(R.string.kg), Integer.valueOf(rightPinValue)));
			}
		});

		mRanger_altura.setSeekPinByValue(160);
		mRanger_peso.setSeekPinByValue(70);
	}

	private void setup_Spinners() {
		List<String> lista_anyos = new ArrayList<>();
		ArrayAdapter adapter_anyos = new ArrayAdapter<>(this, R.layout.spinner_item, lista_anyos);
		int anyo_actual = Calendar.getInstance().get(Calendar.YEAR);
		for (Integer i = anyo_actual - 18; i > anyo_actual - 100; i--) {
			adapter_anyos.add(i);
		}
		mSpinner_anyo_nacimiento.setAdapter(adapter_anyos);
		adapter_anyos.notifyDataSetChanged();

		ArrayAdapter<CharSequence> adapter_meses = ArrayAdapter.createFromResource(this, R.array.meses_nacimiento, R.layout.spinner_item);
		mSpinner_mes_nacimiento.setAdapter(adapter_meses);
		adapter_meses.notifyDataSetChanged();

		ArrayAdapter<CharSequence> adapter_dias = ArrayAdapter.createFromResource(this, R.array.dias_nacimiento, R.layout.spinner_item);
		mSpinner_dia_nacimiento.setAdapter(adapter_dias);
		adapter_dias.notifyDataSetChanged();

		if ("com.rubisoft.lesbiancuddles".equals(getPackageName())){
			mCardView_sexualidad.setVisibility(View.GONE);
			mSpinner_sexo.setSelection(2);
			mSpinner_orientacion.setSelection(2);
		}else {
			ArrayAdapter<CharSequence> adapter_sexos = ArrayAdapter.createFromResource(this, R.array.sexos, R.layout.spinner_item);
			mSpinner_sexo.setAdapter(adapter_sexos);
			adapter_sexos.notifyDataSetChanged();

			ArrayAdapter<CharSequence> adapter_orientaciones = ArrayAdapter.createFromResource(this, R.array.orientaciones_registro, R.layout.spinner_item);
			mSpinner_orientacion.setAdapter(adapter_orientaciones);
			adapter_orientaciones.notifyDataSetChanged();
		}
		ArrayAdapter<CharSequence> adapter_razas = ArrayAdapter.createFromResource(this, R.array.razas, R.layout.spinner_item);
		mSpinner_raza.setAdapter(adapter_razas);
		adapter_razas.notifyDataSetChanged();
	}

	private void setup_Typefaces() {
		Typeface mTypeFace_roboto_light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		mTextView_registro_birth.setTypeface(mTypeFace_roboto_light);
		mTextView_mi_altura.setTypeface(mTypeFace_roboto_light);
		mTextView_mi_peso.setTypeface(mTypeFace_roboto_light);
		mTextView_altura.setTypeface(mTypeFace_roboto_light);
		mTextView_peso.setTypeface(mTypeFace_roboto_light);
		mTextView_raza.setTypeface(mTypeFace_roboto_light);
		// mTextView_orientacion_cambiable.setTypeface(mTypeFace_roboto_light);
		mTextView_nick.setTypeface(mTypeFace_roboto_light);
		//mTextView_otros_datos.setTypeface(mTypeFace_roboto_light);
		mTextView_sexualidad.setTypeface(mTypeFace_roboto_light);
		mTextView_datos_fisicos.setTypeface(mTypeFace_roboto_light);
		mCheckbox.setTypeface(mTypeFace_roboto_light);
		mTextView_terms.setTypeface(mTypeFace_roboto_light);

		mTextView_quiero_dejar_claro.setTypeface(mTypeFace_roboto_light);
		mTextView_opcional.setTypeface(mTypeFace_roboto_light);
		mEditText_quiero_dejar_claro.setTypeface(mTypeFace_roboto_light);
		mEditText_nick.setTypeface(mTypeFace_roboto_light);
	}

	private void setup_Texts() {
		// mTextView_nick.setText(getResources().getString(R.string.ACTIVITY_REGISTRO_NICK_));
		mTextView_registro_birth.setText(getResources().getString(R.string.ACTIVITY_REGISTRO_BIRTH_));
		mTextView_altura.setText(getResources().getString(R.string.ACTIVITY_REGISTRO_ALTURA_));
		mTextView_peso.setText(getResources().getString(R.string.ACTIVITY_REGISTRO_PESO_));
		mTextView_raza.setText(getResources().getString(R.string.ACTIVITY_REGISTRO_RAZA_));
		if (utils.es_anglosajon(User_locale)) {
			mTextView_mi_altura.setText(utils.cm_a_feet_and_inches(160));
		} else {
			mTextView_mi_altura.setText(String.format(getResources().getString(R.string.m),1.60));
		}
		if (utils.es_uk(User_locale)) {
			Pair un_par = utils.kg_a_st_and_lb(70);
			mTextView_mi_peso.setText(getResources().getString(R.string.st_y_lb, (double)un_par.first, (double)un_par.second));
		} else if (utils.es_usa(User_locale)) {
			mTextView_mi_peso.setText(utils.kg_a_lb(70));
		} else {
			mTextView_mi_peso.setText(String.format(getResources().getString(R.string.kg), 70));
		}
	}

	private boolean check_nick() {
		boolean es_correcto;

		String nick = mEditText_nick.getText().toString();
		if (nick != null && !nick.isEmpty()) {
			es_correcto = true;
		} else {
			Toast.makeText(this, R.string.ACTIVITY_REGISTRO_ERROR_FALTA_NICK, Toast.LENGTH_LONG).show();
			es_correcto = false;
		}
		return es_correcto;
	}

	private boolean check_birth() {
		boolean es_correcto;
		int anyo_nacimiento = decodifica_anyo_nacimiento(mSpinner_anyo_nacimiento.getSelectedItemPosition()).intValue();
		int mes_nacimiento = mSpinner_mes_nacimiento.getSelectedItemPosition() + 1;
		int dia_nacimiento = mSpinner_dia_nacimiento.getSelectedItemPosition() + 1;

		if (es_mayor_de_18(anyo_nacimiento, mes_nacimiento, dia_nacimiento)) {
			es_correcto = true;
		} else {
			Toast.makeText(this, String.format(getResources().getString(R.string.ACTIVITY_REGISTRO_ERROR_UNDERAGE),18), Toast.LENGTH_LONG).show();
			es_correcto = false;
		}
		return es_correcto;
	}

	private boolean check_sexuality() {
		boolean congruente = false;
		boolean es_correcto = false;
		int sexo = mSpinner_sexo.getSelectedItemPosition();
		int orientacion = mSpinner_orientacion.getSelectedItemPosition();

		if (orientacion != 0 && sexo != 0) {
			es_correcto = true;
			if (!(sexo == getResources().getInteger(R.integer.HOMBRE) && orientacion == getResources().getInteger(R.integer.LESBIANA)) &&
					!(sexo == getResources().getInteger(R.integer.MUJER) && orientacion == getResources().getInteger(R.integer.GAY))) {
				congruente = true;
			} else {
				Toast.makeText(this, R.string.ACTIVITY_REGISTRO_ERROR_INCONGRUENCIA_SEXUAL, Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, R.string.ACTIVITY_REGISTRO_ERROR_FALTA_SEXUALIDAD, Toast.LENGTH_LONG).show();
		}
		return congruente && es_correcto;
	}

	private boolean check_terms() {
		boolean resultado;
		if (mCheckbox.isChecked()) {
			resultado = true;
		} else {
			resultado = false;
			Toast.makeText(this, R.string.ACTIVITY_REGISTRO_ERROR_TERMS, Toast.LENGTH_LONG).show();
		}
		return resultado;
	}

	private void inicializa_sharedpreferences_busqueda(@NonNull Usuario un_usuario) {

		SharedPreferences busqueda_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_BUSQUEDAS_USUARIO), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor_busqueda_usuario = busqueda_usuario.edit();
		if ((un_usuario.getSexo()== getResources().getInteger(R.integer.HOMBRE)) &&(un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.HETERO))) {
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.HETERO));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),1);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.MUJER)) &&(un_usuario.getOrientacion().intValue()==getResources().getInteger(R.integer.HETERO))){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA),  getResources().getInteger(R.integer.HOMBRE));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.HETERO));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),0);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()==getResources().getInteger(R.integer.HOMBRE)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.GAY)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.HOMBRE));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.GAY));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),2);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.MUJER)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.LESBIANA)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.LESBIANA));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),3);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.MUJER)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.BISEX)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.HOMBRE));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.BISEX));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),4);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}else if ((un_usuario.getSexo()== getResources().getInteger(R.integer.HOMBRE)) && un_usuario.getOrientacion().intValue()== getResources().getInteger(R.integer.BISEX)){
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),  getResources().getInteger(R.integer.BISEX));
			editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),5);//POSICION QUE OCUPA EN EL ARRAY DE BUSQUEDA ESTA ELECCION
		}


		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO));

		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));

		Set default_razas = new HashSet();
		for (int i = 0; i < getResources().getStringArray(R.array.razas).length; i++) {
			default_razas.add(i);
		}
		//por defecto ponemos todas las razas activadas
		editor_busqueda_usuario.putStringSet(getResources().getString(R.string.BUSQUEDA_PERSONAS_RAZA), default_razas);

		int radio_de_busqueda;
		radio_de_busqueda =getResources().getInteger(R.integer.DEFAULT_RADIO) / 2;
		editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda);
		//editor_busqueda_usuario.putBoolean(getResources().getString(R.string.BUSQUEDA_QUE_SEA_PROFESIONAL), false);
		editor_busqueda_usuario.putBoolean(getResources().getString(R.string.BUSQUEDA_QUE_ESTE_ONLINE), false);

		editor_busqueda_usuario.apply();
	}

	private void inicializa_sharedpreferences_perfil(String token_socialauth,@NonNull Usuario un_usuario) {
		try{
		Calendar mCalendar= Calendar.getInstance();
		SharedPreferences perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

		SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();

		editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_NICK), un_usuario.getNick());
		editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), token_socialauth);
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_FECHA_NACIMIENTO), un_usuario.getFecha_nacimiento());
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_SEXO), un_usuario.getSexo());
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION), un_usuario.getOrientacion());
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_PESO), un_usuario.getPeso());
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ALTURA), un_usuario.getAltura());
		editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_QUIERO_DEJAR_CLARO), un_usuario.getQuiero_dejar_claro_que());

		editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_PAIS), User_country);

		editor_perfil_usuario.putFloat(getResources().getString(R.string.PERFIL_USUARIO_LATITUD), un_usuario.getLatitud().floatValue());
		editor_perfil_usuario.putFloat(getResources().getString(R.string.PERFIL_USUARIO_LONGITUD), un_usuario.getLongitud().floatValue());
		editor_perfil_usuario.putBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false);
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_RAZA), un_usuario.getRaza());
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), un_usuario.getEstrellas());
		editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ULTIMA_ESTRELLA_DIARIA_GRATIS),mCalendar.getTimeInMillis());

		editor_perfil_usuario.apply();


		} catch (Exception e) {
			utils.registra_error(e.toString(), "inicializa_sharedpreferences_perfil de registro_persona");
		}
	}

	private void inicializa_sharedpreferences_preferencias(Context mContext, @NonNull String pais) {
		SharedPreferences preferencias_usuario = mContext.getSharedPreferences(mContext.getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);

		SharedPreferences.Editor editor_preferencias_usuario = preferencias_usuario.edit();
		if (pais.contentEquals("US")) {
			editor_preferencias_usuario.putLong(mContext.getResources().getString(R.string.PREFERENCIAS_UNIDADES), mContext.getResources().getInteger(R.integer.AMERICANO));
		} else if (pais.contentEquals("EN")) {
			editor_preferencias_usuario.putLong(mContext.getResources().getString(R.string.PREFERENCIAS_UNIDADES), mContext.getResources().getInteger(R.integer.BRITANICO));
		} else {
			editor_preferencias_usuario.putLong(mContext.getResources().getString(R.string.PREFERENCIAS_UNIDADES), mContext.getResources().getInteger(R.integer.METRICO));
		}
		//editor_preferencias_usuario.putBoolean(mContext.getResources().getString(R.string.AYUDA_FRAGMENT_AMIGOS), true);
		editor_preferencias_usuario.putBoolean(mContext.getResources().getString(R.string.AYUDA_FRAGMENT_FOTO_MANAGER), true);
		editor_preferencias_usuario.putBoolean(mContext.getResources().getString(R.string.AYUDA_ACTIVITY_PRINCIPAL), true);
		//editor_preferencias_usuario.putBoolean(mContext.getResources().getString(R.string.AYUDA_ACTIVITY_CHAT_GENERAL), true);


		editor_preferencias_usuario.putBoolean(mContext.getResources().getString(R.string.PREFERENCIAS_ENABLE_CHAT), true);

		editor_preferencias_usuario.apply();
	}

	private boolean es_mayor_de_18(int anyos, int meses, int dias) {
		Calendar fecha_nacimiento = Calendar.getInstance();
		fecha_nacimiento.set(anyos, meses - 1, dias);
		Calendar hoy = Calendar.getInstance();

		Calendar fecha_limite = Calendar.getInstance();
		fecha_limite.set(hoy.get(Calendar.YEAR) - 18, hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));

		return fecha_limite.getTimeInMillis() >= fecha_nacimiento.getTimeInMillis();
	}

	private Usuario recopila_info(Location localizacion_inicial,String nuevo_token_FCM) {
		Usuario un_usuario =null;

		try {
			String nick = mEditText_nick.getText().toString();
			int anyo_nacimiento = decodifica_anyo_nacimiento(mSpinner_anyo_nacimiento.getSelectedItemPosition()).intValue();
			int mes_nacimiento = mSpinner_mes_nacimiento.getSelectedItemPosition() + 1;
			int dia_nacimiento = mSpinner_dia_nacimiento.getSelectedItemPosition() + 1;
			long app=utils.get_app_code(getApplicationContext().getPackageName());
			long sexo = mSpinner_sexo.getSelectedItemPosition();
			long orientacion = mSpinner_orientacion.getSelectedItemPosition();
			long raza = mSpinner_raza.getSelectedItemPosition();
			long altura = Long.valueOf(mRanger_altura.getRightPinValue());
			long peso = Long.valueOf(mRanger_peso.getRightPinValue());
			String quiero_dejar_claro = mEditText_quiero_dejar_claro.getText().toString();

			Calendar fecha_nacimiento = Calendar.getInstance();
			Calendar hoy = Calendar.getInstance();
			long dentro_de_una_semana = Calendar.getInstance().getTimeInMillis() + (7*24*60*60*1000);

			fecha_nacimiento.set(anyo_nacimiento, mes_nacimiento - 1, dia_nacimiento);


			un_usuario= new Usuario(
					altura,
					app,
					(long)getResources().getInteger(R.integer.LOGRO_SIGNED_UP),
					dentro_de_una_semana,
					fecha_nacimiento.getTimeInMillis(),
					hoy.getTimeInMillis(),
					localizacion_inicial.getLongitude(),
					localizacion_inicial.getLatitude(),
					nick,
					orientacion,
					User_country,
					peso,
					quiero_dejar_claro,
					raza,
					sexo,
					nuevo_token_FCM);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "recopila_info de registro_persona");
		}
		return un_usuario;
	}

	private Long decodifica_anyo_nacimiento(Integer posicion_anyo) {
		DateTime now = DateTime.now();
		int anyo_real = now.getYear() - 18 - posicion_anyo;

		return (long) anyo_real;
	}

	private void actualiza_Firestore(String Token_socialauth,Usuario un_usuario, Logro un_logro) {
		try {
			Calendar hoy = Calendar.getInstance();

			db.collection(getResources().getString(R.string.STATS_PAISES)).document(un_usuario.getPais()).get()
					.addOnCompleteListener(task -> {
						if (task.isSuccessful() && task.getResult().exists()) {
							WriteBatch batch = db.batch();
							DocumentReference ref= db.collection(getResources().getString(R.string.STATS_PAISES)).document(un_usuario.getPais());
							batch.update(ref, "total_usuarios", FieldValue.increment(1));
							batch.update(ref, utils.decodifica_app(utils.get_app_code(getApplicationContext().getPackageName())), FieldValue.increment(1));
							batch.update(ref, utils.decodifica_sexo(un_usuario.getSexo()) + "_" + utils.decodifica_orientacion(un_usuario.getOrientacion()), FieldValue.increment(1));
							batch.commit();
						}else{
							STATS_PAISES stats_paises= new STATS_PAISES(0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,1L);
							db.collection(getResources().getString(R.string.STATS_PAISES)).document(un_usuario.getPais()).set(stats_paises)
									.addOnSuccessListener(aVoid -> {
										try {
											db.collection(getResources().getString(R.string.STATS_PAISES)).document(un_usuario.getPais()).update(utils.decodifica_app(utils.get_app_code(getApplicationContext().getPackageName())), 1);
											db.collection(getResources().getString(R.string.STATS_PAISES)).document(un_usuario.getPais()).update(utils.decodifica_sexo(un_usuario.getSexo()) + "_" + utils.decodifica_orientacion(un_usuario.getOrientacion()),1);
										}catch (Exception e){
											utils.registra_error(e.toString(), "actualiza_Firestore (stats_paises) de Registro_persona ");
										}
									});
						}
					});
			DocumentReference ref;
			ref= db.collection(getResources().getString(R.string.STATS_GLOBAL)).document(getResources().getString(R.string.STATS_GLOBAL_POR_APPS));
			WriteBatch batch_STATS_GLOBAL_POR_APPS = db.batch();
			batch_STATS_GLOBAL_POR_APPS.update(ref, "total_usuarios", FieldValue.increment(1));
			batch_STATS_GLOBAL_POR_APPS.update(ref, utils.decodifica_app(utils.get_app_code(getApplicationContext().getPackageName())), FieldValue.increment(1));
			batch_STATS_GLOBAL_POR_APPS.commit();

			ref= db.collection(getResources().getString(R.string.STATS_GLOBAL)).document(getResources().getString(R.string.STATS_GLOBAL_POR_PERFILES));
			WriteBatch batch_STATS_GLOBAL_POR_PERFILES = db.batch();
			batch_STATS_GLOBAL_POR_PERFILES.update(ref, "total_usuarios", FieldValue.increment(1));
			batch_STATS_GLOBAL_POR_PERFILES.update(ref, utils.decodifica_sexo(un_usuario.getSexo()) + "_" + utils.decodifica_orientacion(un_usuario.getOrientacion()), FieldValue.increment(1));
			batch_STATS_GLOBAL_POR_PERFILES.commit();

			db.collection(getResources().getString(R.string.USUARIOS)).document(Token_socialauth).set(un_usuario);
			db.collection(getResources().getString(R.string.USUARIOS)).document(Token_socialauth).collection(getResources().getString(R.string.LOGROS)).add(un_logro);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "actualiza_Firestore de Registro_persona ");
		}
	}

	private void suscribir_a_grupo(){
		SharedPreferences perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
		String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
		FirebaseMessaging.getInstance().subscribeToTopic(grupo_al_que_pertenece);
	}

	private void ir_a_primeras_fotos(){
	utils.setProgressBar_visibility(mProgressBar, View.INVISIBLE);

	Intent mIntent = new Intent(this, Activity_Primeras_fotos.class);
	mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(mIntent);
	this.finish();
}
}