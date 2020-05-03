package com.rubisoft.womencuddles.Fragments;

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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
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
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.womencuddles.Adapters.Image_Adapter;
import com.rubisoft.womencuddles.BuildConfig;
import com.rubisoft.womencuddles.Classes.Mi_foto;
import com.rubisoft.womencuddles.Classes.NpaGridLayoutManager;
import com.rubisoft.womencuddles.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.womencuddles.R;
import com.rubisoft.womencuddles.activities.Activity_Mi_Perfil;
import com.rubisoft.womencuddles.databinding.FragmentFotoManagerBinding;
import com.rubisoft.womencuddles.tools.utils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

//*********************************************************************************
// ESTE FRAGMENT SE CORRESPONDE CON UNA DE LOS TABS DE Activity_mi_perfil
// sirve para mostrar las fotos que el Usuario_para_listar tiene de perfil y poder cambiarlas
//*******************************************************************descarga_URL**************

//************************************* ESTA ACTIVITY DEBE PERMITIR HACER ********************************************
//* - MOSTRAR TODAS LAS FOTOS DE PERFIL DEL USUARIO
//* - PODER CAMBIARLAS
//* - SI NO ES USUARIO PREMIUM, SOLO PUEDE TENER UNA
//* - SI  ES USUARIO PREMIUM, PUEDE TENER HASTA MAX_NUM_FOTOS
//* - PULSANDO SOBRE UNA DE LAS FOTOS APARECE UN DIALOGO CON LAS OPCIONES DE CAMBIAR FOTO, HACER FOTO O BORRAR FOTO
//********************************************************************************************************************

public class fragment_foto_manager extends Fragment{
	private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
	private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
	private static final int REQUEST_LOAD_IMAGE = 1;
	private static final int REQUEST_CAPTURE_IMAGE = 2;
	private static final int REQUEST_DELETE_IMAGE = 2;
	private static final int RESULT_OK = -1;
	private static final int PERMISSION_CAPTURAR_FOTO = 1;
	private static final int PERMISSION_SELECCIONAR_FOTO = 2;
	private static final int PERMISSION_BORRAR_FOTO = 3;
	private static final int MAX_LABEL_RESULTS = 10;
	private int num_foto_actual;
	private static final String TAG_CAMBIAR_FOTO = "cambiar_foto";

	private Image_Adapter mImageAdapter;
	private SharedPreferences perfil_usuario;
	private SharedPreferences preferencias_usuario;

	private boolean es_premium;
	private FragmentFotoManagerBinding binding;

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_foto_manager, container, false);
		binding = FragmentFotoManagerBinding.inflate(inflater, container, false);
		View mView = binding.getRoot();

		try {
			perfil_usuario = getActivity().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
			preferencias_usuario = getActivity().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);

			Typeface mTypeFace_roboto_bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
			binding.advertencia.setTypeface(mTypeFace_roboto_bold);
			binding.advertencia.setText(getString(R.string.ADVERTENCIA));


			// Set listener, view, data for your dialog fragment
			Drawable icono_cambiar_foto = new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_view_module).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_hacer_foto = new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_photo_camera).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_borrar_foto = new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_delete).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));

			binding.DialogoCambiarFotoButtonCambiarFoto.setImageDrawable(icono_cambiar_foto);
			binding.DialogoCambiarFotoButtonCambiarFoto.setOnClickListener(view -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_SELECCIONAR_FOTO);
					} else {
						lanza_intent_seleccionar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_cambiar_foto) de fragment_foto_manager");
				}
			});
			binding.DialogoCambiarFotoButtonHacerFoto.setImageDrawable(icono_hacer_foto);
			binding.DialogoCambiarFotoButtonHacerFoto.setOnClickListener(view -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAPTURAR_FOTO);
					} else {
						lanza_intent_capturar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_capturar_foto) de fragment_foto_manager");
				}
			});
			binding.DialogoCambiarFotoButtonBorrarFoto.setImageDrawable(icono_borrar_foto);
			binding.DialogoCambiarFotoButtonBorrarFoto.setOnClickListener(view -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_BORRAR_FOTO);
					} else {
						procesa_borrar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_borrar_foto) de fragment_foto_manager");
				}
			});

			crea_ItemTouchListener_perfiles();

		} catch (Exception e) {
			utils.registra_error(e.toString(), "onCreateView de fragment_foto_manager");
		}
		return mView;
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
	public void onResume() {
		super.onResume();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(TAG_CAMBIAR_FOTO);
		if (prev != null) {
			ft.remove(prev);
			ft.commit();
		}

		if (preferencias_usuario.getBoolean(getResources().getString(R.string.AYUDA_FRAGMENT_FOTO_MANAGER), false)) {
			lanza_dialogo_tip();
		}

		Setup_RecyclerView();
		((Activity_Mi_Perfil) getActivity()).setupNavigationDrawer();
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
					String nombre_thumb = utils.get_nombre_thumb(token_socialauth, num_foto_actual);

					File file_src = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombre_thumb);

					uploadImage(Uri.fromFile(file_src));
				}

			}
		}catch (Exception e) {
			utils.registra_error(e.toString(), "onActivityResult de fragment_foto_manager");
		}
	}

	private ArrayList<Mi_foto> getMis_Fotos() {
		ArrayList<Mi_foto> array_de_Mis_fotos = new ArrayList<>(getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM));

		try {
			es_premium = perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false);
			AbsListView.LayoutParams mlayoutParams_perfil = new AbsListView.LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));

			for (int i = 0; i < getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM); i++) {
				Mi_foto una_foto = new Mi_foto();
				if ((i < getResources().getInteger(R.integer.NUM_MAX_FOTOS_NO_PREMIUM)) || es_premium) {
					AppCompatImageView mAppCompatImageView = coloca_ImageView(i);
					mAppCompatImageView.setLayoutParams(mlayoutParams_perfil);

					una_foto.setFoto(mAppCompatImageView);
				} else {
					AppCompatImageView mAppCompatImageView = new AppCompatImageView(getContext());
					mAppCompatImageView.setLayoutParams(mlayoutParams_perfil);
					mAppCompatImageView.setImageDrawable(utils.get_no_pic(getContext(), ContextCompat.getColor(getContext(), R.color.primary_light)));
					mAppCompatImageView.setImageAlpha(50);

					una_foto.setFoto(mAppCompatImageView);
				}
				/*if (perfil_usuario.getBoolean(sharedpreference_foto_privada(i), false)) {
					Drawable icono_hacer_foto_privada_activada = new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_vpn_key).color(ContextCompat.getColor(getContext(), R.color.accent)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
					una_foto.setLlavecita(icono_hacer_foto_privada_activada);
				}
				else {
					una_foto.setLlavecita(null);
				}*/
				array_de_Mis_fotos.add(una_foto);
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "getMis_Fotos de fragment_foto_manager");

		}

		return array_de_Mis_fotos;
	}

	private AppCompatImageView coloca_ImageView(int posicion) {
		//esta función carga una foto de la memoria interna en su posicion del grid de imágenes correspondiente
		//o la foto no_pic a falta de comprobar si está en el GCS
		AppCompatImageView mAppCompatImageView = new AppCompatImageView(getContext());

		try {
			String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), posicion);
			File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File file = new File(storageDir, nombre_thumb);

			mAppCompatImageView.setImageDrawable(utils.get_no_pic(getContext(),ContextCompat.getColor(getContext(), R.color.primary_light)));

			if (file.exists()) {
				new AsyncTask_coloca_Thumb_de_memoria_interna().execute(new Pair<>(file, posicion));
				//existe_foto_usuario[posicion] = true;

			} else {
				//Como último recurso comprobamos si esta en el FireBase Cloud Storage
				descarga_URL(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), posicion);
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "colocoa_ImageView de fragment_foto_manager");
		}
		return mAppCompatImageView;
	}

	//Esta función detecta cuando el Usuario_para_listar toca de alguna forma algún item del RecyclerView y
	//Lo codifica en alguno de los gestos posibles. Dependiendo del gesto, llamará a una función u otra
	//del listener del Recyclerview
	private void crea_ItemTouchListener_perfiles() {
		//Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
		//y que lo ha detectado y codificado primero el  RecyclerTouchListener
		binding.FragmentFotoManagerReciclerView.addOnItemTouchListener(new fragment_foto_manager.RecyclerTouchListener_perfiles(getContext(), binding.FragmentFotoManagerReciclerView, (view, position) -> {
			if ((position < getResources().getInteger(R.integer.NUM_MAX_FOTOS_NO_PREMIUM)) || es_premium) {
				num_foto_actual=position;
			} else {
				Toast.makeText(getContext(), getResources().getString(R.string.SOLO_PREMIUM), Toast.LENGTH_LONG).show();
			}
		}));
	}

	private void Setup_RecyclerView() {
		try {
			ArrayList<Mi_foto> mis_fotos = getMis_Fotos();
			mImageAdapter = new Image_Adapter(mis_fotos);
			binding.FragmentFotoManagerReciclerView.setAdapter(mImageAdapter);
			NpaGridLayoutManager mi_LinearLayoutManager = new NpaGridLayoutManager(getActivity(), utils.get_num_columns_grids(getActivity()));
			binding.FragmentFotoManagerReciclerView.setLayoutManager(mi_LinearLayoutManager);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "Setup_RecyclerView de fragment_foto_manager");
		}
	}

	private void lanza_dialogo_tip() {
		try {
			new MaterialDialog.Builder(getActivity())
					.theme(Theme.LIGHT)
					.title(this.getResources().getString(R.string.DIALOGO_CONSEJO_TITULO))

					.content(this.getResources().getString(R.string.DIALOGO_CONSEJO_FOTO_MANAGER))
					.typeface("Roboto-Bold.ttf", "Roboto-Regular.ttf")
					.positiveText(R.string.ok)
					.onPositive((dialog, which) -> dialog.dismiss())
					.negativeText(R.string.DIALOGO_CONSEJO_YA_NO_MAS)
					.onNegative((dialog, which) -> {
						//SharedPreferences preferencias_usuario = getContext().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);
						SharedPreferences.Editor preferencias_editor = preferencias_usuario.edit();
						preferencias_editor.putBoolean(getResources().getString(R.string.AYUDA_FRAGMENT_FOTO_MANAGER), false);
						preferencias_editor.apply();
					}).canceledOnTouchOutside(false)
					.show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialog_tip de fragment_foto_manager");
		}
	}

	private void descarga_URL(String Token_socialauth, Integer num_foto) {
		try {
			FirebaseStorage storage = FirebaseStorage.getInstance();
			StorageReference storageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket)).child(utils.get_path_thumb(Token_socialauth, num_foto));

			storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
				if ((getActivity() != null) && isAdded()) {
					//existe_foto_usuario[num_foto] = true;

					AppCompatImageView mAppCompatImageView = new AppCompatImageView(getContext());

					AbsListView.LayoutParams mlayoutParams_perfil = new AbsListView.LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
					mAppCompatImageView.setLayoutParams(mlayoutParams_perfil);

					Target target = new Target() {
						@Override
						public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
							mAppCompatImageView.setImageBitmap(bitmap);

							//tenemos que volver a crear un listener
							mAppCompatImageView.setOnClickListener(v -> {
								num_foto_actual = num_foto;
								if (binding.DialogoCambiarFotoLinearLayoutBotonera.getVisibility() == View.INVISIBLE) {
									binding.advertencia.setVisibility(View.INVISIBLE);
									binding.DialogoCambiarFotoLinearLayoutBotonera.setVisibility(View.VISIBLE);
								} else {
									binding.advertencia.setVisibility(View.VISIBLE);
									binding.DialogoCambiarFotoLinearLayoutBotonera.setVisibility(View.INVISIBLE);
								}
							});
							mImageAdapter.setView(num_foto, mAppCompatImageView);
							mImageAdapter.notifyDataSetChanged();
						}

						@Override
						public void onBitmapFailed(Drawable errorDrawable) {
						}


						@Override
						public void onPrepareLoad(Drawable placeHolderDrawable) {
						}
					};

					Picasso.with(getContext())
							.load(uri)
							.placeholder(utils.get_no_pic(getContext(),ContextCompat.getColor(getContext(), R.color.primary_light)))   // optional
							.error(utils.get_no_pic(getContext(),ContextCompat.getColor(getContext(), R.color.primary_light)))       // optional
							.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))                        // optional
							.networkPolicy(NetworkPolicy.NO_CACHE)
							.into(target);

				}
			}).addOnFailureListener(e -> {
			});
		} catch (RejectedExecutionException e){
			try {
				Thread.sleep(500);
			}catch (Exception ignored){}
		}catch (Exception e) {
			utils.registra_error(e.toString(), "descarga_URL  de fragment_foto_manager");
		}
	}

	private class RecyclerTouchListener_perfiles implements RecyclerView.OnItemTouchListener {
		@Nullable
		private final GestureDetector mGestureDetector;
		private final Interface_ClickListener_Menu mInterfaceClickListener;

		RecyclerTouchListener_perfiles(Context context, @NonNull RecyclerView recyclerView, Interface_ClickListener_Menu un_Interface_ClickListener) {
			mInterfaceClickListener = un_Interface_ClickListener;
			mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {

					return true;
				}

				@Override
				public boolean onSingleTapUp(@NonNull MotionEvent e) {
					View child = binding.FragmentFotoManagerReciclerView.findChildViewUnder(e.getX(), e.getY());
					if ((child != null) && (mInterfaceClickListener != null)) {
						mInterfaceClickListener.My_onClick(child, recyclerView.getChildLayoutPosition(child));
					}
					return true;
				}

				@Override
				public void onLongPress(@NonNull MotionEvent e) {

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

	private void lanza_intent_seleccionar_foto() {
		try {
			Intent intent = new Intent();
			intent.setType("image/*");
			//intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
			startActivityForResult(Intent.createChooser(intent, "Select a photo"),REQUEST_LOAD_IMAGE);

		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_intent_seleccionar_foto de fragment_foto_manager");
		}
	}

	private void lanza_intent_capturar_foto() {
		try {
			// create Intent to take a picture and return control to the calling application
			Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (mIntent.resolveActivity(getActivity().getPackageManager()) != null) {
				String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), num_foto_actual);

				File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombre_thumb);

				mIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file));
				startActivityForResult(mIntent, REQUEST_CAPTURE_IMAGE);
			}

		}catch (IllegalArgumentException e){
			Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_intent_capturar_foto de fragment_foto_manager");
		}
	}

	private void procesa_borrar_foto() {
		try {
			//hacer_foto_no_privada();
			String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), num_foto_actual);

			File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

			File file = new File(storageDir, nombre_thumb);
			if (file.exists()) {
				borra_foto_de_memoria_interna(nombre_thumb);
				//actualiza_foto_privada(crea_array_booleanos_despues_de_borrado(num_foto_actual));
				/*SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
				editor_perfil_usuario.putBoolean(sharedpreference_foto_privada(num_foto_actual), false);
				editor_perfil_usuario.apply();*/
			}
			borrar_foto_de_FCS(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), num_foto_actual);


			Intent mIntent = new Intent(getContext(), Activity_Mi_Perfil.class);
			getContext().startActivity(mIntent);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "procesa_borrar_foto de fragment_foto_manager");
		}
	}

	private void borra_foto_de_memoria_interna(@NonNull String file_name) {
		// ************************************************************************************************
		// Esta función borra una foto de memoria interna.
		// ************************************************************************************************
		try {
			File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			File file = new File(storageDir, file_name);
			boolean resultado =file.delete();
			if (!resultado){
				Toast.makeText(getContext(),getResources().getString(R.string.error_deleting_files), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "borra_foto_de_memoria_interna de fragment_foto_manager");
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

				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
				if (bitmap!= null) {
					Vision.Images.Annotate un_Annotate = prepareAnnotationRequest(bitmap);
					new SafeDetectionTask().execute(new Pair<>(uri, un_Annotate));
				}else{
					Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
				}
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA)+ " "+ e, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "uploadImage de fragment_foto_manager");
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

						String packageName = getContext().getPackageName();
						visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

						String sig = getSignature(getContext().getPackageManager(), packageName);

						visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
					}
				};

		Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
		builder.setVisionRequestInitializer(requestInitializer);

		Vision vision = builder.build();

		BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
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

	private class SafeDetectionTask extends AsyncTask< Pair<Uri,Vision.Images.Annotate>, Void, Boolean> {

		@SafeVarargs
		@Override
		protected final Boolean doInBackground(Pair<Uri, Vision.Images.Annotate>... params) {
			BatchAnnotateImagesResponse response;
			Uri uri= params[0].first;
			Vision.Images.Annotate mRequest = params[0].second;
			String message=null;
			boolean resultado= true;
			try {
				response = mRequest.execute();

				SafeSearchAnnotation labels = response.getResponses().get(0).getSafeSearchAnnotation();
				if (labels != null) {
					message = (String.format(Locale.US, " %s", labels.getAdult()));
				}
				switch (message) {
					case " VERY_UNLIKELY":
					case " UNLIKELY":
						//no es obscena
						subir_foto_a_FCS(uri,getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
						File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
						String nombre_foto = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), num_foto_actual);
						File file = new File(storageDir, nombre_foto);

						InputStream iStream =   getContext().getContentResolver().openInputStream(uri);
						byte[] inputData = getBytes(iStream);
						if (!utils.guarda_foto_en_memoria_interna(inputData, file.getPath())){
							Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
						}else{
							Intent mIntent = new Intent(getContext(), Activity_Mi_Perfil.class);
							getContext().startActivity(mIntent);
						}

						break;
					case "VERY_LIKELY":
					case "LIKELY":
					case "POSSIBLE":
						//es obscena seguro, ni si quiera hace falta revisar
						resultado=false;
						break;
				}
			}catch (SocketTimeoutException | UnknownHostException | SSLException | ConnectException e ) {
				Toast.makeText(getContext(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
			}catch (Exception e) {
				utils.registra_error(e.toString(), "SafeDetectionTask (onPostExecute) en fragment_foto_manager");
			}
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
		private void subir_foto_a_FCS(Uri Uri_de_la_foto,int tamanyo_foto_perfiles){
			try {
				int num_foto = num_foto_actual;

				String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");

				FirebaseStorage storage = FirebaseStorage.getInstance();
				StorageReference mStorageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket));

				Bitmap bitmap_foto = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri_de_la_foto);
				StorageReference fotoRef = mStorageRef.child(utils.get_path_foto(token_socialauth, num_foto));
				fotoRef.putBytes(BitmapToBytes(ThumbnailUtils.extractThumbnail(bitmap_foto, 520, 520)));

				Bitmap bitmap_thumb = ThumbnailUtils.extractThumbnail(bitmap_foto, tamanyo_foto_perfiles, tamanyo_foto_perfiles);
				StorageReference thumbRef = mStorageRef.child(utils.get_path_thumb(token_socialauth, num_foto));
				thumbRef.putBytes(BitmapToBytes(bitmap_thumb));
			}catch (Exception e){
				utils.registra_error(e.toString(), "subir_foto_a_FCS en fragment_foto_manager");
			}
		}
		protected void onPostExecute(Boolean resultado) {
			binding.mProgressBar.setVisibility(View.INVISIBLE);
			if (isAdded() && !resultado) {
				Toast.makeText(getContext(), getResources().getString(R.string.foto_inapropiada), Toast.LENGTH_LONG).show();
			}
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

	private void borrar_foto_de_FCS(String token_socialauth, int num_foto) {
		try {
			FirebaseStorage storage = FirebaseStorage.getInstance();
			StorageReference mStorageRef = storage.getReferenceFromUrl(getResources().getString(R.string.my_bucket));

			// Create a reference to the file to delete
			StorageReference fotoRef = mStorageRef.child(utils.get_path_foto(token_socialauth, num_foto));
			StorageReference thumbRef = mStorageRef.child(utils.get_path_thumb(token_socialauth, num_foto));

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
		}catch (RejectedExecutionException e){
			try {
				Thread.sleep(500);
			}catch (Exception ignored){}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "borrar_foto_de_GCS de fragment_foto_manager");
		}
	}

	private class AsyncTask_coloca_Thumb_de_memoria_interna extends AsyncTask<Pair<File, Integer>, Void, Pair<Bitmap, Integer>> {

		@Override
		protected void onPreExecute() {
		}

		@Nullable
		@Override
		protected Pair<Bitmap, Integer> doInBackground(Pair... params) {
			Bitmap un_bitmap = null;

			try {
				File file = (File) params[0].first;
				un_bitmap = utils.decodeSampledBitmapFromFilePath(file.getPath(), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
			} catch (Exception ignored) {
			}
			return new Pair<>(un_bitmap, (Integer) params[0].second);
		}

		@Override
		protected void onPostExecute(@Nullable Pair mi_par) {
			try {
				if (getContext() != null && isAdded()) {
					Bitmap bitmap = (Bitmap) mi_par.first;
					Integer num_foto = (Integer) mi_par.second;
					AppCompatImageView mAppCompatImageView = new AppCompatImageView(getContext());
					mAppCompatImageView.setImageBitmap(bitmap);

					mAppCompatImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

					mImageAdapter.setView(num_foto, mAppCompatImageView);
					mImageAdapter.notifyDataSetChanged();
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "AsyncTask_coloca_Thumb_de_memoria_interna (onPostExecute) en fragment_foto_manager");
			}
		}

	}
}