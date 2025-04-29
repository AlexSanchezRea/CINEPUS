package alex.com.cinepus.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import alex.com.cinepus.BuildConfig;
import alex.com.cinepus.R;
import alex.com.cinepus.adapters.MovieAdapter;
import alex.com.cinepus.adapters.SuggestionAdapter;
import alex.com.cinepus.model.Movie;
import alex.com.cinepus.model.MovieResponse;
import alex.com.cinepus.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SuggestionAdapter.OnSuggestionClickListener {
    private RecyclerView moviesRecyclerView;
    private RecyclerView suggestionsRecyclerView;
    private MovieAdapter movieAdapter;
    private SuggestionAdapter suggestionAdapter;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private EditText searchEditText;
    private CardView suggestionsCardView;
    private boolean isDataLoaded = false;
    private Timer searchTimer;
    private TextWatcher searchTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Manejar la transición del splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        // Mantener el splash screen visible hasta que los datos se carguen
        splashScreen.setKeepOnScreenCondition(() -> !isDataLoaded);
        
        super.onCreate(savedInstanceState);
        
        // Habilitar edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);
        
        // Inicializar vistas
        initializeViews();
        
        // Configurar insets
        setupWindowInsets();
        
        // Configurar búsqueda
        setupSearch();
        
        // Cargar películas
        loadMovies();
    }
    
    private void initializeViews() {
        moviesRecyclerView = findViewById(R.id.moviesRecyclerView);
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        searchEditText = findViewById(R.id.searchEditText);
        suggestionsCardView = findViewById(R.id.suggestionsCardView);

        // Configurar RecyclerView
        moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter(this, new ArrayList<>());
        moviesRecyclerView.setAdapter(movieAdapter);

        // Configurar RecyclerView de sugerencias
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SuggestionAdapter(this);
        suggestionsRecyclerView.setAdapter(suggestionAdapter);

        // Configurar chips de categorías
        setupCategoryChips();
    }
    
    private void setupSearch() {
        searchTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    suggestionsCardView.setVisibility(View.GONE);
                    loadMovies();
                } else {
                    searchTimer = new Timer();
                    searchTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                if (query.length() >= 2) {
                                    searchSuggestions(query);
                                }
                            });
                        }
                    }, 200);
                }
            }
        };
        
        searchEditText.addTextChangedListener(searchTextWatcher);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    suggestionsCardView.setVisibility(View.GONE);
                    searchEditText.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    searchMovies(query);
                }
                return true;
            }
            return false;
        });
    }
    
    private void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);
        View statusBarSpace = findViewById(R.id.statusBarSpace);
        
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            int topInset = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            
            // Ajustar la altura del espacio para el notch
            ViewGroup.LayoutParams params = statusBarSpace.getLayoutParams();
            params.height = topInset;
            statusBarSpace.setLayoutParams(params);
            
            return windowInsets;
        });
    }

    private void loadMovies() {
        showLoading();
        
        String apiKey = BuildConfig.TMDB_API_KEY;
        ApiClient.getApiService().getPopularMovies(apiKey, "es-ES", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        hideLoading();
                        if (response.isSuccessful() && response.body() != null) {
                            movieAdapter.setMovies(response.body().getResults());
                            isDataLoaded = true;
                        } else {
                            showError("Error al cargar las películas");
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        hideLoading();
                        showError("Error de conexión");
                        isDataLoaded = true;
                    }
                });
    }

    private void searchSuggestions(String query) {
        String apiKey = BuildConfig.TMDB_API_KEY;
        ApiClient.getApiService().searchMovies(apiKey, query, "es-ES", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            runOnUiThread(() -> {
                                List<Movie> results = response.body().getResults();
                                if (!results.isEmpty()) {
                                    suggestionAdapter.setSuggestions(results);
                                    suggestionsCardView.setVisibility(View.VISIBLE);
                                } else {
                                    suggestionsCardView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        runOnUiThread(() -> suggestionsCardView.setVisibility(View.GONE));
                    }
                });
    }
    
    @Override
    public void onSuggestionClick(Movie movie) {
        // Ocultamos las sugerencias y el teclado
        suggestionsCardView.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        
        // Actualizamos el texto sin disparar el TextWatcher
        searchEditText.removeTextChangedListener(searchTextWatcher);
        searchEditText.setText(movie.getTitle());
        searchEditText.addTextChangedListener(searchTextWatcher);
        
        // Limpiamos el foco
        searchEditText.clearFocus();
        
        // Actualizamos el adaptador con la película seleccionada
        List<Movie> singleMovieList = new ArrayList<>();
        singleMovieList.add(movie);
        movieAdapter.setMovies(singleMovieList);
    }

    private void searchMovies(String query) {
        showLoading();
        
        String apiKey = BuildConfig.TMDB_API_KEY;
        ApiClient.getApiService().searchMovies(apiKey, query, "es-ES", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        hideLoading();
                        if (response.isSuccessful() && response.body() != null) {
                            movieAdapter.setMovies(response.body().getResults());
                        } else {
                            showError("Error al buscar películas");
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        hideLoading();
                        showError("Error de conexión");
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void setupCategoryChips() {
        com.google.android.material.chip.Chip popularChip = findViewById(R.id.popularChip);
        com.google.android.material.chip.Chip actionChip = findViewById(R.id.actionChip);
        com.google.android.material.chip.Chip animationChip = findViewById(R.id.animationChip);
        com.google.android.material.chip.Chip comedyChip = findViewById(R.id.comedyChip);
        com.google.android.material.chip.Chip dramaChip = findViewById(R.id.dramaChip);
        com.google.android.material.chip.Chip horrorChip = findViewById(R.id.horrorChip);
        com.google.android.material.chip.Chip scifiChip = findViewById(R.id.scifiChip);

        popularChip.setOnClickListener(v -> loadPopularMovies());
        actionChip.setOnClickListener(v -> loadMoviesByGenre(28)); // ID 28 = Acción
        animationChip.setOnClickListener(v -> loadMoviesByGenre(16)); // ID 16 = Animación
        comedyChip.setOnClickListener(v -> loadMoviesByGenre(35)); // ID 35 = Comedia
        dramaChip.setOnClickListener(v -> loadMoviesByGenre(18)); // ID 18 = Drama
        horrorChip.setOnClickListener(v -> loadMoviesByGenre(27)); // ID 27 = Terror
        scifiChip.setOnClickListener(v -> loadMoviesByGenre(878)); // ID 878 = Ciencia Ficción

        // Marcar "Populares" como seleccionado por defecto
        popularChip.setChecked(true);
    }

    private void loadMoviesByGenre(int genreId) {
        showLoading();
        String apiKey = BuildConfig.TMDB_API_KEY;
        ApiClient.getApiService().getMoviesByGenre(apiKey, genreId, "es-ES", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        handleMovieResponse(response);
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        handleMovieError();
                    }
                });
    }

    private void loadPopularMovies() {
        showLoading();
        String apiKey = BuildConfig.TMDB_API_KEY;
        ApiClient.getApiService().getPopularMovies(apiKey, "es-ES", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        handleMovieResponse(response);
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        handleMovieError();
                    }
                });
    }

    private void handleMovieResponse(Response<MovieResponse> response) {
        hideLoading();
        if (response.isSuccessful() && response.body() != null) {
            movieAdapter.setMovies(response.body().getResults());
            isDataLoaded = true;
        } else {
            showError("Error al cargar las películas");
        }
    }

    private void handleMovieError() {
        hideLoading();
        showError("Error de conexión");
        isDataLoaded = true;
    }
} 