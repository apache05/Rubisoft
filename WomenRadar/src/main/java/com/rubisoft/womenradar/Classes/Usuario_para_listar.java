package com.rubisoft.womenradar.Classes;


import android.graphics.drawable.Drawable;

import java.util.Map;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario_para_listar {
    private String nick;
    private String String_edad;
    private String estrellas;
    private Long Long_altura;
    private Long Long_peso;
	private Long Long_edad;
	private Long fecha_nacimiento;
	private Long fecha_cobro_estrellas;
	private Long Long_raza;
	private String String_altura;  // con inches o metros
    private String String_peso;   // con lb o kg
    private String String_raza;
    private String String_distancia;
    private Double Double_distancia;
    private String quiero_dejar_claro;
    private Long orientacion;
    private Long sexo;
    private String foto;

    private Drawable icono_Sexo;
    private Drawable icono_estrella;
    private Drawable icono_premium;


    private Drawable icono_mensajes_sin_leer;
    private Drawable icono_esta_online;

    private boolean premium;

    private String token_socialauth;

    public Usuario_para_listar(String un_nombre, String una_edad, String una_distancia, String una_foto) {
		this.nick = un_nombre;
		this.String_edad = una_edad;
		this.String_distancia = una_distancia;
		this.foto = una_foto;
	}

    public Usuario_para_listar() {
	}
	public Usuario_para_listar(Map<String,Object> mapa) {
		nick = (String) mapa.get("nick");
		estrellas = ( mapa.get("estrellas")).toString();
		String_altura = ( mapa.get("altura")).toString();
		String_peso = ( mapa.get("peso")).toString();
		String_raza = ( mapa.get("raza")).toString();
		Long_altura = ((Long) mapa.get("altura"));
		Long_peso = ((Long) mapa.get("peso"));
		Long_raza = ((Long) mapa.get("raza"));
		quiero_dejar_claro = (String) mapa.get("quiero_dejar_claro_que");
		orientacion = ((Long) mapa.get("orientacion"));
		sexo = ((Long) mapa.get("sexo"));
		premium = (Boolean) mapa.get("es_premium");
		fecha_nacimiento=((Long) mapa.get("fecha_nacimiento"));
		fecha_cobro_estrellas=((Long) mapa.get("fecha_cobro_estrellas"));
	}

	public Long getLong_altura() {
		return Long_altura;
	}

	public void setLong_altura(Long long_altura) {
		Long_altura = long_altura;
	}

	public Long getLong_peso() {
		return Long_peso;
	}

	public void setLong_peso(Long long_peso) {
		Long_peso = long_peso;
	}

	public Long getLong_edad() {
		return Long_edad;
	}

	public void setLong_edad(Long long_edad) {
		Long_edad = long_edad;
	}

	public Long getLong_raza() {
		return Long_raza;
	}

	public void setLong_raza(Long long_raza) {
		Long_raza = long_raza;
	}

	public Long getOrientacion() {
		return orientacion;
	}

	public void setOrientacion(Long orientacion) {
		this.orientacion = orientacion;
	}

	public Long getSexo() {
		return sexo;
	}

	public void setSexo(Long sexo) {
		this.sexo = sexo;
	}

	public String getString_raza() {
		return String_raza;
	}

	public void setString_raza(String string_raza) {
		String_raza = string_raza;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getString_edad() {
		return String_edad;
	}

	public void setString_edad(String string_edad) {
		String_edad = string_edad;
	}


	public String getEstrellas() {
		return estrellas;
	}

	public void setEstrellas(String estrellas) {
		this.estrellas = estrellas;
	}


	public String getString_altura() {
		return String_altura;
	}

	public void setString_altura(String string_altura) {
		String_altura = string_altura;
	}

	public String getString_peso() {
		return String_peso;
	}

	public void setString_peso(String string_peso) {
		String_peso = string_peso;
	}

	public String getString_distancia() {
		return String_distancia;
	}

	public void setString_distancia(String string_distancia) {
		String_distancia = string_distancia;
	}

	public Double getDouble_distancia() {
		return Double_distancia;
	}

	public void setDouble_distancia(Double double_distancia) {
		Double_distancia = double_distancia;
	}

	public boolean isPremium() {
		return premium;
	}

	public void setPremium(boolean premium) {
		this.premium = premium;
	}


	public String getQuiero_dejar_claro() {
		return quiero_dejar_claro;
	}

	public void setQuiero_dejar_claro(String quiero_dejar_claro) {
		this.quiero_dejar_claro = quiero_dejar_claro;
	}

	public String getFoto() {
		return foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

	public Drawable getIcono_Sexo() {
		return icono_Sexo;
	}

	public void setIcono_Sexo(Drawable icono_Sexo) {
		this.icono_Sexo = icono_Sexo;
	}


	public Drawable getIcono_estrella() {
		return icono_estrella;
	}

	public void setIcono_estrella(Drawable icono_estrella) {
		this.icono_estrella = icono_estrella;
	}

	public Drawable getIcono_premium() {
		return icono_premium;
	}

	public void setIcono_premium(Drawable icono_premium) {
		this.icono_premium = icono_premium;
	}

	public Long getFecha_cobro_estrellas() {
		return fecha_cobro_estrellas;
	}

	public void setFecha_cobro_estrellas(Long fecha_cobro_estrellas) {
		this.fecha_cobro_estrellas = fecha_cobro_estrellas;
	}

	public Drawable getIcono_mensajes_sin_leer() {
		return icono_mensajes_sin_leer;
	}

	public void setIcono_mensajes_sin_leer(Drawable icono_mensajes_sin_leer) {
		this.icono_mensajes_sin_leer = icono_mensajes_sin_leer;
	}

	public Drawable getIcono_esta_online() {
		return icono_esta_online;
	}

	public void setIcono_esta_online(Drawable icono_esta_online) {
		this.icono_esta_online = icono_esta_online;
	}

	public boolean is_premium() {
		return premium;
	}

	public void set_premium(boolean es_premium) {
		this.premium = es_premium;
	}

	public Long getFecha_nacimiento() {
		return fecha_nacimiento;
	}

	public void setFecha_nacimiento(Long fecha_nacimiento) {
		this.fecha_nacimiento = fecha_nacimiento;
	}


	public String getToken_socialauth() {
		return token_socialauth;
	}

	public void setToken_socialauth(String token_socialauth) {
		this.token_socialauth = token_socialauth;
	}
}
