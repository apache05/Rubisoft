package com.rubisoft.lesbiancuddles.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.google.common.io.BaseEncoding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.lesbiancuddles.BuildConfig;
import com.rubisoft.lesbiancuddles.R;
import com.rubisoft.lesbiancuddles.databinding.LayoutPrimerasFotosBinding;
import com.rubisoft.lesbiancuddles.tools.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

import javax.net.ssl.SSLException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Activity_Primeras_fotos extends AppCompatActivity {
	private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
	private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
	private static final int REQUEST_LOAD_IMAGE = 1;
	private static final int REQUEST_CAPTURE_IMAGE = 2;
	private static final int RESULT_OK = -1;
	private static final int PERMISSION_CAPTURAR_FOTO = 1;
	private static final int PERMISSION_SELECCIONAR_FOTO = 2;
	private static final int PERMISSION_BORRAR_FOTO = 3;
	private static final int MAX_LABEL_RESULTS = 10;
	private static final String TAG_CAMBIAR_FOTO = "cambiar_foto";

	private LayoutPrimerasFotosBinding binding;

	private SharedPreferences perfil_usuario;

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
			super.onCreate(savedInstanceState);
			binding = LayoutPrimerasFotosBinding.inflate(getLayoutInflater());
			setContentView(binding.getRoot());
			perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

			setup_Views();
			setup_Typeface();
			set_Texts();
			inicializa_anuncios();

			// Set listener, view, data for your dialog fragment
			Drawable icono_cambiar_foto = new IconicsDrawable(getApplicationContext()).icon(GoogleMaterial.Icon.gmd_view_module).color(ContextCompat.getColor(getApplicationContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_hacer_foto = new IconicsDrawable(getApplicationContext()).icon(GoogleMaterial.Icon.gmd_photo_camera).color(ContextCompat.getColor(getApplicationContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_borrar_foto = new IconicsDrawable(getApplicationContext()).icon(GoogleMaterial.Icon.gmd_delete).color(ContextCompat.getColor(getApplicationContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));

			binding.DialogoCambiarFotoButtonCambiarFoto.setImageDrawable(icono_cambiar_foto);
			binding.DialogoCambiarFotoButtonCambiarFoto.setOnClickListener(view -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_SELECCIONAR_FOTO);
					} else {
						lanza_intent_seleccionar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_cambiar_foto) de activity_primeras_fotos");
				}
			});
			binding.DialogoCambiarFotoButtonHacerFoto.setImageDrawable(icono_hacer_foto);
			binding.DialogoCambiarFotoButtonHacerFoto.setOnClickListener(view -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAPTURAR_FOTO);
					} else {
						lanza_intent_capturar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_capturar_foto) de activity_primeras_fotos");
				}
			});
			binding.DialogoCambiarFotoButtonBorrarFoto.setImageDrawable(icono_borrar_foto);
			binding.DialogoCambiarFotoButtonBorrarFoto.setOnClickListener(view -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_BORRAR_FOTO);
					} else {
						procesa_borrar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_borrar_foto) de activity_primeras_fotos");
				}
			});
			binding.LayoutPrimerasFotosAppCompatImageViewAceptar.setOnClickListener(view -> {
				Intent mIntent = new Intent(Activity_Primeras_fotos.this, Activity_Principal.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mIntent);
				finish();
			});

		} catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de primeras_fotos");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_SELECCIONAR_FOTO) {
			if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				lanza_intent_seleccionar_foto();
			}
		}else if (requestCode == PERMISSION_CAPTURAR_FOTO) {
			if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				lanza_intent_capturar_foto();
			}
		}else if (requestCode == PERMISSION_BORRAR_FOTO) {
			if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED) || (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
				procesa_borrar_foto();
			}
		}
	}

	@Override
	public void onBackPressed() {
		try {
			// super.onBackPressed();
			Intent mIntent = new Intent(this, Activity_Principal.class);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mIntent);
			finish();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onBackPressed de activity_primeras_fotos");

		}
	}

	@Override
	public void onResume() {
		try {
			super.onResume();
			Appodeal.onResume(this, Appodeal.BANNER_TOP);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_CAMBIAR_FOTO);
			if (prev != null) {
				ft.remove(prev);
				ft.commit();
			}
			/**/
			String nombre_thumb_1 = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), 0);
			coloca_ImageView(nombre_thumb_1);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onResume de primeras_fotos");

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
					uploadImage(data.getData());

				} else if (requestCode == REQUEST_CAPTURE_IMAGE) {
					String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
					String nombre_thumb = utils.get_nombre_thumb(token_socialauth, 0);

					File file_src = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombre_thumb);

					uploadImage(Uri.fromFile(file_src));
				}

			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onActivityResult de activity_primeras_fotos");
		}
	}

	private void setup_Views() {
		try {
			Drawable icono_seguir = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_done).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(getResources().getInteger(R.integer.Tam_Normal_icons));
			binding.LayoutPrimerasFotosAppCompatImageViewAceptar.setImageDrawable(icono_seguir);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_Views de activity_primeras_fotos");
		}
	}

	private void setup_Typeface() {
		try {
			Typeface mTypeFace_roboto_bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
			Typeface mTypeFace_roboto_light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

			binding.LayoutPrimerasFotosTextViewPonFotos.setTypeface(mTypeFace_roboto_light);
			binding.advertencia.setTypeface(mTypeFace_roboto_bold);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_Typeface de activity_primeras_fotos");
		}
	}

	private void set_Texts() {
		binding.LayoutPrimerasFotosTextViewPonFotos.setText(getString(R.string.ACTIVITY_PRIMERAS_FOTOS_INSERTA_FOTOS));
		binding.advertencia.setText(getString(R.string.ADVERTENCIA));
	}

	private void coloca_ImageView(String nombre_thumb) {
		try {
			File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File file = new File(storageDir, nombre_thumb);

			if (file.exists()) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_WAIT), Toast.LENGTH_LONG).show();

				new AsyncTask_coloca_Thumb_de_memoria_interna().execute(file);

				//mAppCompatImageView_foto1.setImageDrawable(null); // <--- added to force redraw of ImageView
				//mAppCompatImageView_foto1.setImageBitmap(myBitmap);
			} else {
				binding.LayoutPrimerasFotosImageButton1.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
			}

		} catch (Exception e) {
			utils.registra_error(e.toString(), "coloca_ImageView de primeras_fotos");
		}
	}

	private class AsyncTask_coloca_Thumb_de_memoria_interna extends AsyncTask<File, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
		}

		@Nullable
		@Override
		protected Bitmap doInBackground(File... params) {
			Bitmap un_bitmap = null;

			try {
				File file = params[0];
				un_bitmap = utils.decodeSampledBitmapFromFilePath(file.getPath(), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
			} catch (Exception ignored) {
			}
			return un_bitmap;
		}

		@Override
		protected void onPostExecute(@Nullable Bitmap bitmap) {
			try {
				if (getApplicationContext() != null && !isCancelled()) {
					binding.LayoutPrimerasFotosImageButton1.setImageBitmap(bitmap);
					binding.LayoutPrimerasFotosImageButton1.setScaleType(ImageView.ScaleType.CENTER_CROP);
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "exception en AsyncTask_coloca_Thumb_de_memoria_interna (onPostExecute) en Activity_Primeras_Fotos", Toast.LENGTH_LONG).show();
			}
		}

	}

	private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {

		HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
		JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

		VisionRequestInitializer requestInitializer =
				new VisionRequestInitializer(getResources().getString(R.string.CLOUD_VISION_API_KEY)) {
					/**
					 * We override this so we can inject important identifying fields into the HTTP
					 * headers. This enables use of a restricted cloud platform API key.
					 */
					@Override
					protected void initializeVisionRequest(VisionRequest<?> visionRequest)
							throws IOException {
						super.initializeVisionRequest(visionRequest);

						String packageName = getPackageName();
						visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

						String sig = getSignature(getPackageManager(), packageName);

						visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
					}
				};

		Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
		builder.setVisionRequestInitializer(requestInitializer);

		Vision vision = builder.build();

		BatchAnnotateImagesRequest batchAnnotateImagesRequest =
				new BatchAnnotateImagesRequest();
		batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
			AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

			// Add the image
			Image base64EncodedImage = new Image();
			// Convert the bitmap to a JPEG
			// Just in case it's a format that Android understands but Cloud Vision
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
			byte[] imageBytes = byteArrayOutputStream.toByteArray();

			// Base64 encode the JPEG
			base64EncodedImage.encodeContent(imageBytes);
			annotateImageRequest.setImage(base64EncodedImage);

			// add the features we want
			annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
				Feature labelDetection = new Feature();
				labelDetection.setType("SAFE_SEARCH_DETECTION");
				labelDetection.setMaxResults(MAX_LABEL_RESULTS);
				add(labelDetection);
			}});

			// Add the list of one thing to the request
			add(annotateImageRequest);
		}});

		Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
		// Due to a bug: requests to Vision API containing large images fail when GZipped.
		annotateRequest.setDisableGZipContent(true);

		return annotateRequest;
	}

	private void lanza_intent_seleccionar_foto() {
		try {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "Select a photo"), REQUEST_LOAD_IMAGE);
		} catch (IllegalArgumentException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_intent_seleccionar_foto de Activity_primeras_fotos");
		}
	}

	private void lanza_intent_capturar_foto() {
		try {
			// create Intent to take a picture and return control to the calling application
			Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (mIntent.resolveActivity(getPackageManager()) != null) {
				String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), 0);

				File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombre_thumb);

				mIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file));
				startActivityForResult(mIntent, REQUEST_CAPTURE_IMAGE);
			}

		} catch (IllegalArgumentException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_intent_capturar_foto de Activity_primeras_fotos");
		}
	}

	private void inicializa_anuncios() {
		try {
			Consent consent = ConsentManager.getInstance(this).getConsent();
			Appodeal.setTesting(false);
			Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER, consent);
			setup_banner();
		} catch (Exception e) {
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

	private static String getSignature(@NonNull PackageManager pm, @NonNull String packageName) {
		try {
			PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			if (packageInfo == null
					|| packageInfo.signatures == null
					|| packageInfo.signatures.length == 0
					|| packageInfo.signatures[0] == null) {
				return null;
			}
			return signatureDigest(packageInfo.signatures[0]);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	private static String signatureDigest(Signature sig) {
		byte[] signature = sig.toByteArray();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] digest = md.digest(signature);
			return BaseEncoding.base16().lowerCase().encode(digest);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	private void procesa_borrar_foto() {
		try {
			String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), 0);

			File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

			File file = new File(storageDir, nombre_thumb);
			if (file.exists()) {
				borra_foto_de_memoria_interna(nombre_thumb);

			}
			borrar_foto_de_FCS(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));


			Intent mIntent = new Intent(getApplicationContext(), Activity_Primeras_fotos.class);
			startActivity(mIntent);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "procesa_borrar_foto de activity_primeras_fotos");
		}
	}

	private void borra_foto_de_memoria_interna(@NonNull String file_name) {
		// ************************************************************************************************
		// Esta función borra una foto de memoria interna.
		// ************************************************************************************************
		try {
			File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File file = new File(storageDir, file_name);
			boolean resultado = file.delete();
			if (!resultado) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_deleting_files), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "borra_foto_de_memoria_interna de activity_primeras_fotos");
		}
	}

	private void borrar_foto_de_FCS(String token_socialauth) {
		try {
			FirebaseStorage storage = FirebaseStorage.getInstance();
			StorageReference mStorageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket));

			// Create a reference to the file to delete
			StorageReference fotoRef = mStorageRef.child(utils.get_path_foto(token_socialauth, 0));
			StorageReference thumbRef = mStorageRef.child(utils.get_path_thumb(token_socialauth, 0));

			// Delete the foto
			fotoRef.delete().addOnSuccessListener((OnSuccessListener) o -> {

			}).addOnFailureListener(exception -> {
				// Uh-oh, an error occurred!

			});
			// Delete the thumb
			thumbRef.delete().addOnSuccessListener((OnSuccessListener) o -> {

			}).addOnFailureListener(exception -> {
				// Uh-oh, an error occurred!

			});
		} catch (RejectedExecutionException e) {
			try {
				Thread.sleep(500);
			} catch (Exception ignored) {
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "borrar_foto_de_GCS de activity_primeras_fotos");
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	private class SafeDetectionTask extends AsyncTask<Pair<Uri, Vision.Images.Annotate>, Void, Boolean> {
		@SafeVarargs
		@Override
		protected final Boolean doInBackground(Pair<Uri, Vision.Images.Annotate>... params) {
			BatchAnnotateImagesResponse response;
			Uri uri = params[0].first;
			Vision.Images.Annotate mRequest = params[0].second;
			String message = null;
			boolean resultado = true;
			try {
				response = mRequest.execute();

				SafeSearchAnnotation labels = response.getResponses().get(0).getSafeSearchAnnotation();
				if (labels != null) {
					message = (String.format(Locale.US, " %s", labels.getAdult()));
				}
				switch (message) {
					case " VERY_UNLIKELY":
					case " UNLIKELY":
						//probablemente no es obscena
						subir_foto_a_FCS(uri, getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
						File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
						String nombre_foto = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), 0);
						File file = new File(storageDir, nombre_foto);

						InputStream iStream = getContentResolver().openInputStream(uri);
						byte[] inputData = getBytes(iStream);
						if (!utils.guarda_foto_en_memoria_interna(inputData, file.getPath())) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
						} else {
							Intent mIntent = new Intent(Activity_Primeras_fotos.this, Activity_Mi_Perfil.class);
							startActivity(mIntent);
							finish();
						}
						break;
					case " VERY_LIKELY":
					case " LIKELY":
					case "POSSIBLE":
						//es obscena CASI seguro, ni si quiera hace falta revisar
						resultado = false;
						break;
				}
			}catch (SocketTimeoutException | UnknownHostException | SSLException | ConnectException e ) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
			}
			catch (SecurityException e){
				requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAPTURAR_FOTO);
			}
			catch (Exception e) {
				utils.registra_error(e.toString(), "SafeDetectionTask (doInBackground) en Primeras_fotos");
			}
			binding.mProgressBar.setVisibility(View.INVISIBLE);
			return resultado;
		}

		private byte[] BitmapToBytes(Bitmap bitmap) {

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
			} catch (Exception ignored) {
			}
			return byteArrayOutputStream.toByteArray();
		}

		private void subir_foto_a_FCS(Uri Uri_de_la_foto, int tamanyo_foto_perfiles) {
			try {
				String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");

				FirebaseStorage storage = FirebaseStorage.getInstance();
				StorageReference mStorageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket));

				Bitmap bitmap_foto = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri_de_la_foto);
				StorageReference fotoRef = mStorageRef.child(utils.get_path_foto(token_socialauth, 0));
				UploadTask upload_foto_Task = fotoRef.putBytes(BitmapToBytes(ThumbnailUtils.extractThumbnail(bitmap_foto, 520, 520)));
				upload_foto_Task.addOnFailureListener(ignored -> {
				}).addOnSuccessListener(taskSnapshot -> {

				});

				Bitmap bitmap_thumb = ThumbnailUtils.extractThumbnail(bitmap_foto, tamanyo_foto_perfiles, tamanyo_foto_perfiles);
				StorageReference thumbRef = mStorageRef.child(utils.get_path_thumb(token_socialauth, 0));
				UploadTask upload_thumb_Task = thumbRef.putBytes(BitmapToBytes(bitmap_thumb));
				upload_thumb_Task.addOnFailureListener(ignored -> {
				}).addOnSuccessListener(taskSnapshot -> {
				});
			} catch (Exception ignored) {
			}
		}

		protected void onPostExecute(Boolean resultado) {
			if (!resultado) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.foto_inapropiada), Toast.LENGTH_LONG).show();
			}
		}
	}

	private byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		return byteBuffer.toByteArray();
	}

	private void uploadImage(Uri uri) {
		try {
			if (uri != null) {
				binding.mProgressBar.setVisibility(View.VISIBLE);

				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
				if (bitmap!= null) {
					Vision.Images.Annotate un_Annotate = prepareAnnotationRequest(bitmap);
					new SafeDetectionTask().execute(new Pair<>(uri, un_Annotate));
				}else{
					Toast.makeText(this, getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
				}
				Intent mIntent = new Intent(this, Activity_Primeras_fotos.class);
				startActivity(mIntent);
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(this, getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA) + " " + e, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "uploadImage de Activity_Primeras_Fotos");
		}
	}
}