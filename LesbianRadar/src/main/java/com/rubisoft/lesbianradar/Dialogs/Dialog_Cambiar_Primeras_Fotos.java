package com.rubisoft.lesbianradar.Dialogs;

import android.Manifest.permission;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.lesbianradar.BuildConfig;
import com.rubisoft.lesbianradar.R;
import com.rubisoft.lesbianradar.tools.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

public class Dialog_Cambiar_Primeras_Fotos extends DialogFragment {
    private static final int REQUEST_LOAD_IMAGE = 1;
    private static final int REQUEST_CAPTURE_IMAGE = 2;
    private static final int REQUEST_BORRAR_IMAGE = 3;
    private static final String TAG_NUM_FOTO = "num_foto";
    private static final int RESULT_OK = -1;
    private static final int PERMISSION_CAPTURAR_FOTO = 1;
    private static final int PERMISSION_SELECCIONAR_FOTO = 2;
    private static final int PERMISSION_BORRAR_FOTO = 3;
    private SharedPreferences perfil_usuario;
	private  Context mContext;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.dialogo_cambiar_primeras_fotos, container);
        try {
            //si es una tableta hacemos la ventana mas grande
            if (utils.isTablet(getContext())) {
                getDialog().getWindow().setLayout(600, 300);
            }
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            perfil_usuario = getActivity().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

            Typeface mTypeFace_roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

            TextView mTextView_Title = view.findViewById(R.id.Dialogo_cambiar_foto_title);
            mTextView_Title.setText(getResources().getString(R.string.change_pic));
            mTextView_Title.setTypeface(mTypeFace_roboto_light);
			mContext=getContext();
            // Set listener, view, data for your dialog fragment
            Drawable icono_cambiar_foto = new IconicsDrawable(getContext()).icon(Icon.gmd_view_module).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_hacer_foto = new IconicsDrawable(getContext()).icon(Icon.gmd_photo_camera).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_borrar_foto = new IconicsDrawable(getContext()).icon(Icon.gmd_delete).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));

            AppCompatImageView Button_cambiar_foto = view.findViewById(R.id.Dialogo_cambiar_foto_Button_cambiar_foto);
            Button_cambiar_foto.setImageDrawable(icono_cambiar_foto);
            Button_cambiar_foto.setOnClickListener(view13 -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
							(ContextCompat.checkSelfPermission(getActivity(), permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE}, PERMISSION_SELECCIONAR_FOTO);
					} else {
						lanza_intent_seleccionar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_cambiar_foto) de Dialog_Cambiar_Primeras_Fotos");
				}
			});
            AppCompatImageView Button_capturar_foto = view.findViewById(R.id.Dialogo_cambiar_foto_Button_hacer_foto);
            Button_capturar_foto.setImageDrawable(icono_hacer_foto);
            Button_capturar_foto.setOnClickListener(view12 -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
							(ContextCompat.checkSelfPermission(getActivity(), permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ) {
						requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAPTURAR_FOTO);
					} else {
						lanza_intent_capturar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_capturar_foto) de Dialog_Cambiar_Primeras_Fotos");
				}
			});
            AppCompatImageView Button_borrar_foto = view.findViewById(R.id.Dialogo_cambiar_foto_Button_borrar_foto);
            Button_borrar_foto.setImageDrawable(icono_borrar_foto);
            Button_borrar_foto.setOnClickListener(view1 -> {
				try {
					//Tenemos que pedir permiso explicitamente si estamos en un dispositivo Marshmallow
					if ((ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
							(ContextCompat.checkSelfPermission(getActivity(), permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
						requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE, permission.READ_EXTERNAL_STORAGE}, PERMISSION_BORRAR_FOTO);
					} else {
						procesa_borrar_foto();
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_borrar_foto) de Dialog_Cambiar_Primeras_Fotos");
				}
			});
        }catch (Exception e){
            utils.registra_error(e.toString(), "onCreateView de Dialog_Cambiar_Primeras_Fotos");

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
			int num_foto = getArguments().getInt(TAG_NUM_FOTO);
			String token_socialauth = perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
			if (resultCode == RESULT_OK) {
				if ((requestCode == REQUEST_LOAD_IMAGE) && (data != null)) {
					//String nombre_thumb = utils.get_nombre_thumb(token_socialauth, num_foto);

					//File file_src = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), nombre_thumb);
					//file_src.createNewFile();
					//new AsyncTask_procesa_imagen_seleccionada().execute(new Pair<>(data,file_src));

					procesa_imagen_seleccionada(data);
				} else if (requestCode == REQUEST_CAPTURE_IMAGE) {
					procesa_imagen_capturada(token_socialauth, num_foto);
				}
				Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_WAIT), Toast.LENGTH_LONG).show();

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
        } catch (Exception e) {
            utils.registra_error(e.toString(), "borrar_foto_de_FCS  de Dialog_Cambiar_Primeras_Fotos");
        }
    }

    private void lanza_intent_seleccionar_foto() {
        try {
            if (getDialog() != null) {
                //Intent mIntent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);

               // startActivityForResult(mIntent, REQUEST_LOAD_IMAGE);
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_LOAD_IMAGE);
            }
        }catch (ActivityNotFoundException ignored) {
        } catch (Exception e) {
            utils.registra_error(e.toString(), "lanza_intent_seleccionar_foto de Dialog_Cambiar_Primeras_Fotos");
        }
    }

    private void lanza_intent_capturar_foto() {
        try {
            if (getDialog() != null) {
                // create Intent to take a picture and return control to the calling application
                Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (mIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), getArguments().getInt(TAG_NUM_FOTO));

                    File file = new File(storageDir, nombre_thumb);

                    mIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file));
                    startActivityForResult(mIntent, REQUEST_CAPTURE_IMAGE);
                }
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "lanza_intenet_capturar_foto de Dialog_Cambiar_Primeras_Fotos");

        }
    }

    private void procesa_borrar_foto() {
        try {
            String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), getArguments().getInt(TAG_NUM_FOTO));

            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File file = new File(storageDir, nombre_thumb);
            if (file.exists()) {
                borra_foto_de_memoria_interna(nombre_thumb);
            }
			borrar_foto_de_FCS(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), getArguments().getInt(TAG_NUM_FOTO));

            getActivity().startActivityForResult(getActivity().getIntent(), REQUEST_BORRAR_IMAGE);
            dismissAllowingStateLoss();
        } catch (Exception e) {
            utils.registra_error(e.toString(), "procesa_borrar_foto de Dialog_Cambiar_Primeras_Fotos");
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
            utils.registra_error(e.toString(), "borra_foto_de_memoria_interna de Dialog_Cambiar_Primeras_Fotos");
        }
    }

    private void procesa_imagen_capturada(String token_socialauth, int num_foto) {
        try {
            //llegados a este punto hay una foto recien hecha con el nombre correcto en el directorio
            //donde se guardan las fotos. Lo unico que queda es subirla al GCS si no es muy pesada
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String nombre_thumb = utils.get_nombre_thumb(token_socialauth, num_foto);

            File file_src = new File(storageDir, nombre_thumb);

            if (file_src.length() < getResources().getInteger(R.integer.MAX_PHOTO_LENGTH)) {
                if (file_src.getPath() != null) {
                    new AsyncTask_sube_foto_a_FCS().execute(new Pair<>(FileToBytes(file_src.getPath()), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles)));
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_NO_CARGADA), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.FRAGMENT_FOTO_ERROR_FOTO_MUY_PESADA), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            utils.registra_error(e.toString(), "procesa_imagen_capturada de Dialog_Cambiar_Primeras_Fotos");
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

	private void procesa_imagen_seleccionada(Intent data) {
		try {
			//llegados a este punto hay una foto recien hecha con el nombre correcto en el directorio
			//donde se guardan las fotos. Lo unico que queda es subirla al GCS si no es muy pesada
			Uri uri  = data.getData();

			InputStream iStream =   mContext.getContentResolver().openInputStream(uri);
			byte[] byteArray = getBytes(iStream);

			new AsyncTask_sube_foto_a_FCS().execute(new Pair<>(byteArray, getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles)));
			Thread.sleep(1500);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "procesa_imagen_capturada de Dialog_Cambiar_Foto");
		}
	}

	private class AsyncTask_sube_foto_a_FCS extends AsyncTask<Pair<byte[], Integer>, Void, Void> {
        private byte[] BitmapToBytes(Bitmap bitmap) {

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

        @SafeVarargs
        @Override
        protected final Void doInBackground(Pair<byte[], Integer>... params) {
            try {
                byte[] foto_en_bytes = params[0].first;
                int num_foto = getArguments().getInt(TAG_NUM_FOTO);
                int tamanyo_foto_perfiles = params[0].second; //puede que ya se haya desadjuntado de la acitivy
                String token_socialauth = perfil_usuario.getString(mContext.getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference mStorageRef = storage.getReferenceFromUrl(mContext.getResources().getString(R.string.my_bucket));

                Bitmap bitmap_foto = BitmapFactory.decodeByteArray(foto_en_bytes, 0, foto_en_bytes.length);
                StorageReference fotoRef = mStorageRef.child(utils.get_path_foto(token_socialauth, num_foto));
                UploadTask upload_foto_Task = fotoRef.putBytes(BitmapToBytes(ThumbnailUtils.extractThumbnail(bitmap_foto, 520, 520)));
                upload_foto_Task.addOnFailureListener(exception -> {
					// Handle unsuccessful uploads
				}).addOnSuccessListener(taskSnapshot -> {
				});

                Bitmap bitmap_thumb = ThumbnailUtils.extractThumbnail(bitmap_foto, tamanyo_foto_perfiles, tamanyo_foto_perfiles);
                StorageReference thumbRef = mStorageRef.child(utils.get_path_thumb(token_socialauth, num_foto));
                UploadTask upload_thumb_Task = thumbRef.putBytes(BitmapToBytes(bitmap_thumb));
                upload_thumb_Task.addOnFailureListener(e -> {
				}).addOnSuccessListener(taskSnapshot -> {
				});


                /*para comprobar que no hay fotos inapropiadas hacemos una copia en una carpeta a parte para revisarlas posteriormente */
                String path_aux ="para_revisar/" + token_socialauth + "_thumb" + num_foto + ".jpg";
                StorageReference thumbRef_aux = mStorageRef.child(path_aux);
                UploadTask upload_thumb_Task_aux = thumbRef_aux.putBytes(BitmapToBytes(bitmap_thumb));
                upload_thumb_Task_aux.addOnFailureListener(e -> {
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
}