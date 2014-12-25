package io.jari.dumpert;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import com.nispok.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import io.jari.dumpert.api.Item;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * JARI.IO
 * Date: 12-12-14
 * Time: 14:50
 */
public class Image extends Base {

    Item item;

    void setTheme() {
        String theme = preferences.getString("theme", "green");

        if(theme.equals("green")) {
            //default theme, do nothing
        } else if(theme.equals("blue")) {
            super.setTheme(R.style.Theme_Dumpert_NoActionBar_Blue);
        } else if(theme.equals("red")) {
            super.setTheme(R.style.Theme_Dumpert_NoActionBar_Red);
        } else if(theme.equals("pink")) {
            super.setTheme(R.style.Theme_Dumpert_NoActionBar_Pink);
        } else if(theme.equals("orange")) {
            super.setTheme(R.style.Theme_Dumpert_NoActionBar_Orange);
        } else if(theme.equals("bluegray")) {
            super.setTheme(R.style.Theme_Dumpert_NoActionBar_BlueGray);
        } else if(theme.equals("webartisans")) {
            super.setTheme(R.style.Theme_Dumpert_NoActionBar_WebArtisans);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image);

        ImageView imageView = (ImageView)findViewById(R.id.image_image);
        ViewCompat.setTransitionName(imageView, "image");

        String image = getIntent().getStringExtra("image");
        Picasso
                .with(this)
                .load(image)
                .into(imageView);

        new PhotoViewAttacher(imageView);

        this.tip();
    }

    public void tip() {
        SharedPreferences sharedPreferences = getSharedPreferences("dumpert", 0);
        if(!sharedPreferences.getBoolean("seenImageTip", false)) {
            sharedPreferences.edit().putBoolean("seenImageTip", true).apply();

            Snackbar.with(getApplicationContext())
                    .text(getResources().getText(R.string.tip_touch_to_enlarge))
                    .actionLabel(getResources().getString(R.string.tip_close))
                    .actionColor(Color.parseColor("#D32F2F"))
                    .duration(4000)
                    .show(this);
        }
    }

    public static void launch(Activity activity, View transitionView, String image) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, "image");
        Intent intent = new Intent(activity, Image.class);
        intent.putExtra("image", image);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
}
