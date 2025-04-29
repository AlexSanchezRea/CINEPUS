package alex.com.cinepus.network;

import com.google.gson.annotations.SerializedName;
import alex.com.cinepus.models.Movie;
import java.util.List;

public class MovieResponse {
    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<Movie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public int getPage() { return page; }
    public List<Movie> getResults() { return results; }
    public int getTotalPages() { return totalPages; }
    public int getTotalResults() { return totalResults; }
} 