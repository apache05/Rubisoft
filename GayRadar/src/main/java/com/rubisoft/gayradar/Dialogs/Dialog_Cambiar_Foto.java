package com.rubisoft.gayradar.Dialogs;

import android.Manifest.permission;
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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.gayradar.BuildConfig;
import com.rubisoft.gayradar.R;
import com.rubisoft.gayradar.activities.Activity_Mi_Perfil;
import com.rubisoft.gayradar.tools.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

public class Dialog_Cambiar_Foto extends DialogFragment {
	private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
	private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int REQUEST_LOAD_IMAGE = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 2;
    private static final int REQUEST_PRIVATE_IMAGE = 4;
    private static final String TAG_NUM_FOTO = "num_foto";
    private static final String TAG_CON_FOTO = "con_foto";
    private static final int RESULT_OK = -1;
    private static final int PERMISSION_CAPTURAR_FOTO = 1;
    private static final int PERMISSION_SELECCIONAR_FOTO = 2;
    private static final int PERMISSION_BORRAR_FOTO = 3;
	private static final int MAX_LABEL_RESULTS = 10;

	private SharedPreferences perfil_usuario;
	private  Context mContext;

	
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            this.onCreate(savedInstanceState);

            View view = inflater.inflate(R.layout.dialogo_cambiar_foto, container);
        try {


            //si es una tableta hacemos la ventana mas grande
            if (utils.isTablet(getContext())) {
                getDialog().getWindow().setLayout(600, 300);
            }
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            perfil_usuario = getActivity().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

			Bundle args = getArguments();
            Typeface mTypeFace_roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

            TextView mTextView_Title = view.findViewById(R.id.Dialogo_cambiar_foto_title);
            mTextView_Title.setText(getResources().getString(R.string.change_pic));
            mTextView_Title.setTypeface(mTypeFace_roboto_light);
			mContext=getContext();

			// Set listener, view, data for your dialog fragment
            Drawable icono_cambiar_foto = new IconicsDrawable(getContext()).icon(Icon.gmd_view_module).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_hacer_foto = new IconicsDrawable(getContext()).icon(Icon.gmd_photo_camera).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_borrar_foto = new IconicsDrawable(getContext()).icon(Icon.gmd_delete).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_hacer_foto_privada_activada = new IconicsDrawable(getContext()).icon(Icon.gmd_vpn_key).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_hacer_foto_privada_desactivada = new IconicsDrawable(getContext()).icon(Icon.gmd_vpn_key).color(ContextCompat.getColor(getContext(), R.color.gris_transparente)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));

            AppCompatImageView Button_cambiar_foto = view.findViewById(R.id.Dialogo_cambiar_foto_Button_cambiar_foto);
            Button_cambiar_foto.setImageDrawable(icono_cambiar_foto);
            Button_cambiar_foto.setOnClickListener(view13 -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getActivity(), permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE}, PERMISSION_SELECCIONAR_FOTO);
					} else {
						lanza_intent_seleccionar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_cambiar_foto) de Dialog_Cambiar_Foto");
				}
			});
            AppCompatImageView Button_capturar_foto = view.findViewById(R.id.Dialogo_cambiar_foto_Button_hacer_foto);
            Button_capturar_foto.setImageDrawable(icono_hacer_foto);
            Button_capturar_foto.setOnClickListener(view12 -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getActivity(), permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAPTURAR_FOTO);
					} else {
						lanza_intent_capturar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_capturar_foto) de Dialog_Cambiar_Foto");
				}
			});
            AppCompatImageView Button_borrar_foto = view.findViewById(R.id.Dialogo_cambiar_foto_Button_borrar_foto);
            Button_borrar_foto.setImageDrawable(icono_borrar_foto);
            Button_borrar_foto.setOnClickListener(view1 -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
							(ContextCompat.checkSelfPermission(getActivity(), permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE}, PERMISSION_BORRAR_FOTO);
					} else {
						procesa_borrar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_borrar_foto) de Dialog_Cambiar_Foto");
				}
			});
            AppCompatImageView Button_hacer_foto_privada = view.findViewById(R.id.Dialogo_cambiar_foto_Button_hacer_foto_privada);

          /*  if (boton_llave_activado()) {
                Button_hacer_foto_privada.setImageDrawable(icono_hacer_foto_privada_activada);
                Button_hacer_foto_privada.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            boolean estado_actual = perfil_usuario.getBoolean(sharedpreference_foto_privada(args.getInt(TAG_NUM_FOTO, 0)), false);
                            SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
                            editor_perfil_usuario.putBoolean(sharedpreference_foto_privada(args.getInt(TAG_NUM_FOTO, 0)), !estado_actual);
                            editor_perfil_usuario.apply();
							actualiza_foto_privada(crea_Bean_Perfil_modificado(args.getInt(TAG_NUM_FOTO, 0), estado_actual));
                        } catch (Exception e) {
                            utils.registra_error(e.toString(), "onCreateView (Button_hacer_foto_privada) de Dialog_Cambiar_Foto");

                        } finally {
                            startActivityForResult(getActivity().getIntent(), REQUEST_PRIVATE_IMAGE);
                        }
                    }
                });
            }
            else {
                Button_hacer_foto_privada.setImageDrawable(icono_hacer_foto_privada_desactivada);

                if (args.getInt(TAG_NUM_FOTO, 0) != 0) {
                    Button_hacer_foto_privada.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try{
								if (getArguments().getBoolean(TAG_CON_FOTO, false)) {
									Toast.makeText(getContext(), getResources().getString(R.string.SOLO_PREMIUM), Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(getContext(), getResources().getString(R.string.FRAGMENT_FOTO_SIN_FOTO), Toast.LENGTH_LONG).show();
								}
                            startActivityForResult(getActivity().getIntent(), REQUEST_PRIVATE_IMAGE);
                            } catch (Exception e) {
                                utils.registra_error(e.toString(), "onCreateView (Button_hacer_foto_privada) de Dialog_Cambiar_Foto");

                            }
                        }
                    });
                }
                //HABRIA QUE PONER QUE LA PRIMERA NO PUEDE SER PRIVADA
            }*/
        }catch (Exception e){
            utils.registra_error(e.toString(), "onCreateView de Dialog_Cambiar_Foto");
        }
        return view;
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
                    lanza_intent_capturar_foto();
                }
                break;
            case PERMISSION_BORRAR_FOTO:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Si nos da permiso, continuamos
                    procesa_borrar_foto();
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
					uploadImage(data.getData());

				} else if (requestCode == REQUEST_CAPTURE_IMAGE) {
					int num_foto = getArguments().getInt(TAG_NUM_FOTO);
					String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
					String nombre_thumb = utils.get_nombre_thumb(token_socialauth, num_foto);

					File file_src = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombre_thumb);

					uploadImage(Uri.fromFile(file_src));
                }

			} else {
                Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
            }
		}catch (Exception e) {
            utils.registra_error(e.toString(), "onActivityResult de Dialog_Cambiar_Foto");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

/*
	private Usuario crea_Bean_Perfil_modificado(int num_foto, boolean estado_actual) {
		Usuario un_usuario = new Usuario();

        try {
			Map<String,Boolean> array_booleanos =new  HashMap<>();

            for (int i = 0; i < getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM); i++) {
                //tenemos que dejar el array tal como está en sharedpreferences excepto en la foto que estamos modificando
                if (i == num_foto) {
					array_booleanos.put(Integer.toString(i),estado_actual);
                } else {
					array_booleanos.put(Integer.toString(i),perfil_usuario.getBoolean(sharedpreference_foto_privada(i), false));
                }
            }
			//un_usuario.setToken_socialauth(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));
			//un_usuario.setFotos_privadas(array_booleanos);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "crea_Bean_Perfil_modificado de Dialog_Cambiar_Foto");
        }
        return un_usuario;
    }
*/


/*
    private Usuario crea_Bean_Perfil_despues_de_borrado(int num_foto) {
		Usuario un_usuario = new Usuario();
        try {
			Map<String,Boolean> array_booleanos =new  HashMap<>();

            for (int i = 0; i < getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM); i++) {
                //tenemos que dejar el array tal como está en sharedpreferences excepto en la foto que estamos modificando
                if (i == num_foto) {
					array_booleanos.put(Integer.toString(i),false);
                } else {
                    array_booleanos.put(Integer.toString(i),perfil_usuario.getBoolean(sharedpreference_foto_privada(i), false));
                }
            }
			//un_usuario.setToken_socialauth(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));
			//un_usuario.setFotos_privadas(array_booleanos);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "Cre_Bean_Perfil_Despues_de_borrado de Dialog_Cambiar_Foto");
        }
        return un_usuario;
    }
*/

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
            utils.registra_error(e.toString(), "borrar_foto_de_GCS de Dialog_Cambiar_Foto");
        }
    }

    private void lanza_intent_seleccionar_foto() {
        try {
            if (getDialog() != null) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select a photo"),REQUEST_LOAD_IMAGE);
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "lanza_intent_seleccionar_foto de Dialog_Cambiar_Foto");
        }
    }

    private void lanza_intent_capturar_foto() {
        try {
			if (getDialog() != null) {
				// create Intent to take a picture and return control to the calling application
				Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (mIntent.resolveActivity(getActivity().getPackageManager()) != null) {
					String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), getArguments().getInt(TAG_NUM_FOTO));

					File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombre_thumb);

					mIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file));
					startActivityForResult(mIntent, REQUEST_CAPTURE_IMAGE);
				}
			}
		}catch (IllegalArgumentException e){
			Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
            utils.registra_error(e.toString(), "lanza_intent_capturar_foto de Dialog_Cambiar_Foto");
        }
    }

    private void procesa_borrar_foto() {
        try {
            //hacer_foto_no_privada();
            String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), getArguments().getInt(TAG_NUM_FOTO));

            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File file = new File(storageDir, nombre_thumb);
            if (file.exists()) {
                borra_foto_de_memoria_interna(nombre_thumb);
				/*actualiza_foto_privada(crea_Bean_Perfil_despues_de_borrado(getArguments().getInt(TAG_NUM_FOTO)));
                SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
                editor_perfil_usuario.putBoolean(sharedpreference_foto_privada(args.getInt(TAG_NUM_FOTO, 0)), false);
                editor_perfil_usuario.apply();*/
            }
			borrar_foto_de_FCS(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), getArguments().getInt(TAG_NUM_FOTO));


			Intent mIntent = new Intent(getContext(), Activity_Mi_Perfil.class);
            getContext().startActivity(mIntent);
            dismissAllowingStateLoss();
        } catch (Exception e) {
            utils.registra_error(e.toString(), "procesa_borrar_foto de Dialog_Cambiar_Foto");
        }
    }

/*
    private void hacer_foto_no_privada() {
        SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
        editor_perfil_usuario.putBoolean(sharedpreference_foto_privada(args.getInt(TAG_NUM_FOTO, 0)), false);
        editor_perfil_usuario.apply();
		actualiza_foto_privada(crea_Bean_Perfil_despues_de_borrado(args.getInt(TAG_NUM_FOTO, 0)));
        //new AsyncTask_actualiza_foto_privada().execute();
    }
*/

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
            utils.registra_error(e.toString(), "borra_foto_de_memoria_interna de Dialog_Cambiar_Foto");
        }
    }
    
	private void uploadImage(Uri uri) {
		if (uri != null) {
			try {
				Bitmap bitmap =	MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);

				if (bitmap!= null) {
					Vision.Images.Annotate un_Annotate = prepareAnnotationRequest(bitmap);
					new SafeDetectionTask().execute(new Pair<>(uri,un_Annotate));
				}else{
					Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
				}
			} catch (FileNotFoundException e) {
				Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA)+ " "+ e, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				utils.registra_error(e.toString(), "procesa_imagen_capturada de Dialog_Cambiar_Foto");
			}
		} else {
			Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
		}
	}

/*
	private String sharedpreference_foto_privada(int num_foto) {
        String resultado = "";
        switch (num_foto) {
            case 0:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_0);
                break;
            case 1:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_1);
                break;
            case 2:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_2);
                break;
            case 3:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_3);
                break;
            case 4:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_4);
                break;
            case 5:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_5);
                break;
            case 6:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_6);
                break;
            case 7:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_7);
                break;
            case 8:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_8);
                break;
            case 9:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_9);
                break;
            case 10:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_10);
                break;
            case 11:
                resultado = getResources().getString(R.string.PERFIL_USUARIO_FOTO_PRIVADA_11);
                break;

        }
        return resultado;
    }
*/

    private boolean boton_llave_activado() {
        boolean con_llave;

        con_llave = perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false) && getArguments().getBoolean(TAG_CON_FOTO, false);
        return con_llave;
    }
	
    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) {
		Vision.Images.Annotate annotateRequest=null;
    	try {
			HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
			JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

			VisionRequestInitializer requestInitializer =
					new VisionRequestInitializer(mContext.getResources().getString(R.string.CLOUD_VISION_API_KEY)) {
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

							String sig = getSignature(mContext.getPackageManager(), packageName);

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

			 annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
			// Due to a bug: requests to Vision API containing large images fail when GZipped.
			annotateRequest.setDisableGZipContent(true);
		}catch (Exception e){
			utils.registra_error(e.toString(), "prepareAnnotationRequest de Dialog_Cambiar_Foto");

		}
		return annotateRequest;
	}

	private  class SafeDetectionTask extends AsyncTask< Pair<Uri,Vision.Images.Annotate>, Void, Boolean> {

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
						subir_foto_a_FCS(uri,mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
						break;
					case " VERY_LIKELY":
					case " LIKELY":
						//es obscena
						resultado=false;
						break;
					default:
						//es dudosa. La revisamos
						subir_foto_para_revisar(uri, mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
						break;
				}
			}catch (Exception ignored) {
			}
			return resultado;
		}
		private byte[] BitmapToBytes(Bitmap bitmap) {

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
			} catch (Exception e) {
				utils.registra_error(e.toString(), "BitmapToBytes de Dialog_Cambiar_Foto");

			}
			return byteArrayOutputStream.toByteArray();
		}
		private void subir_foto_para_revisar(Uri Uri_de_la_foto,int tamanyo_foto_perfiles){
			try {
				int num_foto = getArguments().getInt(TAG_NUM_FOTO);
				String token_socialauth = perfil_usuario.getString(mContext.getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");

				FirebaseStorage storage = FirebaseStorage.getInstance();
				StorageReference mStorageRef = storage.getReferenceFromUrl(mContext.getResources().getString(R.string.my_bucket));

				Bitmap bitmap_foto = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri_de_la_foto);
				Bitmap bitmap_thumb = ThumbnailUtils.extractThumbnail(bitmap_foto, tamanyo_foto_perfiles, tamanyo_foto_perfiles);

				/*para comprobar que no hay fotos inapropiadas hacemos una copia en una carpeta a parte para revisarlas posteriormente */
				String path_aux = "para_revisar/" + token_socialauth + "_thumb" + num_foto + ".jpg";
				StorageReference thumbRef_aux = mStorageRef.child(path_aux);
				UploadTask upload_thumb_Task_aux = thumbRef_aux.putBytes(BitmapToBytes(bitmap_thumb));
				upload_thumb_Task_aux.addOnFailureListener(e -> {
				}).addOnSuccessListener(taskSnapshot -> {
				});
			}catch (Exception ignored){}
		}
		private void subir_foto_a_FCS(Uri Uri_de_la_foto,int tamanyo_foto_perfiles){
			try {
				int num_foto = getArguments().getInt(TAG_NUM_FOTO);

				String token_socialauth = perfil_usuario.getString(mContext.getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");

				FirebaseStorage storage = FirebaseStorage.getInstance();
				StorageReference mStorageRef = storage.getReferenceFromUrl(mContext.getResources().getString(R.string.my_bucket));

				Bitmap bitmap_foto = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri_de_la_foto);
				StorageReference fotoRef = mStorageRef.child(utils.get_path_foto(token_socialauth, num_foto));
				UploadTask upload_foto_Task = fotoRef.putBytes(BitmapToBytes(ThumbnailUtils.extractThumbnail(bitmap_foto, 520, 520)));
				upload_foto_Task.addOnFailureListener(ignored -> {
				}).addOnSuccessListener(taskSnapshot -> {

				});

				Bitmap bitmap_thumb = ThumbnailUtils.extractThumbnail(bitmap_foto, tamanyo_foto_perfiles, tamanyo_foto_perfiles);
				StorageReference thumbRef = mStorageRef.child(utils.get_path_thumb(token_socialauth, num_foto));
				UploadTask upload_thumb_Task = thumbRef.putBytes(BitmapToBytes(bitmap_thumb));
				upload_thumb_Task.addOnFailureListener(ignored -> {
				}).addOnSuccessListener(taskSnapshot -> {
				});
			}catch (Exception ignored){
			}
		}
		protected void onPostExecute(Boolean resultado) {
			if (!resultado) {
				Toast.makeText(getContext(), "This photo is considered inappropriate", Toast.LENGTH_LONG).show();
			}
			dismiss();
		}
	}

/*
	private void actualiza_foto_privada(Usuario un_usuario){
		String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		DocumentReference ref = db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth);
		for (int i=0; i<getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM);i++){
			ref.update(getResources().getString(R.string.PERFIL_USUARIO_FOTOS_PRIVADAS), un_usuario.getFotos_privadas());
		}
	}
*/
/*
    private static class AsyncTask_actualiza_foto_privada extends AsyncTask<BeanPerfil, Void, Exception> {
        // ************************************************************************************************
        // Esta función llama al endpoint para subir una foto al GCS
        // ************************************************************************************************
        private EndpointPerfilApi mEndpointPerfilApi;

        @Nullable
        @Override
        protected Exception doInBackground(BeanPerfil... params) {
            Exception e = null;
            try {
                EndpointPerfilApi.Builder builder1 = new EndpointPerfilApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null).setRootUrl("https://metal-hologram-101314.appspot.com/_ah/api/");
                mEndpointPerfilApi = builder1.build();
                mEndpointPerfilApi.actualizarPerfil(params[0]).execute();
            }catch (SocketTimeoutException | SSLHandshakeException ignored) {
            } catch (Exception una_exception) {
                e = una_exception;
            }
            return e;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e != null) {
                utils.registra_error(e.toString(), "AsyncTask_actualiza_foto_privada de Dialog_cambiar_foto");
            }
        }
    }
*/

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
}