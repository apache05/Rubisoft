package com.rubisoft.mencuddles.Dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.mencuddles.Classes.Usuario_para_listar;
import com.rubisoft.mencuddles.R;
import com.rubisoft.mencuddles.databinding.DialogInfoBinding;
import com.rubisoft.mencuddles.tools.utils;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

public class Dialog_info extends DialogFragment {

	private SharedPreferences perfil_usuario;
	private DialogInfoBinding binding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.onCreate(savedInstanceState);

		binding = DialogInfoBinding.inflate(inflater, container, false);
		View mView = binding.getRoot();
		try {
			Bundle args = getArguments();

			//si es una tableta hacemos la ventana mas grande
			if (utils.isTablet(getContext())) {
				getDialog().getWindow().setLayout(600, 300);
			}

			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_MaterialComponents_Dialog);
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			perfil_usuario = getActivity().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

			get_document( (String)args.get(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA)));
		}catch (Exception e){
			utils.registra_error(e.toString(), "oncreateview  de dialog_info");
		}
		return mView;
	}

	private void get_document(String token_socialauth_de_la_otra_persona){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		DocumentReference docRef = db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth_de_la_otra_persona);
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document.exists()) {
					//Document exists  --> mostramos info
					muestra_info_persona(document.getData(),document.getId());
				}  //Document doesn't exist

			}  //Query failed

		});
	}

	private void muestra_info_persona(Map<String,Object> un_perfil,String token_socialauth_de_la_otra_persona){
		Double distanceBetweenTwoPoints = utils.getDistancia(getContext(),(Double)un_perfil.get(getResources().getString(R.string.USUARIO_LATITUD)),(Double)un_perfil.get(getResources().getString(R.string.USUARIO_LONGITUD)));
		Usuario_para_listar un_usuario_para_listar=new Usuario_para_listar( un_perfil);
		un_usuario_para_listar.setDouble_distancia(distanceBetweenTwoPoints);
		un_usuario_para_listar.setLong_edad(Integer.valueOf(utils.getEdad((Long)un_perfil.get(getString(R.string.USUARIO_FECHA_NACIMIENTO)))).longValue());
		un_usuario_para_listar.setToken_socialauth(token_socialauth_de_la_otra_persona);
		un_usuario_para_listar = utils.prepara_datos_usuario_para_listar(getContext(),un_usuario_para_listar, perfil_usuario);

		try {
			Drawable icono_estrella;
			icono_estrella = new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_star).color(ContextCompat.getColor(getContext(), R.color.accent));
			binding.DialogInfoImageViewEstrella.setImageDrawable(icono_estrella);

			switch (un_usuario_para_listar.getSexo().intValue()){
				case 1:
					binding.DialogInfoImageViewSexualidad.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.gender_man)));
					break;
				case 2:
					binding.DialogInfoImageViewSexualidad.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.gender_woman)));
					break;
			}
			binding.DialogInfoTextViewNombre.setText(un_usuario_para_listar.getNick());
			binding.DialogInfoTextViewRaza.setText(un_usuario_para_listar.getString_raza());
			binding.DialogInfoTextViewAltura.setText(un_usuario_para_listar.getString_altura());
			binding.DialogInfoTextViewPeso.setText(un_usuario_para_listar.getString_peso());
			binding.DialogInfoTextViewDistancia.setText(un_usuario_para_listar.getString_distancia());
			binding.DialogInfoTextViewEdad.setText(un_usuario_para_listar.getString_edad());
			binding.DialogInfoTextViewEstrellas.setText(un_usuario_para_listar.getEstrellas());
			binding.DialogInfoTextViewQuieroDejarClaro.setText(un_usuario_para_listar.getQuiero_dejar_claro());

		}catch (Exception e){
			utils.registra_error(e.toString(), "muestra_info_persona de dialog_info");

		}
	}
}