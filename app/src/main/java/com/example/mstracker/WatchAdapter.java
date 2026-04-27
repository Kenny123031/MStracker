package com.example.mstracker;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mstracker.model.WatchItem;
import com.google.android.material.chip.Chip;
import java.util.List;
import java.util.Locale;

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
        
        // 1. Order Number (01, 02, ...)
        holder.orderNumber.setText(String.format(Locale.getDefault(), "%02d", position + 1));

        holder.title.setText(item.getTitle());
        
        // 2. Metadata (Year · Country · Type)
        String country = (item.getCountry() != null && !item.getCountry().isEmpty()) ? item.getCountry() : "Unknown";
        holder.info.setText(String.format("%s · %s · %s", item.getYear(), country, item.getType()));
        
        // 3. Creator
        String creator = (item.getCreator() != null && !item.getCreator().isEmpty()) ? item.getCreator() : "Unknown";
        holder.creator.setText("Creator: " + creator);

        // Status Styling
        String statusText = item.getStatus();
        holder.status.setText(statusText.toUpperCase());
        
        if ("Watching".equalsIgnoreCase(statusText)) {
            holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.blue));
            holder.status.setBackgroundResource(R.drawable.pill_watching);
        } else if ("Completed".equalsIgnoreCase(statusText)) {
            holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.green));
            holder.status.setBackgroundResource(R.drawable.pill_completed);
        } else if ("Plan to Watch".equalsIgnoreCase(statusText)) {
            holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.accent));
            holder.status.setBackgroundResource(R.drawable.pill_plan);
        } else {
            holder.status.setTextColor(holder.itemView.getContext().getColor(R.color.text_sub));
            holder.status.setBackgroundResource(R.drawable.pill_background);
        }

        holder.rating.setText(String.format(Locale.getDefault(), "%.1f", item.getRating()));
        
        // Stars Logic
        StringBuilder stars = new StringBuilder();
        int roundedRating = Math.round(item.getRating() / 2); // Scale 10 to 5
        for (int i = 0; i < 5; i++) {
            stars.append(i < roundedRating ? "★" : "☆");
        }
        holder.stars.setText(stars.toString());

        // 4. Slidable Genres
        holder.genreContainer.removeAllViews();
        if (item.getGenre() != null && !item.getGenre().isEmpty()) {
            String[] genres = item.getGenre().split(",\\s*");
            for (String g : genres) {
                View chipView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_genre_chip, holder.genreContainer, false);
                TextView tvGenre = chipView.findViewById(R.id.tvGenreName);
                tvGenre.setText(g.trim());
                holder.genreContainer.addView(chipView);
            }
        }

        int placeholder = "Series".equalsIgnoreCase(item.getType()) ? R.drawable.ic_tv_show : R.drawable.ic_movie_clapper;
        
        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + item.getPosterPath())
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

        holder.btnMore.setOnClickListener(v -> {
            android.widget.Toast.makeText(holder.itemView.getContext(), "More options for " + item.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
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
        ImageView poster, btnMore;
        TextView title, info, status, rating, stars, orderNumber, creator;
        LinearLayout genreContainer;

        public WatchViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.posterImage);
            btnMore = itemView.findViewById(R.id.btnMoreOptions);
            title = itemView.findViewById(R.id.itemTitle);
            info = itemView.findViewById(R.id.itemInfo);
            status = itemView.findViewById(R.id.itemStatus);
            rating = itemView.findViewById(R.id.itemRating);
            stars = itemView.findViewById(R.id.itemStars);
            orderNumber = itemView.findViewById(R.id.tvOrderNumber);
            creator = itemView.findViewById(R.id.itemCreator);
            genreContainer = itemView.findViewById(R.id.genreContainer);
        }
    }
}
