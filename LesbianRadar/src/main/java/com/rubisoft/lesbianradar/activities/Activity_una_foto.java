package com.rubisoft.lesbianradar.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.rubisoft.lesbianradar.R;
import com.rubisoft.lesbianradar.tools.utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.concurrent.RejectedExecutionException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

public class Activity_una_foto extends AppCompatActivity {
	private AppCompatImageView expanded_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_una_foto);
        expanded_image = findViewById(R.id.Layout_una_foto_ImageView_expanded_image);
        Bundle bundle = getIntent().getExtras();

        String uri_foto= bundle.getString(getResources().getString(R.string.RELACIONES_TOKEN_SOCIALAUTH_DE_LA_OTRA_PERSONA), "");

        descarga_foto_grande(uri_foto);
    }

    private void descarga_foto_grande(String uri_foto_thumb){
        try {
                String uri_foto = uri_foto_thumb.replace("_thumb.jpg", ".jpg");
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference storageRef = storage.getReferenceFromUrl(uri_foto);

                Target target = new Target() {
                    @Override
                    public void onPrepareLoad(Drawable arg0) {

                    }

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                        expanded_image.setImageBitmap(bitmap);
                        expanded_image.setAlpha(1f);
                        expanded_image.invalidate();
                    }

                    @Override
                    public void onBitmapFailed(Drawable arg0) {
                        expanded_image.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary))); /*BitmapFactory.decodeResource(getResources(), R.drawable.no_pic)*/
                    }
                };
                expanded_image.setTag(target);

            /*    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
					try {
						if (getApplicationContext() != null) { //puede que ya no estemos en la activity
*/
				Picasso.with(getApplicationContext())
						.load(uri_foto)
						.placeholder(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary)))   // optional
						.error(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary)))       // optional
						.resize(getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles), getResources().getDimensionPixelSize(R.dimen.tamanyo_foto_grid_perfiles))// optional
						.centerCrop()
						.into(target);

					/*	}
					} catch (Exception ignored) {
					}
				}).addOnFailureListener(e -> {
					expanded_image.setImageDrawable(utils.get_no_pic(getApplicationContext(), ContextCompat.getColor(getApplicationContext(), R.color.primary))); *//*BitmapFactory.decodeResource(getResources(), R.drawable.no_pic)*//*
				});*/

        }catch (RejectedExecutionException e){
            try {
                Thread.sleep(500);
            }catch (Exception ignored){}
        }catch (Exception e) {
            utils.registra_error(e.toString(), "descarga_foto_grande");
        }
    }
}
