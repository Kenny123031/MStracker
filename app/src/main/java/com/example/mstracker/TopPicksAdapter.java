package com.example.mstracker;

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
import java.util.Locale;

public class TopPicksAdapter extends RecyclerView.Adapter<TopPicksAdapter.ViewHolder> {
    private final List<WatchItem> items;

    public TopPicksAdapter(List<WatchItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_pick, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WatchItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvRating.setText(String.format(Locale.getDefault(), "★ %.1f", item.getRating()));

        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w200" + item.getPosterPath())
                .centerCrop()
                .placeholder(R.color.bg_surface)
                .into(holder.ivPoster);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvRating, tvTitle;

        ViewHolder(View view) {
            super(view);
            ivPoster = view.findViewById(R.id.ivPoster);
            tvRating = view.findViewById(R.id.tvRating);
            tvTitle = view.findViewById(R.id.tvTitle);
        }
    }
}
