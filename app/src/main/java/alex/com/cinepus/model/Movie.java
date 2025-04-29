package alex.com.cinepus.model;

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

    // Getters con manejo de valores nulos o vacíos
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title != null && !title.isEmpty() ? title : "Sin título";
    }

    public String getOverview() {
        return overview != null && !overview.isEmpty() ? overview : "No hay descripción disponible";
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate != null && !releaseDate.isEmpty() ? releaseDate : "Fecha no disponible";
    }

    // Método para obtener la URL completa del póster
    public String getPosterUrl() {
        if (posterPath == null || posterPath.isEmpty()) {
            return null; // Retornará null para que Picasso use la imagen de error
        }
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    // Método para obtener la URL completa del backdrop
    public String getBackdropUrl() {
        if (backdropPath == null || backdropPath.isEmpty()) {
            return null; // Retornará null para que Picasso use la imagen de error
        }
        return "https://image.tmdb.org/t/p/w500" + backdropPath;
    }
} 