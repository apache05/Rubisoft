package com.rubisoft.lesbiancuddles.Classes;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Denuncia {
	private String token_socialauth_denunciado;
	private String token_socialauth_denunciante;
	private String causa;

	public Denuncia(String token_socialauth_denunciado, String token_socialauth_denunciante, String causa) {
		this.token_socialauth_denunciado = token_socialauth_denunciado;
		this.token_socialauth_denunciante = token_socialauth_denunciante;
		this.causa = causa;
	}

	public Denuncia() {

	}
	public String getToken_socialauth_denunciado() {
		return token_socialauth_denunciado;
	}

	public void setToken_socialauth_denunciado(String token_socialauth_denunciado) {
		this.token_socialauth_denunciado = token_socialauth_denunciado;
	}

	public String getToken_socialauth_denunciante() {
		return token_socialauth_denunciante;
	}

	public void setToken_socialauth_denunciante(String token_socialauth_denunciante) {
		this.token_socialauth_denunciante = token_socialauth_denunciante;
	}

	public String getCausa() {
		return causa;
	}

	public void setCausa(String causa) {
		this.causa = causa;
	}
}
