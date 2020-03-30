package com.rubisoft.gayradar.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.rubisoft.gayradar.Classes.Relacion_para_listar;
import com.rubisoft.gayradar.R;
import com.rubisoft.gayradar.tools.utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerView_Messages_Adapter extends RecyclerView.Adapter<RecyclerView_Messages_Adapter.CardView_ViewHolder> {
    private final Context mContext;
    private final List<Relacion_para_listar> lista_relaciones;
    // Allows to remember the last item shown on screen
    private int lastPosition = -1;
    private RecyclerView_Messages_Adapter.Clicklistener clicklistener;

    public RecyclerView_Messages_Adapter(Context un_Context) {
		lista_relaciones = new ArrayList<>();
		mContext = un_Context;
	}

    public void addItem(int i, Relacion_para_listar item) {
        lista_relaciones.add(i, item);
    }

    public void replaceItem(int i, Relacion_para_listar item) {
        lista_relaciones.set(i, item);
        notifyItemChanged(i);
    }

    public void removeItem(Relacion_para_listar item) {
        int position = lista_relaciones.indexOf(item);
        if (position != -1) {
            lista_relaciones.remove(item);
            notifyItemRemoved(position);
        }
    }

    public void removeItem(int position) {
        lista_relaciones.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public RecyclerView_Messages_Adapter.CardView_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_mensajes, viewGroup, false);

        return new RecyclerView_Messages_Adapter.CardView_ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return lista_relaciones.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView_Messages_Adapter.CardView_ViewHolder un_CardView_ViewHolder, int pos) {
        try {

            un_CardView_ViewHolder.user_name.setText(lista_relaciones.get(pos).getNombre());
            un_CardView_ViewHolder.mensajes_sin_leer.setImageDrawable(lista_relaciones.get(pos).getIcono_mensajes_sin_leer());
            Picasso.with(mContext)
                    .load(lista_relaciones.get(pos).getFoto())
                    .placeholder(utils.get_no_pic(mContext,ContextCompat.getColor(mContext, R.color.primary_light)))
                    .error(utils.get_no_pic(mContext,ContextCompat.getColor(mContext, R.color.primary_light)))
                    .resize(mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))
                    .into(un_CardView_ViewHolder.user_foto);
        } catch (Exception e) {
            utils.registra_error(e.toString(), "onBindViewHolder de  recyclerview_messages_adapter");

            //utils.registra_error("", "Exception en onBindViewHolder de RecyclerView_Principal_Adapter: " + e));
        }
    }

    public void setClicklistener(RecyclerView_Messages_Adapter.Clicklistener un_clicklistener) {
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

        @NonNull
        private final AppCompatImageView user_foto;


        @NonNull
        private final AppCompatImageView mensajes_sin_leer;

        CardView_ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            user_name = itemView.findViewById(R.id.Mensajes_list_TextView);
            user_foto = itemView.findViewById(R.id.Mensajes_list_ImageView);
            mensajes_sin_leer = itemView.findViewById(R.id.Mensajes_list_ImageView_mensaje_sin_leer);
            Typeface typeFace_roboto_bold = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/Roboto-Bold.ttf");
            user_name.setTypeface(typeFace_roboto_bold);
        }

        @Override
        public void onClick(View v) {
            if (clicklistener != null) {
                clicklistener.itemClicked(v, getLayoutPosition());
            }
        }
    }
}