package com.rubisoft.mencuddles.activities;

import android.R.layout;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appyvet.rangebar.RangeBar;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.mencuddles.Adapters.Drawer_Adapter;
import com.rubisoft.mencuddles.Classes.Drawer_Item;
import com.rubisoft.mencuddles.Classes.MultiSpinner;
import com.rubisoft.mencuddles.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.mencuddles.R;
import com.rubisoft.mencuddles.tools.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Activity_Configura_Radar extends AppCompatActivity {
	private int radio_de_busqueda;
	private SharedPreferences busqueda_usuario;
	private SharedPreferences perfil_usuario;
	private SharedPreferences preferencias_usuario;  //aquí guardaremos el idioma y las unidades que prefiere el Usuario_para_listar. no lo borraremos nunca

	private Spinner Spinner_personas_que_busco;
	private MultiSpinner Spinner_raza_persona;
	private RangeBar RangeBar_radio_persona;
	private RangeBar RangeBar_edad_persona;
	private RangeBar RangeBar_altura_persona;
	private RangeBar RangeBar_peso_persona;
	private TextView TextView_min_altura_persona;
	private TextView TextView_max_altura_persona;
	private TextView TextView_min_peso_persona;
	private TextView TextView_max_peso_persona;
	private TextView TextView_min_edad_persona;
	private TextView TextView_max_edad_persona;
	private TextView TextView_radio_persona;
	private TextView TextView_raza_persona;
	private TextView TextView_Rango_edades_persona;
	private TextView TextView_peso_entre_persona;
	private TextView TextView_altura_entre_persona;

	private TextView TextView_que_este_online_para_persona;
	private TextView TextView_que_sea_nuevo_para_persona;
	private TextView TextView_solo_premium_persona;

	private ImageView ImageView_Actualizar_radar_personas;

	private SwitchCompat SwitchCompat_que_sea_nuevo_personas;
	private SwitchCompat SwitchCompat_que_este_online_personas;

	//navigation drawer
	private Toolbar toolbar;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout mDrawerLayout;
	private RecyclerView recyclerViewDrawer;
	private ImageView mImageView_PictureMain;

	private LinearLayout Main_LinearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
		super.onCreate(savedInstanceState);

		try {
			if (utils.isNetworkAvailable((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))) {
				perfil_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);
				if (perfil_usuario.getString(getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "").isEmpty()) {
					salir();
				} else {
					setContentView(R.layout.layout_configura_radar);
					busqueda_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_BUSQUEDAS_USUARIO), Context.MODE_PRIVATE);
					preferencias_usuario = getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PREFERENCIAS_USUARIO), Context.MODE_PRIVATE);

					//Inicializamos las opciones tal y como se guardaron
					set_default_radio_busqueda();
					setup_Views_personas();

					setup_spinners_personas();

					setup_rangebars_personas();
					setup_Typeface_personas();

					set_texts_comunes();
					set_texts_personas();
					set_texts_colors_personas();

					setup_switchcompats_personas();
					setup_RangeBar_listeners_Personas();
					setup_Actualizar_radar_personas();

					inicializa_anuncios();
					setup_toolbar();// Setup toolbar and statusBar (really FrameLayout)
				}
			} else {
				Intent mIntent = new Intent(this, Activity_Sin_Conexion.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(mIntent);
				finish();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onCreate de configura_radar");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_options, menu);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (drawerToggle != null) {
			drawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		utils.gestiona_menu(item.getItemId(), this);

		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			Appodeal.onResume(this, Appodeal.BANNER_TOP);

			setupNavigationDrawer();// Setup navigation drawer
		} catch (Exception e) {
			utils.registra_error(e.toString(), "onResume de Configura Radar");
		}
	}

	@Override
	public void onBackPressed() {
			actualizar_radar_personas();

	}

	private void set_default_radio_busqueda() {
		radio_de_busqueda = this.perfil_usuario.getBoolean(this.getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false) ? getResources().getInteger(R.integer.DEFAULT_RADIO) : getResources().getInteger(R.integer.DEFAULT_RADIO) / 2;
	}

	private void crea_ItemTouchListener_menu() {
		//Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
		//y que lo ha detectado y codificado primero el  RecyclerTouchListener
		recyclerViewDrawer.addOnItemTouchListener(new Activity_Configura_Radar.RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Configura_Radar.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				mDrawerLayout.closeDrawers();
			}
		}));
	}

	private void setup_Views_personas() {
		try {
			ImageView_Actualizar_radar_personas = findViewById(R.id.Layout_configura_radar_de_persona_Button_actualizar_configuracion_radar);
			Spinner_personas_que_busco = findViewById(R.id.Layout_configura_radar_de_persona_spinner_que_busca);

			Spinner_raza_persona = findViewById(R.id.Layout_configura_radar_de_persona_Spinner_razas);
			RangeBar_radio_persona = findViewById(R.id.Layout_configura_radar_de_persona_RangeBar_radio);
			RangeBar_edad_persona = findViewById(R.id.Layout_configura_radar_de_persona_RangeBar_edad);
			RangeBar_altura_persona = findViewById(R.id.Layout_configura_radar_de_persona_RangeBar_altura);
			RangeBar_peso_persona = findViewById(R.id.Layout_configura_radar_de_persona_RangeBar_peso);


			SwitchCompat_que_este_online_personas = findViewById(R.id.Layout_configura_radar_de_persona_SwitchCompat_que_este_online);
			SwitchCompat_que_sea_nuevo_personas = findViewById(R.id.Layout_configura_radar_de_persona_SwitchCompat_que_sea_nuevo);


			TextView_min_altura_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_min_altura);
			TextView_max_altura_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_max_altura);
			TextView_min_peso_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_min_peso);
			TextView_max_peso_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_max_peso);
			TextView_min_edad_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_min_edad);
			TextView_max_edad_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_max_edad);
			TextView_radio_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_radio);
			TextView_raza_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_raza);
			TextView_Rango_edades_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_Rango_edades);
			TextView_altura_entre_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_altura_entre);
			TextView_peso_entre_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_peso_entre);
			TextView_peso_entre_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_peso_entre);
			TextView_peso_entre_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_peso_entre);

			TextView_que_este_online_para_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_que_este_online);
			TextView_que_sea_nuevo_para_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_que_sea_nuevo);
			TextView_solo_premium_persona = findViewById(R.id.Layout_configura_radar_de_persona_TextView_only_premiums);
			Main_LinearLayout = findViewById(R.id.Main_LinearLayout);
			mDrawerLayout = findViewById(R.id.mDrawerLayout);

		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_Views_personas de configura_radar");
		}
	}

	private void setup_Typeface_personas() {
		try {
			Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
			Typeface typeFace_roboto_Bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

			TextView_min_altura_persona.setTypeface(typeFace_roboto_Light);
			TextView_max_altura_persona.setTypeface(typeFace_roboto_Light);
			TextView_min_peso_persona.setTypeface(typeFace_roboto_Light);
			TextView_max_peso_persona.setTypeface(typeFace_roboto_Light);
			TextView_min_edad_persona.setTypeface(typeFace_roboto_Light);
			TextView_max_edad_persona.setTypeface(typeFace_roboto_Light);
			TextView_radio_persona.setTypeface(typeFace_roboto_Bold);
			TextView_raza_persona.setTypeface(typeFace_roboto_Bold);
			TextView_Rango_edades_persona.setTypeface(typeFace_roboto_Bold);
			TextView_altura_entre_persona.setTypeface(typeFace_roboto_Bold);
			TextView_peso_entre_persona.setTypeface(typeFace_roboto_Bold);

			Spinner_raza_persona.setTypeface(typeFace_roboto_Light);

			TextView_que_sea_nuevo_para_persona.setTypeface(typeFace_roboto_Bold);
			TextView_que_este_online_para_persona.setTypeface(typeFace_roboto_Bold);
			TextView_solo_premium_persona.setTypeface(typeFace_roboto_Bold);

		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_Typeface_personas de configura_radar");
		}
	}

	private void setup_spinners_personas() {
		try {
			Spinner_raza_persona.setAllText(getResources().getString(R.string.ACTIVITY_CONFIGURA_RADAR_CUALQUIER_RAZA));
			ArrayAdapter<String> adapter;

			adapter= new ArrayAdapter<>(this, R.layout.spinner_item);
			adapter.addAll(getResources().getStringArray(R.array.razas));
			Spinner_raza_persona.setAdapter(adapter, false, null);

			Set default_razas = new HashSet();
			for (int i = 0; i < getResources().getStringArray(R.array.razas).length; i++) {
				default_razas.add(i);
			}
			boolean[] selectedItems = new boolean[adapter.getCount()];
			Set razas_seleccionadas = busqueda_usuario.getStringSet(getResources().getString(R.string.BUSQUEDA_PERSONAS_RAZA), default_razas);
			//Las pasamos a un array de booleanos
			for (int j = 0; j < getResources().getStringArray(R.array.razas).length; j++) {
				// select second item
				selectedItems[j] = razas_seleccionadas.contains(j);
			}
			Spinner_raza_persona.setSelected(selectedItems);

			ArrayAdapter<CharSequence> adapter_que_busca;
			adapter_que_busca = ArrayAdapter.createFromResource(this, R.array.busqueda_personas, R.layout.spinner_item);
			adapter_que_busca.setDropDownViewResource(layout.simple_spinner_dropdown_item);
			Spinner_personas_que_busco.setAdapter(adapter_que_busca);
			adapter_que_busca.notifyDataSetChanged();

			Spinner_personas_que_busco.setSelection((int)busqueda_usuario.getLong(getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA), 0));
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_spinners_personas de configura_radar");
		}
	}

	private void setup_rangebars_personas() {
		//Con este método ponemos los controles tal como hizo la última búsqueda
		try {
			//HACEMOS ESTO POR SEGURIDAD
			SharedPreferences.Editor editor_busqueda_usuario = busqueda_usuario.edit();
			int altura_minima = this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA);
			int altura_maxima = this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA);
			int edad_minima = this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA);
			int edad_maxima = this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA);
			int peso_minimo = this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO);
			int peso_maximo = this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO);

			if ((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) < this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));
			} else {
				altura_minima = (int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));
			}
			if ((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) > this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
			} else {
				altura_maxima = (int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
			}

			if ((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) < this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO));
			} else {
				peso_minimo = (int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO));
			}
			if ((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) > this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO));
			} else {
				peso_maximo = (int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO));
			}

			if ((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_EDAD_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA)) < this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA)) {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA));
			} else {
				edad_minima = (int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_EDAD_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA));
			}
			if ((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA)) > this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA)) {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA));
			} else {
				edad_maxima = (int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA));
			}

			if ((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda) > radio_de_busqueda) {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda);
			} else {
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_RADIO), (int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda));
			}
			editor_busqueda_usuario.apply();

			RangeBar_radio_persona.setTickEnd(radio_de_busqueda);

			RangeBar_radio_persona.setSeekPinByValue((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda));
			RangeBar_edad_persona.setRangePinsByValue(edad_minima, edad_maxima);
			RangeBar_altura_persona.setRangePinsByValue(altura_minima, altura_maxima);
			RangeBar_peso_persona.setRangePinsByValue(peso_minimo, peso_maximo);
		} catch (IllegalArgumentException ignore){

		}catch (Exception e) {
			utils.registra_error(e.toString(), "setup_rangebars_personas de configura_radar");
		}
	}

	private void setup_switchcompats_personas(){
		if (this.perfil_usuario.getBoolean(this.getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
			SwitchCompat_que_sea_nuevo_personas.setEnabled(true);
			SwitchCompat_que_este_online_personas.setEnabled(true);
			SwitchCompat_que_sea_nuevo_personas.setChecked(this.busqueda_usuario.getBoolean(this.getResources().getString(R.string.BUSQUEDA_QUE_SEA_NUEVO), false));
			SwitchCompat_que_este_online_personas.setChecked(this.busqueda_usuario.getBoolean(this.getResources().getString(R.string.BUSQUEDA_QUE_ESTE_ONLINE), false));
		}else{
			SwitchCompat_que_sea_nuevo_personas.setChecked(false);
			SwitchCompat_que_este_online_personas.setChecked(false);
			SwitchCompat_que_sea_nuevo_personas.setEnabled(false);
			SwitchCompat_que_este_online_personas.setEnabled(false);
		}
	}

	private void set_texts_comunes() {
		DecimalFormat formatter = new DecimalFormat("#.##");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(dfs);

	}

	private void set_texts_personas() {
		try {
			switch ((int) preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0)) {
				case 0:  //METRICA
					TextView_radio_persona.setText(String.format(getResources().getString(R.string.ACTIVITY_CONFIGURA_RADAR_DISTANCIA_KM),(int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda)));
					TextView_min_peso_persona.setText(String.format(getResources().getString(R.string.kg), busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO))));
					TextView_max_peso_persona.setText(String.format(getResources().getString(R.string.kg), busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO))));
					TextView_min_altura_persona.setText(String.format(getResources().getString(R.string.m), (float) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) / 100));
					TextView_max_altura_persona.setText(String.format(getResources().getString(R.string.m), (float) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) / 100));
					break;
				case 1:  //BRITANICA
					TextView_radio_persona.setText(String.format(getResources().getString(R.string.ACTIVITY_CONFIGURA_RADAR_DISTANCIA_MI), utils.km_a_mi((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda))));
					Pair un_par_min = utils.kg_a_st_and_lb((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)));
					TextView_min_peso_persona.setText(getResources().getString(R.string.st_y_lb,(int) un_par_min.first, (double)un_par_min.second));
					Pair un_par_max = utils.kg_a_st_and_lb((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)));
					TextView_max_peso_persona.setText(getResources().getString(R.string.st_y_lb, (int)un_par_max.first,(double) un_par_max.second));
					TextView_min_altura_persona.setText(utils.cm_a_feet_and_inches((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA))));
					TextView_max_altura_persona.setText(utils.cm_a_feet_and_inches((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA))));

					break;
				case 2:   //AMERICANA
					TextView_radio_persona.setText(String.format(getResources().getString(R.string.ACTIVITY_CONFIGURA_RADAR_DISTANCIA_MI), utils.km_a_mi((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_RADIO), radio_de_busqueda))));
					TextView_min_peso_persona.setText(utils.kg_a_lb((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO))));
					TextView_max_peso_persona.setText(utils.kg_a_lb((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO))));
					TextView_min_altura_persona.setText(utils.cm_a_feet_and_inches((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA))));
					TextView_max_altura_persona.setText(utils.cm_a_feet_and_inches((int) busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA))));

					break;
			}
			TextView_min_edad_persona.setText(String.format(getResources().getString(R.string.anyos), busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_EDAD_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA))));
			TextView_max_edad_persona.setText(String.format(getResources().getString(R.string.anyos), busqueda_usuario.getLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA))));

		} catch (Exception e) {
			utils.registra_error(e.toString(), "set_texts_personas de configura_radar");
		}
	}

	private void setup_RangeBar_listeners_Personas() {
		RangeBar_radio_persona.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
			try {
				//Debemos mostrar las unidades de distancia acorde a los acorde a los distintos usos de cada región
				switch ((int) preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0)) {
					case 0: //METRICA
						TextView_radio_persona.setText(String.format(getResources().getString(R.string.ACTIVITY_CONFIGURA_RADAR_DISTANCIA_KM), Integer.valueOf(RangeBar_radio_persona.getRightPinValue())));
						break;
					case 1:  //BRITANICA
					case 2:  //AMERICANA
						TextView_radio_persona.setText(String.format(getResources().getString(R.string.ACTIVITY_CONFIGURA_RADAR_DISTANCIA_MI), utils.km_a_mi(Integer.valueOf(RangeBar_radio_persona.getRightPinValue()))));
						break;
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "setup_RangeBar_listeners_Personas");

			}
		});
		RangeBar_peso_persona.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
			try {
				//Debemos mostrar las unidades de peso acorde a los acorde a los distintos usos de cada región
				switch ((int) preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0)) {
					case 0: //METRICA
						TextView_min_peso_persona.setText(String.format(getResources().getString(R.string.kg), Integer.valueOf(RangeBar_peso_persona.getLeftPinValue())));
						TextView_max_peso_persona.setText(String.format(getResources().getString(R.string.kg), Integer.valueOf(RangeBar_peso_persona.getRightPinValue())));
						break;
					case 1:  //BRITANICA
						Pair un_par_min = utils.kg_a_st_and_lb(Integer.valueOf(RangeBar_peso_persona.getLeftPinValue()));
						TextView_min_peso_persona.setText(getResources().getString(R.string.st_y_lb, (int)un_par_min.first, (double)un_par_min.second));
						Pair un_par_max = utils.kg_a_st_and_lb(Integer.valueOf(RangeBar_peso_persona.getRightPinValue()));
						TextView_max_peso_persona.setText(getResources().getString(R.string.st_y_lb, (int)un_par_max.first, (double)un_par_max.second));
						break;
					case 2:  //AMERICANA
						TextView_min_peso_persona.setText(utils.kg_a_lb(Integer.valueOf(RangeBar_peso_persona.getLeftPinValue())));
						TextView_max_peso_persona.setText(utils.kg_a_lb(Integer.valueOf(RangeBar_peso_persona.getRightPinValue())));
						break;
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "setup_RangeBar_listeners_Personas");
			}
		});
		RangeBar_altura_persona.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
			try {
				//Debemos mostrar las unidades de altura acorde a los acorde a los distintos usos de cada región
				switch ((int) preferencias_usuario.getLong(getResources().getString(R.string.PREFERENCIAS_UNIDADES), 0)) {
					case 0:  //METRICA
						Float altura_min = Float.valueOf(RangeBar_altura_persona.getLeftPinValue()) / 100; //lo pasamos a metros
						Float altura_max = Float.valueOf(RangeBar_altura_persona.getRightPinValue()) / 100;
						TextView_min_altura_persona.setText(String.format(getResources().getString(R.string.m), altura_min));
						TextView_max_altura_persona.setText(String.format(getResources().getString(R.string.m), altura_max));
						break;
					case 1: //BRITANICA
					case 2: //AMERICANA
						TextView_min_altura_persona.setText(utils.cm_a_feet_and_inches(Integer.valueOf(RangeBar_altura_persona.getLeftPinValue())));
						TextView_max_altura_persona.setText(utils.cm_a_feet_and_inches(Integer.valueOf(RangeBar_altura_persona.getRightPinValue())));
						break;
				}
			} catch (Exception e) {
				utils.registra_error(e.toString(), "setup_RangeBar_listeners_Personas");

			}
		});
		RangeBar_edad_persona.setOnRangeBarChangeListener((rangeBar, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
			try {


				if (Integer.valueOf(RangeBar_edad_persona.getLeftPinValue()) >= 18) {
					TextView_min_edad_persona.setText(String.format(getResources().getString(R.string.anyos), Integer.valueOf(RangeBar_edad_persona.getLeftPinValue())));
				} else {
					TextView_min_edad_persona.setText(String.format(getResources().getString(R.string.anyos), 18));
				}
				if (Integer.valueOf(RangeBar_edad_persona.getRightPinValue()) <= 99) {
					TextView_max_edad_persona.setText(String.format(getResources().getString(R.string.anyos), Integer.valueOf(RangeBar_edad_persona.getRightPinValue())));
				} else {
					TextView_max_edad_persona.setText(String.format(getResources().getString(R.string.anyos), 99));
				}
			}catch (Exception e){
				utils.registra_error(e.toString(), "setup_RangeBar_listeners_Personas");

			}
		});

	}

	private void actualizar_radar_personas(){
		try {
			if (Spinner_raza_persona.hay_alguno_seleccionado()) { //al menos debe elegir una raza

				SharedPreferences.Editor editor_busqueda_usuario = busqueda_usuario.edit();

				if (Long.valueOf(RangeBar_altura_persona.getRightPinValue()) > this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA)) {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MAXIMA));
				} else {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MAXIMA), Long.valueOf(RangeBar_altura_persona.getRightPinValue()));
				}
				if (Long.valueOf(RangeBar_altura_persona.getLeftPinValue()) < this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA)) {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_ALTURA_MINIMA));
				} else {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_ALTURA_MINIMA), Long.valueOf(RangeBar_altura_persona.getLeftPinValue()));
				}
				if (Long.valueOf(RangeBar_peso_persona.getRightPinValue()) > this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO)) {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MAXIMO));
				} else {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MAXIMO), Long.valueOf(RangeBar_peso_persona.getRightPinValue()));
				}

				if (Long.valueOf(RangeBar_peso_persona.getLeftPinValue()) < this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO)) {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), this.getResources().getInteger(R.integer.DEFAULT_PESO_MINIMO));
				} else {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_PESO_MINIMO), Long.valueOf(RangeBar_peso_persona.getLeftPinValue()));
				}

				if (Long.valueOf(RangeBar_edad_persona.getRightPinValue()) > this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA)) {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MAXIMA));
				} else {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MAXIMA), Long.valueOf(RangeBar_edad_persona.getRightPinValue()));
				}

				if (Long.valueOf(RangeBar_edad_persona.getLeftPinValue()) < this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA)) {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MINIMA), this.getResources().getInteger(R.integer.DEFAULT_EDAD_MINIMA));
				} else {
					editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_EDAD_MINIMA), Long.valueOf(RangeBar_edad_persona.getLeftPinValue()));
				}
				Set razas = new HashSet();
				for (int i = 0; i < getResources().getStringArray(R.array.razas).length; i++) {
					if (Spinner_raza_persona.getSelected()[i]) {
						razas.add(i);
					}
				}
				editor_busqueda_usuario.putStringSet(getResources().getString(R.string.BUSQUEDA_PERSONAS_RAZA), razas);
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_RADIO), Long.valueOf(RangeBar_radio_persona.getRightPinValue()));
				editor_busqueda_usuario.putBoolean(getResources().getString(R.string.BUSQUEDA_QUE_ESTE_ONLINE),SwitchCompat_que_este_online_personas.isChecked());
				editor_busqueda_usuario.putBoolean(getResources().getString(R.string.BUSQUEDA_QUE_SEA_NUEVO),SwitchCompat_que_sea_nuevo_personas.isChecked());
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),getResources().getInteger(R.integer.HETERO));

				switch (Spinner_personas_que_busco.getSelectedItemPosition()) {
					case 0: //busca mujeres hetero
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),getResources().getInteger(R.integer.HETERO));
						break;
					case 1: // busca hombres hetero
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA),getResources().getInteger(R.integer.HOMBRE));
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA),getResources().getInteger(R.integer.HETERO));
						break;

					case 2: //busca hombres gay
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA),getResources().getInteger( R.integer.HOMBRE));
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.GAY));
						break;
					case 3: //busca lesbianas
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.LESBIANA));
						break;

					case 4: //busca mujeres bisexuales
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.MUJER));
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.BISEX));
						break;
					case 5: //busca hombres bisexuales
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_SEXO_QUE_BUSCA), getResources().getInteger(R.integer.HOMBRE));
						editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_ORIENTACION_QUE_BUSCA), getResources().getInteger(R.integer.BISEX));
						break;

				}
				editor_busqueda_usuario.putLong(getResources().getString(R.string.BUSQUEDA_PERSONAS_QUE_BUSCA),Spinner_personas_que_busco.getSelectedItemPosition());
				editor_busqueda_usuario.apply();

				borra_cached_search(getCacheDir());

				Intent mIntent = new Intent(this, Activity_Principal.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(mIntent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.ACTIVITY_CONFIGURA_RADAR_AL_MENOS_1_RAZA), Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "actualizar_radar_personas");
		}
	}

	private void borra_cached_search(File file){
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory()) {
						borra_cached_search(f);
					} else {
						f.delete();
					}
				}
			}
		}
	}

	private void setup_Actualizar_radar_personas() {
		Drawable icono1 = new IconicsDrawable(this).icon(Icon.gmd_done).color(ContextCompat.getColor(this, R.color.accent)).sizeDp(getResources().getInteger(R.integer.Tam_Normal_icons));
		ImageView_Actualizar_radar_personas.setImageDrawable(icono1);
		ImageView_Actualizar_radar_personas.setOnClickListener(v -> actualizar_radar_personas());
	}

	private void set_texts_colors_personas() {
		try {
			if (!this.perfil_usuario.getBoolean(this.getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				TextView_que_este_online_para_persona.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris_semi_transparente));
				TextView_que_sea_nuevo_para_persona.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gris_semi_transparente));

			}
		} catch (Exception e) {
			utils.registra_error(e.toString(), "set_texts_colors_personas");
		}
	}

	private void inicializa_anuncios(){
		try{
			if (!perfil_usuario.getBoolean(getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				Consent consent = ConsentManager.getInstance(this).getConsent();
				Appodeal.setTesting(false);
				Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER, consent);
				setup_banner();
			}else {
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.setMargins(0, 0, 0, 0);
				if (mDrawerLayout!=null){
					mDrawerLayout.setLayoutParams(layoutParams);
				}else{
					Main_LinearLayout.setLayoutParams(layoutParams);
				}
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "inicializa_anuncios de Activity_Principal");
		}
	}

	private void setup_banner() {
		try {
			Appodeal.setBannerCallbacks(new BannerCallbacks() {
				@Override
				public void onBannerLoaded(int height, boolean isPrecache) {

				}
				@Override
				public void onBannerFailedToLoad() {

				}
				@Override
				public void onBannerShown() {

				}
				@Override
				public void onBannerClicked() {

				}
				@Override
				public void onBannerExpired() {

				}
				@Override
				public void onBannerShowFailed() {

				}
			});
			Appodeal.show(this, Appodeal.BANNER_TOP);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_banner de Activity_Ayuda");
		}
	}

	private void setup_toolbar() {
		try {
			// Setup toolbar and statusBar (really FrameLayout)
			toolbar = findViewById(R.id.mToolbar);
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
			getSupportActionBar().setDisplayHomeAsUpEnabled(!utils.isTablet(this));
			getSupportActionBar().setHomeButtonEnabled(true);
		} catch (Exception e) {
			utils.registra_error(e.toString(), "setup_toolbar");
		}

	}

	private void setupNavigationDrawer() {
		try {
			if (mDrawerLayout!=null) {
				// Setup Drawer Icon
				drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
				mDrawerLayout.addDrawerListener(drawerToggle);
				drawerToggle.syncState();

				TypedValue typedValue = new TypedValue();
				int color = typedValue.data;
				mDrawerLayout.setStatusBarBackgroundColor(color);
			}
			// Setup RecyclerViews inside drawer
			setupNavigationDrawerRecyclerViews();

			mImageView_PictureMain = findViewById(R.id.mImageView_PictureMain);
			TextView mTextView_Name = findViewById(R.id.Drawer_TextView_Name);
			TextView mTextView_Numero_estrellas = findViewById(R.id.Drawer_TextView_Num_Estrellas);
			TextView mTextView_Premium_User = findViewById(R.id.Drawer_TextView_Premium_User);

			Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

			mTextView_Name.setTypeface(typeFace_roboto_Light);
			mTextView_Numero_estrellas.setTypeface(typeFace_roboto_Light);

			new AsyncTask_Coloca_PictureMain().execute();

			mTextView_Name.setText(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_NICK), ""));
			mTextView_Name.setTextColor(ContextCompat.getColor(this, R.color.white));

			mTextView_Numero_estrellas.setText(String.format(getResources().getString(R.string.numero),perfil_usuario.getLong(getResources().getString(R.string.PERFIL_USUARIO_ESTRELLAS), 0)));
			mTextView_Numero_estrellas.setTextColor(ContextCompat.getColor(this, R.color.white));

			if (perfil_usuario.getBoolean(getResources().getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)){
				mTextView_Premium_User.setText(getResources().getString(R.string.premium_user));
			}
			Drawable icono_estrella;
			icono_estrella = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_star).color(ContextCompat.getColor(this, R.color.white));
			ImageView mImageView_Estrella = findViewById(R.id.Drawer_ImageView_estrella);
			mImageView_Estrella.setImageDrawable(icono_estrella);

		} catch (Exception e) {
			utils.registra_error(e.toString(), "setupNavigationDrawer de Activity_Configura_Radar");
		}
	}

	private void setupNavigationDrawerRecyclerViews() {
		try {
			recyclerViewDrawer = findViewById(R.id.recyclerViewDrawer);
			recyclerViewDrawer.setLayoutManager(new LinearLayoutManager(this));

			ArrayList<Drawer_Item> drawerItems = new ArrayList<>();
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_track_changes).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[0]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_tune).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[1]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_email).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[2]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_perm_identity).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[3]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_star_border).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[4]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_attach_money).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[5]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(Icon.gmd_forum).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[6]));
			//drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_whatshot).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[7]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_help_outline).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[7]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[8]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_thumbs_up_down).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[9]));
			drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_power_settings_new).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[10]));
			RecyclerView.Adapter drawerAdapter = new Drawer_Adapter(drawerItems);
			recyclerViewDrawer.setAdapter(drawerAdapter);
			recyclerViewDrawer.setMinimumHeight(utils.Dp2Px(144, this));
			recyclerViewDrawer.setHasFixedSize(true);
			crea_ItemTouchListener_menu();

			Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
			for (int i = 0; i < recyclerViewDrawer.getChildCount(); i++) {
				TextView Drawer_item_TextView_title = recyclerViewDrawer.getChildAt(i).findViewById(R.id.Drawer_item_TextView_title);
				Drawer_item_TextView_title.setTypeface(typeFace_roboto_Light);
			}
		} catch (Exception ignored) {
		}
	}

	private void salir() {
		//Si hemos llegado de rebote aquí sin tener una sesión abierta, debemos salir

		finish();
	}

	private class AsyncTask_Coloca_PictureMain extends AsyncTask<Void, Void, Bitmap> {
		@Override
		protected void onPreExecute() {
		}

		@Nullable
		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap mBitmap = null;
			try {

				String nombre_thumb = utils.get_nombre_thumb(perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""), 0);
				File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

				File file = new File(storageDir, nombre_thumb);
				mBitmap = file.exists() ? utils.decodeSampledBitmapFromFilePath(file.getPath(), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles)) : null;
			} catch (Exception ignored) {
			} return mBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap mBitmap) {
			try {
				AppCompatImageView mAppCompatImageView = new AppCompatImageView(getApplicationContext());
				LinearLayout.LayoutParams mlayoutParams_perfil = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_perfiles));
				mAppCompatImageView.setLayoutParams(mlayoutParams_perfil);
				if (mBitmap != null) {
					mAppCompatImageView.setImageBitmap(mBitmap);
					mAppCompatImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				} else {
					mAppCompatImageView.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary_light)));
				}
				mImageView_PictureMain.setImageDrawable(mAppCompatImageView.getDrawable());
			}catch (Exception e) {
				utils.registra_error(e.toString(), "AsyncTask_Coloca_PictureMain");
			}
		}
	}

	private class RecyclerTouchListener_menu implements RecyclerView.OnItemTouchListener {
		@Nullable
		private final GestureDetector mGestureDetector;
		private final Interface_ClickListener_Menu mInterfaceClickListener;

		RecyclerTouchListener_menu(Context context, @NonNull RecyclerView recyclerView, Interface_ClickListener_Menu un_Interface_ClickListener) {
			mInterfaceClickListener = un_Interface_ClickListener;
			mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

				@Override
				public boolean onSingleTapUp(@NonNull MotionEvent e) {
					View child = recyclerViewDrawer.findChildViewUnder(e.getX(), e.getY());
					if ((child != null) && (mInterfaceClickListener != null)) {
						mInterfaceClickListener.My_onClick(child, recyclerView.getChildLayoutPosition(child));
					}
					return true;
				}

			});
		}

		@Override
		public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
			//View child= rv.findChildViewUnder(e.getX(),e.getY());
			//mGestureDetector.onTouchEvent llamará al método mas adecuado
			mGestureDetector.onTouchEvent(e);
			return false;
		}

		@Override
		public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

		}

		@Override
		public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

		}
	}
}