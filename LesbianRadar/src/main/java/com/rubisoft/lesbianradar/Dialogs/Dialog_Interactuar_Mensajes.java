package com.rubisoft.lesbianradar.Dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.lesbianradar.Classes.Denuncia;
import com.rubisoft.lesbianradar.R;
import com.rubisoft.lesbianradar.activities.Activity_Chat_Individual;
import com.rubisoft.lesbianradar.activities.Activity_Mensajes;
import com.rubisoft.lesbianradar.activities.Activity_Un_Perfil;
import com.rubisoft.lesbianradar.tools.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

public class Dialog_Interactuar_Mensajes extends DialogFragment {
	private static final String TAG_INFO = "info";
	private int seleccion=-1;

	private Bundle args;
	private String token_socialauth_de_la_otra_persona;
	private String token_socialauth_mio;
	private String id_relacion;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.onCreate(savedInstanceState);

		View view = inflater.inflate(R.layout.dialogo_interactuar_mensajes, container);
		try {
			//si es una tableta hacemos la ventana mas grande
			if (utils.isTablet(getContext())) {
				getDialog().getWindow().setLayout(600, 300);
			}
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			SharedPreferences perfil_usuario = getActivity().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

			args = getArguments();
			token_socialauth_de_la_otra_persona = args.getString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA));
			token_socialauth_mio= perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
			id_relacion = args.getString(getResources().getString(R.string.RELACIONES_ID_RELACION));
			Typeface mTypeFace_roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

			TextView mTextView_Title = view.findViewById(R.id.Dialogo_interactuar_title);
			mTextView_Title.setText(getResources().getString(R.string.DIALOGO_INTERACTUAR_TITULO));
			mTextView_Title.setTypeface(mTypeFace_roboto_light);

			// Set listener, view, data for your dialog fragment
			Drawable icono_mandar_mensaje = new IconicsDrawable(getContext()).icon(Icon.gmd_comment).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_ver_fotos = new IconicsDrawable(getContext()).icon(Icon.gmd_recent_actors).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_info = new IconicsDrawable(getContext()).icon(Icon.gmd_info).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_borrar = new IconicsDrawable(getContext()).icon(Icon.gmd_close).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
			Drawable icono_denunciar = new IconicsDrawable(getContext()).icon(Icon.gmd_remove_circle).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));

			AppCompatImageView Button_ver_fotos = view.findViewById(R.id.Dialogo_interactuar_Button_ver_fotos);
			Button_ver_fotos.setImageDrawable(icono_ver_fotos);
			Button_ver_fotos.setOnClickListener(view16 -> {
				try {
					Intent mIntent = new Intent(getContext(), Activity_Un_Perfil.class);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Bundle mBundle = new Bundle();
					mBundle.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), token_socialauth_de_la_otra_persona);
					mBundle.putInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO), getResources().getInteger(R.integer.VENGO_DE_MENSAJES));
					mBundle.putInt(getResources().getString(R.string.PAGINA), args.getInt(getResources().getString(R.string.PAGINA)));
					mIntent.putExtras(mBundle);

					startActivity(mIntent);
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_ver_fotos) lanza_dialogo_info de Dialog_Interactuar_Mensajes");
				}
			});
			AppCompatImageView Button_escribir_mensaje = view.findViewById(R.id.Dialogo_interactuar_Button_escribir_mensaje);
			Button_escribir_mensaje.setImageDrawable(icono_mandar_mensaje);
			Button_escribir_mensaje.setOnClickListener(view15 -> {
				try {
					marca_mensajes_como_leido(id_relacion,token_socialauth_mio);
					Intent mIntent = new Intent(getContext(), Activity_Chat_Individual.class);
					Bundle bundle = new Bundle();
					bundle.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), token_socialauth_de_la_otra_persona);
					bundle.putString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA), args.getString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA)));
					bundle.putInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO), getResources().getInteger(R.integer.VENGO_DE_MENSAJES));

					mIntent.putExtras(bundle);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					startActivity(mIntent);
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_escribir_mensaje) lanza_dialogo_info de Dialog_Interactuar_Mensajes");
				}
			});
			AppCompatImageView Button_info = view.findViewById(R.id.Dialogo_interactuar_Button_info);
			Button_info.setImageDrawable(icono_info);
			Button_info.setOnClickListener(view14 -> {
				try {
					lanza_dialogo_info();
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_info) lanza_dialogo_info de Dialog_Interactuar_Mensajes");
				}
			});
			AppCompatImageView Button_borrar = view.findViewById(R.id.Dialogo_interactuar_Button_borrar_conversacion);
			Button_borrar.setImageDrawable(icono_borrar);
			Button_borrar.setOnClickListener(view13 -> {
				try {
					borrar_relacion();
					ir_a_mensajes();
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_borrar) lanza_dialogo_info de Dialog_Interactuar_Mensajes");
				}
			});
			AppCompatImageView Button_denunciar = view.findViewById(R.id.Dialogo_interactuar_Button_bloquear);
			Button_denunciar.setImageDrawable(icono_denunciar);
			Button_denunciar.setOnClickListener(view12 -> {
				try {
					new MaterialDialog.Builder(getActivity())
							.theme(Theme.LIGHT)
							.typeface("Roboto-Bold.ttf", "Roboto-Regular.ttf")
							.title(getResources().getString(R.string.DIALOGO_DENUNCIAR_PREGUNTA_MOTIVO))
							.items(R.array.causas_deuncia)
							.itemsCallbackSingleChoice(-1, (dialog, view1, which, text) -> {
								seleccion = which;
								denunciar_usuario();
								Toast.makeText(getContext(), getResources().getString(R.string.DIALOGO_DENUNCIAR_DENUNCIADO), Toast.LENGTH_LONG).show();

								dialog.dismiss();
								return true;
							})
							.positiveText(R.string.DIALOGO_DENUNCIAR_DENUNCIAR)
							.onPositive((dialog, which) -> {

							})
							.negativeText(R.string.Cancelar)
							.onNegative((dialog, which) -> dialog.dismiss())
							.show();
					dismiss();
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_denunciar) lanza_dialogo_info de Dialog_Interactuar_Mensajes");
				}
			});
		}catch (Exception e){
			utils.registra_error(e.toString(), "onCreateView lanza_dialogo_info de Dialog_Interactuar_Mensajes");
		}
		return view;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		//No call for super(). Bug on API Level > 11.
	}

	private void marca_mensajes_como_leido(String id_relacion,String para_quien){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getString(R.string.CHAT))
				.whereEqualTo("id_relacion",id_relacion)
				.whereEqualTo("para_quien",para_quien)
				.whereEqualTo("leido",false)
				.get()
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						for (QueryDocumentSnapshot document : task.getResult()) {
							db.collection(getString(R.string.CHAT)).document(document.getId()).update("leido",true);
						}
					}
				});
	}

	private void lanza_dialogo_info() {
		try {
			Dialog_info mDialog_Info = new Dialog_info();
			Bundle args = new Bundle();

			args.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA),token_socialauth_de_la_otra_persona);
			mDialog_Info.setArguments(args);

			FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			ft.add(mDialog_Info, TAG_INFO);
			ft.commitAllowingStateLoss();
		} catch (Exception e) {
			utils.registra_error(e.toString(), "lanza_dialogo_info de Dialog_Interactuar_Mensajes");
		}
	}

	private void denunciar_usuario(){
		Denuncia una_denuncia= new Denuncia();
		una_denuncia.setCausa(utils.decodifica_causa_denuncia( seleccion));
		una_denuncia.setToken_socialauth_denunciado(token_socialauth_de_la_otra_persona);
		una_denuncia.setToken_socialauth_denunciante(token_socialauth_mio);

		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.DENUNCIAS)).add(una_denuncia);
		termina_relacion();
	}

	private void termina_relacion(){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_mio)
				.collection(getResources().getString(R.string.RELACIONES)).document(id_relacion)
				.update(getResources().getString(R.string.RELACIONES_ESTADO_DE_LA_RELACION),getResources().getInteger(R.integer.RELACION_DENUNCIADA));
	}

	private void borrar_relacion(){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_mio)
				.collection(getResources().getString(R.string.RELACIONES)).document(id_relacion)
				.update(getResources().getString(R.string.RELACIONES_ESTADO_DE_LA_RELACION),getResources().getInteger(R.integer.RELACION_BORRADA));
	}

	private void ir_a_mensajes(){
		Intent mIntent = new Intent(getContext(), Activity_Mensajes.class);
		mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(mIntent);
	}
}