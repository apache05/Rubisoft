package com.rubisoft.bisexcuddles.Dialogs;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.bisexcuddles.Classes.Denuncia;
import com.rubisoft.bisexcuddles.R;
import com.rubisoft.bisexcuddles.activities.Activity_Chat_Individual;
import com.rubisoft.bisexcuddles.activities.Activity_Un_Perfil;
import com.rubisoft.bisexcuddles.databinding.DialogoInteractuarPrincipalBinding;
import com.rubisoft.bisexcuddles.tools.utils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class Dialog_Interactuar_Principal extends DialogFragment {
    private int seleccion=-1;

    private Bundle args;
    private String token_socialauth_de_la_otra_persona;
	private String token_socialauth_mio;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.onCreate(savedInstanceState);

		DialogoInteractuarPrincipalBinding binding = DialogoInteractuarPrincipalBinding.inflate(inflater, container, false);
		View mView = binding.getRoot();

		try {
            //si es una tableta hacemos la ventana mas grande
            if (utils.isTablet(getContext())) {
                getDialog().getWindow().setLayout(600, 300);
            }
            setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            args = getArguments();
            token_socialauth_de_la_otra_persona = args.getString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA));
			token_socialauth_mio = args.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH));

			Typeface mTypeFace_roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

			binding.DialogoInteractuarTitle.setText(getResources().getString(R.string.DIALOGO_INTERACTUAR_TITULO));
			binding.DialogoInteractuarTitle.setTypeface(mTypeFace_roboto_light);

            // Set listener, view, data for your dialog fragment
            Drawable icono_fotos_usuario = new IconicsDrawable(getContext()).icon(Icon.gmd_recent_actors).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_mandar_mensaje = new IconicsDrawable(getContext()).icon(Icon.gmd_message).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            Drawable icono_denunciar = new IconicsDrawable(getContext()).icon(Icon.gmd_remove_circle).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));
            //Drawable icono_info = new IconicsDrawable(getContext()).icon(Icon.gmd_info).color(ContextCompat.getColor(getContext(), R.color.primary)).sizeDp(getResources().getInteger(R.integer.Tam_Small_icons));


			binding.DialogoInteractuarButtonVerFotos.setImageDrawable(icono_fotos_usuario);
			binding.DialogoInteractuarButtonVerFotos.setOnClickListener(view14 -> {
				try {
					Intent mIntent = new Intent(getContext(), Activity_Un_Perfil.class);
					mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Bundle mBundle = new Bundle();
					mBundle.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), token_socialauth_de_la_otra_persona);
					mBundle.putInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO), args.getInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO)));
					mBundle.putInt(getResources().getString(R.string.PAGINA), args.getInt(getResources().getString(R.string.PAGINA)));
					mIntent.putExtras(mBundle);

					startActivity(mIntent);
					dismiss();
				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_ver_fotos) de Dialog_Interactuar_Principal");
				}
			});

            binding.DialogoInteractuarButtonEscribirMensaje.setImageDrawable(icono_mandar_mensaje);
			binding.DialogoInteractuarButtonEscribirMensaje.setOnClickListener(view13 -> {
				try {
						Intent mIntent = new Intent(getContext(), Activity_Chat_Individual.class);
						Bundle bundle = new Bundle();
						bundle.putString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), token_socialauth_de_la_otra_persona);
						bundle.putString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA), args.getString(getResources().getString(R.string.RELACIONES_NICK_DE_LA_OTRA_PERSONA)));
						bundle.putInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO), args.getInt(getResources().getString(R.string.PERFIL_USUARIO_DE_DONDE_VENGO)));

						mIntent.putExtras(bundle);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						startActivity(mIntent);

				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_escribir_mensaje) de Dialog_Interactuar_Principal");
				}
			});

            binding.DialogoInteractuarButtonDenunciar.setImageDrawable(icono_denunciar);
			binding.DialogoInteractuarButtonDenunciar.setOnClickListener(view12 -> {
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
								dismiss();
								return true;
							})
							.positiveText(R.string.DIALOGO_DENUNCIAR_DENUNCIAR)
							.onPositive((dialog, which) -> {

							})
							.negativeText(R.string.Cancelar)
							.onNegative((dialog, which) -> dialog.dismiss())
							.show();

				} catch (Exception e) {
					utils.registra_error(e.toString(), "onCreateView (Button_denunciar) de Dialog_Interactuar_Principal");
				}
			});
        }catch (Exception e){
            utils.registra_error(e.toString(), "onCreateView de Dialog_Interactuar_Principal");

        }
        return mView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private void denunciar_usuario(){

		Denuncia una_denuncia= new Denuncia();
		una_denuncia.setCausa(utils.decodifica_causa_denuncia( seleccion));
		una_denuncia.setToken_socialauth_denunciado(token_socialauth_de_la_otra_persona);
		una_denuncia.setToken_socialauth_denunciante(token_socialauth_mio);
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.DENUNCIAS)).add(una_denuncia);
	}
}