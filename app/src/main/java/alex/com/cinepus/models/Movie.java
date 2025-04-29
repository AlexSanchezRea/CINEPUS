package alex.com.cinepus.models;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("id")
    private int id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("overview")
    private String overview;
    
    @SerializedName("poster_path")
    private String posterPath;
    
    @SerializedName("backdrop_path")
    private String backdropPath;
    
    @SerializedName("vote_average")
    private double voteAverage;
    
    @SerializedName("release_date")
    private String releaseDate;

    // Constructor
    public Movie(int id, String title, String overview, String posterPath, String backdropPath, double voteAverage, String releaseDate) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public double getVoteAverage() { return voteAverage; }
    public String getReleaseDate() { return releaseDate; }

    // Método para obtener la URL completa del póster
    public String getFullPosterPath() {
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    // Método para obtener la URL completa del backdrop
    public String getFullBackdropPath() {
        return "https://image.tmdb.org/t/p/original" + backdropPath;
    }
} 