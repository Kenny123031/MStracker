package com.example.mstracker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mstracker.database.AppDatabase;
import com.example.mstracker.model.WatchItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private WatchAdapter adapter;
    private TextView filterAll, filterMovies, filterSeries;
    private ImageView profileImage;
    private AppDatabase db;
    private String currentFilter = "All";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = AppDatabase.getInstance(getContext());
        recyclerView = view.findViewById(R.id.recyclerViewHome);
        filterAll = view.findViewById(R.id.filterAll);
        filterMovies = view.findViewById(R.id.filterMovies);
        filterSeries = view.findViewById(R.id.filterSeries);
        profileImage = view.findViewById(R.id.profileImage);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WatchAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        filterAll.setOnClickListener(v -> updateFilter("All"));
        filterMovies.setOnClickListener(v -> updateFilter("Movie"));
        filterSeries.setOnClickListener(v -> updateFilter("Series"));

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserProfileActivity.class);
            startActivity(intent);
        });

        loadProfileImage();
        loadData();
        return view;
    }

    private void loadProfileImage() {
        if (getContext() != null) {
            String uriString = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getString("profile_image_uri", null);
            if (uriString != null) {
                Glide.with(this)
                        .load(Uri.parse(uriString))
                        .centerCrop()
                        .into(profileImage);
            }
        }
    }

    private void updateFilter(String filter) {
        currentFilter = filter;
        filterAll.setBackgroundResource(filter.equals("All") ? R.drawable.pill_background_selected : R.drawable.pill_background);
        filterAll.setTextColor(ContextCompat.getColor(getContext(), filter.equals("All") ? R.color.bg : R.color.text_sub));
        
        filterMovies.setBackgroundResource(filter.equals("Movie") ? R.drawable.pill_background_selected : R.drawable.pill_background);
        filterMovies.setTextColor(ContextCompat.getColor(getContext(), filter.equals("Movie") ? R.color.bg : R.color.text_sub));

        filterSeries.setBackgroundResource(filter.equals("Series") ? R.drawable.pill_background_selected : R.drawable.pill_background);
        filterSeries.setTextColor(ContextCompat.getColor(getContext(), filter.equals("Series") ? R.color.bg : R.color.text_sub));

        loadData();
    }

    private void loadData() {
        executor.execute(() -> {
            List<WatchItem> items;
            if (currentFilter.equals("All")) {
                items = db.watchDao().getAll();
            } else {
                items = db.watchDao().getByType(currentFilter);
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.updateData(items));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileImage();
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
