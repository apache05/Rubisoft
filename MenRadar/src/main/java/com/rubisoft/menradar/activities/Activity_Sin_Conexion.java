package com.rubisoft.menradar.activities;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial.Icon;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.menradar.R;

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
        setContentView(R.layout.layout_sin_conexion);
        Typeface typeFace_roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        TextView mTextView_no_hay_conexion = findViewById(R.id.Layout_sin_conexion_TextView_NO_HAY_CONEXION);
        mTextView_no_hay_conexion.setTypeface(typeFace_roboto);
        mTextView_no_hay_conexion.setText(getResources().getString(R.string.ACTIVITY_SIN_CONEXION_NO_HAY_CONEXION));


        Drawable fondo = new IconicsDrawable(this.getApplicationContext())
                .icon(Icon.gmd_signal_wifi_off)
                .color(ContextCompat.getColor(this.getApplicationContext(), R.color.gris_transparente));
        LinearLayout LinearLayout_fondo = this.findViewById(R.id.Layout_sin_conexion_LinearLayout);
        LinearLayout_fondo.setBackground(fondo);
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
