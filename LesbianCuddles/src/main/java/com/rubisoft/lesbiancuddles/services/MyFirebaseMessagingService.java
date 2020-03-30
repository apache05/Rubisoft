package com.rubisoft.lesbiancuddles.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rubisoft.lesbiancuddles.R;
import com.rubisoft.lesbiancuddles.tools.utils;

import java.util.Map;

import androidx.annotation.NonNull;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
	@Override
	public void onNewToken(@NonNull String un_token_FCM) {
		SharedPreferences perfil_usuario= this.getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

		String TOKEN_SOCIALAUTH= perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
		if (TOKEN_SOCIALAUTH.isEmpty()) {
			SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
			editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_FCM), un_token_FCM);
			editor_perfil_usuario.apply();
		} else {
			actualiza_token_FCM(un_token_FCM, TOKEN_SOCIALAUTH);
			SharedPreferences.Editor editor_perfil_usuario = perfil_usuario.edit();
			editor_perfil_usuario.putString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_FCM), "");
			editor_perfil_usuario.apply();
		}
	}

	@Override  //la aplicación está en foreground
	public void onMessageReceived(@NonNull RemoteMessage message) {
		try {
			SharedPreferences perfil_usuario = this.getSharedPreferences(this.getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
			if (!perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
				Map data = message.getData();

				String nick_de_quien_lo_dijo = (String) data.get("nick_de_quien_lo_dijo");
				String tipo_mensaje = (String) data.get("tipo_mensaje");
				String quien_lo_dijo = (String) data.get("quien_lo_dijo");
				String que_dijo = (String) data.get("que_dijo");
				String cuando_lo_dijo = (String) data.get("cuando_lo_dijo");

				if (tipo_mensaje.equals(getResources().getString(R.string.TIPO_MENSAJE_INDIVIDUAL))) {
					play_sound_new_message_individual();
					send_orderedbroadcastintent_nuevo_mensaje(que_dijo,quien_lo_dijo,nick_de_quien_lo_dijo,cuando_lo_dijo);
				} else if (tipo_mensaje.equals(getResources().getString(R.string.TIPO_MENSAJE_GRUPAL))) {
					play_sound_new_message_chat();
					send_orderedbroadcastintent_chat_general(que_dijo,quien_lo_dijo,nick_de_quien_lo_dijo,cuando_lo_dijo);
				}
			}
		} catch(Exception e){
			utils.registra_error(e.toString(), "onMessageReceived de MyFirebaseMessagingService");
		}
	}

	private void actualiza_token_FCM(String Token_fcm,String Token_socialauth){
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		db.collection(getResources().getString(R.string.USUARIOS)).document(Token_socialauth).update(getResources().getString(R.string.USUARIO_TOKEN_FCM),Token_fcm);
	}

	private void send_orderedbroadcastintent_chat_general(String que_dijo, String quien_lo_dijo, String nick_de_quien_lo_dijo,String cuando_lo_dijo) {
		try {
			Intent intent = new Intent(getResources().getString(R.string.GOT_MESSAGE_CHAT_GENERAL));
			intent.putExtra("que_dijo", que_dijo);
			intent.putExtra("quien_lo_dijo", quien_lo_dijo);
			intent.putExtra("nick_de_quien_lo_dijo", nick_de_quien_lo_dijo);
			intent.putExtra("cuando_lo_dijo", cuando_lo_dijo); //realmente no lo utilizamos luego

			this.sendOrderedBroadcast(intent, null, new MyFirebaseMessagingService.MyBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
		} catch (Exception ignored) {
		}
	}

	private void send_orderedbroadcastintent_nuevo_mensaje(String que_dijo, String quien_lo_dijo, String nick_de_quien_lo_dijo,String cuando_lo_dijo) {
		try {
			Intent intent = new Intent(getResources().getString(R.string.GOT_MESSAGE_CHAT_INDIVIDUAL));
			intent.putExtra("que_dijo", que_dijo);
			intent.putExtra("quien_lo_dijo", quien_lo_dijo);
			intent.putExtra("nick_de_quien_lo_dijo", nick_de_quien_lo_dijo);
			intent.putExtra("cuando_lo_dijo", cuando_lo_dijo); //realmente no lo utilizamos luego

			this.sendOrderedBroadcast(intent, null, new MyFirebaseMessagingService.MyBroadcastReceiver(), null, Activity.RESULT_OK, null, null);
		} catch (Exception e) {
			// new utils.AsyncTask_registra_error().execute(new android.support.v4.util.Pair<>("","Exception en send_orderedbroadcastintent_chat de MyFirebaseMessagingService: "+ e));
		}
	}

	private static class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			/*
			 * to capture result after all broadreceivers are finished
			 * executing
			 */
		}
	}

	private void play_sound_new_message_individual(){
		MediaPlayer notification_sound = MediaPlayer.create(getApplicationContext(), R.raw.notification);
		if (notification_sound!=null) {
			notification_sound.start();
		}
	}

	private void play_sound_new_message_chat(){
		MediaPlayer notification_sound = MediaPlayer.create(getApplicationContext(), R.raw.new_message);
		if (notification_sound!=null) {
			notification_sound.start();
		}
	}
}