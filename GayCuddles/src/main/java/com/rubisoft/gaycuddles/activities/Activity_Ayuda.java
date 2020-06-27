package com.rubisoft.gaycuddles.activities;

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
import com.rubisoft.gaycuddles.Adapters.Drawer_Adapter;
import com.rubisoft.gaycuddles.Classes.Drawer_Item;
import com.rubisoft.gaycuddles.Interfaces.Interface_ClickListener_Menu;
import com.rubisoft.gaycuddles.R;
import com.rubisoft.gaycuddles.databinding.LayoutAyudaBinding;
import com.rubisoft.gaycuddles.tools.utils;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Activity_Ayuda extends AppCompatActivity {
    private SharedPreferences perfil_usuario;

    //navigation drawer
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;

    private RecyclerView recyclerViewDrawer;
    private ImageView mImageView_PictureMain;
	private LayoutAyudaBinding binding;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            super.onCreate(savedInstanceState);
			binding = LayoutAyudaBinding.inflate(getLayoutInflater());
			setContentView(binding.getRoot());
            perfil_usuario= getSharedPreferences(getResources().getString(R.string.SHAREDPREFERENCES_PERFIL_USUARIO), Context.MODE_PRIVATE);

            setup_toolbar();
			inicializa_anuncios();

            Typeface typeFace_roboto_Light = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
            Typeface typeFace_roboto_Bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                TypedValue outValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
				binding.LayoutAyudaImageViewIconoEmail.setBackgroundResource(outValue.resourceId);
				binding.LayoutAyudaImageViewIconoEmail.setClickable(true);
				binding.LayoutAyudaImageViewIconoTwitter.setBackgroundResource(outValue.resourceId);
				binding.LayoutAyudaImageViewIconoTwitter.setClickable(true);
            }

			binding.LayoutAyudaImageViewPrincipal.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_track_changes).color(ContextCompat.getColor(this, R.color.gris)));
            binding.LayoutAyudaConfiguraRadar.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_tune).color(ContextCompat.getColor(this, R.color.gris)));
            binding.LayoutAyudaImageViewMensajes.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_email).color(ContextCompat.getColor(this, R.color.gris)));
			binding.LayoutAyudaImageViewMiPerfil.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_perm_identity).color(ContextCompat.getColor(this, R.color.gris)));
			binding.LayoutAyudaImageViewCompras.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_attach_money).color(ContextCompat.getColor(this, R.color.gris)));
			binding.LayoutAyudaImageViewAjustes.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).color(ContextCompat.getColor(this, R.color.gris)));
			binding.LayoutAyudaImageViewChatGeneral.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_forum).color(ContextCompat.getColor(this, R.color.gris)));
			binding.LayoutAyudaImageViewFeedback.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_thumbs_up_down).color(ContextCompat.getColor(this, R.color.gris)));
			binding.LayoutAyudaImageViewEstrellas.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_star_border).color(ContextCompat.getColor(this, R.color.gris)));
			binding.LayoutAyudaImageViewIconoEmail.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_email).color(ContextCompat.getColor(this, R.color.accent)));
			binding.LayoutAyudaImageViewIconoTwitter.setImageDrawable(utils.get_icono_twitter(getApplicationContext(),ContextCompat.getColor(this, R.color.accent)));
			binding.LayoutAyudaImageViewIconoEmail.setOnClickListener(v -> manda_email());
			binding.LayoutAyudaImageViewIconoTwitter.setOnClickListener(v -> manda_twitter());

            binding.LayoutAyudaTextViewNombreAplicacion.setTypeface(typeFace_roboto_Bold);
			binding.LayoutAyudaTextViewVersionAplicacion.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewExplicacionTitle.setTypeface(typeFace_roboto_Bold);
			binding.LayoutAyudaTextViewExplicacion.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewPrincipal.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewConfiguraRadar.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewMiPerfil.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewCompras.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewAjustes.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewChatGeneral.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewFeedback.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewEstrellas.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewMensajes.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewComoContactar.setTypeface(typeFace_roboto_Bold);
			binding.LayoutAyudaTextViewParaCualquierConsulta.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewRubisoftEmail.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewRubisoftTwitter.setTypeface(typeFace_roboto_Light);
			binding.LayoutAyudaTextViewRubisoftTwitter.setTextColor(ContextCompat.getColor(this, R.color.accent));
			binding.LayoutAyudaTextViewRubisoftEmail.setTextColor(ContextCompat.getColor(this, R.color.accent));
			binding.LayoutAyudaTextViewExplicacionTitle.setText(String.format(getResources().getString(R.string.help_info_title),getString(R.string.app_name)));
			binding.LayoutAyudaTextViewExplicacion.setText(String.format(getResources().getString(R.string.help_info),getString(R.string.app_name)));
			binding.LayoutAyudaTextViewNombreAplicacion.setText(getString(R.string.app_name));
			binding.LayoutAyudaTextViewPrincipal.setText(getResources().getString(R.string.help_principal));
			binding.LayoutAyudaTextViewConfiguraRadar.setText(getResources().getString(R.string.help_configura_radar));
			binding.LayoutAyudaTextViewMensajes.setText((getResources().getString(R.string.help_mensajes)));
			binding.LayoutAyudaTextViewMiPerfil.setText(String.format(getResources().getString(R.string.help_mi_perfil),getResources().getInteger(R.integer.NUM_MAX_FOTOS_PREMIUM)));
			binding.LayoutAyudaTextViewCompras.setText(getResources().getString(R.string.help_compras));
			binding.LayoutAyudaTextViewAjustes.setText(getResources().getString(R.string.help_ajustes));
			binding.LayoutAyudaTextViewChatGeneral.setText(getResources().getString(R.string.help_chat_general));
			binding.LayoutAyudaTextViewEstrellas.setText( String.format(getResources().getString(R.string.help_ganar_estrellas),(getResources().getInteger(R.integer.LOGRO_COBRO_SEMANAL)*-1)));
			binding.LayoutAyudaTextViewFeedback.setText(getResources().getString(R.string.help_feedback));
			binding.LayoutAyudaTextViewRubisoftEmail.setText(getString(R.string.rubisoft_email));
			binding.LayoutAyudaTextViewRubisoftTwitter.setText(getString(R.string.rubisoft_twitter));
			binding.LayoutAyudaTextViewComoContactar.setText(String.format(getResources().getString(R.string.help_como_contactar),getString(R.string.app_name)));
			binding.LayoutAyudaTextViewVersionAplicacion.setText(String.format(getString(R.string.version),  utils.get_Version_Name(this)));
			binding.LayoutAyudaTextViewRubisoftEmail.setOnClickListener(v -> manda_email());
			binding.LayoutAyudaTextViewRubisoftTwitter.setOnClickListener(v -> manda_twitter());
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

			if (!perfil_usuario.getBoolean(getString(R.string.PERFIL_USUARIO_ES_PREMIUM), false)) {
				Consent consent = ConsentManager.getInstance(this).getConsent();
				Appodeal.setTesting(false);
				Appodeal.initialize(this, getResources().getString(R.string.APPODEAL_APP_KEY), Appodeal.BANNER, consent);
				setup_banner();
				int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
				layoutParams.setMargins(0, px, 0, 0);

			}else {
				layoutParams.setMargins(0, 0, 0, 0);
			}
			if (binding.mDrawerLayout!=null){
				binding.mDrawerLayout.setLayoutParams(layoutParams);
			}else{
				binding.MainLinearLayout.setLayoutParams(layoutParams);
			}
		}catch (Exception e){
			utils.registra_error(e.toString(), "inicializa_anuncios de Activity_Ayuda");
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
			if (binding.mDrawerLayout!=null) {
				// Setup Drawer Icon
				drawerToggle = new ActionBarDrawerToggle(this, binding.mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
				binding.mDrawerLayout.addDrawerListener(drawerToggle);
				drawerToggle.syncState();

				TypedValue typedValue = new TypedValue();
				int color = typedValue.data;
				binding.mDrawerLayout.setStatusBarBackgroundColor(color);
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
				binding.mDrawerLayout.closeDrawers();
			}
		}));
    }

}
