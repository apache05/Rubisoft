package com.rubisoft.bisexcuddles.Classes;

import java.io.Serializable;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA

public class STATS_RELACIONES  implements Serializable {
	private Long menfinder;
	private Long womenfinder;
	private Long bisexfinder;
	private Long gayfinder;
	private Long lesbianfinder;
	private Long womenradar;
	private Long gayradar;
	private Long total;
	public STATS_RELACIONES() {
	}

	public STATS_RELACIONES(Long menfinder, Long womenfinder, Long bisexfinder, Long gayfinder, Long lesbianfinder, Long womenradar, Long gayradar, Long total) {
		this.menfinder = menfinder;
		this.womenfinder = womenfinder;
		this.bisexfinder = bisexfinder;
		this.gayfinder = gayfinder;
		this.lesbianfinder = lesbianfinder;
		this.womenradar = womenradar;
		this.gayradar = gayradar;
		this.total = total;
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

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}
}
