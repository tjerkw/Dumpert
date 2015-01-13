package io.jari.dumpert;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import io.jari.dumpert.api.Item;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * JARI.IO
 * Date: 11-12-14
 * Time: 23:30
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private ArrayList<Item> dataSet;
    private Activity context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public Activity context;
        public Item item;

        public ViewHolder(CardView v, final Activity context) {
            super(v);
            cardView = v;
            this.context = context;

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewItem.launch(ViewHolder.this.context, cardView.findViewById(R.id.card_image), item);
                }
            });
        }

        public void update(Item item) {
            final ImageView imageView = (ImageView)cardView.findViewById(R.id.card_image);
            ImageView type = (ImageView)cardView.findViewById(R.id.card_type);
            final TextView title = (TextView)cardView.findViewById(R.id.card_title);
            final TextView description = (TextView)cardView.findViewById(R.id.card_description);
            TextView stats = (TextView)cardView.findViewById(R.id.card_stats);
            TextView date = (TextView)cardView.findViewById(R.id.card_date);

            Picasso
                    .with(context)
                    .load(item.imageUrls == null ? null : item.imageUrls[0])
                    .into(imageView, new Callback.EmptyCallback() {
                        @Override public void onSuccess() {
                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if(swatch != null ){
                                        cardView.setBackgroundColor(swatch.getRgb());
                                        title.setTextColor(swatch.getTitleTextColor());
                                        description.setTextColor(swatch.getBodyTextColor());
                                    }
                                }
                            });
                        }
                    });

            if(item.photo) type.setImageResource(R.drawable.ic_photo);
            else if(item.video) type.setImageResource(R.drawable.ic_play_circle_fill);
            else if(item.audio) type.setImageResource(R.drawable.ic_audiotrack);

            title.setText(item.title);
            description.setText(Html.fromHtml(item.description));
            Linkify.addLinks(description, Linkify.ALL);
            stats.setText(item.stats);
            date.setText(item.date);

            int gray = context.getResources().getColor(R.color.gray_bg);
            if(item.audio) {
                imageView.setBackgroundColor(context.obtainStyledAttributes(new int[]{R.attr.colorPrimaryDark}).getColor(0, gray));
            } else {
                imageView.setBackgroundColor(gray);
            }
            this.item = item;
        }
    }

    public CardAdapter(Item[] dataSet, Activity context) {
        this.dataSet = new ArrayList<Item>(Arrays.asList(dataSet));
        this.context = context;
    }

    public void removeAll() {
        for (int i = dataSet.size()-1; i >= 0; i--) {
            remove(dataSet.get(i));
        }
    }

    public void add(Item item) {
        dataSet.add(item);
        notifyItemInserted(dataSet.size()-1);
    }

    public void addItems(Item[] items) {
        for(Item item : items) {
            add(item);
        }
    }

    public void remove(Item item) {
        int position = dataSet.indexOf(item);
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView card = (CardView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        return new ViewHolder(card, context);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.update(dataSet.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
