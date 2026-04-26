package com.example.mstracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mstracker.database.AppDatabase;
import com.example.mstracker.model.WatchItem;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsFragment extends Fragment {

    private TextView txtTotal, txtMovies, txtSeries, txtAvgRating, txtStars;
    private TextView txtMoviePercent, txtSeriesPercent;
    private TextView txtWatchingCount, txtPlanningCount;
    private ProgressBar progressBreakdown;
    private RecyclerView rvRecent;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StatisticsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        db = AppDatabase.getInstance(getContext());
        txtTotal = view.findViewById(R.id.statTotalCount);
        txtMovies = view.findViewById(R.id.statMoviesCount);
        txtSeries = view.findViewById(R.id.statSeriesCount);
        txtAvgRating = view.findViewById(R.id.statAvgRating);
        txtStars = view.findViewById(R.id.statStars);
        txtMoviePercent = view.findViewById(R.id.txtMoviePercent);
        txtSeriesPercent = view.findViewById(R.id.txtSeriesPercent);
        txtWatchingCount = view.findViewById(R.id.statWatchingCount);
        txtPlanningCount = view.findViewById(R.id.statPlanningCount);
        progressBreakdown = view.findViewById(R.id.statProgress);
        rvRecent = view.findViewById(R.id.rvRecentlyAdded);

        // Ensure horizontal layout for recent items
        if (rvRecent != null) {
            rvRecent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        loadStats();
        return view;
    }

    private void loadStats() {
        executor.execute(() -> {
            List<WatchItem> allItems = db.watchDao().getAll();
            int total = allItems.size();
            int moviesCount = 0;
            int seriesCount = 0;
            int completedCount = 0;
            int watchingCount = 0;
            int planningCount = 0;
            float totalRating = 0;
            int ratedCount = 0;

            for (WatchItem item : allItems) {
                if ("Movie".equalsIgnoreCase(item.getType())) moviesCount++;
                else if ("Series".equalsIgnoreCase(item.getType())) seriesCount++;

                if ("Completed".equalsIgnoreCase(item.getStatus())) completedCount++;
                else if ("Watching".equalsIgnoreCase(item.getStatus())) watchingCount++;
                else if ("Plan to Watch".equalsIgnoreCase(item.getStatus())) planningCount++;

                if (item.getRating() > 0) {
                    totalRating += item.getRating();
                    ratedCount++;
                }
            }

            float avg = ratedCount > 0 ? totalRating / ratedCount : 0f;
            int movieProgress = total > 0 ? (moviesCount * 100) / total : 0;
            int seriesProgress = total > 0 ? 100 - movieProgress : 0;

            final int finalTotal = completedCount;
            final int finalMovies = moviesCount;
            final int finalSeries = seriesCount;
            final int finalWatching = watchingCount;
            final int finalPlanning = planningCount;
            final float finalAvg = avg;
            final int finalMovieProgress = movieProgress;
            final int finalSeriesProgress = seriesProgress;
            
            // Limit to top 10 recent items
            final List<WatchItem> recentItems = allItems.size() > 10 ? allItems.subList(0, 10) : allItems;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtTotal.setText(String.valueOf(finalTotal));
                    txtMovies.setText(String.valueOf(finalMovies));
                    txtSeries.setText(String.valueOf(finalSeries));
                    txtWatchingCount.setText(String.format(Locale.getDefault(), "%d Watching", finalWatching));
                    txtPlanningCount.setText(String.format(Locale.getDefault(), "%d Planning", finalPlanning));
                    txtAvgRating.setText(String.format(Locale.getDefault(), "%.1f", finalAvg));
                    progressBreakdown.setProgress(finalMovieProgress);
                    
                    txtMoviePercent.setText(String.format(Locale.getDefault(), "Movies %d%%", finalMovieProgress));
                    txtSeriesPercent.setText(String.format(Locale.getDefault(), "Series %d%%", finalSeriesProgress));

                    // Rating scaling
                    StringBuilder stars = new StringBuilder();
                    int starCount = (int) Math.round(finalAvg / 2);
                    for(int i=0; i<5; i++) stars.append(i < starCount ? "★" : "☆");
                    txtStars.setText(stars.toString());

                    // Update Recent Items
                    if (rvRecent != null) {
                        rvRecent.setAdapter(new RecentAdapter(recentItems));
                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
