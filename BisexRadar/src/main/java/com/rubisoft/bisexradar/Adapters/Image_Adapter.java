package com.rubisoft.bisexradar.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rubisoft.bisexradar.Classes.Mi_foto;
import com.rubisoft.bisexradar.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

//ESTO ES PARA CREAR EL GRID DE IMAGENES
public class Image_Adapter extends RecyclerView.Adapter<Image_Adapter.ViewHolder> {
    private final ArrayList<Mi_foto> Mis_Fotos;

    // Provide a suitable constructor (depends on the kind of dataset)
    public Image_Adapter(ArrayList<Mi_foto> un_arraylist) {
		Mis_Fotos = un_arraylist;
	}

    // Create new views (invoked by the layout manager)
    @NonNull
	@Override
    public Image_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mi_foto, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull Image_Adapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Mi_foto una_foto = Mis_Fotos.get(position);
        holder.bindMi_foto(una_foto);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Mis_Fotos.size();
    }

    public void setView(int position, AppCompatImageView una_foto) {
        Mis_Fotos.get(position).setFoto(una_foto);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private final ImageView mImageView_foto;
       // private final ImageView mImageView_llavecita;

        ViewHolder(View itemView) {
            super(itemView);
            mImageView_foto = itemView.findViewById(R.id.ImageView_mi_foto);
           // mImageView_llavecita = itemView.findViewById(R.id.ImageView_llavecita);
        }

        void bindMi_foto(Mi_foto mMi_foto) {
            mImageView_foto.setImageDrawable(mMi_foto.getFoto().getDrawable());
          //  mImageView_llavecita.setImageDrawable(mMi_foto.getLlavecita());
        }
    }

}
