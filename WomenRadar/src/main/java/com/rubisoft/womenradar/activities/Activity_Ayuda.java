package com.rubisoft.womenradar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.explorestack.consent.Consent;
import com.explorestack.consent.ConsentManager;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.rubisoft.womenradar.Adapters.Drawer_Adapter;
import com.rubisoft.womenradar.Classes.Drawer_Item;
import com.rubisoft.womenradar.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.womenradar.R;
import com.rubisoft.womenradar.tools.utils;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Activity_Ayuda extends AppCompatActivity {
    private SharedPreferences perfil_usuario;

    //navigation drawer
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private RecyclerView recyclerViewDrawer;
    private ImageView mImageView_PictureMain;

	private LinearLayout Main_LinearLayout;

	private TextView TextView_nombre_aplicacion;
	private TextView TextView_version_aplicacion;
	private TextView TextView_explicacion_xradar_title;
	private TextView TextView_explicacion_xradar;
	private TextView TextView_principal;
	private TextView TextView_configura_radar;
	private TextView TextView_mensajes;
	private TextView TextView_mi_perfil;
	private TextView TextView_compras;
	private TextView TextView_ajustes;
	private TextView TextView_chat_general;
	private TextView TextView_estrellas;
	private TextView TextView_feedback;
	private TextView TextView_como_contactar;
	private TextView TextView_para_cualquier_consulta;
	private AppCompatButton AppCompatButton_rubisoft_email;
	private AppCompatButton AppCompatButton_rubisoft_twitter;
	private AppCompatImageView ImageView_principal;
	private AppCompatImageView ImageView_configura_radar;
	private AppCompatImageView ImageView_mensajes;
	private AppCompatImageView ImageView_mi_perfil;
	private AppCompatImageView ImageView_compras;
	private AppCompatImageView ImageView_ajustes;
	private AppCompatImageView ImageView_chat_general;
	private AppCompatImageView ImageView_estrellas;
	private AppCompatImageView ImageView_feedback;
	private AppCompatImageView ImageView_email;
	private AppCompatImageView ImageView_twitter;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            super.onCreate(savedInstanceState);

			setContentView(R.layout.layout_ayuda);
            perfil_usuario= getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

			setup_views();
            setup_toolbar();
			inicializa_anuncios();

            Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
            Typeface typeFace_roboto_Bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");


			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                TypedValue outValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);

                ImageView_email.setBackgroundResource(outValue.resourceId);
                ImageView_email.setClickable(true);
                ImageView_twitter.setBackgroundResource(outValue.resourceId);
                ImageView_twitter.setClickable(true);
            }

            ImageView_principal.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_track_changes).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_configura_radar.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_tune).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_mensajes.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_email).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_mi_perfil.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_perm_identity).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_compras.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attach_money).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_ajustes.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_chat_general.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_forum).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_feedback.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_thumbs_up_down).color(ContextCompat.getColor(this, R.color.gris)));
            ImageView_estrellas.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_star_border).color(ContextCompat.getColor(this, R.color.gris)));

            ImageView_email.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_email).color(ContextCompat.getColor(this, R.color.accent)));
            ImageView_twitter.setImageDrawable(utils.get_icono_twitter(getApplicationContext(),ContextCompat.getColor(this, R.color.accent)));
            ImageView_email.setOnClickListener(v -> manda_email());
            ImageView_twitter.setOnClickListener(v -> manda_twitter());

            TextView_nombre_aplicacion.setTypeface(typeFace_roboto_Bold);
            TextView_version_aplicacion.setTypeface(typeFace_roboto_Light);
            TextView_explicacion_xradar_title.setTypeface(typeFace_roboto_Bold);
            TextView_explicacion_xradar.setTypeface(typeFace_roboto_Light);
            TextView_principal.setTypeface(typeFace_roboto_Light);
            TextView_configura_radar.setTypeface(typeFace_roboto_Light);

            TextView_mi_perfil.setTypeface(typeFace_roboto_Light);
            TextView_compras.setTypeface(typeFace_roboto_Light);
            TextView_ajustes.setTypeface(typeFace_roboto_Light);
            TextView_chat_general.setTypeface(typeFace_roboto_Light);
            TextView_feedback.setTypeface(typeFace_roboto_Light);
            TextView_estrellas.setTypeface(typeFace_roboto_Light);
            TextView_mensajes.setTypeface(typeFace_roboto_Light);
            TextView_como_contactar.setTypeface(typeFace_roboto_Bold);
            TextView_para_cualquier_consulta.setTypeface(typeFace_roboto_Light);
            AppCompatButton_rubisoft_email.setTypeface(typeFace_roboto_Light);
            AppCompatButton_rubisoft_twitter.setTypeface(typeFace_roboto_Light);
            AppCompatButton_rubisoft_twitter.setTextColor(ContextCompat.getColor(this, R.color.accent));
            AppCompatButton_rubisoft_email.setTextColor(ContextCompat.getColor(this, R.color.accent));

            TextView_explicacion_xradar_title.setText(String.format(getResources().getString(R.string.help_info_title),getString(R.string.app_name)));
            TextView_explicacion_xradar.setText(String.format(getResources().getString(R.string.help_info),getString(R.string.app_name)));
            TextView_nombre_aplicacion.setText(getString(R.string.app_name));
            TextView_principal.setText(getResources().getString(R.string.help_principal));
            TextView_configura_radar.setText(getResources().getString(R.string.help_configura_radar));
            TextView_mensajes.setText((getResources().getString(R.string.help_mensajes)));
            TextView_mi_perfil.setText(String.format(getResources().getString(R.string.help_mi_perfil),getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM)));
            TextView_compras.setText(getResources().getString(R.string.help_compras));
            TextView_ajustes.setText(getResources().getString(R.string.help_ajustes));
            TextView_chat_general.setText(getResources().getString(R.string.help_chat_general));
			TextView_estrellas.setText( String.format(getResources().getString(R.string.help_ganar_estrellas),(getResources().getInteger(R.integer.LOGRO_COBRO_SEMANAL)*-1)));

            TextView_feedback.setText(getResources().getString(R.string.help_feedback));
            AppCompatButton_rubisoft_email.setText(getString(R.string.rubisoft_email));
            AppCompatButton_rubisoft_twitter.setText(getString(R.string.rubisoft_twitter));
            TextView_como_contactar.setText(String.format(getResources().getString(R.string.help_como_contactar),getString(R.string.app_name)));

            TextView_version_aplicacion.setText(String.format(getString(R.string.version),  utils.get_Version_Name(this)));

            AppCompatButton_rubisoft_email.setOnClickListener(v -> manda_email());

            AppCompatButton_rubisoft_twitter.setOnClickListener(v -> manda_twitter());
        }catch (Exception e){
            utils.registra_error(e.toString(), "oncreate de activity_ayuda");
        }
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
    protected void onResume() {
        super.onResume();

        // Setup navigation drawer
        setupNavigationDrawer();
		Appodeal.onResume(this, Appodeal.BANNER_TOP);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        Intent mIntent = new Intent(this, Activity_Principal.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mIntent);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerToggle != null) {
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    private void manda_email(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"rubisoft.apps@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My ID is "+perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), ""));

        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private void manda_twitter(){
        String tweetUrl = "https://twitter.com/intent/tweet?text=@Rubisoft_apps My ID is "+perfil_usuario.getString(getResources().getString(R.string.PERFIL_USUARIO_TOKEN_SOCIALAUTH), "");
        Uri uri = Uri.parse(tweetUrl);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private class RecyclerTouchListener_menu implements RecyclerView.OnItemTouchListener {
        @NonNull
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
        // Setup toolbar and statusBar (really FrameLayout)
        toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!utils.isTablet(this));
        getSupportActionBar().setHomeButtonEnabled(true);
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
			utils.registra_error(e.toString(), "setupNavigationDrawer de Activity_ayuda");
		}
	}

	private void setup_views(){
		TextView_nombre_aplicacion = findViewById(R.id.Layout_sobre_xradar_TextView_nombre_aplicacion);
		 TextView_version_aplicacion = findViewById(R.id.Layout_sobre_xradar_TextView_version_aplicacion);
		 TextView_explicacion_xradar_title = findViewById(R.id.Layout_sobre_xradar_TextView_explicacion_xradar_title);
		 TextView_explicacion_xradar = findViewById(R.id.Layout_sobre_xradar_TextView_explicacion_xradar);
		 TextView_principal = findViewById(R.id.Layout_sobre_xradar_TextView_principal);
		 TextView_configura_radar = findViewById(R.id.Layout_sobre_xradar_TextView_configura_radar);
		 TextView_mensajes = findViewById(R.id.Layout_sobre_xradar_TextView_mensajes);
		 TextView_mi_perfil = findViewById(R.id.Layout_sobre_xradar_TextView_mi_perfil);
		 TextView_compras = findViewById(R.id.Layout_sobre_xradar_TextView_compras);
		 TextView_ajustes = findViewById(R.id.Layout_sobre_xradar_TextView_ajustes);
		 TextView_chat_general = findViewById(R.id.Layout_sobre_xradar_TextView_chat_general);
		 TextView_estrellas = findViewById(R.id.Layout_sobre_xradar_TextView_estrellas);

		 TextView_feedback = findViewById(R.id.Layout_sobre_xradar_TextView_feedback);
		 TextView_como_contactar = findViewById(R.id.Layout_sobre_xradar_TextView_como_contactar);
		 TextView_para_cualquier_consulta = findViewById(R.id.Layout_sobre_xradar_TextView_para_cualquier_consulta);
		 AppCompatButton_rubisoft_email = findViewById(R.id.Layout_sobre_xradar_TextView_rubisoft_email);
		 AppCompatButton_rubisoft_twitter = findViewById(R.id.Layout_sobre_xradar_TextView_rubisoft_twitter);

		 ImageView_principal = findViewById(R.id.Layout_sobre_xradar_ImageView_principal);
		 ImageView_configura_radar = findViewById(R.id.Layout_sobre_xradar_configura_radar);
		 ImageView_mensajes = findViewById(R.id.Layout_sobre_xradar_ImageView_mensajes);
		 ImageView_mi_perfil = findViewById(R.id.Layout_sobre_xradar_ImageView_mi_perfil);
		 ImageView_compras = findViewById(R.id.Layout_sobre_xradar_ImageView_compras);
		 ImageView_ajustes = findViewById(R.id.Layout_sobre_xradar_ImageView_ajustes);
		 ImageView_chat_general = findViewById(R.id.Layout_sobre_xradar_ImageView_chat_general);
		 ImageView_estrellas = findViewById(R.id.Layout_sobre_xradar_ImageView_estrellas);

		 ImageView_feedback = findViewById(R.id.Layout_sobre_xradar_ImageView_feedback);
		 ImageView_email = findViewById(R.id.Layout_sobre_xradar_ImageView_icono_email);
		 ImageView_twitter = findViewById(R.id.Layout_sobre_xradar_ImageView_icono_twitter);

		Main_LinearLayout = findViewById(R.id.Main_LinearLayout);
		mDrawerLayout = findViewById(R.id.mDrawerLayout);

	}

	private void setupNavigationDrawerRecyclerViews() {
        try {
            recyclerViewDrawer = findViewById(R.id.recyclerViewDrawer);
            recyclerViewDrawer.setLayoutManager(new LinearLayoutManager(this));

            ArrayList<Drawer_Item> drawerItems = new ArrayList<>();
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_track_changes).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[0]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_tune).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[1]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_email).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[2]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_perm_identity).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[3]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_star_border).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[4]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attach_money).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[5]));
            drawerItems.add(new Drawer_Item(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_forum).color(ContextCompat.getColor(this, R.color.white)), getResources().getStringArray(R.array.nav_drawer_items)[6]));
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
                utils.registra_error(e.toString(), "AsyncTask_Coloca_PictureMain de Activity_ayuda");
            }
        }

    }

    private void crea_ItemTouchListener_menu() {
        //Aquí hacemos las acciones pertinentes según el gesto que haya hecho el Usuario_para_listar
        //y que lo ha detectado y codificado primero el  RecyclerTouchListener
        recyclerViewDrawer.addOnItemTouchListener(new Activity_Ayuda.RecyclerTouchListener_menu(this, recyclerViewDrawer, (view, position) -> {
			utils.gestiona_onclick_menu_principal(Activity_Ayuda.this, position);
			if (!utils.isTablet(getApplicationContext())) {
				mDrawerLayout.closeDrawers();
			}
		}));
    }

}
