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

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentViewHolder> {
    private List<WatchItem> items;

    public RecentAdapter(List<WatchItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent, parent, false);
        return new RecentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        WatchItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.info.setText(item.getType() + " · " + item.getYear());
        
        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + item.getPosterPath())
                .placeholder(R.color.elevated)
                .into(holder.poster);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RecentViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, info;

        public RecentViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.recentPoster);
            title = itemView.findViewById(R.id.recentTitle);
            info = itemView.findViewById(R.id.recentType);
        }
    }
}
