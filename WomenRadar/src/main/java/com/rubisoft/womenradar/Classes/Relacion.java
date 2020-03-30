package com.rubisoft.womenradar.Classes;

import java.util.Map;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Relacion  {

	private String token_socialauth_de_la_otra_persona;
	private String nick_de_la_otra_persona;
	private Long estado_de_la_relacion;
	private Boolean tiene_mensajes_sin_leer;

	public Relacion (Map<String,Object> mapa) {
		token_socialauth_de_la_otra_persona = (String) mapa.get("token_socialauth_de_la_otra_persona");
		nick_de_la_otra_persona = (String) mapa.get("nick_de_la_otra_persona");
		estado_de_la_relacion = (Long) mapa.get("estado_de_la_relacion");
		tiene_mensajes_sin_leer = (Boolean) mapa.get("tiene_mensajes_sin_leer");

	}
	public Relacion(String un_token_socialauth_de_la_otra_persona,String un_nick_de_la_otra_persona,Long un_estado,Boolean tiene_o_no_tiene) {
		token_socialauth_de_la_otra_persona = un_token_socialauth_de_la_otra_persona;
		nick_de_la_otra_persona = un_nick_de_la_otra_persona;
		estado_de_la_relacion = un_estado;
		tiene_mensajes_sin_leer = tiene_o_no_tiene;
	}

	public String getToken_socialauth_de_la_otra_persona() {
		return token_socialauth_de_la_otra_persona;
	}

	public void setToken_socialauth_de_la_otra_persona(String token_socialauth_de_la_otra_persona) {
		this.token_socialauth_de_la_otra_persona = token_socialauth_de_la_otra_persona;
	}

	public String getNick_de_la_otra_persona() {
		return nick_de_la_otra_persona;
	}

	public void setNick_de_la_otra_persona(String nick_de_la_otra_persona) {
		this.nick_de_la_otra_persona = nick_de_la_otra_persona;
	}

	public Long getEstado_de_la_relacion() {
		return estado_de_la_relacion;
	}

	public void setEstado_de_la_relacion(Long estado_de_la_relacion) {
		this.estado_de_la_relacion = estado_de_la_relacion;
	}

	public Boolean getTiene_mensajes_sin_leer() {
		return tiene_mensajes_sin_leer;
	}

	public void setTiene_mensajes_sin_leer(Boolean tiene_mensajes_sin_leer) {
		this.tiene_mensajes_sin_leer = tiene_mensajes_sin_leer;
	}
}
