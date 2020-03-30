package com.rubisoft.gaycuddles.Classes;


import android.graphics.drawable.Drawable;

//esta clase es necesario para implementar el RecyclerView
public class Relacion_para_listar {
    private String Token_socialauth;
    private String Nombre;

    private String Foto;

    private Drawable icono_mensajes_sin_leer;

    public Relacion_para_listar(String un_nombre, String una_foto) {
		this.Nombre = un_nombre;

		this.Foto = una_foto;
	}

    public Relacion_para_listar() {
	}


    public Drawable getIcono_mensajes_sin_leer() {
        return icono_mensajes_sin_leer;
    }

    public void setIcono_mensajes_sin_leer(Drawable tiene_mensajes_sin_leer) {
        icono_mensajes_sin_leer = tiene_mensajes_sin_leer;
    }
    public String getNombre() {
        return this.Nombre;
    }

    public void setNombre(String un_Nombre) {
        this.Nombre = un_Nombre;
    }

    public String getToken_socialauth() {
        return this.Token_socialauth;
    }

    public void setToken_socialauth(String un_Token_socialauth) {
        this.Token_socialauth = un_Token_socialauth;
    }
    public String getFoto() {
        return this.Foto;
    }

    public void setFoto(String una_Foto) {
        this.Foto = una_Foto;
    }


}
