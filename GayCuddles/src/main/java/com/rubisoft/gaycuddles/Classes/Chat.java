package com.rubisoft.gaycuddles.Classes;


import java.util.Calendar;

import androidx.annotation.Keep;
@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA

public class Chat  {
	private String id_relacion;  //ESTO Y EL ID DEL DOCUMENTO SON LA FECHA DE CREACIÓN DEL MENSAJE
	private String de_quien;
	private String para_quien;
	private String nick;
	private String que_dijo;
	private Long fecha;

	public Chat(String un_id_relacion, String de_quien, String para_quien, String nick, String que_dijo) {
		this.fecha= Calendar.getInstance().getTimeInMillis();
		this.de_quien = de_quien;
		this.para_quien = para_quien;
		this.nick = nick;
		this.que_dijo = que_dijo;
		this.id_relacion = un_id_relacion;
	}

	public Long getFecha() {
		return fecha;
	}

	public void setFecha(Long fecha) {
		this.fecha = fecha;
	}

	public String getId_relacion() {
		return id_relacion;
	}

	public void setId_relacion(String id_relacion) {
		this.id_relacion = id_relacion;
	}

	public String getDe_quien() {
		return de_quien;
	}

	public void setDe_quien(String de_quien) {
		this.de_quien = de_quien;
	}

	public String getPara_quien() {
		return para_quien;
	}

	public void setPara_quien(String para_quien) {
		this.para_quien = para_quien;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getQue_dijo() {
		return que_dijo;
	}

	public void setQue_dijo(String que_dijo) {
		this.que_dijo = que_dijo;
	}
}