package com.rubisoft.menradar.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.rubisoft.menradar.R;
import com.rubisoft.menradar.databinding.LayoutUnaFotoBinding;
import com.rubisoft.menradar.tools.utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.concurrent.RejectedExecutionException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Activity_una_foto extends AppCompatActivity {
	private LayoutUnaFotoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		binding = LayoutUnaFotoBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		Bundle bundle = getIntent().getExtras();

        String uri_foto= bundle.getString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), "");

        descarga_foto_grande(uri_foto);
    }

    private void descarga_foto_grande(String uri_foto_thumb){
        try {
                String uri_foto = uri_foto_thumb.replace("_thumb.jpg", ".jpg");

                Target target = new Target() {
                    @Override
                    public void onPrepareLoad(Drawable arg0) {

                    }

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
						binding.LayoutUnaFotoImageViewExpandedImage.setImageBitmap(bitmap);
						binding.LayoutUnaFotoImageViewExpandedImage.setAlpha(1f);
						binding.LayoutUnaFotoImageViewExpandedImage.invalidate();
                    }

                    @Override
                    public void onBitmapFailed(Drawable arg0) {
						binding.LayoutUnaFotoImageViewExpandedImage.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary))); /*BitmapFactory.decodeResource(getResources(), R.drawable.no_pic)*/
                    }
                };
                binding.LayoutUnaFotoImageViewExpandedImage.setTag(target);

				Picasso.with(getApplicationContext())
						.load(uri_foto)
						.placeholder(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary)))   // optional
						.error(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary)))       // optional
						.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))// optional
						.centerCrop()
						.into(target);

        }catch (RejectedExecutionException e){
            try {
                Thread.sleep(500);
            }catch (Exception ignored){}
        }catch (Exception e) {
            utils.registra_error(e.toString(), "descarga_foto_grande");
        }
    }
}
