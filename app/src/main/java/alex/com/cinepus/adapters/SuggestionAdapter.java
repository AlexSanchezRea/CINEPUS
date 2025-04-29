package alex.com.cinepus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import alex.com.cinepus.R;
import alex.com.cinepus.model.Movie;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {
    private List<Movie> suggestions;
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Movie movie);
    }

    public SuggestionAdapter(OnSuggestionClickListener listener) {
        this.suggestions = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        Movie movie = suggestions.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void setSuggestions(List<Movie> suggestions) {
        this.suggestions = suggestions;
        notifyDataSetChanged();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(android.R.id.text1);
        }
    }
} 