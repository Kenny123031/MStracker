package com.example.mstracker;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mstracker.model.WatchItem;
import java.util.List;

public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.WatchViewHolder> {
    private List<WatchItem> items;

    public WatchAdapter(List<WatchItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public WatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watch, parent, false);
        return new WatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchViewHolder holder, int position) {
        WatchItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.info.setText(item.getType() + " · " + item.getYear());
        holder.status.setText(item.getStatus().toUpperCase());
        holder.rating.setText(String.valueOf(item.getRating()));
        
        // Stars Logic
        StringBuilder stars = new StringBuilder();
        int roundedRating = Math.round(item.getRating() / 2); // Scale 10 to 5
        for (int i = 0; i < 5; i++) {
            stars.append(i < roundedRating ? "★" : "☆");
        }
        holder.stars.setText(stars.toString());

        // Status styling
        if ("Watching".equalsIgnoreCase(item.getStatus())) {
            holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.blue));
            holder.status.setBackgroundResource(R.drawable.pill_watching);
        } else if ("Completed".equalsIgnoreCase(item.getStatus())) {
            holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.green));
            holder.status.setBackgroundResource(R.drawable.pill_completed);
        } else {
            holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.text_sub));
            holder.status.setBackgroundResource(R.drawable.pill_plan);
        }

        int placeholder = "Series".equalsIgnoreCase(item.getType()) ? R.drawable.ic_tv_show : R.drawable.ic_movie_clapper;
        
        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w200" + item.getPosterPath())
                .placeholder(placeholder)
                .error(placeholder)
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.EXTRA_TITLE, item.getTitle());
            intent.putExtra(MovieDetailActivity.EXTRA_TYPE, item.getType().toUpperCase());
            intent.putExtra(MovieDetailActivity.EXTRA_YEAR, item.getYear());
            intent.putExtra(MovieDetailActivity.EXTRA_POSTER_PATH, item.getPosterPath());
            intent.putExtra(MovieDetailActivity.EXTRA_TMDB_ID, item.getTmdbId());
            intent.putExtra(MovieDetailActivity.EXTRA_TMDB_TYPE, item.getTmdbType());
            intent.putExtra(MovieDetailActivity.EXTRA_SCORE, String.valueOf(item.getRating()));
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData(List<WatchItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class WatchViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, info, status, rating, stars;

        public WatchViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.posterImage);
            title = itemView.findViewById(R.id.itemTitle);
            info = itemView.findViewById(R.id.itemInfo);
            status = itemView.findViewById(R.id.itemStatus);
            rating = itemView.findViewById(R.id.itemRating);
            stars = itemView.findViewById(R.id.itemStars);
        }
    }
}
