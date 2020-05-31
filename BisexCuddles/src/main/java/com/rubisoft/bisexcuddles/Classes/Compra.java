package com.rubisoft.bisexcuddles.Classes;

import java.util.Calendar;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Compra  {
	private String order_id;   //para encontrarla en la tabla de compras de google y poder cancelarla
	private String token_socialauth;  //para identificar el usuario
	private String token_compra;  //Si es una suscripción, para ver si sigue vigente o ha caducado
	private Long response_code;  //0 success, otherwise error
	private Long fecha_compra;
	public Compra() {
	}

	public Compra(String un_orderId,String un_token_socialauth,String un_token_compra,Long un_response_code) {
		order_id = un_orderId;
		token_socialauth = un_token_socialauth;
		token_compra = un_token_compra;
		response_code = un_response_code;
		fecha_compra= Calendar.getInstance().getTimeInMillis();
	}

	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	public String getToken_socialauth() {
		return token_socialauth;
	}

	public void setToken_socialauth(String token_socialauth) {
		this.token_socialauth = token_socialauth;
	}

	public String getToken_compra() {
		return token_compra;
	}

	public void setToken_compra(String token_compra) {
		this.token_compra = token_compra;
	}

	public Long getResponse_code() {
		return response_code;
	}

	public void setResponse_code(Long response_code) {
		this.response_code = response_code;
	}

	public Long getFecha_compra() {
		return fecha_compra;
	}

	public void setFecha_compra(Long fecha_compra) {
		this.fecha_compra = fecha_compra;
	}
}
