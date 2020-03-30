package com.rubisoft.gaycuddles.Classes;

import java.util.Calendar;
import java.util.Map;

import androidx.annotation.Keep;
@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Usuario {
	private Long altura;
	private Long app;
	private Long estrellas;
	private Boolean es_premium;
	private Long fecha_cobro_estrellas;//fecha de referencia para cobrar estrellas. se cobra cada mes
	private Long fecha_nacimiento;
	private Long fecha_ultimo_acceso;
	private Long fecha_registro;

	private Double latitud;
	private Double longitud;
	private String nick;
	private Long orientacion;
	private String pais; // Para filtrar mejor los usuarios a la hora de buscar
	private Long peso;
	private String quiero_dejar_claro_que;
	private Long raza;
	private Long sexo;
	private Boolean tiene_mensajes_sin_leer;
	private String token_fcm; //Google Cloud Messaging
	private String token_suscripcion_1_anyo_premium; //para comprobar si es premium o no
	private String token_suscripcion_medio_anyo_premium; //para comprobar si es premium o no
	public Usuario(Map<String,Object> mapa) {
		super();
		altura = (Long) mapa.get("altura");
		estrellas = (Long) mapa.get("estrellas");
		fecha_cobro_estrellas = (Long) mapa.get("fecha_cobro_estrellas");
		fecha_nacimiento = (Long) mapa.get("fecha_nacimiento");
		fecha_ultimo_acceso = (Long) mapa.get("fecha_ultimo_acceso");
		latitud = (Double) mapa.get("latitud");
		longitud = (Double) mapa.get("longitud");
		nick = (String) mapa.get("nick");
		orientacion = (Long) mapa.get("orientacion");
		pais = (String) mapa.get("pais");
		peso = (Long) mapa.get("peso");
		quiero_dejar_claro_que = (String) mapa.get("quiero_dejar_claro_que");
		raza = (Long) mapa.get("raza");
		sexo = (Long) mapa.get("sexo");
		token_fcm = (String) mapa.get("token_fcm");
		app = (Long) mapa.get("app");
		es_premium = (Boolean) mapa.get("es_premium");
	}
	public Usuario(long una_altura,
				   long una_app,
				   long unas_estrellas,
				   long una_fecha_cobro_estrellas,
				   long una_fecha_nacimiento,
				   long una_fecha_ultimo_acceso,
					double una_latitud,
				   double una_longitud,
				   String un_nick,
					long una_orientacion,
				   String un_pais,
				   long un_peso,
				   String un_quiero_dejar_claro_que,
				   long una_raza,
				   long un_sexo,
				   String un_token_fcm
				   ) {
		super();
		altura = una_altura;
		app = una_app;
		estrellas = unas_estrellas;
		es_premium = false;
		fecha_cobro_estrellas = una_fecha_cobro_estrellas;
		fecha_nacimiento = una_fecha_nacimiento;
		fecha_ultimo_acceso = una_fecha_ultimo_acceso;
		latitud = una_latitud;
		longitud = una_longitud;
		nick = un_nick;
		orientacion = una_orientacion;
		pais = un_pais;
		peso = un_peso;
		quiero_dejar_claro_que = un_quiero_dejar_claro_que;
		raza = una_raza;
		sexo = un_sexo;
		token_fcm = un_token_fcm;
		tiene_mensajes_sin_leer = false;
		token_suscripcion_1_anyo_premium = "";
		token_suscripcion_medio_anyo_premium = "";
		fecha_registro= Calendar.getInstance().getTimeInMillis();
	}

	public Usuario() {
		super();

	}

	public Long getAltura() {
		return altura;
	}

	public void setAltura(Long una_altura) {
		altura = una_altura;
	}

	public Long getEstrellas() {
		return estrellas;
	}

	public void setEstrellas(Long unas_estrellas) {
		estrellas = unas_estrellas;
	}

	public Long getFecha_cobro_estrellas() {
		return fecha_cobro_estrellas;
	}

	public void setFecha_cobro_estrellas(Long una_fecha_cobro_estrellas) {
		fecha_cobro_estrellas = una_fecha_cobro_estrellas;
	}

	public Long getFecha_nacimiento() {
		return fecha_nacimiento;
	}

	public void setFecha_nacimiento(Long una_fecha_nacimiento) {
		fecha_nacimiento = una_fecha_nacimiento;
	}

	public Long getFecha_ultimo_acceso() {
		return fecha_ultimo_acceso;
	}

	public void setFecha_ultimo_acceso(Long una_fecha_ultimo_acceso) {
		fecha_ultimo_acceso = una_fecha_ultimo_acceso;
	}

	public Double getLatitud() {
		return latitud;
	}

	public void setLatitud(Double una_latitud) {
		latitud = una_latitud;
	}

	public Double getLongitud() {
		return longitud;
	}

	public void setLongitud(Double una_longitud) {
		longitud = una_longitud;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String un_nick) {
		nick = un_nick;
	}

	public Long getOrientacion() {
		return orientacion;
	}

	public void setOrientacion(Long una_orientacion) {
		orientacion = una_orientacion;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String un_pais) {
		pais = un_pais;
	}

	public Long getPeso() {
		return peso;
	}

	public void setPeso(Long un_peso) {
		peso = un_peso;
	}

	public String getQuiero_dejar_claro_que() {
		return quiero_dejar_claro_que;
	}

	public void setQuiero_dejar_claro_que(String quiero_dejar_claro_que) {
		this.quiero_dejar_claro_que = quiero_dejar_claro_que;
	}

	public Long getRaza() {
		return raza;
	}

	public void setRaza(Long una_raza) {
		raza = una_raza;
	}

	public Long getSexo() {
		return sexo;
	}

	public void setSexo(Long un_sexo) {
		sexo = un_sexo;
	}

	public String getToken_fcm() {
		return token_fcm;
	}

	public void setToken_fcm(String token_fcm) {
		this.token_fcm = token_fcm;
	}

	public String getToken_suscripcion_1_anyo_premium() {
		return token_suscripcion_1_anyo_premium;
	}

	public void setToken_suscripcion_1_anyo_premium(String token_suscripcion_1_anyo_premium) {
		this.token_suscripcion_1_anyo_premium = token_suscripcion_1_anyo_premium;
	}

	public String getToken_suscripcion_medio_anyo_premium() {
		return token_suscripcion_medio_anyo_premium;
	}

	public void setToken_suscripcion_medio_anyo_premium(String token_suscripcion_medio_anyo_premium) {
		this.token_suscripcion_medio_anyo_premium = token_suscripcion_medio_anyo_premium;
	}

	public Boolean getEs_premium() {
		return es_premium;
	}

	public void setEs_premium(Boolean es_premium) {
		this.es_premium = es_premium;
	}

	public Boolean getTiene_mensajes_sin_leer() {
		return tiene_mensajes_sin_leer;
	}

	public void setTiene_mensajes_sin_leer(Boolean un_tiene_mensajes_sin_leer) {
		tiene_mensajes_sin_leer = un_tiene_mensajes_sin_leer;
	}

	public Long getApp() {
		return app;
	}

	public void setApp(Long una_app) {
		app = una_app;
	}

	public Long getFecha_registro() {
		return fecha_registro;
	}

	public void setFecha_registro(Long fecha_registro) {
		this.fecha_registro = fecha_registro;
	}
}
