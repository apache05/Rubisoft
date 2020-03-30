package com.rubisoft.lesbianradar.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.rubisoft.lesbianradar.Classes.Usuario_para_listar;
import com.rubisoft.lesbianradar.R;
import com.rubisoft.lesbianradar.tools.utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class RecyclerView_Chat_Adapter extends RecyclerView.Adapter<RecyclerView_Chat_Adapter.CardView_ViewHolder> {
    private final Context mContext;
    private final List<Usuario_para_listar> lista_usuarios;
    // Allows to remember the last item shown on screen
    private int lastPosition = -1;
    private RecyclerView_Chat_Adapter.Clicklistener clicklistener;

    public RecyclerView_Chat_Adapter(Context un_Context) {
		lista_usuarios = new ArrayList<>();
		mContext = un_Context;
	}

    public void addItem(int i, Usuario_para_listar item) {
        lista_usuarios.add(i, item);
        //notifyItemInserted(i);
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
    public RecyclerView_Chat_Adapter.CardView_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat, viewGroup, false);

        return new RecyclerView_Chat_Adapter.CardView_ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return lista_usuarios.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView_Chat_Adapter.CardView_ViewHolder un_CardView_ViewHolder, int pos) {
        try {
            un_CardView_ViewHolder.user_name.setText(lista_usuarios.get(pos).getNick());

            Picasso.with(mContext)
                    .load(lista_usuarios.get(pos).getFoto())
                    .placeholder(utils.get_no_pic(mContext, ContextCompat.getColor(mContext, R.color.primary_light)))
                    .error(utils.get_no_pic(mContext,ContextCompat.getColor(mContext, R.color.primary_light)))
                    .resize(mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), mContext.getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))
                    .into(un_CardView_ViewHolder.user_foto);

        } catch (Exception e) {
            utils.registra_error(e.toString(), "onBindViewHolder de recyclerview_chat_adapter");
        }
    }

    public void setClicklistener(RecyclerView_Chat_Adapter.Clicklistener un_clicklistener) {
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
        private final de.hdodenhof.circleimageview.CircleImageView user_foto;


        CardView_ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            user_name = itemView.findViewById(R.id.Chat_List_TextView);
            user_foto = itemView.findViewById(R.id.Chat_List_ImageView);

            Typeface typeFace_roboto_light = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/Roboto-Light.ttf");
            user_name.setTypeface(typeFace_roboto_light);
        }

        @Override
        public void onClick(View v) {
            if (clicklistener != null) {
                clicklistener.itemClicked(v, getLayoutPosition());
            }
        }
    }
}