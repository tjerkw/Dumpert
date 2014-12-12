package io.jari.dumpert;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import io.jari.dumpert.api.Item;
import org.w3c.dom.Text;

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

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
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
                    ViewItem.launch(ViewHolder.this.context, cardView.findViewById(R.id.card_frame), item);
                }
            });
        }

        public void update(Item item) {
            ImageView imageView = (ImageView)cardView.findViewById(R.id.card_image);
            TextView title = (TextView)cardView.findViewById(R.id.card_title);
            TextView description = (TextView)cardView.findViewById(R.id.card_description);
            TextView stats = (TextView)cardView.findViewById(R.id.card_stats);
            TextView date = (TextView)cardView.findViewById(R.id.card_date);

            Picasso
                    .with(context)
                    .load(item.imageUrl)
                    .into(imageView);

            title.setText(item.title);
            description.setText(item.description);
            stats.setText(item.stats);
            date.setText(item.date);
            this.item = item;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
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

        ViewHolder viewHolder = new ViewHolder(card, context);
        return viewHolder;
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
