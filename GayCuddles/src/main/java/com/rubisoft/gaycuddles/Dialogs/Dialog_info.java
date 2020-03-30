package com.rubisoft.gaycuddles.Dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.gaycuddles.Classes.Usuario_para_listar;
import com.rubisoft.gaycuddles.R;
import com.rubisoft.gaycuddles.tools.utils;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

 class Dialog_info extends DialogFragment {

	private SharedPreferences perfil_usuario;

	private View view;
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.onCreate(savedInstanceState);

		view = inflater.inflate(R.layout.dialog_info, container);
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
		return view;
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
			TextView mTextView_nick = view.findViewById(R.id.Cardview_list_persona_TextView_nombre);
			TextView mTextView_raza = view.findViewById(R.id.Cardview_list_persona_TextView_raza);
			TextView mTextView_altura = view.findViewById(R.id.Cardview_list_persona_TextView_altura);
			TextView mTextView_peso = view.findViewById(R.id.Cardview_list_persona_TextView_peso);
			TextView mTextView_distancia = view.findViewById(R.id.Cardview_list_persona_TextView_distancia);
			TextView mTextView_edad = view.findViewById(R.id.Cardview_list_persona_TextView_edad);
			TextView mTextView_estrellas = view.findViewById(R.id.Cardview_list_persona_TextView_estrellas);
			TextView mTextView_quiero_dejar_claro = view.findViewById(R.id.Cardview_list_persona_TextView_quiero_dejar_claro);
			//  TextView mTextView_ultima_conexion = view.findViewById(R.id.Cardview_list_persona_Last_seen);

			AppCompatImageView mAppCompatImageView_sexualidad = view.findViewById(R.id.Cardview_list_persona_ImageView_sexualidad);
			AppCompatImageView mAppCompatImageView_estrella = view.findViewById(R.id.Cardview_list_persona_ImageView_estrella);
		//	AppCompatImageView mAppCompatImageView_premium = view.findViewById(R.id.Cardview_list_persona_ImageView_premium);

			Drawable icono_estrella;
			icono_estrella = new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_star).color(ContextCompat.getColor(getContext(), R.color.accent));
			mAppCompatImageView_estrella.setImageDrawable(icono_estrella);

			switch (un_usuario_para_listar.getSexo().intValue()){
				case 1:
					mAppCompatImageView_sexualidad.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.gender_man)));
					break;
				case 2:
					mAppCompatImageView_sexualidad.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.gender_woman)));
					break;
			}
			mTextView_nick.setText(un_usuario_para_listar.getNick());
			mTextView_raza.setText(un_usuario_para_listar.getString_raza());
			mTextView_altura.setText(un_usuario_para_listar.getString_altura());
			mTextView_peso.setText(un_usuario_para_listar.getString_peso());
			mTextView_distancia.setText(un_usuario_para_listar.getString_distancia());
			mTextView_edad.setText(un_usuario_para_listar.getString_edad());
			mTextView_estrellas.setText(un_usuario_para_listar.getEstrellas());
			mTextView_quiero_dejar_claro.setText(un_usuario_para_listar.getQuiero_dejar_claro());



			//mAppCompatImageView_sexualidad.setImageDrawable(un_usuario_para_listar.getIcono_Sexo());
			//mAppCompatImageView_coche.setImageDrawable(un_usuario.getIcono_coche());
			//mAppCompatImageView_casa.setImageDrawable(un_usuario.getIcono_casa());
			//mAppCompatImageView_premium.setImageDrawable(un_usuario_para_listar.getIcono_premium());
			//mAppCompatImageView_moto.setImageDrawable(un_usuario.getIcono_moto());
			//mAppCompatImageView_estrella.setImageDrawable(un_usuario_para_listar.getIcono_estrella());

		}catch (Exception e){
			utils.registra_error(e.toString(), "muestra_info_persona de dialog_info");

		}
	}
}