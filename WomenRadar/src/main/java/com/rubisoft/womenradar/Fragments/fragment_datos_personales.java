package com.rubisoft.womenradar.Fragments;

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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.womenradar.Classes.Usuario;
import com.rubisoft.womenradar.R;
import com.rubisoft.womenradar.activities.Activity_Principal;
import com.rubisoft.womenradar.databinding.FragmentDatosPersonalesBinding;
import com.rubisoft.womenradar.tools.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

public class fragment_datos_personales extends Fragment {
	private SharedPreferences perfil_usuario;
	private SharedPreferences preferencias_usuario;

	private FragmentDatosPersonalesBinding binding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.preferencias_usuario = this.getActivity().getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);
		this.perfil_usuario = this.getActivity().getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
		binding = FragmentDatosPersonalesBinding.inflate(inflater, container, false);
		View mView = binding.getRoot();
		try {
			this.set_Views_TypeFace();
			this.set_TextViews_Text();
			this.setup_RangerBars();

			Drawable icono = new IconicsDrawable(this.getContext()).icon(Icon.gmd_done).color(ContextCompat.getColor(this.getContext(), R.color.accent)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
			binding.FragmentDatosPersonalesButtonActualizarDatosPersonales.setImageDrawable(icono);
			binding.FragmentDatosPersonalesButtonActualizarDatosPersonales.setOnClickListener(view -> {
				if (no_hay_incongruencias_sexuales((int) perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_SEXO), 0L), binding.FragmentDatosPersonalesSpinnerCambiarMiOrientacion.getSelectedItemPosition() + 1)) {
					if(binding.FragmentDatosPersonalesEdittextQuieroDejarClaro.getText().length()==0){
						desuscribir_de_grupo();
					}
					//guardamos la busqueda que ha hecho para recordarla y que no tenga que tocar otra vez los controles
					Editor editor_perfil_usuario = perfil_usuario.edit();
					long peso;
					if (Long.parseLong(binding.FragmentDatosPersonalesRangeBarMiPeso.getRightPinValue()) > getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) {
						peso = (long) getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO);
					} else if (Long.parseLong(binding.FragmentDatosPersonalesRangeBarMiPeso.getRightPinValue()) < getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) {
						peso = (long) getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO);
					} else {
						peso = Long.parseLong(binding.FragmentDatosPersonalesRangeBarMiPeso.getRightPinValue());
					}
					long altura;
					if (Long.parseLong(binding.FragmentDatosPersonalesRangeBarMiAltura.getRightPinValue()) > getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) {
						altura = (long) getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA);
					} else if (Long.parseLong(binding.FragmentDatosPersonalesRangeBarMiAltura.getRightPinValue()) < getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) {
						altura = (long) getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA);
					} else {
						altura = Long.parseLong(binding.FragmentDatosPersonalesRangeBarMiAltura.getRightPinValue());
					}
					
					editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_NICK), binding.FragmentDatosPersonalesEditTextNick.getText().toString());
					editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_PESO), peso);
					editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ALTURA), altura);
					editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_QUIERO_DEJAR_CLARO), binding.FragmentDatosPersonalesEdittextQuieroDejarClaro.getText().toString());
					editor_perfil_usuario.putLong(getResources().getString(R.string.PERFIL_USUARIO_ORIENTACION), (long) (binding.FragmentDatosPersonalesSpinnerCambiarMiOrientacion.getSelectedItemPosition() + 1));
					editor_perfil_usuario.apply();

					Usuario un_usuario=new Usuario();
					un_usuario.setOrientacion((long) binding.FragmentDatosPersonalesSpinnerCambiarMiOrientacion.getSelectedItemPosition() + 1);
					un_usuario.setPeso(Long.valueOf(binding.FragmentDatosPersonalesRangeBarMiPeso.getRightPinValue()));
					un_usuario.setAltura(Long.valueOf(binding.FragmentDatosPersonalesRangeBarMiAltura.getRightPinValue()));
					un_usuario.setNick(binding.FragmentDatosPersonalesEditTextNick.getText().toString());
					un_usuario.setQuiero_dejar_claro_que(binding.FragmentDatosPersonalesEdittextQuieroDejarClaro.getText().toString());
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
		return mView;
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

	private void set_Views_TypeFace() {
		Typeface typeFace_roboto_light = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Light.ttf");
		Typeface typeFace_roboto_bold = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

		binding.FragmentDatosPersonalesTextViewRaza.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesTextViewPeso.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesTextViewAltura.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesTextViewEdad.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesTextViewNick.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesEditTextNick.setTypeface(typeFace_roboto_light);
		binding.FragmentDatosPersonalesTextViewMiRaza.setTypeface(typeFace_roboto_light);
		binding.FragmentDatosPersonalesTextViewMiPeso.setTypeface(typeFace_roboto_light);
		binding.FragmentDatosPersonalesTextViewMiAltura.setTypeface(typeFace_roboto_light);
		binding.FragmentDatosPersonalesTextViewMiEdad.setTypeface(typeFace_roboto_light);
		binding.FragmentDatosPersonalesTextViewY.setTypeface(typeFace_roboto_light);
		binding.FragmentDatosPersonalesTextViewTusDatos.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesTextViewSexualidad.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesTextViewQuieroDejarClaroQue.setTypeface(typeFace_roboto_bold);
		binding.FragmentDatosPersonalesTextViewSexo.setTypeface(typeFace_roboto_light);

		binding.FragmentDatosPersonalesEdittextQuieroDejarClaro.setTypeface(typeFace_roboto_light);
	}

	private void set_TextViews_Text() {
		try {
			binding.FragmentDatosPersonalesEditTextNick.setText(this.perfil_usuario.getString(this.getResources().getString(R.string.PERFIL_USUARIO_NICK), ""));
			binding.FragmentDatosPersonalesEditTextNick.requestFocus();

			binding.FragmentDatosPersonalesTextViewMiRaza.setText(String.format(this.getResources().getString(R.string.texto), this.getResources().getStringArray(R.array.razas)[(int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_RAZA), 1)]));
			binding.FragmentDatosPersonalesTextViewMiEdad.setText(String.format(this.getResources().getString(R.string.numero), this.get_edad()));

			if (this.preferencias_usuario.getLong(this.getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == this.getResources().getInteger(R.integer.BRITANICO)) {
				Pair un_par = utils.kg_a_st_and_lb((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L));
				binding.FragmentDatosPersonalesTextViewMiPeso.setText(String.format(this.getResources().getString(R.string.st_y_lb), (int)un_par.first,(double) un_par.second));
				binding.FragmentDatosPersonalesTextViewMiAltura.setText(String.format(this.getResources().getString(R.string.texto), utils.cm_a_feet_and_inches((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L))));
			} else if (this.preferencias_usuario.getLong(this.getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == this.getResources().getInteger(R.integer.AMERICANO)) {
				binding.FragmentDatosPersonalesTextViewMiPeso.setText(String.format(this.getResources().getString(R.string.texto), utils.kg_a_lb((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L))));
				binding.FragmentDatosPersonalesTextViewMiAltura.setText(String.format(this.getResources().getString(R.string.texto), utils.cm_a_feet_and_inches((int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L))));
			} else {
				binding.FragmentDatosPersonalesTextViewMiPeso.setText(String.format(this.getResources().getString(R.string.kg), this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L)));
				binding.FragmentDatosPersonalesTextViewMiAltura.setText(String.format(this.getResources().getString(R.string.m), this.get_altura()));
			}

			this.setup_spinners_perfil(binding.FragmentDatosPersonalesSpinnerCambiarMiOrientacion);

			binding.FragmentDatosPersonalesTextViewSexo.setText(this.getResources().getTextArray(R.array.sexos)[(int) this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_SEXO), 0L)]);
			binding.FragmentDatosPersonalesEdittextQuieroDejarClaro.setText(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_QUIERO_DEJAR_CLARO), ""));

		} catch (Exception e) {
			utils.registra_error(e.toString(), "set_TextViews_Text de fragment_datos_presonales");
		}
	}

	private void setup_RangerBars() {
		try {
			if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L) > getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) {
				binding.FragmentDatosPersonalesRangeBarMiAltura.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
			} else if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L) < getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) {
				binding.FragmentDatosPersonalesRangeBarMiAltura.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));
			} else {
				binding.FragmentDatosPersonalesRangeBarMiAltura.setSeekPinByValue(this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_ALTURA), 0L));
			}

			if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L) > getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) {
				binding.FragmentDatosPersonalesRangeBarMiPeso.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO));
			} else if (this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L) < getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) {
				binding.FragmentDatosPersonalesRangeBarMiPeso.setSeekPinByValue(getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO));
			} else {
				binding.FragmentDatosPersonalesRangeBarMiPeso.setSeekPinByValue(this.perfil_usuario.getLong(this.getResources().getString(R.string.PERFIL_USUARIO_PESO), 0L));
			}

			binding.FragmentDatosPersonalesRangeBarMiAltura.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
				try {
					if ((preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.BRITANICO)) || ((preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.AMERICANO)))) {
						binding.FragmentDatosPersonalesTextViewMiAltura.setText(utils.cm_a_feet_and_inches(Integer.valueOf(rightPinValue)));
					} else {
						binding.FragmentDatosPersonalesTextViewMiAltura.setText(String.format(getResources().getString(R.string.m),Float.valueOf(rightPinValue)/100));
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "en FragmentDatosPersonalesRangeBarMiAltura de fragment_datos_personales");
				}
			});
			binding.FragmentDatosPersonalesRangeBarMiPeso.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
				try{
					if (preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.BRITANICO)) {
						Pair un_par = utils.kg_a_st_and_lb(Integer.valueOf(rightPinValue));
						binding.FragmentDatosPersonalesTextViewMiPeso.setText(getResources().getString(R.string.st_y_lb, (int)un_par.first, Double.valueOf((double)un_par.second).floatValue()));
					} else if (preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0L) == getResources().getInteger(R.integer.AMERICANO)) {
						binding.FragmentDatosPersonalesTextViewMiPeso.setText(utils.kg_a_lb(Integer.valueOf(rightPinValue)));
					} else {
						binding.FragmentDatosPersonalesTextViewMiPeso.setText(String.format(getResources().getString(R.string.kg), Integer.valueOf(rightPinValue)));
					}
				} catch (Exception e) {
					utils.registra_error(e.toString(), "en FragmentDatosPersonalesRangeBarMiPeso de fragment_datos_personales");
				}
			});
		} catch (Exception e) {
			utils.registra_error(e.toString(), "en setup_RangerBars de fragment_datos_personales");
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
