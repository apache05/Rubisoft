package com.rubisoft.mencuddles.Fragments;

import android.R.layout;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.mencuddles.Classes.Usuario;
import com.rubisoft.mencuddles.R;
import com.rubisoft.mencuddles.activities.Activity_Principal;
import com.rubisoft.mencuddles.tools.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

public class fragment_datos_personales extends Fragment {
	private ViewGroup rootView;
	private SharedPreferences perfil_usuario;
	private SharedPreferences preferencias_usuario;

	private TextView mTextView_sexo;
	private TextView mTextView_nick;
	private EditText mEditText_nick;

	private TextView mTextView_raza;
	private TextView mTextView_peso;
	private TextView mTextView_altura;
	private TextView mTextView_edad;
	private TextView mTextView_mi_raza;
	private RangeBar mRangeBar_mi_peso;
	private RangeBar mRangeBar_mi_altura;
	private TextView mTextView_mi_peso;
	private TextView mTextView_mi_altura;
	private TextView mTextView_mi_edad;
	private TextView mTextView_y;
	private TextView mTextView_Quiero_deja_claro;

	private TextView mTextView_Sexualidad;
	private TextView mTextView_Tus_Datos;
	private EditText mEditText_quiero_dejar_claro;
	private Spinner mSpinner_orientacion;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.rootView = (ViewGroup) inflater.inflate(R.layout.fragment_datos_personales, container, false);
		this.preferencias_usuario = this.getActivity().getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);
		this.perfil_usuario = this.getActivity().getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

		try {
			this.setup_Views();
			this.set_Views_TypeFace();
			this.set_TextViews_Text();
			this.setup_RangerBars();

			AppCompatImageView Button_guarda_datos_personales = this.rootView.findViewById(R.id.Fragment_datos_personales_Button_actualizar_datos_personales);
			Drawable icono = new IconicsDrawable(this.getContext()).icon(Icon.gmd_done).color(ContextCompat.getColor(this.getContext(), R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
			Button_guarda_datos_personales.setImageDrawable(icono);
			Button_guarda_datos_personales.setOnClickListener(view -> {
				if (no_hay_incongruencias_sexuales((int) perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_SEXO), 0), mSpinner_orientacion.getSelectedItemPosition() + 1)) {
					if(mEditText_quiero_dejar_claro.getText().length()==0){
						desuscribir_de_grupo();
					}
					//guardamos la busqueda que ha hecho para recordarla y que no tenga que tocar otra vez los controles
					Editor editor_perfil_usuario = perfil_usuario.edit();
					long peso;
					if (Long.valueOf(mRangeBar_mi_peso.getRightPinValue()) > getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) {
						peso = (long) getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO);
					} else if (Long.valueOf(mRangeBar_mi_peso.getRightPinValue()) < getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) {
						peso = (long) getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO);
					} else {
						peso = Long.parseLong(mRangeBar_mi_peso.getRightPinValue());
					}
					long altura;
					if (Long.valueOf(mRangeBar_mi_altura.getRightPinValue()) > getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) {
						altura = (long) getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA);
					} else if (Long.valueOf(mRangeBar_mi_altura.getRightPinValue()) < getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) {
						altura = (long) getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA);
					} else {
						altura = Long.parseLong(mRangeBar_mi_altura.getRightPinValue());
					}
					
					editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_NICK), mEditText_nick.getText().toString());
					editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_PESO), peso);
					editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ALTURA), altura);
					editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_QUIERO_DEJAR_CLARO), mEditText_quiero_dejar_claro.getText().toString());
					editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION), (long) (mSpinner_orientacion.getSelectedItemPosition() + 1));
					editor_perfil_usuario.apply();

					Usuario un_usuario=new Usuario();
					un_usuario.setOrientacion((long) mSpinner_orientacion.getSelectedItemPosition() + 1);
					un_usuario.setPeso(Long.valueOf(mRangeBar_mi_peso.getRightPinValue()));
					un_usuario.setAltura(Long.valueOf(mRangeBar_mi_altura.getRightPinValue()));
					un_usuario.setNick(mEditText_nick.getText().toString());
					un_usuario.setQuiero_dejar_claro_que(mEditText_quiero_dejar_claro.getText().toString());
					actualiza_perfil_en_Firestore(un_usuario);

					if (un_usuario.getOrientacion()!=null) {
						suscribir_a_grupo();
					}

					Intent mIntent = new Intent(getContext(), Activity_Principal.class);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

					startActivity(mIntent);
					getActivity().finish();
				} else {
					Toast.makeText(getActivity(), R.string.ACTIVITY_REGISTRO_ERROR_INCONGRUENCIA_SEXUAL, Toast.LENGTH_LONG).show();
				}
			});
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onCreateView de fragment_datos_personales");
		}
		return this.rootView;
	}


	private boolean no_hay_incongruencias_sexuales(Integer ACTIVITY_REGISTRO_SEXO_elegido, Integer ACTIVITY_REGISTRO_ORIENTACION_elegida) {
		boolean congruente = true;
		try {
			if (ACTIVITY_REGISTRO_ORIENTACION_elegida == 0) {
				congruente = false;
			}
			if (((ACTIVITY_REGISTRO_SEXO_elegido == this.getResources().getInteger(R.integer.HOMBRE)) && (ACTIVITY_REGISTRO_ORIENTACION_elegida == this.getResources().getInteger(R.integer.LESBIANA))) ||
					((ACTIVITY_REGISTRO_SEXO_elegido == this.getResources().getInteger(R.integer.MUJER)) &&
					( (ACTIVITY_REGISTRO_ORIENTACION_elegida == this.getResources().getInteger(R.integer.GAY))))) {
				congruente = false;
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "no_hay_incongruencias_sexuales de fragment_datos_presonales");
		}
		return congruente;
	}

	private float get_altura() {
		DecimalFormat formatter = new DecimalFormat("#.##");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(dfs);
		float float_altura = (float) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0);
		return float_altura / 100;
	}

	private int get_edad() {
		return utils.getEdad(this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_FECHA_NACIMIENTO), 0));
	}

	private void setup_Views() {
		this.mTextView_sexo = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_sexo);
		this.mTextView_Tus_Datos = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Tus_Datos);
		this.mTextView_Sexualidad = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Sexualidad);
		this.mTextView_nick = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Nick);
		this.mEditText_nick = this.rootView.findViewById(R.id.Fragment_datos_personales_EditText_nick);
		this.mTextView_y = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_y);
		this.mTextView_Quiero_deja_claro = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Quiero_dejar_claro_que);

		this.mTextView_raza = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Raza);
		this.mTextView_peso = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Peso);
		this.mTextView_altura = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Altura);
		this.mTextView_edad = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_Edad);
		this.mTextView_mi_raza = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_mi_Raza);
		this.mRangeBar_mi_peso = this.rootView.findViewById(R.id.Fragment_datos_personales_RangeBar_mi_Peso);
		this.mRangeBar_mi_altura = this.rootView.findViewById(R.id.Fragment_datos_personales_RangeBar_mi_Altura);
		this.mTextView_mi_peso = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_mi_Peso);
		this.mTextView_mi_altura = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_mi_Altura);
		this.mTextView_mi_edad = this.rootView.findViewById(R.id.Fragment_datos_personales_TextView_mi_Edad);
		this.mSpinner_orientacion = this.rootView.findViewById(R.id.Fragment_datos_personales_Spinner_cambiar_mi_orientacion);
		this.mEditText_quiero_dejar_claro = rootView.findViewById(R.id.Fragment_datos_personales_Edittext_quiero_dejar_claro);

	}

	private void set_Views_TypeFace() {
		Typeface typeFace_roboto_light = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Light.ttf");
		Typeface typeFace_roboto_bold = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

		this.mTextView_raza.setTypeface(typeFace_roboto_bold);
		this.mTextView_peso.setTypeface(typeFace_roboto_bold);
		this.mTextView_altura.setTypeface(typeFace_roboto_bold);
		this.mTextView_edad.setTypeface(typeFace_roboto_bold);
		this.mTextView_nick.setTypeface(typeFace_roboto_bold);
		this.mEditText_nick.setTypeface(typeFace_roboto_light);
		this.mTextView_mi_raza.setTypeface(typeFace_roboto_light);
		this.mTextView_mi_peso.setTypeface(typeFace_roboto_light);
		this.mTextView_mi_altura.setTypeface(typeFace_roboto_light);
		this.mTextView_mi_edad.setTypeface(typeFace_roboto_light);
		this.mTextView_y.setTypeface(typeFace_roboto_light);
		this.mTextView_Tus_Datos.setTypeface(typeFace_roboto_bold);
		this.mTextView_Sexualidad.setTypeface(typeFace_roboto_bold);
		this.mTextView_Quiero_deja_claro.setTypeface(typeFace_roboto_bold);
		this.mTextView_sexo.setTypeface(typeFace_roboto_light);

		mEditText_quiero_dejar_claro.setTypeface(typeFace_roboto_light);
	}

	private void set_TextViews_Text() {
		try {
			this.mEditText_nick.setText(this.perfil_usuario.getString(this.getResources().getString(R.string.PERFIL_USUARIO_NICK), ""));
			mEditText_nick.requestFocus();

			this.mTextView_mi_raza.setText(String.format(this.getResources().getString(R.string.texto), this.getResources().getStringArray(R.array.razas)[(int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_RAZA), 1)]));
			this.mTextView_mi_edad.setText(String.format(this.getResources().getString(R.string.numero), this.get_edad()));

			if (this.preferencias_usuario.getLong(this.getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == this.getResources().getInteger(R.integer.BRITANICO)) {
				Pair un_par = utils.kg_a_st_and_lb((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L));
				this.mTextView_mi_peso.setText(String.format(this.getResources().getString(R.string.st_y_lb), (double)un_par.first,(double) un_par.second));
				this.mTextView_mi_altura.setText(String.format(this.getResources().getString(R.string.texto), utils.cm_a_feet_and_inches((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L))));
			} else if (this.preferencias_usuario.getLong(this.getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == this.getResources().getInteger(R.integer.AMERICANO)) {
				this.mTextView_mi_peso.setText(String.format(this.getResources().getString(R.string.texto), utils.kg_a_lb((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L))));
				this.mTextView_mi_altura.setText(String.format(this.getResources().getString(R.string.texto), utils.cm_a_feet_and_inches((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L))));
			} else {
				this.mTextView_mi_peso.setText(String.format(this.getResources().getString(R.string.kg), this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L)));
				this.mTextView_mi_altura.setText(String.format(this.getResources().getString(R.string.m), this.get_altura()));
			}

			this.setup_spinners_perfil(this.mSpinner_orientacion);

			this.mTextView_sexo.setText(this.getResources().getTextArray(R.array.sexos)[(int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_SEXO), 1L)]);
			mEditText_quiero_dejar_claro.setText(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_QUIERO_DEJAR_CLARO), ""));

		} catch (Exception e) {
			utils.registra_error(e.toString(), "set_TextViews_Text de fragment_datos_presonales");
		}
	}

	private void setup_RangerBars() {
		try {
			if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L) > getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) {
				mRangeBar_mi_altura.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
			} else if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L) < getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) {
				mRangeBar_mi_altura.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));
			} else {
				mRangeBar_mi_altura.setSeekPinByValue(this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L));
			}

			if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L) > getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) {
				mRangeBar_mi_peso.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO));
			} else if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L) < getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) {
				mRangeBar_mi_peso.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO));
			} else {
				mRangeBar_mi_peso.setSeekPinByValue(this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L));
			}

			mRangeBar_mi_altura.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
				try {
					if ((preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.BRITANICO)) || ((preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.AMERICANO)))) {
						mTextView_mi_altura.setText(utils.cm_a_feet_and_inches(Integer.valueOf(rightPinValue)));
					} else {
						mTextView_mi_altura.setText(String.format(getResources().getString(R.string.m),Float.valueOf(rightPinValue)/100));
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "");
				}
			});
			mRangeBar_mi_peso.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
				try{
					if (preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.BRITANICO)) {
						Pair un_par = utils.kg_a_st_and_lb(Integer.valueOf(rightPinValue));
						mTextView_mi_peso.setText(getResources().getString(R.string.st_y_lb, (double)un_par.first,(double) un_par.second));
					} else if (preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.AMERICANO)) {
						mTextView_mi_peso.setText(utils.kg_a_lb(Integer.valueOf(rightPinValue)));
					} else {
						mTextView_mi_peso.setText(String.format(getResources().getString(R.string.kg), Integer.valueOf(rightPinValue)));
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "");
				}
			});
		} catch (Exception e) {
			utils.registra_error(e.toString(), "");
		}
	}

	private void setup_spinners_perfil(@NonNull Spinner mSpinner_orientacion) {
		// **********************************************************************************************************
		// Esta función se encarga de poner los spinners con el número correspondiente segun la información del perfil actual del Usuario_para_listar
		// **********************************************************************************************************
		try {
			// Create an ArrayAdapter using the string array and a default spinner layout
			ArrayAdapter<CharSequence> adapter_orientaciones;
			adapter_orientaciones = ArrayAdapter.createFromResource(this.getActivity(), R.array.orientaciones_mi_perfil, R.layout.spinner_item);

			// Specify the layout to use when the list of choices appears
			adapter_orientaciones.setDropDownViewResource(layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			mSpinner_orientacion.setAdapter(adapter_orientaciones);
			mSpinner_orientacion.setSelection((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION), 2) - 1);

		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_spinners_perfil de datos_presonales");
		}
	}

	private void actualiza_perfil_en_Firestore(Usuario un_usuario){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		DocumentReference Ref= db.collection(getResources().getString(R.string.USUARIOS)).document(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));
		if (un_usuario.getOrientacion()!=null){
			Ref.update(getResources().getString(R.string.USUARIO_NICK),un_usuario.getOrientacion());
		}
		if (un_usuario.getPeso()!=null){
			Ref.update(getResources().getString(R.string.USUARIO_PESO),un_usuario.getPeso());
		}
		if (un_usuario.getAltura()!=null){
			Ref.update(getResources().getString(R.string.USUARIO_ALTURA),un_usuario.getAltura());
		}
		if (un_usuario.getQuiero_dejar_claro_que()!=null){
			Ref.update(getResources().getString(R.string.USUARIO_QUIERO_DEJAR_CLARO),un_usuario.getQuiero_dejar_claro_que());
		}
		if (un_usuario.getNick()!=null){
			Ref.update(getResources().getString(R.string.USUARIO_NICK),un_usuario.getNick());
		}
	}
	private void desuscribir_de_grupo(){
		try{
			String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
			FirebaseMessaging.getInstance().unsubscribeFromTopic(grupo_al_que_pertenece);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "desuscribir_de_grupo de Activity_Principal");
		}
	}

	private void suscribir_a_grupo(){
		String grupo_al_que_pertenece= utils.grupo_al_que_pertenece(perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION),0L),perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_PAIS),""));
		FirebaseMessaging.getInstance().subscribeToTopic(grupo_al_que_pertenece);
	}
}
