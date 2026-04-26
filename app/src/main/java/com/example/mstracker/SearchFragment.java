package com.example.mstracker;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mstracker.api.RetrofitClient;
import com.example.mstracker.api.TMDBService;
import com.example.mstracker.model.TMDBResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText editTextSearch;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private TMDBService tmdbService;
    private View btnBack;
    private TextView tvSearchTitle;
    
    private static final String API_KEY = "0a0e2bf9fe54e6e65320d51734e258a4";

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerView = view.findViewById(R.id.recyclerViewResults);
        btnBack = view.findViewById(R.id.btnBack);
        tvSearchTitle = view.findViewById(R.id.header).findViewById(R.id.btnBack).getRootView().findViewById(R.id.header).findViewWithTag("search_title");
        
        // Let's just find the Search title TextView by its text if needed, 
        // but it's easier to just use the layout as is.
        // Actually, let's update the layout to show "Trending" when not searching.

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MovieAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        tmdbService = RetrofitClient.getClient().create(TMDBService.class);

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof HomeScreen) {
                ((HomeScreen) getActivity()).switchToTab(R.id.msHome);
            }
        });

        // Load trending on start
        loadTrending();

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    performSearch(s.toString(), false);
                } else if (s.length() == 0) {
                    loadTrending();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(v.getText().toString(), true);
                hideKeyboard();
                return true;
            }
            return false;
        });

        return view;
    }

    private void loadTrending() {
        tmdbService.getTrending(API_KEY).enqueue(new Callback<TMDBResponse>() {
            @Override
            public void onResponse(Call<TMDBResponse> call, Response<TMDBResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateMovies(response.body().getResults());
                }
            }

            @Override
            public void onFailure(Call<TMDBResponse> call, Throwable t) {
                // Fail silently for trending
            }
        });
    }

    private void performSearch(String query, boolean showToastOnError) {
        if (query.isEmpty()) {
            loadTrending();
            return;
        }

        tmdbService.searchMulti(API_KEY, query).enqueue(new Callback<TMDBResponse>() {
            @Override
            public void onResponse(Call<TMDBResponse> call, Response<TMDBResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateMovies(response.body().getResults());
                } else if (showToastOnError) {
                    Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TMDBResponse> call, Throwable t) {
                if (showToastOnError) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
