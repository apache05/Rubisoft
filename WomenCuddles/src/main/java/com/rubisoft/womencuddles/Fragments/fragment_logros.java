package com.rubisoft.womencuddles.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.womencuddles.Classes.Logro;
import com.rubisoft.womencuddles.R;
import com.rubisoft.womencuddles.databinding.FragmentLogrosBinding;
import com.rubisoft.womencuddles.tools.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class fragment_logros extends Fragment {
	private FragmentLogrosBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentLogrosBinding.inflate(inflater, container, false);
		View mView = binding.getRoot();
    	try {
            SharedPreferences perfil_usuario = getActivity().getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
			get_logros(perfil_usuario.getString(this.getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));

            Drawable icono_reloj = new IconicsDrawable(this.getContext()).icon(Icon.gmd_query_builder).color(ContextCompat.getColor(this.getContext(), R.color.primary)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
            Drawable icono_motivo = new IconicsDrawable(this.getContext()).icon(Icon.gmd_assignment_turned_in).color(ContextCompat.getColor(this.getContext(), R.color.primary)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
            Drawable icono_variacion = new IconicsDrawable(this.getContext()).icon(Icon.gmd_swap_vert).color(ContextCompat.getColor(this.getContext(), R.color.primary)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));
            Drawable icono_total = new IconicsDrawable(this.getContext()).icon(Icon.gmd_star).color(ContextCompat.getColor(this.getContext(), R.color.primary)).sizeDp(this.getResources().getInteger(R.integer.Tam_Normal_icons));

            binding.FragmentLogrosImageViewReloj.setImageDrawable(icono_reloj);
			binding.FragmentLogrosImageViewMotivo.setImageDrawable(icono_motivo);
			binding.FragmentLogrosImageViewVariacion.setImageDrawable(icono_variacion);
			binding.FragmentLogrosImageViewTotal.setImageDrawable(icono_total);

        } catch (Exception e) {
            utils.registra_error(e.toString(), "oncreateview  de fragment_logros");
        }

        return mView;
    }

    private void get_logros(String token_socialauth ){
    	try {
			List<Logro> un_listado_logros = new ArrayList<>();
			FirebaseFirestore db = FirebaseFirestore.getInstance();
			db.collection(getResources().getString(R.string.USUARIOS)).document(token_socialauth).collection(getResources().getString(R.string.LOGROS)).orderBy("fecha_del_logro", Query.Direction.DESCENDING).get()
					.addOnCompleteListener(task -> {
						if (task.isSuccessful()) {
							for (QueryDocumentSnapshot document : task.getResult()) {
								Logro un_logro = new Logro(document.getData());
								un_listado_logros.add(un_logro);
							}
							muestra_logros(un_listado_logros);
						}
					});
		}catch (Exception ignored)
		{
		}
	}

	private void muestra_logros(List<Logro> listado_logros){
    	try{
			if ((listado_logros != null) && (this.getActivity() != null)) {
				Typeface mTypeFace_roboto_light = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Light.ttf");

				int i = 0;
				String pais = utils.get_locale(getActivity());
				for (Logro un_logro : listado_logros) {
					if (i != 0) {
						View separador = new View(this.getActivity());
						separador.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 2));
						separador.setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.gris));

						binding.FragmentLogrosTableLayoutLogros.addView(separador);
					}
					TextView mTextView_fecha = new TextView(this.getActivity());
					TextView mTextView_motivo = new TextView(this.getActivity());
					TextView mTextView_variacion = new TextView(this.getActivity());
					TextView mTextView_estrellas_actuales = new TextView(this.getActivity());

					mTextView_fecha.setTypeface(mTypeFace_roboto_light);
					mTextView_motivo.setTypeface(mTypeFace_roboto_light);
					mTextView_variacion.setTypeface(mTypeFace_roboto_light);
					mTextView_estrellas_actuales.setTypeface(mTypeFace_roboto_light);

					mTextView_fecha.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.25F));
					mTextView_motivo.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.25F));
					mTextView_variacion.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.25F));
					mTextView_estrellas_actuales.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.25F));

					mTextView_fecha.setGravity(Gravity.CENTER_HORIZONTAL);
					mTextView_motivo.setGravity(Gravity.CENTER_HORIZONTAL);
					mTextView_variacion.setGravity(Gravity.CENTER_HORIZONTAL);
					mTextView_estrellas_actuales.setGravity(Gravity.CENTER_HORIZONTAL);
					//  if(utils.isTablet(getContext())) {
					float SCREEN_DENSITY = getResources().getDisplayMetrics().density;

					mTextView_fecha.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_s_plus)/SCREEN_DENSITY );
					mTextView_motivo.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.tamanyo_letra_s_plus)/SCREEN_DENSITY);
					mTextView_variacion.setTextSize(TypedValue.COMPLEX_UNIT_SP,  getResources().getDimension(R.dimen.tamanyo_letra_s_plus)/SCREEN_DENSITY);
					mTextView_estrellas_actuales.setTextSize(TypedValue.COMPLEX_UNIT_SP,  getResources().getDimension(R.dimen.tamanyo_letra_s_plus)/SCREEN_DENSITY);

					long fecha_logro = un_logro.getFecha_del_logro();

					mTextView_fecha.setText(construye_fecha(fecha_logro, pais));
					mTextView_motivo.setText(decodifica_motivo(un_logro.getMotivo().intValue()));
					DecimalFormat fmt = new DecimalFormat("#;-#");
					String variacion = fmt.format(un_logro.getEstrellas_logradas().intValue());
					//String aux = (variacion > 0) ? ("+" + variacion) : variacion.toString();
					mTextView_variacion.setText(variacion);
					mTextView_estrellas_actuales.setText(String.format(getResources().getString(R.string.numero),un_logro.getTotal_estrellas()));

					TableRow nueva_fila = new TableRow(this.getActivity());
					nueva_fila.addView(mTextView_fecha);
					nueva_fila.addView(mTextView_motivo);
					nueva_fila.addView(mTextView_variacion);
					nueva_fila.addView(mTextView_estrellas_actuales);

					binding.FragmentLogrosTableLayoutLogros.addView(nueva_fila);
					i++;
				}
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "AsyncTask_get_logros de fragment_logros");
		}
	}

	private String decodifica_motivo(int un_motivo) {
		String explicacion = "";
		if (un_motivo == 0L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_SIGNED_UP);
		} else if (un_motivo == 1L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_COMPRA_ESTRELLAS);
		} else if (un_motivo == 2L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_COMPRA_ESTRELLAS);
		} else if (un_motivo == 3L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_COMPRA_ESTRELLAS);
		} else if (un_motivo == 4L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_COMPRA_ESTRELLAS);
		} else if (un_motivo == 5L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_ANUNCIO_BONIFICADO);
		} else if (un_motivo == 6L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_COBRO_SEMANAL);
		} else if (un_motivo == 7L) {
			explicacion = getActivity().getResources().getString(R.string.MOTIVO_COBRO_PETICION_CHAT_GENERAL);
		}
		return explicacion;
	}

	private String construye_fecha(long milliseconds, @NonNull String pais) {
		DecimalFormat formatter = new DecimalFormat("##");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(milliseconds);

		String fecha;
		switch (pais) {
			case "US":
				fecha = formatter.format(cal.get(Calendar.MONTH) + 1) + '/' + formatter.format(cal.get(Calendar.DAY_OF_MONTH)) + '/' + formatter.format(cal.get(Calendar.YEAR));
				break;
			case "JP":
				fecha = formatter.format(cal.get(Calendar.YEAR)) + "年/" + formatter.format(cal.get(Calendar.MONTH) + 1) + "月/" + formatter.format(cal.get(Calendar.DAY_OF_MONTH)) + '日';
				break;
			default:
				fecha = formatter.format(cal.get(Calendar.DAY_OF_MONTH)) + '/' + formatter.format(cal.get(Calendar.MONTH) + 1) + '/' + formatter.format(cal.get(Calendar.YEAR));
				break;
		}
		return fecha;
	}
}
