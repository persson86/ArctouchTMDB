package dev.lfspersson.arctouchtmdb.adapters;

/**
 * Created by LFSPersson on 01/12/16.
 */

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import dev.lfspersson.arctouchtmdb.R;
import dev.lfspersson.arctouchtmdb.database.GenreListModel;
import dev.lfspersson.arctouchtmdb.database.GenreModel;
import dev.lfspersson.arctouchtmdb.database.MovieModel;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.GridItemViewHolder> {
    private List<MovieModel> movies;
    private GenreListModel genres;
    private Context context;
    private Activity activity;
    private AdapterView.OnItemClickListener itemClickListener;

    public RecycleAdapter(Context context, Activity activity, List<MovieModel> movies, GenreListModel genres) {
        this.movies = movies;
        this.genres = genres;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public GridItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new GridItemViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(GridItemViewHolder holder, int position) {
        String imageBaseUrl = context.getString(R.string.image_base_url);
        MovieModel movie = movies.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(getGenreMovie(position));
        holder.tvReleaseDate.setText(movie.getRelease_date());

        Glide.with(holder.ivPoster.getContext())
                .load(imageBaseUrl + movie.getPoster_path())
                .into(holder.ivPoster);
    }

    private String getGenreMovie(int position) {
        List<Integer> listGenreIds = movies.get(position).getGenre_ids();
        String genre_description = "";
        String separator = " - ";

        int count = 0;
        for (int id : listGenreIds) {
            count++;
            int pos = 0;

            for (GenreModel g : genres.getGenreList()) {
                pos++;
                if (g.getId() == id) {
                    pos = pos - 1;
                    break;
                }
            }

            if (count != listGenreIds.size()) {
                if (count == 1)
                    genre_description = genres.getGenreList().get(pos).getName() + separator;
                else {
                    genre_description = genre_description + genres.getGenreList().get(pos).getName() + separator;
                }
            } else {
                genre_description = genre_description + genres.getGenreList().get(pos).getName();
            }
        }
        return genre_description;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(GridItemViewHolder itemHolder) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    public class GridItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivPoster;
        public TextView tvTitle;
        public TextView tvGenre;
        public TextView tvReleaseDate;
        public RecycleAdapter recycleAdapter;

        public GridItemViewHolder(View itemView, RecycleAdapter adapter) {
            super(itemView);
            recycleAdapter = adapter;
            ivPoster = (ImageView) itemView.findViewById(R.id.ivPoster);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvGenre = (TextView) itemView.findViewById(R.id.tvGenre);
            tvReleaseDate = (TextView) itemView.findViewById(R.id.tvReleaseDate);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recycleAdapter.onItemHolderClick(this);
        }
    }
}

