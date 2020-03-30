package com.rubisoft.mencuddles.Classes;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Feedback  {
	private String id;
	private Long fecha;
	private String pais;
	private String comentario;
	private String version;
	private String app;
	private String motivo;

	public Feedback(String un_id, Long una_fecha, String un_pais, String un_comentario, String una_version, String una_app, String un_motivo) {
		super();
		this.id = un_id;
		this.fecha = una_fecha;
		this.pais = un_pais;
		this.comentario = un_comentario;
		this.version = una_version;
		this.app = una_app;
		this.motivo = un_motivo;
	}
	public Feedback() {
		super();

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getFecha() {
		return fecha;
	}

	public void setFecha(Long fecha) {
		this.fecha = fecha;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

}
