package com.example.mstracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mstracker.api.RetrofitClient;
import com.example.mstracker.api.TMDBService;
import com.example.mstracker.database.AppDatabase;
import com.example.mstracker.model.CreditsResponse;
import com.example.mstracker.model.MovieDetailsResponse;
import com.example.mstracker.model.TMDBResponse;
import com.example.mstracker.model.TVDetailsResponse;
import com.example.mstracker.model.WatchItem;
import java.util.List;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<TMDBResponse.Movie> movies;
    private static final String API_KEY = "0a0e2bf9fe54e6e65320d51734e258a4";

    public MovieAdapter(List<TMDBResponse.Movie> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_search, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        TMDBResponse.Movie movie = movies.get(position);
        Context context = holder.itemView.getContext();
        
        holder.title.setText(movie.getTitle());
        String info = (movie.getMediaType() != null ? 
            (movie.getMediaType().equalsIgnoreCase("tv") ? "Series" : "Movie") : "Movie") 
            + " · " + (movie.getReleaseDate() != null ? movie.getReleaseDate() : "—");
        holder.info.setText(info);

        int placeholder = "tv".equalsIgnoreCase(movie.getMediaType()) ? R.drawable.ic_tv_show : R.drawable.ic_movie_clapper;

        String posterUrl = "https://image.tmdb.org/t/p/w200" + movie.getPosterPath();
        Glide.with(context)
             .load(posterUrl)
             .placeholder(placeholder)
             .error(placeholder)
             .into(holder.poster);

        String tmdbType = movie.getMediaType() != null ? movie.getMediaType() : "movie";
        
        // Check if already in library
        Executors.newSingleThreadExecutor().execute(() -> {
            WatchItem existing = AppDatabase.getInstance(context).watchDao().getByTmdbId(movie.getId(), tmdbType);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (existing != null) {
                        holder.addBtn.setVisibility(View.GONE);
                        holder.removeBtn.setVisibility(View.VISIBLE);
                    } else {
                        holder.addBtn.setVisibility(View.VISIBLE);
                        holder.removeBtn.setVisibility(View.GONE);
                    }
                });
            }
        });

        holder.addBtn.setOnClickListener(v -> {
            String type = movie.getMediaType() != null ? 
                (movie.getMediaType().equalsIgnoreCase("tv") ? "Series" : "Movie") : "Movie";
            
            fetchDetailsAndSave(movie, type, tmdbType, context, holder.getAdapterPosition());
        });

        holder.removeBtn.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                WatchItem existing = AppDatabase.getInstance(context).watchDao().getByTmdbId(movie.getId(), tmdbType);
                if (existing != null) {
                    AppDatabase.getInstance(context).watchDao().delete(existing);
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, "\"" + movie.getTitle() + "\" removed from library", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(holder.getAdapterPosition());
                        });
                    }
                }
            });
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.EXTRA_TITLE, movie.getTitle());
            String displayType = movie.getMediaType() != null ? 
                (movie.getMediaType().equalsIgnoreCase("tv") ? "SERIES" : "MOVIE") : "MOVIE";
            intent.putExtra(MovieDetailActivity.EXTRA_TYPE, displayType);
            intent.putExtra(MovieDetailActivity.EXTRA_RELEASE, movie.getReleaseDate());
            intent.putExtra(MovieDetailActivity.EXTRA_YEAR, movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4 ? 
                    movie.getReleaseDate().substring(0, 4) : "—");
            intent.putExtra(MovieDetailActivity.EXTRA_POSTER_PATH, movie.getPosterPath());
            intent.putExtra(MovieDetailActivity.EXTRA_TMDB_ID, movie.getId());
            intent.putExtra(MovieDetailActivity.EXTRA_TMDB_TYPE, tmdbType);
            intent.putExtra(MovieDetailActivity.EXTRA_SCORE, String.valueOf(movie.getVoteAverage())); 
            context.startActivity(intent);
        });
    }

    private void fetchDetailsAndSave(TMDBResponse.Movie movie, String type, String tmdbType, Context context, int position) {
        TMDBService service = RetrofitClient.getClient().create(TMDBService.class);
        if ("movie".equalsIgnoreCase(tmdbType)) {
            service.getMovieDetails(movie.getId(), API_KEY).enqueue(new Callback<MovieDetailsResponse>() {
                @Override
                public void onResponse(Call<MovieDetailsResponse> call, Response<MovieDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MovieDetailsResponse details = response.body();
                        String country = "Unknown";
                        if (details.getProductionCountries() != null && !details.getProductionCountries().isEmpty()) {
                            country = shortenCountry(details.getProductionCountries().get(0).getName());
                        }
                        final String finalCountry = country;

                        service.getMovieCredits(movie.getId(), API_KEY).enqueue(new Callback<CreditsResponse>() {
                            @Override
                            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                                String director = "Unknown";
                                if (response.isSuccessful() && response.body() != null) {
                                    for (CreditsResponse.Crew crew : response.body().getCrew()) {
                                        if ("Director".equalsIgnoreCase(crew.getJob())) {
                                            director = crew.getName();
                                            break;
                                        }
                                    }
                                }
                                saveToLibrary(movie, type, tmdbType, finalCountry, director, context, position);
                            }
                            @Override
                            public void onFailure(Call<CreditsResponse> call, Throwable t) {
                                saveToLibrary(movie, type, tmdbType, finalCountry, "Unknown", context, position);
                            }
                        });
                    }
                }
                @Override
                public void onFailure(Call<MovieDetailsResponse> call, Throwable t) {}
            });
        } else {
            service.getTVDetails(movie.getId(), API_KEY).enqueue(new Callback<TVDetailsResponse>() {
                @Override
                public void onResponse(Call<TVDetailsResponse> call, Response<TVDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        TVDetailsResponse details = response.body();
                        String country = "Unknown";
                        if (details.getProductionCountries() != null && !details.getProductionCountries().isEmpty()) {
                            country = shortenCountry(details.getProductionCountries().get(0).getName());
                        }
                        String creator = "Unknown";
                        if (details.getCreatedBy() != null && !details.getCreatedBy().isEmpty()) {
                            creator = details.getCreatedBy().get(0).getName();
                        }
                        saveToLibrary(movie, type, tmdbType, country, creator, context, position);
                    }
                }
                @Override
                public void onFailure(Call<TVDetailsResponse> call, Throwable t) {}
            });
        }
    }

    private String shortenCountry(String country) {
        if (country == null) return "Unknown";
        switch (country) {
            case "United States of America": return "America";
            case "South Korea": return "Korea";
            case "United Kingdom": return "UK";
            default: return country;
        }
    }

    private void saveToLibrary(TMDBResponse.Movie movie, String type, String tmdbType, String country, String creator, Context context, int position) {
        WatchItem newItem = new WatchItem(
            movie.getTitle(),
            type,
            "Plan to Watch",
            0.0f,
            movie.getPosterPath(),
            movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4 ? 
                movie.getReleaseDate().substring(0, 4) : "—"
        );
        newItem.setTmdbId(movie.getId());
        newItem.setTmdbType(tmdbType);
        newItem.setCountry(country);
        newItem.setCreator(creator);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(context).watchDao().insert(newItem);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "\"" + movie.getTitle() + "\" added to library!", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public void updateMovies(List<TMDBResponse.Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, info, addBtn, removeBtn;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.res_poster);
            title = itemView.findViewById(R.id.res_title);
            info = itemView.findViewById(R.id.res_info);
            addBtn = itemView.findViewById(R.id.add_btn);
            removeBtn = itemView.findViewById(R.id.remove_btn);
        }
    }
}
