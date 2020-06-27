package com.rubisoft.bisexradar.Classes;

import java.io.Serializable;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA

public class STATS_GLOBAL implements Serializable {
	private Long menfinder;
	private Long menradar;

	private Long womenfinder;
	private Long womenradar;

	private Long bisexfinder;
	private Long bisexradar;

	private Long lesbianfinder;
	private Long lesbianradar;

	private Long gayfinder;
	private Long gayradar;

	private Long hombre_bi;
	private Long hombre_gay;
	private Long hombre_hetero;
	private Long mujer_bi;
	private Long mujer_hetero;
	private Long mujer_lesbiana;

	private Long total_usuarios;

	public STATS_GLOBAL(Long menfinder, Long womenfinder, Long bisexfinder, Long gayfinder, Long lesbianfinder, Long womenradar, Long gayradar, Long bisexradar, Long menradar, Long lesbianradar, Long hombre_bi, Long hombre_gay, Long hombre_hetero, Long mujer_bi, Long mujer_hetero, Long mujer_lesbiana, Long total_usuarios) {
		this.menfinder = menfinder;
		this.womenfinder = womenfinder;
		this.bisexfinder = bisexfinder;
		this.gayfinder = gayfinder;
		this.lesbianfinder = lesbianfinder;
		this.womenradar = womenradar;
		this.gayradar = gayradar;
		this.menradar = menradar;
		this.lesbianradar = lesbianradar;
		this.bisexradar = bisexradar;


		this.hombre_bi = hombre_bi;
		this.hombre_gay = hombre_gay;
		this.hombre_hetero = hombre_hetero;
		this.mujer_bi = mujer_bi;
		this.mujer_hetero = mujer_hetero;
		this.mujer_lesbiana = mujer_lesbiana;
		this.total_usuarios = total_usuarios;
	}

	public STATS_GLOBAL() {
		total_usuarios=1L;
	}

	public Long getMenfinder() {
		return menfinder;
	}

	public void setMenfinder(Long menfinder) {
		this.menfinder = menfinder;
	}

	public Long getWomenfinder() {
		return womenfinder;
	}

	public void setWomenfinder(Long womenfinder) {
		this.womenfinder = womenfinder;
	}

	public Long getBisexfinder() {
		return bisexfinder;
	}

	public void setBisexfinder(Long bisexfinder) {
		this.bisexfinder = bisexfinder;
	}

	public Long getGayfinder() {
		return gayfinder;
	}

	public void setGayfinder(Long gayfinder) {
		this.gayfinder = gayfinder;
	}

	public Long getLesbianfinder() {
		return lesbianfinder;
	}

	public void setLesbianfinder(Long lesbianfinder) {
		this.lesbianfinder = lesbianfinder;
	}

	public Long getWomenradar() {
		return womenradar;
	}

	public void setWomenradar(Long womenradar) {
		this.womenradar = womenradar;
	}

	public Long getGayradar() {
		return gayradar;
	}

	public void setGayradar(Long gayradar) {
		this.gayradar = gayradar;
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

	public Long getTotal_usuarios() {
		return total_usuarios;
	}

	public void setTotal_usuarios(Long total_usuarios) {
		this.total_usuarios = total_usuarios;
	}

	public Long getMenradar() {
		return menradar;
	}

	public void setMenradar(Long menradar) {
		this.menradar = menradar;
	}

	public Long getBisexradar() {
		return bisexradar;
	}

	public void setBisexradar(Long bisexradar) {
		this.bisexradar = bisexradar;
	}

	public Long getLesbianradar() {
		return lesbianradar;
	}

	public void setLesbianradar(Long lesbianradar) {
		this.lesbianradar = lesbianradar;
	}
}