package io.jari.dumpert;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.nispok.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Comment;
import io.jari.dumpert.api.Item;
import io.jari.dumpert.api.ItemInfo;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    ArrayList<Comment> dataSet;
    Activity activity;

    public CommentsAdapter(Comment[] comments, Activity activity) {
        this.dataSet = new ArrayList<Comment>(Arrays.asList(comments));
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View comment = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment, parent, false);
            return new CommentView(comment);
        } else if (viewType == TYPE_HEADER) {
            View header= LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            return new HeaderView(header);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentView) {
            Comment comment = getItem(position);
            ((CommentView)holder).update(comment);
        } else if (holder instanceof HeaderView) {
            //??
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    public void removeAll() {
        for (int i = dataSet.size()-1; i >= 0; i--) {
            remove(dataSet.get(i));
        }
    }

    public void add(Comment item) {
        dataSet.add(item);
        notifyItemInserted(dataSet.size());
    }

    public void addItems(Comment[] items) {
        for(Comment item : items) {
            add(item);
        }
    }

    public void remove(Comment item) {
        int position = dataSet.indexOf(item);
        dataSet.remove(position);
        notifyItemRemoved(position - 1);
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private Comment getItem(int position) {
        return dataSet.get(position - 1);
    }

    class CommentView extends RecyclerView.ViewHolder {
        Comment comment;
        View view;
        public CommentView(View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public void update(Comment comment) {
            this.comment = comment;
            TextView author = (TextView)view.findViewById(R.id.comment_author);
            TextView message = (TextView)view.findViewById(R.id.comment_message);
            TextView time = (TextView)view.findViewById(R.id.comment_time);

            author.setText(comment.author);
            message.setText(comment.content);
            time.setText(comment.time);

        }
    }

    class HeaderView extends RecyclerView.ViewHolder {
        Item item;
        ItemInfo itemInfo;

        public HeaderView(View itemView) {
            super(itemView);
            final ImageView itemImage = (ImageView)itemView.findViewById(R.id.item_image);
            item = (Item)activity.getIntent().getSerializableExtra("item");
//            ViewCompat.setTransitionName(itemImage, "item");
            Picasso.with(activity).load(item.imageUrl).into(itemImage);

            final ImageView itemType = (ImageView)itemView.findViewById(R.id.item_type);
            itemType.setImageDrawable(activity.getResources().getDrawable(item.photo ? R.drawable.ic_photo : R.drawable.ic_play_circle_fill));

            if(item.video) {
                final ProgressBar progressBar = (ProgressBar)itemView.findViewById(R.id.item_loading);
                progressBar.setVisibility(View.VISIBLE);
                itemType.setVisibility(View.GONE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean error = false;
                        try {
                            if(!Utils.isOffline(activity))
                                itemInfo = API.getItemInfo(item.url);
                        } catch (Exception e) {
                            error = true;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.with(activity)
                                            .text(R.string.video_failed)
                                            .textColor(Color.parseColor("#FFCDD2"))
                                            .show(activity);
                                }
                            });
                        }

                        final boolean err = error;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(!err && !Utils.isOffline(activity) && true) { //replace true with preference check
                                    ((ViewItem)activity).startVideo(itemInfo);
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    itemType.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }).start();
            }

            item = (Item)activity.getIntent().getSerializableExtra("item");
            ViewCompat.setTransitionName(itemImage, "item");

            itemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.photo)
                        Image.launch(activity, itemImage, item.imageUrl);
                    else if(item.video && itemInfo != null) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(itemInfo.tabletVideo)));
                    }
                }
            });

        }
    }
}