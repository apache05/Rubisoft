package com.rubisoft.womencuddles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.rubisoft.womencuddles.R;
import com.rubisoft.womencuddles.tools.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Activity_Politica_Privacidad extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_politica_privacidad);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
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
    public void onBackPressed() {
        // super.onBackPressed();
        Intent mIntent = new Intent(this, Activity_Principal.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mIntent);
        finish();
    }
}