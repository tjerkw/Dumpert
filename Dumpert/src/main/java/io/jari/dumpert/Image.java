package io.jari.dumpert;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import com.nispok.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.apache.http.util.ByteArrayBuffer;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * JARI.IO
 * Date: 12-12-14
 * Time: 14:50
 */
public class Image extends Base {

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

        final String image = getIntent().getStringExtra("image");
        Picasso
                .with(this)
                .load(image)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //the only way we can check if this is a gif, probably not more needed
                        if(image.endsWith(".gif")) {
                            final View loader = findViewById(R.id.image_loader);
                            loader.setVisibility(View.VISIBLE);


                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //do gif magic
                                        final GifImageView gifImageView = (GifImageView)findViewById(R.id.image_image);

                                        String file = getCacheDir().getPath() + "/" + Uri.parse(image).getLastPathSegment();
                                        download(image, file);

                                        final GifDrawable gif = new GifDrawable(file);

                                        //clean up
                                        new File(file).delete();

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                gifImageView.setImageDrawable(gif);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Snackbar.with(Image.this)
                                                .text(R.string.gif_failed)
                                                .textColor(Color.parseColor("#FFCDD2"))
                                                .show(Image.this);
                                    }
                                    finally {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                loader.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });

        new PhotoViewAttacher(imageView);

        this.tip();
    }

    public void download(String url, String fileName) throws IOException {  //this is the downloader method
        URLConnection ucon = new URL(url).openConnection();
        File file = new File(fileName);

        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);

        ByteArrayBuffer baf = new ByteArrayBuffer(50);
        int current = 0;
        while ((current = bis.read()) != -1) {
            baf.append((byte) current);
        }


        FileOutputStream fos = new FileOutputStream(file);
        fos.write(baf.toByteArray());
        fos.close();
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
