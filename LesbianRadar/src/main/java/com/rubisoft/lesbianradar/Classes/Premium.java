package com.rubisoft.lesbianradar.Classes;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Premium  {
	private Long fecha_limite;
	public Premium() {
	}

	public Premium( Long fecha_limite) {
		this.fecha_limite = fecha_limite;
	}

	public Long getFecha_limite() {
		return fecha_limite;
	}

	public void setFecha_limite(Long fecha_limite) {
		this.fecha_limite = fecha_limite;
	}
}
