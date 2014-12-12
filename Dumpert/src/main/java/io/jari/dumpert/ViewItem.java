package io.jari.dumpert;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.squareup.picasso.Picasso;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Item;
import io.jari.dumpert.api.ItemInfo;
import org.json.JSONException;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.IOException;
import java.net.URL;

/**
 * JARI.IO
 * Date: 12-12-14
 * Time: 14:50
 */
public class ViewItem extends Base {

    Item item;
    ItemInfo itemInfo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item);

        final ImageView itemImage = (ImageView)findViewById(R.id.item_image);
        item = (Item)getIntent().getSerializableExtra("item");
        ViewCompat.setTransitionName(itemImage, "item");
        Picasso.with(this).load(item.imageUrl).into(itemImage);

        final ImageView itemType = (ImageView)findViewById(R.id.item_type);
        itemType.setImageDrawable(getResources().getDrawable(item.photo ? R.drawable.ic_photo : R.drawable.ic_play_circle_fill));

        if(item.video) {
//            itemImage.setAlpha(0.5f);
            final ProgressBar progressBar = (ProgressBar)findViewById(R.id.item_loading);
            progressBar.setVisibility(View.VISIBLE);
            itemType.setVisibility(View.GONE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        itemInfo = API.getItemInfo(item.url);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                itemImage.setAlpha(1f);
                                progressBar.setVisibility(View.GONE);
                                itemType.setVisibility(View.VISIBLE);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item.photo)
                    Image.launch(ViewItem.this, itemImage, item.imageUrl);
                else if(item.video && itemInfo != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(itemInfo.tabletVideo)));
                }
            }
        });

        item = (Item)getIntent().getSerializableExtra("item");
        ViewCompat.setTransitionName(itemImage, "item");


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(item.title);
    }

    public static void launch(Activity activity, View transitionView, Item item) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, "item");
        Intent intent = new Intent(activity, ViewItem.class);
        intent.putExtra("item",  item);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
}
