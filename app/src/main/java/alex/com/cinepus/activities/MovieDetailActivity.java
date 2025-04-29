package alex.com.cinepus.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;
import alex.com.cinepus.BuildConfig;
import alex.com.cinepus.R;
import alex.com.cinepus.network.ApiClient;
import retrofit2.Call;
import retrofit2.Response;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;
import android.view.ViewGroup;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import android.widget.Toast;
import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {
    private YouTubePlayerView youtubePlayerView;
    private ImageView posterImageView;
    private TextView titleTextView, overviewTextView, ratingTextView, releaseDateTextView;
    private TextView currentTimeTextView, totalTimeTextView;
    private SeekBar videoSeekBar;
    private ProgressBar progressBar;
    private View videoContainer;
    private ImageButton shareButton, favoriteButton, fullscreenButton;
    private YouTubePlayer activePlayer;
    private boolean isVideoPlaying = false;
    private boolean isFullscreen = false;
    private String currentVideoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configurar el manejador del botón atrás
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isVideoPlaying && activePlayer != null) {
                    activePlayer.pause();
                }
                finish();
            }
        });
        
        // Configurar la barra de estado y navegación
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }
        
        setContentView(R.layout.activity_movie_detail);
        initializeViews();
        setupListeners();
        loadMovieData();
    }

    private void initializeViews() {
        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        posterImageView = findViewById(R.id.posterImageView);
        titleTextView = findViewById(R.id.titleTextView);
        overviewTextView = findViewById(R.id.overviewTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        releaseDateTextView = findViewById(R.id.releaseDateTextView);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        videoSeekBar = findViewById(R.id.videoSeekBar);
        progressBar = findViewById(R.id.progressBar);
        videoContainer = findViewById(R.id.videoContainer);
        shareButton = findViewById(R.id.shareButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        fullscreenButton = findViewById(R.id.fullscreenButton);

        // Inicializar YouTubePlayerView
        getLifecycle().addObserver(youtubePlayerView);
    }

    private void setupListeners() {
        shareButton.setOnClickListener(v -> shareMovie());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
        
        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && activePlayer != null) {
                    float time = progress / 100f;
                    activePlayer.seekTo(time);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadMovieData() {
        int movieId = getIntent().getIntExtra("movie_id", 0);
        String title = getIntent().getStringExtra("movie_title");
        String overview = getIntent().getStringExtra("movie_overview");
        String posterUrl = getIntent().getStringExtra("movie_poster");
        double rating = getIntent().getDoubleExtra("movie_rating", 0.0);
        String releaseDate = getIntent().getStringExtra("movie_release_date");

        titleTextView.setText(title != null && !title.isEmpty() ? title : "Sin título");
        overviewTextView.setText(overview != null && !overview.isEmpty() ? overview : "No hay descripción disponible");
        ratingTextView.setText(rating > 0 ? String.format("%.1f ★", rating) : "Sin calificación");
        releaseDateTextView.setText(releaseDate != null && !releaseDate.isEmpty() ? 
            "Fecha de estreno: " + releaseDate : "Fecha no disponible");
        
        if (posterUrl != null && !posterUrl.isEmpty()) {
            Picasso.get()
                   .load(posterUrl)
                   .placeholder(R.drawable.placeholder_movie)
                   .error(R.drawable.error_movie)
                   .into(posterImageView, new com.squareup.picasso.Callback() {
                       @Override
                       public void onSuccess() {}

                       @Override
                       public void onError(Exception e) {
                           posterImageView.setImageResource(R.drawable.error_movie);
                       }
                   });
        } else {
            posterImageView.setImageResource(R.drawable.error_movie);
        }

        if (movieId > 0) {
            loadMovieVideo(movieId);
        } else {
            hideLoading();
            showPosterOnly();
        }
    }

    private void loadMovieVideo(int movieId) {
        progressBar.setVisibility(View.VISIBLE);
        
        ApiClient.getApiService().getMovieVideos(movieId, BuildConfig.TMDB_API_KEY, "es-ES")
                .enqueue(new retrofit2.Callback<alex.com.cinepus.network.VideoResponse>() {
                    @Override
                    public void onResponse(Call<alex.com.cinepus.network.VideoResponse> call, Response<alex.com.cinepus.network.VideoResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            alex.com.cinepus.network.VideoResponse.Video bestVideo = findBestVideo(response.body().getResults());
                            
                            if (bestVideo != null) {
                                currentVideoId = bestVideo.getKey();
                                showVideo(currentVideoId);
                            } else {
                                showPosterOnly();
                                Toast.makeText(MovieDetailActivity.this, 
                                    "No hay trailer disponible para esta película", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            handleVideoError(new Exception("Error en la respuesta"));
                        }
                    }

                    @Override
                    public void onFailure(Call<alex.com.cinepus.network.VideoResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        handleVideoError(new Exception(t.getMessage()));
                    }
                });
    }

    private alex.com.cinepus.network.VideoResponse.Video findBestVideo(List<alex.com.cinepus.network.VideoResponse.Video> videos) {
        String[] preferredTypes = {"Trailer", "Teaser", "Clip", "Behind the Scenes", ""};
        
        for (String type : preferredTypes) {
            for (alex.com.cinepus.network.VideoResponse.Video video : videos) {
                if ("YouTube".equalsIgnoreCase(video.getSite()) && 
                    (type.isEmpty() || type.equalsIgnoreCase(video.getType()))) {
                    return video;
                }
            }
        }
        return null;
    }

    private void showVideo(String videoId) {
        if (isFinishing() || !getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            return;
        }

        posterImageView.setVisibility(View.GONE);
        youtubePlayerView.setVisibility(View.VISIBLE);
        fullscreenButton.setVisibility(View.VISIBLE);
        isVideoPlaying = true;

        youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                activePlayer = youTubePlayer;
                youTubePlayer.loadVideo(videoId, 0);
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                currentTimeTextView.setText(formatTime((int)second));
                videoSeekBar.setProgress((int)(second * 100));
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                totalTimeTextView.setText(formatTime((int)duration));
                videoSeekBar.setMax((int)(duration * 100));
            }
        });
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void shareMovie() {
        String title = getIntent().getStringExtra("movie_title");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira " + title + " en CINEPUS!");
        startActivity(Intent.createChooser(shareIntent, "Compartir película"));
    }

    private void toggleFavorite() {
        // Aquí implementarías la lógica para marcar/desmarcar como favorito
        favoriteButton.setSelected(!favoriteButton.isSelected());
    }

    private void hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().hide(
                android.view.WindowInsets.Type.systemBars()
            );
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void showSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getInsetsController().show(
                android.view.WindowInsets.Type.systemBars()
            );
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            showSystemBars();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            hideSystemBars();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        isFullscreen = !isFullscreen;
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showPosterOnly() {
        youtubePlayerView.setVisibility(View.GONE);
        posterImageView.setVisibility(View.VISIBLE);
        fullscreenButton.setVisibility(View.GONE);
        isVideoPlaying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youtubePlayerView != null) {
            getLifecycle().removeObserver(youtubePlayerView);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemBars();
        } else {
            showSystemBars();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFullscreen) {
            hideSystemBars();
        }
    }

    private void handleVideoError(Exception error) {
        progressBar.setVisibility(View.GONE);
        showPosterOnly();
        Toast.makeText(this, "Error al cargar el video", Toast.LENGTH_SHORT).show();
    }
} 