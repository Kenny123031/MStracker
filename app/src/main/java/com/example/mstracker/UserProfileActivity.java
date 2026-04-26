package com.example.mstracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mstracker.database.AppDatabase;
import com.example.mstracker.model.WatchItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvUsername, tvHandle;
    private RecyclerView rvTopPicks;
    private ChipGroup chipGroupGenres;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<String> getContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupImagePicker();
        loadProfileImage();
        loadUserData();
        setClickListeners();
    }

    private void bindViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUsername = findViewById(R.id.tvUsername);
        tvHandle = findViewById(R.id.tvHandle);
        rvTopPicks = findViewById(R.id.rvTopPicks);
        chipGroupGenres = findViewById(R.id.chipGroupGenres);

        rvTopPicks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupImagePicker() {
        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        getSharedPreferences("user_prefs", MODE_PRIVATE).edit()
                                .putString("profile_image_uri", uri.toString()).apply();
                        Glide.with(this).load(uri).centerCrop().into(ivAvatar);
                    }
                });
    }

    private void loadProfileImage() {
        String uriString = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("profile_image_uri", null);
        if (uriString != null) {
            Glide.with(this).load(Uri.parse(uriString)).centerCrop().into(ivAvatar);
        }
    }

    private void loadUserData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<WatchItem> topPicks = db.watchDao().getTopPicks();
            List<WatchItem> allItems = db.watchDao().getAll();

            // Extract genres (this is basic since we just added the field)
            // For now, let's just use some sample genres if list is empty
            List<String> topGenres = getTopGenres(allItems);

            runOnUiThread(() -> {
                tvUsername.setText("Twin");
                tvHandle.setText("@twin · member since 2024");

                if (topPicks.isEmpty()) {
                    findViewById(R.id.labelTopPicks).setVisibility(View.GONE);
                    rvTopPicks.setVisibility(View.GONE);
                } else {
                    rvTopPicks.setAdapter(new TopPicksAdapter(topPicks));
                }

                updateGenreChips(topGenres);
            });
        });
    }

    private List<String> getTopGenres(List<WatchItem> items) {
        if (items.isEmpty()) {
            return Arrays.asList("Action", "Sci-Fi", "Drama");
        }
        Map<String, Integer> counts = new HashMap<>();
        for (WatchItem item : items) {
            if (item.getGenre() != null) {
                String[] genres = item.getGenre().split(", ");
                for (String g : genres) {
                    counts.put(g, counts.getOrDefault(g, 0) + 1);
                }
            }
        }
        List<String> sortedGenres = new ArrayList<>(counts.keySet());
        sortedGenres.sort((a, b) -> {
            Integer valB = counts.get(b);
            Integer valA = counts.get(a);
            return (valB != null ? valB : 0) - (valA != null ? valA : 0);
        });
        
        if (sortedGenres.isEmpty()) return Arrays.asList("Movies", "Series", "Tracker");
        return sortedGenres.subList(0, Math.min(5, sortedGenres.size()));
    }

    private void updateGenreChips(List<String> genres) {
        chipGroupGenres.removeAllViews();
        for (String genre : genres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setChipBackgroundColorResource(R.color.bg_surface);
            chip.setChipStrokeColorResource(R.color.border);
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));
            chipGroupGenres.addView(chip);
        }
    }

    private void setClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ivAvatar.setOnClickListener(v -> getContent.launch("image/*"));
        
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> 
                Toast.makeText(this, "Edit profile coming soon", Toast.LENGTH_SHORT).show());
                
        findViewById(R.id.settingDarkMode).setOnClickListener(v -> 
                Toast.makeText(this, "Theme settings coming soon", Toast.LENGTH_SHORT).show());
                
        findViewById(R.id.settingBackup).setOnClickListener(v -> 
                Toast.makeText(this, "Data backup coming soon", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
