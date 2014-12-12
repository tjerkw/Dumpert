package io.jari.dumpert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import io.jari.dumpert.api.Item;

/**
 * JARI.IO
 * Date: 12-12-14
 * Time: 14:50
 */
public class ViewItem extends Base {

    Item item;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item);

        ImageView itemImage = (ImageView)findViewById(R.id.item_image);
        item = (Item)getIntent().getSerializableExtra("item");
        ViewCompat.setTransitionName(itemImage, "item");
        Picasso.with(this).load(item.imageUrl).into(itemImage);

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
