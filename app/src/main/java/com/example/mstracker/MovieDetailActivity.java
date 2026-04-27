package com.example.mstracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mstracker.api.RetrofitClient;
import com.example.mstracker.api.TMDBService;
import com.example.mstracker.database.AppDatabase;
import com.example.mstracker.model.MovieDetailsResponse;
import com.example.mstracker.model.TVDetailsResponse;
import com.example.mstracker.model.WatchItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE       = "extra_title";
    public static final String EXTRA_DIRECTOR    = "extra_director";
    public static final String EXTRA_YEAR        = "extra_year";
    public static final String EXTRA_RUNTIME     = "extra_runtime";
    public static final String EXTRA_RUNTIME_MIN = "extra_runtime_min";
    public static final String EXTRA_RELEASE     = "extra_release";
    public static final String EXTRA_LANGUAGE    = "extra_language";
    public static final String EXTRA_SCORE       = "extra_score";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_TYPE        = "extra_type";
    public static final String EXTRA_POSTER_PATH = "extra_poster_path";
    public static final String EXTRA_TMDB_ID     = "extra_tmdb_id";
    public static final String EXTRA_TMDB_TYPE   = "extra_tmdb_type";

    private static final String API_KEY = "0a0e2bf9fe54e6e65320d51734e258a4";

    // ── Views ────────────────────────────────────────────────
    private ImageView ivPoster;
    private ImageView btnBack;
    private ImageView btnFavourite;
    private ImageView btnMore;
    private TextView  tvTypeBadge;
    private TextView  tvImdbScore;
    private TextView  tvMovieTitle;
    private TextView  tvDirectorYear;
    private TextView  tvRuntime;
    private TextView  tvDescription;
    private TextView  tvReleaseDate;
    private TextView  tvRuntimeDetail;
    private TextView  tvLanguage;
    private TextView  tvGenres;
    private TextView  tvCompletedDate;
    private Button    btnAddToLibrary;
    private Button    btnRemoveFromLibrary;
    private View      layoutLibraryControls;
    private Spinner   spinnerStatus;
    private RatingBar ratingBar;

    // ── State ────────────────────────────────────────────────
    private boolean isFavourited = false;
    private String  movieTitle   = "";
    private String  movieCountry = "Unknown";
    private String  movieCreator = "Unknown";
    private String  movieYear    = "—";
    private String  moviePosterPath = "";
    private WatchItem existingItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        loadIntentData();
        setClickListeners();
    }

    // ── Bind all views ───────────────────────────────────────
    private void bindViews() {
        ivPoster         = findViewById(R.id.ivPoster);
        btnBack          = findViewById(R.id.btnBack);
        btnFavourite     = findViewById(R.id.btnFavourite);
        btnMore          = findViewById(R.id.btnMore);
        tvTypeBadge      = findViewById(R.id.tvTypeBadge);
        tvImdbScore      = findViewById(R.id.tvImdbScore);
        tvMovieTitle     = findViewById(R.id.tvMovieTitle);
        tvDirectorYear   = findViewById(R.id.tvDirectorYear);
        tvRuntime        = findViewById(R.id.tvRuntime);
        tvDescription    = findViewById(R.id.tvDescription);
        tvReleaseDate    = findViewById(R.id.tvReleaseDate);
        tvRuntimeDetail  = findViewById(R.id.tvRuntimeDetail);
        tvLanguage       = findViewById(R.id.tvLanguage);
        tvGenres         = findViewById(R.id.tvGenres);
        tvCompletedDate  = findViewById(R.id.tvCompletedDate);
        btnAddToLibrary  = findViewById(R.id.btnAddToLibrary);
        btnRemoveFromLibrary = findViewById(R.id.btnRemoveFromLibrary);
        layoutLibraryControls = findViewById(R.id.layoutLibraryControls);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        ratingBar = findViewById(R.id.ratingBar);

        btnAddToLibrary.setEnabled(false); // Disable until details are fetched
        setupStatusSpinner();
    }

    private void setupStatusSpinner() {
        String[] statuses = {"Plan to Watch", "Watching", "Completed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = statuses[position];
                
                if (existingItem != null) {
                    // Reset rating if changing away from Completed
                    if (!"Completed".equals(selected) && "Completed".equals(existingItem.getStatus())) {
                        existingItem.setRating(0.0f);
                        ratingBar.setRating(0.0f);
                        existingItem.setCompletedDate(0);
                    }
                    
                    if ("Completed".equals(selected) && !"Completed".equals(existingItem.getStatus())) {
                        existingItem.setCompletedDate(System.currentTimeMillis());
                    }

                    existingItem.setStatus(selected);
                    updateLibraryItem();
                    updateCompletedDateUI();
                }

                ratingBar.setEnabled("Completed".equals(selected));
                ratingBar.setAlpha("Completed".equals(selected) ? 1.0f : 0.5f);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser && existingItem != null) {
                existingItem.setRating(rating * 2); // Scale to 10
                updateLibraryItem();
            }
        });
    }

    private void updateCompletedDateUI() {
        if (existingItem != null && "Completed".equals(existingItem.getStatus()) && existingItem.getCompletedDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(existingItem.getCompletedDate()));
            tvCompletedDate.setText("Done watching on " + dateStr);
            tvCompletedDate.setVisibility(View.VISIBLE);
        } else {
            tvCompletedDate.setVisibility(View.GONE);
        }
    }

    private void updateLibraryItem() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(this).watchDao().update(existingItem);
        });
    }

    // ── Pull data from Intent extras and populate views ──────
    private void loadIntentData() {
        Intent intent = getIntent();
        int tmdbId = intent.getIntExtra(EXTRA_TMDB_ID, -1);
        String tmdbType = intent.getStringExtra(EXTRA_TMDB_TYPE);

        if (tmdbId != -1 && tmdbType != null) {
            fetchFullDetails(tmdbId, tmdbType);
            checkIfAlreadyInLibrary(tmdbId, tmdbType);
        } else {
            // Fallback to basic data if no ID provided
            movieTitle = intent.getStringExtra(EXTRA_TITLE) != null
                    ? intent.getStringExtra(EXTRA_TITLE) : "Unknown Title";

            String director   = intent.getStringExtra(EXTRA_DIRECTOR) != null
                    ? intent.getStringExtra(EXTRA_DIRECTOR) : "Unknown";
            String year       = intent.getStringExtra(EXTRA_YEAR) != null
                    ? intent.getStringExtra(EXTRA_YEAR) : "—";
            String runtime    = intent.getStringExtra(EXTRA_RUNTIME) != null
                    ? intent.getStringExtra(EXTRA_RUNTIME) : "—";
            String runtimeMin = intent.getStringExtra(EXTRA_RUNTIME_MIN) != null
                    ? intent.getStringExtra(EXTRA_RUNTIME_MIN) : "—";
            String release    = intent.getStringExtra(EXTRA_RELEASE) != null
                    ? intent.getStringExtra(EXTRA_RELEASE) : "—";
            String language   = intent.getStringExtra(EXTRA_LANGUAGE) != null
                    ? intent.getStringExtra(EXTRA_LANGUAGE) : "—";
            String score      = intent.getStringExtra(EXTRA_SCORE) != null
                    ? intent.getStringExtra(EXTRA_SCORE) : "—";
            String desc       = intent.getStringExtra(EXTRA_DESCRIPTION) != null
                    ? intent.getStringExtra(EXTRA_DESCRIPTION) : "No description available.";
            String type       = intent.getStringExtra(EXTRA_TYPE) != null
                    ? intent.getStringExtra(EXTRA_TYPE) : "MOVIE";

            // Populate views
            tvMovieTitle.setText(movieTitle);
            tvDirectorYear.setText(director + "  ·  " + year);
            tvRuntime.setText(runtime);
            tvDescription.setText(desc);
            tvReleaseDate.setText(release);
            tvRuntimeDetail.setText(runtimeMin);
            tvLanguage.setText(language);
            tvGenres.setText("—");
            tvTypeBadge.setText(type);
            tvImdbScore.setText("★  " + score);

            String posterPath = intent.getStringExtra(EXTRA_POSTER_PATH);
            if (posterPath != null && !posterPath.isEmpty()) {
                String fullPosterUrl = "https://image.tmdb.org/t/p/w500" + posterPath;
                com.bumptech.glide.Glide.with(this).load(fullPosterUrl).centerCrop().into(ivPoster);
            }
        }
    }

    private void checkIfAlreadyInLibrary(int tmdbId, String tmdbType) {
        Executors.newSingleThreadExecutor().execute(() -> {
            existingItem = AppDatabase.getInstance(this).watchDao().getByTmdbId(tmdbId, tmdbType);
            runOnUiThread(() -> {
                if (existingItem != null) {
                    btnAddToLibrary.setVisibility(View.GONE);
                    btnRemoveFromLibrary.setVisibility(View.VISIBLE);
                    layoutLibraryControls.setVisibility(View.VISIBLE);
                    
                    // Set spinner selection
                    String[] statuses = {"Plan to Watch", "Watching", "Completed"};
                    for (int i = 0; i < statuses.length; i++) {
                        if (statuses[i].equals(existingItem.getStatus())) {
                            spinnerStatus.setSelection(i);
                            break;
                        }
                    }
                    ratingBar.setRating(existingItem.getRating() / 2);
                    ratingBar.setEnabled("Completed".equals(existingItem.getStatus()));
                    ratingBar.setAlpha("Completed".equals(existingItem.getStatus()) ? 1.0f : 0.5f);
                    updateCompletedDateUI();
                } else {
                    btnAddToLibrary.setVisibility(View.VISIBLE);
                    btnRemoveFromLibrary.setVisibility(View.GONE);
                    layoutLibraryControls.setVisibility(View.GONE);
                }
            });
        });
    }

    private void fetchFullDetails(int tmdbId, String type) {
        TMDBService service = RetrofitClient.getClient().create(TMDBService.class);
        if ("movie".equalsIgnoreCase(type)) {
            service.getMovieDetails(tmdbId, API_KEY).enqueue(new Callback<MovieDetailsResponse>() {
                @Override
                public void onResponse(Call<MovieDetailsResponse> call, Response<MovieDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        populateMovieDetails(response.body());
                        fetchMovieCredits(tmdbId);
                    }
                }
                @Override
                public void onFailure(Call<MovieDetailsResponse> call, Throwable t) {
                    Log.e("MovieDetail", "Error: " + t.getMessage());
                }
            });
        } else {
            service.getTVDetails(tmdbId, API_KEY).enqueue(new Callback<TVDetailsResponse>() {
                @Override
                public void onResponse(Call<TVDetailsResponse> call, Response<TVDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        populateTVDetails(response.body());
                    }
                }
                @Override
                public void onFailure(Call<TVDetailsResponse> call, Throwable t) {
                    Log.e("MovieDetail", "Error: " + t.getMessage());
                }
            });
        }
    }

    private void fetchMovieCredits(int movieId) {
        TMDBService service = RetrofitClient.getClient().create(TMDBService.class);
        service.getMovieCredits(movieId, API_KEY).enqueue(new Callback<com.example.mstracker.model.CreditsResponse>() {
            @Override
            public void onResponse(Call<com.example.mstracker.model.CreditsResponse> call, Response<com.example.mstracker.model.CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (com.example.mstracker.model.CreditsResponse.Crew crew : response.body().getCrew()) {
                        if ("Director".equalsIgnoreCase(crew.getJob())) {
                            movieCreator = crew.getName();
                            tvDirectorYear.setText(movieCreator + "  ·  " + movieYear);
                            break;
                        }
                    }
                }
                btnAddToLibrary.setEnabled(true);
            }
            @Override
            public void onFailure(Call<com.example.mstracker.model.CreditsResponse> call, Throwable t) {}
        });
    }

    private String shortenCountry(String country) {
        if (country == null) return "Unknown";
        switch (country) {
            case "United States of America":
                return "America";
            case "South Korea":
                return "Korea";
            case "United Kingdom":
                return "UK";
            case "Philippines":
                return "Philippines";
            default:
                return country;
        }
    }

    private void populateMovieDetails(MovieDetailsResponse movie) {
        movieTitle = movie.getTitle();
        moviePosterPath = movie.getPosterPath();
        tvMovieTitle.setText(movie.getTitle());
        tvDescription.setText(movie.getOverview());
        tvReleaseDate.setText(formatDate(movie.getReleaseDate()));
        tvRuntime.setText(movie.getRuntime() + " min");
        tvRuntimeDetail.setText(movie.getRuntime() + " min");
        tvImdbScore.setText("★  " + String.format("%.1f", movie.getVoteAverage()));
        tvLanguage.setText(movie.getOriginalLanguage().toUpperCase());
        tvTypeBadge.setText("MOVIE");
        
        if (movie.getProductionCountries() != null && !movie.getProductionCountries().isEmpty()) {
            movieCountry = shortenCountry(movie.getProductionCountries().get(0).getName());
        }

        movieYear = movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4 ? 
                movie.getReleaseDate().substring(0, 4) : "—";
        tvDirectorYear.setText("Movie  ·  " + movieYear);

        if (movie.getPosterPath() != null) {
            String fullPosterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
            com.bumptech.glide.Glide.with(this).load(fullPosterUrl).centerCrop().into(ivPoster);
        }

        if (movie.getGenres() != null) {
            List<String> genreNames = new ArrayList<>();
            for (MovieDetailsResponse.Genre genre : movie.getGenres()) {
                genreNames.add(genre.getName());
            }
            tvGenres.setText(String.join(", ", genreNames));
        }
    }

    private void populateTVDetails(TVDetailsResponse tv) {
        btnAddToLibrary.setEnabled(true);
        movieTitle = tv.getName();
        moviePosterPath = tv.getPosterPath();
        tvMovieTitle.setText(tv.getName());
        tvDescription.setText(tv.getOverview());
        tvReleaseDate.setText(formatDate(tv.getFirstAirDate()));
        
        if (tv.getProductionCountries() != null && !tv.getProductionCountries().isEmpty()) {
            movieCountry = tv.getProductionCountries().get(0).getName();
        }

        String runtimeStr;
        if (tv.getEpisodeRunTime() != null && !tv.getEpisodeRunTime().isEmpty()) {
            runtimeStr = tv.getEpisodeRunTime().get(0) + " min";
        } else if (tv.getNumberOfEpisodes() > 0) {
            runtimeStr = tv.getNumberOfEpisodes() + " episodes";
        } else {
            runtimeStr = "TV Series"; 
        }
        tvRuntime.setText(runtimeStr);
        tvRuntimeDetail.setText(runtimeStr);
        
        tvImdbScore.setText("★  " + String.format("%.1f", tv.getVoteAverage()));
        tvLanguage.setText(tv.getOriginalLanguage().toUpperCase());
        tvTypeBadge.setText("SERIES");

        if (tv.getProductionCountries() != null && !tv.getProductionCountries().isEmpty()) {
            movieCountry = shortenCountry(tv.getProductionCountries().get(0).getName());
        }

        movieYear = tv.getFirstAirDate() != null && tv.getFirstAirDate().length() >= 4 ? 
                tv.getFirstAirDate().substring(0, 4) : "—";
        
        String creators = "Unknown";
        if (tv.getCreatedBy() != null && !tv.getCreatedBy().isEmpty()) {
            creators = tv.getCreatedBy().get(0).getName();
            movieCreator = creators;
        }
        tvDirectorYear.setText(creators + "  ·  " + movieYear);

        if (tv.getPosterPath() != null) {
            String fullPosterUrl = "https://image.tmdb.org/t/p/w500" + tv.getPosterPath();
            com.bumptech.glide.Glide.with(this).load(fullPosterUrl).centerCrop().into(ivPoster);
        }

        if (tv.getGenres() != null) {
            List<String> genreNames = new ArrayList<>();
            for (TVDetailsResponse.Genre genre : tv.getGenres()) {
                genreNames.add(genre.getName());
            }
            tvGenres.setText(String.join(", ", genreNames));
        }
    }

    private String formatDate(String inputDate) {
        if (inputDate == null || inputDate.isEmpty()) return "—";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.US);
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (Exception e) {
            return inputDate;
        }
    }

    // ── Click listeners ──────────────────────────────────────
    private void setClickListeners() {

        // Back → close this screen
        btnBack.setOnClickListener(v -> finish());

        // Add to Library
        btnAddToLibrary.setOnClickListener(v -> {
            String type = getIntent().getStringExtra(EXTRA_TYPE);
            String formattedType = (type != null && (type.equalsIgnoreCase("SERIES") || type.equalsIgnoreCase("TV"))) ? "Series" : "Movie";
            
            WatchItem newItem = new WatchItem(
                movieTitle,
                formattedType,
                "Plan to Watch",
                0.0f,
                moviePosterPath,
                movieYear
            );
            newItem.setTmdbId(getIntent().getIntExtra(EXTRA_TMDB_ID, -1));
            newItem.setTmdbType(getIntent().getStringExtra(EXTRA_TMDB_TYPE));
            newItem.setGenre(tvGenres.getText().toString()); // Save genres
            newItem.setCountry(movieCountry);
            newItem.setCreator(movieCreator);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(this).watchDao().insert(newItem);
                runOnUiThread(() -> {
                    Toast.makeText(this, "\"" + movieTitle + "\" added to your library!", Toast.LENGTH_SHORT).show();
                    checkIfAlreadyInLibrary(newItem.getTmdbId(), newItem.getTmdbType());
                });
            });
        });

        // Remove from Library
        btnRemoveFromLibrary.setOnClickListener(v -> {
            if (existingItem != null) {
                int id = existingItem.getTmdbId();
                String type = existingItem.getTmdbType();
                Executors.newSingleThreadExecutor().execute(() -> {
                    AppDatabase.getInstance(this).watchDao().delete(existingItem);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "\"" + movieTitle + "\" removed from library", Toast.LENGTH_SHORT).show();
                        checkIfAlreadyInLibrary(id, type);
                    });
                });
            }
        });

        // Favourite / heart toggle
        btnFavourite.setOnClickListener(v -> {
            isFavourited = !isFavourited;

            if (isFavourited) {
                // Filled heart — tint gold
                btnFavourite.setColorFilter(
                        getResources().getColor(R.color.gold, getTheme()));
                Toast.makeText(this, "Added to favourites", Toast.LENGTH_SHORT).show();
            } else {
                // Outline heart — tint grey
                btnFavourite.setColorFilter(
                        getResources().getColor(R.color.text_muted, getTheme()));
                Toast.makeText(this, "Removed from favourites", Toast.LENGTH_SHORT).show();
            }
        });

        // More options (3-dot)
        btnMore.setOnClickListener(v -> showMoreOptions());
    }

    // ── More options bottom sheet / popup ────────────────────
    private void showMoreOptions() {
        Toast.makeText(this, "More options coming soon", Toast.LENGTH_SHORT).show();
    }
}
