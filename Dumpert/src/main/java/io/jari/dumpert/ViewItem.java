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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nispok.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import io.jari.dumpert.animators.SlideInOutBottomItemAnimator;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Comment;
import io.jari.dumpert.api.Item;
import io.jari.dumpert.api.ItemInfo;

import java.io.IOException;
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
    SwipeRefreshLayout swipeRefreshLayout;

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

        this.loadComments(false);
        this.initHeader();

        ViewCompat.setTransitionName(findViewById(R.id.item_frame), "item");

        getSupportActionBar().setTitle(item.title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.tip();

        //set up ze refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                commentsAdapter.removeAll();
                loadComments(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(item.video) {
            final VideoView videoView = (VideoView) findViewById(R.id.item_video);
            videoView.suspend();
            videoView.setVisibility(View.GONE); //paranoia
            findViewById(R.id.item_video_frame).setVisibility(View.GONE);
            findViewById(R.id.item_frame).setVisibility(View.VISIBLE);
            findViewById(R.id.item_loading).setVisibility(View.GONE);
        } else if(item.audio) {
            findViewById(R.id.item_frame).setVisibility(View.VISIBLE);
            findViewById(R.id.item_video_frame).setAlpha(0f);
            if(audioHandler != null) audioHandler.controller.hide();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if(item == null || (!item.video && !item.audio)) {
            super.onPause();
            return;
        }
        if(item.video) {
            //videoview starts tripping once activity gets paused, so stop the thing, hide it, show progressbar
            final VideoView videoView = (VideoView) findViewById(R.id.item_video);
            final View videoViewFrame = findViewById(R.id.item_video_frame);
            findViewById(R.id.item_loading).setVisibility(View.VISIBLE);
            findViewById(R.id.item_type).setVisibility(View.GONE);
            findViewById(R.id.item_frame).setVisibility(View.VISIBLE);
            videoViewFrame.setAlpha(0f);
            videoView.stopPlayback();
        } else {
            if(audioHandler != null) audioHandler.pause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if(itemInfo != null && (item != null) && (item.video || item.audio)) {
            if(item.video) {
                //when we return to the activity, restart the video
                startMedia(itemInfo, item);
            } else {
                if(audioHandler != null) audioHandler.start();
            }

        }
        super.onResume();
    }

    AudioHandler audioHandler;

    public void startMedia(final ItemInfo itemInfo, final Item item) {
        final View cardFrame = findViewById(R.id.item_frame);
        final View videoViewFrame = findViewById(R.id.item_video_frame);
        final VideoView videoView = (VideoView) findViewById(R.id.item_video);

        if(item.video) {
            videoView.setVideoURI(Uri.parse(itemInfo.media));

            final MediaController mediaController = new MediaController(this);

            mediaController.setAnchorView(videoViewFrame);

            videoView.setMediaController(mediaController);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d("dumpert.viewitem", "onPrepared");
                    cardFrame.setVisibility(View.GONE);
                    videoViewFrame.setAlpha(1f);
//                ViewCompat.setTransitionName(videoViewFrame, "item");
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    findViewById(R.id.item_loading).setVisibility(View.GONE);
                    findViewById(R.id.item_type).setVisibility(View.VISIBLE);
                    videoViewFrame.setAlpha(0f);

                    Snackbar.with(ViewItem.this)
                            .text(R.string.video_failed)
                            .textColor(Color.parseColor("#FFCDD2"))
                            .show(ViewItem.this);

                    return true;
                }
            });


            videoView.start();
        }

        if(item.audio) {
            //audiohandler is sync, so don't call from main thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final FrameLayout master = (FrameLayout)findViewById(R.id.item_master_frame);
                        audioHandler = new AudioHandler() {
                            @Override
                            public void onPrepared(MediaPlayer mediaplayer) {
                                super.onPrepared(mediaplayer);

                                //hide progressbar etc
                                cardFrame.setVisibility(View.GONE);
                                videoViewFrame.setAlpha(1f);
                            }
                        };
                        audioHandler.playAudio(itemInfo.media, ViewItem.this, master);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    
    public void initHeader() {
        final ImageView itemImage = (ImageView)findViewById(R.id.item_image);
        item = (Item)getIntent().getSerializableExtra("item");
        Picasso.with(this).load(item.imageUrl).into(itemImage);

        final ImageView itemType = (ImageView)findViewById(R.id.item_type);
        if(item.photo)
            itemType.setImageResource(R.drawable.ic_photo);
        else if(item.video)
            itemType.setImageResource(R.drawable.ic_play_circle_fill);
        else if(item.audio)
            itemType.setImageResource(R.drawable.ic_audiotrack);

        int gray = getResources().getColor(R.color.gray_bg);
        if(item.audio) {
            FrameLayout itemFrame = (FrameLayout)findViewById(R.id.item_frame);
            FrameLayout master = (FrameLayout)findViewById(R.id.item_master_frame);
            ViewGroup.LayoutParams layoutParams = master.getLayoutParams();
            ViewGroup.LayoutParams layoutParams2 = itemFrame.getLayoutParams();
            layoutParams.height = layoutParams2.height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
            master.setLayoutParams(layoutParams);
            itemFrame.setLayoutParams(layoutParams2);

            itemImage.setBackgroundColor(obtainStyledAttributes(new int[]{R.attr.colorPrimaryDark}).getColor(0, gray));
        } else {
            itemImage.setBackgroundColor(gray);
        }

        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.item_loading);
        if(item.video || item.audio) {
            progressBar.setVisibility(View.VISIBLE);
            itemType.setVisibility(View.GONE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean error = false;
                    try {
                        if(!Utils.isOffline(ViewItem.this))
                            itemInfo = API.getItemInfo(item, ViewItem.this);
                    } catch (Exception e) {
                        error = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.with(ViewItem.this)
                                        .text(R.string.video_failed)
                                        .textColor(Color.parseColor("#FFCDD2"))
                                        .show(ViewItem.this);
                            }
                        });
                    }

                    final boolean err = error;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!err && !Utils.isOffline(ViewItem.this) && preferences.getBoolean("autoplay_vids", true)) {
                                startMedia(itemInfo, item);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                itemType.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }).start();
        }

        item = (Item)getIntent().getSerializableExtra("item");
        ViewCompat.setTransitionName(itemImage, "item");

        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(item.photo)
                    Image.launch(ViewItem.this, itemImage, item.imageUrl);
                else if(item.video && itemInfo != null && progressBar.getVisibility() != View.VISIBLE) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(itemInfo.media)));
                }
            }
        });
    }

    public void loadComments(final boolean refresh) {
        if(!refresh) {
            commentsAdapter = new CommentsAdapter(new Comment[0], this);
            comments.setAdapter(commentsAdapter);
        }

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
                            if(refresh) swipeRefreshLayout.setRefreshing(false);
                            else {
                                comments.setVisibility(View.VISIBLE);
                                findViewById(R.id.comments_loader).setVisibility(View.GONE);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.with(ViewItem.this)
                                    .text(R.string.comments_failed)
                                    .textColor(Color.parseColor("#FFCDD2"))
                                    .show(ViewItem.this);
                        }
                    });
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
