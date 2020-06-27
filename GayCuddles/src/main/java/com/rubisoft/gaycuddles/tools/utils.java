package com.rubisoft.gaycuddles.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.rubisoft.gaycuddles.BuildConfig;
import com.rubisoft.gaycuddles.Classes.Error;
import com.rubisoft.gaycuddles.Classes.Logro;
import com.rubisoft.gaycuddles.Classes.Usuario_para_listar;
import com.rubisoft.gaycuddles.R;
import com.rubisoft.gaycuddles.activities.Activity_Ayuda;
import com.rubisoft.gaycuddles.activities.Activity_Chat_General;
import com.rubisoft.gaycuddles.activities.Activity_Compras;
import com.rubisoft.gaycuddles.activities.Activity_Condiciones_Uso;
import com.rubisoft.gaycuddles.activities.Activity_Configura_Radar;
import com.rubisoft.gaycuddles.activities.Activity_Configuracion;
import com.rubisoft.gaycuddles.activities.Activity_Feedback;
import com.rubisoft.gaycuddles.activities.Activity_Inicio;
import com.rubisoft.gaycuddles.activities.Activity_Mensajes;
import com.rubisoft.gaycuddles.activities.Activity_Mi_Perfil;
import com.rubisoft.gaycuddles.activities.Activity_Politica_Privacidad;
import com.rubisoft.gaycuddles.activities.Activity_Principal;
import com.rubisoft.gaycuddles.activities.Activity_estrellas_gratis;

import org.joda.time.LocalDate;
import org.joda.time.Years;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class utils {
	private static final double earthRadius = 6372.8;//kilometers

	public static int Dp2Px(int dp, Activity activity) {
		// Get the screen's density scale
		float scale = activity.getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) ((dp * scale) + 0.5f);
	}

	public static int getEdad(Long milliseconds) {
		Calendar fecha_nacimiento = Calendar.getInstance();
		fecha_nacimiento.setTimeInMillis(milliseconds);


		LocalDate birthdate = new LocalDate(fecha_nacimiento.get(Calendar.YEAR), fecha_nacimiento.get(Calendar.MONTH) + 1, fecha_nacimiento.get(Calendar.DAY_OF_MONTH));
		LocalDate now = new LocalDate();
		return Years.yearsBetween(birthdate, now).getYears();
	}

	public static boolean isNetworkAvailable(ConnectivityManager mConnectivityManager) {
		NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		return (activeNetworkInfo != null) && activeNetworkInfo.isConnected();
	}

	/********************** Denben coincidir con los que tenemos en el google play developer console **********************/
	public static String get_SKU_compra_5_estrellas() {
		return "comprar_5_estrellas";
	}

	public static String get_SKU_compra_10_estrellas() {return "comprar_10_estrellas";}

	public static String get_SKU_compra_20_estrellas() {
		return "comprar_20_estrellas";
	}

	public static String get_SKU_compra_40_estrellas() {
		return "comprar_40_estrellas";
	}

	public static String get_SKU_suscribe_1_anyo_premium() {
		return "suscribir_12_meses";
	}

	public static String get_SKU_suscribe_medio_anyo_premium() {
		return "suscribir_6_meses";
	}

	public static Boolean es_anglosajon(String pais) {
		boolean resultado = false;
		if (pais.toLowerCase().contentEquals("us") || pais.toLowerCase().contentEquals("en")) {
			resultado = true;
		}
		return resultado;
	}

	public static Boolean es_uk(String pais) {
		boolean resultado = false;
		if (pais.toLowerCase().contentEquals("en")) {
			resultado = true;
		}
		return resultado;
	}

	public static Boolean es_usa(String pais) {
		boolean resultado = false;
		if (pais.toLowerCase().contentEquals("us")) {
			resultado = true;
		}
		return resultado;
	}

	public static String km_a_mi(float km) {
		String result = "";
		try {
			DecimalFormat formatter = new DecimalFormat("#.##");
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator('.');
			formatter.setDecimalFormatSymbols(dfs);

			result = formatter.format(km * 0.621371);
		} catch (Exception e) {
			registra_error(e.toString(), "km_a_mi");
		}
		return result;

	}

	public static String cm_a_feet_and_inches(int cm) {
		String result = "";
		try {
			DecimalFormat formatter = new DecimalFormat("#");
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator('.');
			formatter.setDecimalFormatSymbols(dfs);

			double feetPart = (int) Math.floor(cm / 2.54 / 12);
			double inchesPart = (int) Math.floor((cm / 2.54) - (feetPart * 12));
			result = formatter.format(feetPart) + '\'' + formatter.format(inchesPart) + "''";

		} catch (Exception e) {
			registra_error(e.toString(), "cm_a_feet_and_inches");
		} return result;
	}

	public static String kg_a_lb(int kg) {
		String result = "";
		try {
			DecimalFormat formatter = new DecimalFormat("#.##");
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator('.');
			formatter.setDecimalFormatSymbols(dfs);
			double lb = kg * 2.20462;
			result = formatter.format(lb) + " lb";
		} catch (Exception e) {
			registra_error(e.toString(), "kg_a_lb");
		} return result;
	}

	public static Pair kg_a_st_and_lb(int kg) {
		int int_stones = 0;
		double pounds = 0;
		try {
			DecimalFormat formatter = new DecimalFormat("#");
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator('.');
			formatter.setDecimalFormatSymbols(dfs);
			float stones = kg * 0.157473f;
			int_stones = (int) stones;
			pounds = (stones - int_stones) / 0.0714286f;
			//result = formatter.format(int_stones) + " st " + formatter.format(pounds) + " lb";
		} catch (Exception e) {
			registra_error(e.toString(), "kg_a_st_and_lb");
		}
		return new Pair(int_stones, pounds);

	}

	private static String cm_a_m(int cm) {
		String result = "";
		try {
			DecimalFormat formatter = new DecimalFormat("#.##");
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setDecimalSeparator('.');
			formatter.setDecimalFormatSymbols(dfs);

			result = formatter.format(cm / 100.0F) + " m";
		} catch (Exception e) {
			registra_error(e.toString(), "cm_a_m");
		} return result;
	}

	public static String get_nombre_thumb(String token_socialauth, int num_foto) {
		return token_socialauth + "_thumb" + num_foto + ".jpg";
	}

	public static String get_path_foto(String token_socialauth, int num_foto) {
		return "fotos_usuarios/" + token_socialauth + '/' + token_socialauth + "_foto" + num_foto + ".jpg";
	}

	public static String get_path_thumb(String token_socialauth, int num_foto) {
		return "fotos_usuarios/" + token_socialauth + '/' + token_socialauth + "_thumb" + num_foto + ".jpg";
	}

	public static void gestiona_onclick_menu_principal(Activity contexto, int position) {
		try {
			switch (position) {
				case 0:
					//inhabilitamos el boton del menu para que sólo se utilice el botón flotante de reload
					if (!(contexto.getPackageName()+".Activities.Activity_Principal").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent0 = new Intent(contexto, Activity_Principal.class);

						mIntent0.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						contexto.startActivity(mIntent0);
						contexto.finish();
						return;
					}
					break;
				case 1:
					if (!(contexto.getPackageName()+".Activities.Activity_Configura_Radar").equals(contexto.getComponentName().getClassName())) {

						Intent mIntent1 = new Intent(contexto, Activity_Configura_Radar.class);

						mIntent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent1);
						contexto.finish();
						return;
					}
					break;
				case 2:
					if (!(contexto.getPackageName()+".Activities.Activity_Mensajes").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent2 = new Intent(contexto, Activity_Mensajes.class);

						mIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent2);
						contexto.finish();
						return;
					}
					break;
				case 3:
					if (!(contexto.getPackageName()+".Activities.Activity_Mi_Perfil").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent3 = new Intent(contexto, Activity_Mi_Perfil.class);

						mIntent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent3);
						contexto.finish();
						return;
					}
					break;
				case 4:
					if (!(contexto.getPackageName()+".Activities.Activity_estrellas_gratis").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent4 = new Intent(contexto, Activity_estrellas_gratis.class);

						mIntent4.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent4);
						contexto.finish();
						return;
					}
					break;
				case 5:
					if (!(contexto.getPackageName()+".Activities.Activity_Compras").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent5 = new Intent(contexto, Activity_Compras.class);

						mIntent5.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent5);
						contexto.finish();
						return;
					}
					break;
				case 6:
					if (!(contexto.getPackageName()+".Activities.Activity_Chat_General").equals(contexto.getComponentName().getClassName())) {
						// Code to be executed when an ad request fails.
						Intent mIntent6 = new Intent(contexto, Activity_Chat_General.class);

						mIntent6.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent6);
						contexto.finish();
						return;
					}
					break;
                /*case 7:
                    if (!(contexto.getPackageName()+".Activities.Activity_Contest").equals(contexto.getComponentName().getClassName())) {
                        Intent mIntent7 = new Intent(contexto, Activity_Contest.class);

                        mIntent7.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        contexto.startActivity(mIntent7);
                        contexto.finish();
                        return;
                    }
                    break;*/
				case 7:
					if (!(contexto.getPackageName()+".Activities.Activity_Ayuda").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent8 = new Intent(contexto, Activity_Ayuda.class);

						mIntent8.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent8);
						contexto.finish();
						return;
					}


					break;
				case 8:
					if (!(contexto.getPackageName()+".Activities.Activity_Configuracion").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent9 = new Intent(contexto, Activity_Configuracion.class);

						mIntent9.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent9);
						contexto.finish();
						return;
					}
					break;

				case 9:
					if (!(contexto.getPackageName()+".Activities.Activity_feedback").equals(contexto.getComponentName().getClassName())) {
						Intent mIntent10 = new Intent(contexto, Activity_Feedback.class);

						mIntent10.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						contexto.startActivity(mIntent10);
						contexto.finish();
						return;
					}
					break;
				case 10:
					//para discernir si la sesion esta abierta o cerrada miramos si hay información en perfil_usuario
					//por eso lo borramos  al salir
					FirebaseAuth.getInstance().signOut();

					SharedPreferences perfil_usuario_6 = contexto.getSharedPreferences(contexto.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
					SharedPreferences chat_general = contexto.getSharedPreferences(contexto.getString(R.string.SHAREDPREFERENCES_PRINCIPAL), Context.MODE_PRIVATE);

					Editor editor_perfiles = perfil_usuario_6.edit();
					editor_perfiles.clear();
					editor_perfiles.apply();

					Editor editor_chat_general = chat_general.edit();
					editor_chat_general.clear();
					editor_chat_general.apply();

					Intent mIntent = new Intent(contexto, Activity_Inicio.class);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					contexto.startActivity(mIntent);
					contexto.finish();
			}
		} catch (Exception e) {

			registra_error(e.toString(), "gestiona_onclick_menu_principal");
		}
	}

	public static void gestiona_menu(int id, Activity contexto) {
		Intent mIntent;

		switch (id) {
			case R.id.menu_options_condiciones_de_uso:
				mIntent = new Intent(contexto, Activity_Condiciones_Uso.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				contexto.startActivity(mIntent);
				contexto.finish();
				break;
			case R.id.menu_options_politica_privacidad:
				mIntent = new Intent(contexto, Activity_Politica_Privacidad.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				contexto.startActivity(mIntent);
				contexto.finish();
				break;
           /* case R.id.menu_options_sobre_xradar:
                mIntent = new Intent(contexto, Activity_Ayuda.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                contexto.startActivity(mIntent);
                contexto.finish();
                break;*/
		}
	}

	public static void setProgressBar_visibility(ProgressBar mProgressBar, int una_visibility) {
		//cambia la visibilidad de la rueda de espera
		mProgressBar.setVisibility(una_visibility);
		mProgressBar.invalidate();
	}

	public static void set_RecyclerView_Animator(RecyclerView mRecyclerView) {
		DefaultItemAnimator mAnimator = new DefaultItemAnimator();
		mAnimator.setAddDuration(800);
		mAnimator.setRemoveDuration(500);
		mAnimator.setChangeDuration(500);
		mRecyclerView.setItemAnimator(mAnimator);
	}

	public static boolean guarda_foto_en_memoria_interna(byte[] foto_en_bytes, String file_dest) {
		// ************************************************************************************************
		// Esta función baja una foto descargada del GCS como String a memoria interna de la app
		// ************************************************************************************************
		try {
			FileOutputStream mFileOutputStream = new FileOutputStream(new File(file_dest));
			ByteArrayInputStream mByteArrayInputStream = new ByteArrayInputStream(foto_en_bytes);

			byte[] buffer = new byte[1024 * 4];
			int bytes_leidos;
			while ((bytes_leidos = mByteArrayInputStream.read(buffer, 0, buffer.length)) != -1) {
				mFileOutputStream.write(buffer, 0, bytes_leidos);
			}

			mFileOutputStream.close();
			mByteArrayInputStream.close();
			return true;

		}catch (IOException e) {
			return false;
		}catch (Exception e) {
			registra_error(e.toString(), "guarda_foto_en_memoria_interna");
			return false;

		}
	}

	public static Bitmap decodeSampledBitmapFromFilePath(String file_path, int reqWidth, int reqHeight) {
		try {
			//ESTA FUNCION DEVUELVE UN BITMAP CON LAS DIMENSIONES AJUSTADAS AL IMAGEVIEW DONDE VA A SER MOSTRADO

			// First decode with inJustDecodeBounds=true to check dimensions
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file_path, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(file_path, options);
		}catch (Exception e){
			registra_error(e.toString(), "decodeSampledBitmapFromFilePath");

			//new AsyncTask_registra_error().execute(new Pair<>(e.toString(), "decodeSampledBitmapFromFilePath"));
			return null;
		}
	}

	public static Bitmap decodeSampledBitmapFromBytes(byte[] bytes, int reqWidth, int reqHeight) {
		// First decode with inJustDecodeBounds=true to check dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			//ESTA FUNCION DEVUELVE UN BITMAP CON LAS DIMENSIONES AJUSTADAS AL IMAGEVIEW DONDE VA A SER MOSTRADO


			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
		} catch (Exception e) {
			registra_error(e.toString(), "decodeSampledBitmapFromBytes");
		}
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		//ESTA FUNCION CALCULA LAS MEDIDAS MÍNIMAS QUE DEBERÍA TENER EL BITMAP QUE VA A SER CARGADO
		//PARA AJUSTARSE A LAS MEDIDAS DEL IMAGEVIEW DONDE VA A SER MOSTRADO Y ASÍ AHORRAR MEMORIA

		// Raw height and width of image
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;

		if ((height > reqHeight) || (width > reqWidth)) {

			int halfHeight = height / 2;
			int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while (((halfHeight / inSampleSize) > reqHeight)
					&& ((halfWidth / inSampleSize) > reqWidth)) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	public static int get_num_columns_grids(Context mContext) {
		//dependiendo del tamanyo de la pantalla y si esta en landscape o portrait mostraremos un recyclerview_gridlayout con mas o menso columnas
		int num_columns;
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

		//si no es tableta o si es tableta pero esta en portrait mode (no tiene el navigation drawer fijo)
		if (!isTablet(mContext) || (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)) {
			num_columns = metrics.widthPixels / ((int) mContext.getResources().getDimension(R.dimen.tamanyo_foto_grid_perfiles) + 15);
		} else {
			//hacemos lo mismo pero le restamos lo que mide el drawer
			int drawer_width = (int) mContext.getResources().getDimension(R.dimen.drawer_width);
			num_columns = (metrics.widthPixels - drawer_width) / ((int) mContext.getResources().getDimension(R.dimen.tamanyo_foto_grid_perfiles) + 15);
		}
		return num_columns;
	}

	public static String decodifica_sexo(Long sexo_codificado){
		String sexo_decodificado="";
		switch (sexo_codificado.intValue()) {
			case 1:
				sexo_decodificado = "hombre";
				break;
			case 2:
				sexo_decodificado = "mujer";
				break;
			default:
				sexo_decodificado=sexo_decodificado.toString();
		}
		return sexo_decodificado;
	}
	public static String decodifica_orientacion(Long orientacion_codificada){
		String orientacion_decodificada="";
		switch (orientacion_codificada.intValue()) {
			case 1:
				orientacion_decodificada = "hetero";
				break;
			case 2:
				orientacion_decodificada = "lesbiana";
				break;
			case 3:
				orientacion_decodificada = "gay";
				break;
			case 4:
				orientacion_decodificada = "bi";
				break;
			default:
				orientacion_decodificada=orientacion_codificada.toString();
		}
		return orientacion_decodificada;
	}
	public static String decodifica_app(Long app_codificada){
		String app_decodificada="";
		switch (app_codificada.intValue()) {
			case 1:
				app_decodificada = "womenfinder";
				break;
			case 2:
				app_decodificada = "menfinder";
				break;
			case 3:
				app_decodificada = "gayfinder";
				break;
			case 4:
				app_decodificada = "lesbianfinder";
				break;
			case 5:
				app_decodificada = "bisexfinder";
				break;
			case 6:
				app_decodificada = "womenradar";
				break;
			case 7:
				app_decodificada = "gayradar";
				break;
			case 8:
				app_decodificada = "menradar";
				break;
			case 9:
				app_decodificada = "lesbianradar";
				break;
			case 10:
				app_decodificada = "bisexradar";
				break;
		}
		return app_decodificada;
	}
	public static String decodifica_raza(Long raza_codificada){
		String raza_decodificada="";
		switch (raza_codificada.intValue()) {
			case 0:
				raza_decodificada = "White (Blonde)";
				break;
			case 1:
				raza_decodificada = "White (Dark)";
				break;
			case 2:
				raza_decodificada = "Black";
				break;
			case 3:
				raza_decodificada = "Mulate";
				break;
			case 4:
				raza_decodificada = "Oriental (East)";
				break;
			case 5:
				raza_decodificada = "Oriental (Southeast)";
				break;
			case 6:
				raza_decodificada = "Latin";
				break;
			case 7:
				raza_decodificada = "Arab";
				break;
			case 8:
				raza_decodificada = "Hindu";
				break;
		}
		return raza_decodificada;
	}
	public static String decodifica_causa_denuncia(int int_causa){
		String String_causa="";
		switch (int_causa){
			case 0:
				String_causa="Foto obscena";
				break;
			case 1:
				String_causa="Perfil falso";
				break;
			case 2:
				String_causa="Foto violenta";
				break;
			case 3:
				String_causa="Foto inapropiada u ofensiva";
				break;
			case 4:
				String_causa="Protección de la infancia";
				break;

		}
		return String_causa;
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				>= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static String get_codigo_pais(Context context,double lat, double lng) {
		String User_country = "us";
		try {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String simCountry = tm.getSimCountryIso();
			if ((simCountry != null) && (simCountry.length() == 2)) { // SIM country code is available
				User_country = simCountry.toLowerCase();
			} else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
				String networkCountry = tm.getNetworkCountryIso();
				// network country code is available
				User_country = (networkCountry != null) && (networkCountry.length() == 2) ? networkCountry.toLowerCase() : Locale.getDefault().getCountry().toLowerCase();
			}
			if (User_country.isEmpty()) {
				Geocoder gcd = new Geocoder(context, Locale.getDefault());
				List<Address> addresses = gcd.getFromLocation(lat, lng, 1);

				if (!addresses.isEmpty()) {
					User_country = addresses.get(0).getCountryCode().toLowerCase();
				}
			}
		}catch (IOException ignored){
		}
		catch (Exception e){
			registra_error(e.toString(), "get_codigo_pais");
		}
		return User_country;
	}

	public static String get_locale(Context context) {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? context.getResources().getConfiguration().getLocales().get(0).getCountry() : context.getResources().getConfiguration().locale.getCountry();
	}

	public static double convertToDecimal(double doubleValue, int numOfDecimals) {
		BigDecimal bd = new BigDecimal(doubleValue);

		try {
			bd = bd.setScale(numOfDecimals, BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			registra_error(e.toString(), "convertToDecimal");
		}
		return bd.doubleValue();
	}

	public static String grupo_al_que_pertenece(Long Long_orientacion,String pais){
		String String_orientacion="";
		switch (Long_orientacion.intValue()){
			case 1:
				String_orientacion="HETERO";
				break;
			case 2:
				String_orientacion="LESBIANA";
				break;
			case 3:
			case 4:
			case 5:
				String_orientacion="GAY";
				break;
			case 6:
				String_orientacion="BI";
				break;
		}
		return pais+"_"+String_orientacion;

	}

	public static Usuario_para_listar prepara_datos_usuario_para_listar(Context mContext, Usuario_para_listar un_usuario, SharedPreferences preferencias_usuario ) {
		try {
			String distancia;
			float distancia_float = Double.valueOf(convertToDecimal(un_usuario.getDouble_distancia(), 2)).floatValue();

			String altura;
			String peso;
			//SI ESTAMOS EN UN USA O UK LA DISTANCIA LA MEDIMOS EN MILLAS
			if (preferencias_usuario.getLong(mContext.getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0) == mContext.getResources().getInteger(R.integer.BRITANICO)) {
				altura = cm_a_feet_and_inches(un_usuario.getLong_altura().intValue());
				Pair un_par = kg_a_st_and_lb(un_usuario.getLong_peso().intValue());
				peso = mContext.getResources().getString(R.string.st_y_lb, (double)un_par.first, (double)un_par.second);
				distancia = String.format(mContext.getResources().getString(R.string.Mi), km_a_mi(distancia_float));
			} else if (preferencias_usuario.getLong(mContext.getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0) == mContext.getResources().getInteger(R.integer.AMERICANO)) {
				altura = cm_a_feet_and_inches(un_usuario.getLong_altura().intValue());
				peso = kg_a_lb(un_usuario.getLong_peso().intValue());
				distancia = String.format(mContext.getResources().getString(R.string.Mi), km_a_mi(distancia_float));
			} else {
				altura = String.valueOf(cm_a_m(un_usuario.getLong_altura().intValue()));
				peso = String.format(mContext.getResources().getString(R.string.kg), un_usuario.getLong_peso().intValue());
				distancia = String.format(mContext.getResources().getString(R.string.km), distancia_float);
			}
			un_usuario.setString_distancia(distancia);
			un_usuario.setString_altura(altura);
			un_usuario.setString_peso(peso);
			un_usuario.setString_edad(String.format(mContext.getResources().getString(R.string.anyos), getEdad(un_usuario.getFecha_nacimiento())));

			String raza = mContext.getResources().getStringArray(R.array.razas)[un_usuario.getLong_raza().intValue()];

			un_usuario.setString_raza(raza);
		} catch (Exception e) {
			registra_error(e.toString(), "get_datos_persona del usuario ");
		}
		return un_usuario;
	}

	public static double distFrom(Double lat1, Double lng1, Double lat2, Double lng2) {
		try {
			if (lat1 != null && lng1 != null && lat2 != null && lng2 != null) {
				if (lat1 != 0D && lng1 != 0D && lat2 != 0D && lng2 != 0D) {
					double dLat = Math.toRadians(lat2 - lat1);
					double dLng = Math.toRadians(lng2 - lng1);
					double sindLat = Math.sin(dLat / 2);
					double sindLng = Math.sin(dLng / 2);
					double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
							* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
					double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

					return earthRadius * c;
				} else {
					return 301D;
				}
			} else {
				return -1D;
			}
		} catch (Exception e) {
			return -1D;
		}
	}

	public static double getDistancia(Context mContext, Double latitud, Double longitud){
		SharedPreferences preferencias_usuario;

		preferencias_usuario = mContext.getSharedPreferences(mContext.getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
		double mi_latitud= preferencias_usuario.getFloat(mContext.getResources().getString(R.string.PERFIL_USUARIO_LATITUD),0.0F);
		double mi_longitud= preferencias_usuario.getFloat(mContext.getResources().getString(R.string.PERFIL_USUARIO_LONGITUD),0.0F);

		return  distFrom(latitud, longitud, mi_latitud, mi_longitud);

	}

	public static void acomodar_conversacion_en_sharedpreferences(Context mContext, String quien_lo_dijo, String que_dijo, Long cuando_lo_dijo, String nick_remitente) {
		try {
			int i = 0;
			SharedPreferences chat_general;
			chat_general = mContext.getSharedPreferences(mContext.getResources().getString(R.string.SHAREDPREFERENCES_PRINCIPAL), Context.MODE_PRIVATE);

			Editor editor = chat_general.edit();

			while (chat_general.getString(mContext.getString(R.string.CHAT_GENERAL_QUIEN_LO_DIJO) + i, null) != null) {
				i++;
			}
			if (i >= mContext.getResources().getInteger(R.integer.MAX_CHAT_LENGTH)) {
				for (int j = 1; j < mContext.getResources().getInteger(R.integer.MAX_CHAT_LENGTH); j++) {
					editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_NICK_DE_QUIEN_LO_DIJO) + (j - 1), chat_general.getString(mContext.getString(R.string.CHAT_GENERAL_NICK_DE_QUIEN_LO_DIJO) + j, null));
					editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_QUIEN_LO_DIJO) + (j - 1), chat_general.getString(mContext.getString(R.string.CHAT_GENERAL_QUIEN_LO_DIJO) + j, null));
					editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_QUE_DIJO) + (j - 1), chat_general.getString(mContext.getString(R.string.CHAT_GENERAL_QUE_DIJO) + j, null));
					editor.putLong(mContext.getResources().getString(R.string.CHAT_GENERAL_CUANDO_LO_DIJO) + (j - 1), chat_general.getLong(mContext.getString(R.string.CHAT_GENERAL_CUANDO_LO_DIJO) + j, -1L));
				}
				editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_NICK_DE_QUIEN_LO_DIJO) + (mContext.getResources().getInteger(R.integer.MAX_CHAT_LENGTH) - 1), nick_remitente);
				editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_QUIEN_LO_DIJO) + (mContext.getResources().getInteger(R.integer.MAX_CHAT_LENGTH) - 1), quien_lo_dijo);
				editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_QUE_DIJO) + (mContext.getResources().getInteger(R.integer.MAX_CHAT_LENGTH) - 1), que_dijo);
				editor.putLong(mContext.getResources().getString(R.string.CHAT_GENERAL_CUANDO_LO_DIJO) + (mContext.getResources().getInteger(R.integer.MAX_CHAT_LENGTH) - 1), cuando_lo_dijo);
				editor.apply();
			} else {
				editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_NICK_DE_QUIEN_LO_DIJO) + i, nick_remitente);
				editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_QUIEN_LO_DIJO) + i, quien_lo_dijo);
				editor.putString(mContext.getResources().getString(R.string.CHAT_GENERAL_QUE_DIJO) + i, que_dijo);
				editor.putLong(mContext.getResources().getString(R.string.CHAT_GENERAL_CUANDO_LO_DIJO) + i, cuando_lo_dijo);
				editor.apply();
			}
		} catch (Exception e) {
			registra_error(e.toString(), "acomodar_conversacion_en_sharedpreferences");
		}
	}

	public static void registra_error(String error,String method_name){
		Calendar mCalendar= Calendar.getInstance();
		Error un_error= new Error(method_name,error,BuildConfig.VERSION_NAME,String.valueOf(Build.VERSION.SDK_INT),BuildConfig.APPLICATION_ID);
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection("ERRORES").document(String.valueOf(mCalendar.getTimeInMillis())).set(un_error);
	}

	public static Drawable get_no_pic(Context mContext, int color) {
		Drawable drawable = null;
		try {

			drawable = DrawableCompat.wrap(ContextCompat.getDrawable(mContext, R.drawable.no_pic));

			DrawableCompat.setTint(drawable, color);

		}catch (Exception e) {
			registra_error(e.toString(), "get_Version_Name");

			//new AsyncTask_registra_error().execute(new Pair<>(e.toString(), "get_Version_Name de utils"));
		}
		return drawable;

	}

	public static Drawable get_icono_twitter(Context mContext, int color){
		Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(mContext, R.drawable.icono_twitter));

		DrawableCompat.setTint(drawable, color);

		return drawable;
	}

	public static String get_Version_Name(Context mContext){
		String result="";
		try{
			PackageManager packageManager = mContext.getPackageManager();
			result=packageManager.getPackageInfo(mContext.getPackageName(), 0).versionName;
		}catch (Exception e){
			registra_error(e.toString(), "get_Version_Name");
		}
		return result;
	}

	public static void actualizacion_semanal_de_estrellas(Context mContext, String token_socialauth,Long fecha_cobro_estrellas){
		long fecha_actual_miliseconds= Calendar.getInstance().getTimeInMillis();
		int diferencia=(fecha_cobro_estrellas.intValue()- (int) fecha_actual_miliseconds)/(24*60*60*1000);
		if( diferencia>7){
			FirebaseFirestore db = FirebaseFirestore.getInstance();
			DocumentReference Ref=  db.collection(mContext.getResources().getString(R.string.USUARIOS)).document(token_socialauth);
			Ref.get().addOnCompleteListener(task -> {
				if (task.isSuccessful()) {
					DocumentSnapshot document = task.getResult();
					if (document.exists()) {
						Long estrellas_antes= document.getLong(mContext.getResources().getString(R.string.USUARIO_ESTRELLAS));
						Long num_semanas_transcurridas=roundUp(diferencia,7);
						Long estrellas_a_restar=Integer.valueOf(mContext.getResources().getInteger(R.integer.LOGRO_COBRO_SEMANAL)).longValue();
						long estrellas_ahora=estrellas_antes+ estrellas_a_restar*num_semanas_transcurridas;

						WriteBatch batch = db.batch();

						batch.update(Ref,mContext.getResources().getString(R.string.USUARIO_ESTRELLAS), FieldValue.increment(estrellas_ahora));
						batch.update(Ref,mContext.getResources().getString(R.string.USUARIO_FECHA_COBRO_ESTRELLAS), FieldValue.increment(7*24*60*60*1000));
						batch.commit();

						for (int i=0; i<num_semanas_transcurridas;i++) {
							Logro un_logro = new Logro(Integer.valueOf(mContext.getResources().getInteger(R.integer.MOTIVO_COBRO_SEMANAL)).longValue(), Integer.valueOf(mContext.getResources().getInteger(R.integer.LOGRO_COBRO_SEMANAL)).longValue(), Calendar.getInstance().getTimeInMillis(), estrellas_antes+estrellas_a_restar*i);
							db.collection(mContext.getResources().getString(R.string.USUARIOS)).document(token_socialauth).collection(mContext.getResources().getString(R.string.LOGROS)).add(un_logro);
						}
					}
				}
			});
		}
	}

	private static long roundUp(long num, long divisor) {
		return (num + divisor - 1) / divisor;
	}

	public static Long get_app_code(String package_name){
		long code=0L;
		switch (package_name){

			case ("com.rubisoft.womencuddles"):
				code=1L;
				break;
			case ("com.rubisoft.mencuddles"):
				code=2L;
				break;
			case ("com.rubisoft.gaycuddles"):
				code=3L;
				break;
			case ("com.rubisoft.lesbiancuddles"):
				code=4L;
				break;
			case ("com.rubisoft.bisexcuddles"):
				code=5L;
				break;
			case ("com.rubisoft.womenradar"):
				code=6L;
				break;
			case ("com.rubisoft.gayradar"):
				code=7L;
				break;
			case ("com.rubisoft.menradar"):
				code=8L;
				break;
			case ("com.rubisoft.lesbianradar"):
				code=9L;
				break;
			case ("com.rubisoft.bisexradar"):
				code=10L;
				break;
		}
		return code;
	}
}



