package io.jari.dumpert;

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

/**
 * JARI.IO
 * Date: 11-12-14
 * Time: 23:30
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private Item[] dataSet;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public Context context;

        public ViewHolder(CardView v, Context context) {
            super(v);
            cardView = v;
            this.context = context;
        }

        public void update(Item item) {
            ImageView imageView = (ImageView)cardView.findViewById(R.id.card_image);
            TextView title = (TextView)cardView.findViewById(R.id.card_title);

            Picasso
                    .with(context)
                    .load(item.imageUrl)
                    .into(imageView);

            title.setText(item.title);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardAdapter(Item[] dataSet, Context context) {
        this.dataSet = dataSet;
        this.context = context;
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
        holder.update(dataSet[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.length;
    }
}
