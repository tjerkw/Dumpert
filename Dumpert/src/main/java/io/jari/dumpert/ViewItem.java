package io.jari.dumpert;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;
import com.nispok.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import io.jari.dumpert.animators.SlideInOutBottomItemAnimator;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Comment;
import io.jari.dumpert.api.Item;
import io.jari.dumpert.api.ItemInfo;
import org.json.JSONException;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JARI.IO
 * Date: 12-12-14
 * Time: 14:50
 */
public class ViewItem extends Base {

    Item item;
    ItemInfo itemInfo;
    RecyclerView comments;
    CommentsAdapter commentsAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemview);

        item = (Item) getIntent().getSerializableExtra("item");

        comments = (RecyclerView) findViewById(R.id.comments);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        comments.setHasFixedSize(true);

        comments.setItemAnimator(new SlideInOutBottomItemAnimator(comments));

        // use a linear layout manager
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        comments.setLayoutManager(linearLayoutManager);

        this.loadComments();

        ViewCompat.setTransitionName(comments, "item");

        getSupportActionBar().setTitle(item.title);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.tip();
    }

    public void startVideo(ItemInfo itemInfo) {
//        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.item_loading);
        final View cardFrame = findViewById(R.id.item_frame);
//        cardFrame.setVisibility(View.GONE);
        final VideoView videoView = (VideoView) findViewById(R.id.item_video);
//        videoView.setVisibility(View.VISIBLE);

        videoView.setVideoURI(Uri.parse(itemInfo.tabletVideo));

        MediaController mediaController = new MediaController(this);

        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("dumpert.viewitem", "onPrepared");
                cardFrame.setVisibility(View.GONE);
                videoView.setAlpha(1f);
            }
        });


        videoView.start();
    }

    public void loadComments() {
        commentsAdapter = new CommentsAdapter(new Comment[0], this);
        comments.setAdapter(commentsAdapter);

        //get id from url
        Pattern pattern = Pattern.compile("/mediabase/([0-9]*)/([a-z0-9]*)/");
        Matcher matcher = pattern.matcher(item.url);
        if (!matcher.find()) throw new InvalidParameterException("ViewItem got a invalid url passed to it :(");
        final String id = matcher.group(1) + "_" + matcher.group(2);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Comment[] commentsData = API.getComments(id);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            commentsAdapter.addItems(commentsData);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void tip() {
        if (!item.photo) return;
        SharedPreferences sharedPreferences = getSharedPreferences("dumpert", 0);
        if (!sharedPreferences.getBoolean("seenItemTip", false)) {
            sharedPreferences.edit().putBoolean("seenItemTip", true).apply();

            Snackbar.with(getApplicationContext())
                    .text(getResources().getText(R.string.tip_touch_to_enlarge))
                    .actionLabel("Sluit")
                    .actionColor(Color.parseColor("#66BB6A"))
                    .duration(4000)
                    .show(this);
        }
    }

    public static void launch(Activity activity, View transitionView, Item item) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, "item");
        Intent intent = new Intent(activity, ViewItem.class);
        intent.putExtra("item", item);
        ActivityCompat.startActivity(activity, intent, options.toBundle());

//        Intent intent = new Intent(activity, ViewItem.class);
//        intent.putExtra("item",  item);
//        activity.startActivity(intent);
    }
}
