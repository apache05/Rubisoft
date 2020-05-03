package com.rubisoft.lesbiancuddles.activities;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;

import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.lesbiancuddles.R;
import com.rubisoft.lesbiancuddles.databinding.LayoutSinConexionBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
// **********************************************************************************************************
// Esta activity aparecerá en cualquier instante que el Usuario_para_listar haga una acción y no haya conexión
// Muestra un mensaje avisando al Usuario_para_listar que no tiene conexión
// **********************************************************************************************************

public class Activity_Sin_Conexion extends AppCompatActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        super.onCreate(savedInstanceState);
		com.rubisoft.lesbiancuddles.databinding.LayoutSinConexionBinding binding = LayoutSinConexionBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		Typeface typeFace_roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        binding.LayoutSinConexionTextViewNOHAYCONEXION.setTypeface(typeFace_roboto);
		binding.LayoutSinConexionTextViewNOHAYCONEXION.setText(getResources().getString(R.string.ACTIVITY_SIN_CONEXION_NO_HAY_CONEXION));


        Drawable fondo = new IconicsDrawable(this.getApplicationContext())
                .icon(Icon.gmd_signal_wifi_off)
                .color(ContextCompat.getColor(this.getApplicationContext(), R.color.gris_transparente));
		binding.LayoutSinConexionLinearLayout.setBackground(fondo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

}
