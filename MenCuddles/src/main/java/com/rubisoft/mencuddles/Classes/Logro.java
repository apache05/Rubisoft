package com.rubisoft.mencuddles.Classes;

import java.util.Map;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Logro{
	private Long motivo;
	private Long estrellas_logradas;
	private Long fecha_del_logro;
	private Long total_estrellas;


	public Logro( Long motivo, Long estrellas_logradas,Long una_fecha,Long un_total_estrellas) {
		this.motivo = motivo;
		this.estrellas_logradas = estrellas_logradas;
		this.fecha_del_logro = una_fecha;
		this.total_estrellas = un_total_estrellas;
	}

	public Logro(Map<String,Object> un_logro) {
		this.fecha_del_logro = (Long) un_logro.get("fecha_del_logro");
		this.total_estrellas = (Long) un_logro.get("total_estrellas");
		this.motivo = (Long) un_logro.get("motivo");
		this.estrellas_logradas = (Long) un_logro.get("estrellas_logradas");

	}

	public Long getTotal_estrellas() {
		return total_estrellas;
	}

	public void setTotal_estrellas(Long total_estrellas) {
		this.total_estrellas = total_estrellas;
	}

	public Long getFecha_del_logro() {
		return fecha_del_logro;
	}

	public void setFecha_del_logro(Long fecha_del_logro) {
		this.fecha_del_logro = fecha_del_logro;
	}

	public Long getMotivo() {
		return motivo;
	}

	public void setMotivo(Long motivo) {
		this.motivo = motivo;
	}

	public Long getEstrellas_logradas() {
		return estrellas_logradas;
	}

	public void setEstrellas_logradas(Long estrellas_logradas) {
		this.estrellas_logradas = estrellas_logradas;
	}
}
