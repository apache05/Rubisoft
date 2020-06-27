package com.rubisoft.bisexradar.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.bisexradar.Classes.Usuario_para_listar;
import com.rubisoft.bisexradar.R;
import com.rubisoft.bisexradar.tools.utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerView_Principal_Adapter extends RecyclerView.Adapter<RecyclerView_Principal_Adapter.CardView_ViewHolder> {
	private final Context mContext;
	private final List<Usuario_para_listar> lista_usuarios;
	// Allows to remember the last item shown on screen
	private int lastPosition = -1;
	private RecyclerView_Principal_Adapter.Clicklistener clicklistener;

	public RecyclerView_Principal_Adapter(Context un_Context) {
		lista_usuarios = new ArrayList<>();
		mContext = un_Context;
	}

	public void addItem(int i, Usuario_para_listar item) {
		lista_usuarios.add(i, item);
	}

	public void replaceItem(int i, Usuario_para_listar item) {
		lista_usuarios.set(i, item);
		notifyItemChanged(i);
	}

	public void removeItem(Usuario_para_listar item) {
		int position = lista_usuarios.indexOf(item);
		if (position != -1) {
			lista_usuarios.remove(item);
			notifyItemRemoved(position);
		}
	}

	public void removeItem(int position) {
		lista_usuarios.remove(position);
		notifyItemRemoved(position);
	}

	@NonNull
	@Override
	public RecyclerView_Principal_Adapter.CardView_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

		View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_principal, viewGroup, false);

		return new RecyclerView_Principal_Adapter.CardView_ViewHolder(itemView);
	}

	@Override
	public int getItemCount() {
		return lista_usuarios.size();
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView_Principal_Adapter.CardView_ViewHolder un_CardView_ViewHolder, int pos) {
		try {
			switch (lista_usuarios.get(pos).getSexo().intValue()){
				case 1:
					un_CardView_ViewHolder.user_sex.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(mContext, R.drawable.gender_man)));
					break;
				case 2:
					un_CardView_ViewHolder.user_sex.setImageDrawable(DrawableCompat.wrap(ContextCompat.getDrawable(mContext, R.drawable.gender_woman)));
					break;
			}

			un_CardView_ViewHolder.Cardview_list_persona_TextView_edad.setText(lista_usuarios.get(pos).getString_edad());
			un_CardView_ViewHolder.Cardview_list_persona_TextView_peso.setText(lista_usuarios.get(pos).getString_peso());
			un_CardView_ViewHolder.Cardview_list_persona_TextView_altura.setText(lista_usuarios.get(pos).getString_altura());
			un_CardView_ViewHolder.Cardview_list_persona_TextView_raza.setText(lista_usuarios.get(pos).getString_raza());

			un_CardView_ViewHolder.Cardview_list_persona_TextView_distancia.setText(lista_usuarios.get(pos).getString_distancia());

			un_CardView_ViewHolder.user_name.setText(lista_usuarios.get(pos).getNick());

			Picasso.with(mContext)
					.load(lista_usuarios.get(pos).getFoto())
					.placeholder(utils.get_no_pic(mContext,ContextCompat.getColor(mContext, R.color.primary_light)))
					.error(utils.get_no_pic(mContext,ContextCompat.getColor(mContext, R.color.primary_light)))
					.resize(mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))
					.into(un_CardView_ViewHolder.user_foto);
			un_CardView_ViewHolder.user_stars.setText(lista_usuarios.get(pos).getEstrellas());

			Drawable icono_estrella;
			icono_estrella = new IconicsDrawable(mContext).icon(GoogleMaterial.Icon.gmd_star).color(ContextCompat.getColor(mContext, R.color.accent));
			un_CardView_ViewHolder.star_icon.setImageDrawable(icono_estrella);

			if (lista_usuarios.get(pos).getQuiero_dejar_claro() == null){
				un_CardView_ViewHolder.user_quiero_dejar_claro.setVisibility(View.GONE);
			}else {
				un_CardView_ViewHolder.user_quiero_dejar_claro.setText(lista_usuarios.get(pos).getQuiero_dejar_claro());
			}

			un_CardView_ViewHolder.Esta_online.setImageDrawable(lista_usuarios.get(pos).getIcono_esta_online());
			if (lista_usuarios.get(pos).isPremium()) {
				un_CardView_ViewHolder.mCardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.primary));
				un_CardView_ViewHolder.Cardview_list_persona_TextView_altura.setTextColor(ContextCompat.getColor(mContext, R.color.white));
				un_CardView_ViewHolder.Cardview_list_persona_TextView_distancia.setTextColor(ContextCompat.getColor(mContext, R.color.white));
				un_CardView_ViewHolder.Cardview_list_persona_TextView_edad.setTextColor(ContextCompat.getColor(mContext, R.color.white));
				un_CardView_ViewHolder.Cardview_list_persona_TextView_peso.setTextColor(ContextCompat.getColor(mContext, R.color.white));
				un_CardView_ViewHolder.Cardview_list_persona_TextView_raza.setTextColor(ContextCompat.getColor(mContext, R.color.white));

				un_CardView_ViewHolder.user_name.setTextColor(ContextCompat.getColor(mContext, R.color.white));
				un_CardView_ViewHolder.user_stars.setTextColor(ContextCompat.getColor(mContext, R.color.white));
				un_CardView_ViewHolder.user_quiero_dejar_claro.setTextColor(ContextCompat.getColor(mContext, R.color.white));
			}
			setAnimation(un_CardView_ViewHolder.mCardView, pos);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onBindViewHolder de recyclerview_principal_adapter");
		}
	}

	public void setClicklistener(RecyclerView_Principal_Adapter.Clicklistener un_clicklistener) {
		clicklistener = un_clicklistener;
	}

	private void setAnimation(View viewToAnimate, int position) {
		// If the bound view wasn't previously displayed on screen, it's animated
		if (position > lastPosition) {
			Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
			viewToAnimate.startAnimation(animation);
			lastPosition = position;
		}
	}

	interface Clicklistener {
		void itemClicked(View view, int position);
	}

	public class CardView_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		@NonNull
		private final TextView user_name;
		//@NonNull
		//private final TextView Fecha_ultimo_acceso;
		@NonNull
		private final AppCompatTextView user_stars;
		@NonNull
		private final AppCompatImageView user_foto;
		@NonNull
		private final AppCompatImageView star_icon;
		@NonNull
		private final AppCompatImageView Esta_online;
		@NonNull
		private final TextView user_quiero_dejar_claro;


		@NonNull
		private final ImageView user_sex;

		private final CardView mCardView;

		@NonNull
		private final TextView Cardview_list_persona_TextView_edad;
		private final TextView Cardview_list_persona_TextView_altura;
		private final TextView Cardview_list_persona_TextView_peso;
		private final TextView Cardview_list_persona_TextView_raza;
		private final TextView Cardview_list_persona_TextView_distancia;

		CardView_ViewHolder(@NonNull View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			mCardView = itemView.findViewById(R.id.List_principal_CardView);

			Cardview_list_persona_TextView_edad = itemView.findViewById(R.id.List_principal_TextView_edad);
			Cardview_list_persona_TextView_altura = itemView.findViewById(R.id.List_principal_TextView_altura);
			Cardview_list_persona_TextView_peso = itemView.findViewById(R.id.List_principal_TextView_peso);
			Cardview_list_persona_TextView_raza = itemView.findViewById(R.id.List_principal_TextView_raza);
			Cardview_list_persona_TextView_distancia = itemView.findViewById(R.id.List_principal_TextView_distancia);

			user_name = itemView.findViewById(R.id.List_principal_TextView_nombre);
			user_foto = itemView.findViewById(R.id.List_principal_ImageView_usuario);
			user_stars = itemView.findViewById(R.id.List_principal_TextView_estrellas);
			star_icon = itemView.findViewById(R.id.List_principal_ImageView_estrella);
			user_sex = itemView.findViewById(R.id.List_principal_ImageView_sexualidad);

			user_quiero_dejar_claro = itemView.findViewById(R.id.List_principal_TextView_quiero_dejar_claro);
			Esta_online = itemView.findViewById(R.id.List_principal_ImageView_online);

			Typeface typeFace_roboto_bold = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/Roboto-Bold.ttf");
			Typeface typeFace_roboto_light = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/Roboto-Light.ttf");
			user_name.setTypeface(typeFace_roboto_bold);
			Cardview_list_persona_TextView_distancia.setTypeface(typeFace_roboto_light);

			Cardview_list_persona_TextView_edad.setTypeface(typeFace_roboto_light);
			Cardview_list_persona_TextView_peso.setTypeface(typeFace_roboto_light);
			Cardview_list_persona_TextView_altura.setTypeface(typeFace_roboto_light);
			Cardview_list_persona_TextView_raza.setTypeface(typeFace_roboto_light);
			user_quiero_dejar_claro.setTypeface(typeFace_roboto_light);
		}

		@Override
		public void onClick(View v) {
			if (clicklistener != null) {
				clicklistener.itemClicked(v, getLayoutPosition());
			}
		}
	}
}