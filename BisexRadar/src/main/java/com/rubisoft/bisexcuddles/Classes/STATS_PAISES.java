package com.rubisoft.bisexradar.Classes;

import java.io.Serializable;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA

public class STATS_PAISES implements Serializable {
	private long bifinder;
	private long gayfinder;
	private long gayradar;
	private long lesbianfinder;
	private long menfinder;
	private long womenradar;
	private long womenfinder;
	private long hombre_bi;
	private long hombre_gay;
	private long hombre_hetero;
	private long mujer_bi;
	private long mujer_hetero;
	private long mujer_lesbiana;
	private long total_usuarios;

	public STATS_PAISES(long bifinder, long gayfinder, long gayradar, long lesbianfinder, long menfinder, long womenradar, long womenfinder, long hombre_bi, long hombre_gay, long hombre_hetero, long mujer_bi, long mujer_hetero, long mujer_lesbiana, long total_usuarios) {
		this.bifinder = bifinder;
		this.gayfinder = gayfinder;
		this.gayradar = gayradar;
		this.lesbianfinder = lesbianfinder;
		this.menfinder = menfinder;
		this.womenradar = womenradar;
		this.womenfinder = womenfinder;
		this.hombre_bi = hombre_bi;
		this.hombre_gay = hombre_gay;
		this.hombre_hetero = hombre_hetero;
		this.mujer_bi = mujer_bi;
		this.mujer_hetero = mujer_hetero;
		this.mujer_lesbiana = mujer_lesbiana;
		this.total_usuarios = total_usuarios;
	}

	public STATS_PAISES(){
		total_usuarios=1L;
	}

	public long getTotal_usuarios() {
		return total_usuarios;
	}

	public void setTotal_usuarios(long total_usuarios) {
		this.total_usuarios = total_usuarios;
	}

	public long getBifinder() {
		return bifinder;
	}

	public void setBifinder(long bifinder) {
		this.bifinder = bifinder;
	}

	public long getGayfinder() {
		return gayfinder;
	}

	public void setGayfinder(long gayfinder) {
		this.gayfinder = gayfinder;
	}

	public long getGayradar() {
		return gayradar;
	}

	public void setGayradar(long gayradar) {
		this.gayradar = gayradar;
	}

	public long getLesbianfinder() {
		return lesbianfinder;
	}

	public void setLesbianfinder(long lesbianfinder) {
		this.lesbianfinder = lesbianfinder;
	}

	public long getMenfinder() {
		return menfinder;
	}

	public void setMenfinder(long menfinder) {
		this.menfinder = menfinder;
	}

	public long getWomenradar() {
		return womenradar;
	}

	public void setWomenradar(long womenradar) {
		this.womenradar = womenradar;
	}

	public long getWomenfinder() {
		return womenfinder;
	}

	public void setWomenfinder(long womenfinder) {
		this.womenfinder = womenfinder;
	}

	public long getHombre_bi() {
		return hombre_bi;
	}

	public void setHombre_bi(long hombre_bi) {
		this.hombre_bi = hombre_bi;
	}

	public long getHombre_gay() {
		return hombre_gay;
	}

	public void setHombre_gay(long hombre_gay) {
		this.hombre_gay = hombre_gay;
	}

	public long getHombre_hetero() {
		return hombre_hetero;
	}

	public void setHombre_hetero(long hombre_hetero) {
		this.hombre_hetero = hombre_hetero;
	}

	public long getMujer_bi() {
		return mujer_bi;
	}

	public void setMujer_bi(long mujer_bi) {
		this.mujer_bi = mujer_bi;
	}

	public long getMujer_hetero() {
		return mujer_hetero;
	}

	public void setMujer_hetero(long mujer_hetero) {
		this.mujer_hetero = mujer_hetero;
	}

	public long getMujer_lesbiana() {
		return mujer_lesbiana;
	}

	public void setMujer_lesbiana(long mujer_lesbiana) {
		this.mujer_lesbiana = mujer_lesbiana;
	}
}
