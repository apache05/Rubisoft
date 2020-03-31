package com.rubisoft.bisexradar.Classes;

import java.text.DecimalFormat;
import java.util.Calendar;

import androidx.annotation.Keep;

@Keep //NO QUITAR. SINO EN FIRESTORE SE GUARDAR LA INFORMACION MINIFICADA
public class Error  {
	private String metodo;
	private String error;
	private String fecha;
	private String version;
	private String sdk;
	private String app;

	public Error(String un_metodo,String un_error,String una_version,String un_sdk, String una_app) {
		metodo = un_metodo;
		error = un_error;
		version = una_version;
		sdk = un_sdk;
		app = una_app;
		fecha= construye_fecha(Calendar.getInstance().getTimeInMillis());
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSdk() {
		return sdk;
	}

	public void setSdk(String sdk) {
		this.sdk = sdk;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}
	private String construye_fecha(long milliseconds) {
		DecimalFormat formatter = new DecimalFormat("##");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(milliseconds);
		return formatter.format(cal.get(Calendar.DAY_OF_MONTH)) + '/' + formatter.format(cal.get(Calendar.MONTH) + 1) + '/' + formatter.format(cal.get(Calendar.YEAR));
	}

}