package alex.com.cinepus.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Path;
import alex.com.cinepus.model.MovieResponse;
import alex.com.cinepus.network.VideoResponse;

public interface ApiService {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(
        @Query("api_key") String apiKey,
        @Query("language") String language,
        @Query("page") int page
    );
    
    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(
        @Query("api_key") String apiKey,
        @Query("language") String language,
        @Query("page") int page
    );
    
    @GET("movie/upcoming")
    Call<MovieResponse> getUpcomingMovies(
        @Query("api_key") String apiKey,
        @Query("language") String language,
        @Query("page") int page
    );
    
    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlayingMovies(
        @Query("api_key") String apiKey,
        @Query("language") String language,
        @Query("page") int page
    );
    
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
        @Query("api_key") String apiKey,
        @Query("query") String query,
        @Query("language") String language,
        @Query("page") int page
    );
    
    @GET("movie/{movie_id}/videos")
    Call<VideoResponse> getMovieVideos(
        @Path("movie_id") int movieId,
        @Query("api_key") String apiKey,
        @Query("language") String language
    );

    @GET("discover/movie")
    Call<MovieResponse> getMoviesByGenre(
        @Query("api_key") String apiKey,
        @Query("with_genres") int genreId,
        @Query("language") String language,
        @Query("page") int page
    );
} 