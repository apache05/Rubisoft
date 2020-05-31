package com.rubisoft.bisexcuddles.Classes;


import androidx.appcompat.widget.AppCompatImageView;

//esta clase es necesarioa para implementar el RecyclerView
public class Mi_foto {

    private AppCompatImageView Foto;
  //  private Drawable Llavecita;


    public AppCompatImageView getFoto() {
        return this.Foto;
    }

    public void setFoto(AppCompatImageView una_Foto) {
        this.Foto = una_Foto;
    }

   /* public Drawable getLlavecita() {
        return this.Llavecita;
    }

    public void setLlavecita(Drawable llavecita) {
        this.Llavecita = llavecita;
    }*/
}
