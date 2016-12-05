package dev.lfspersson.arctouchtmdb.adapters;

/**
 * Created by LFSPersson on 01/12/16.
 */

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
import dev.lfspersson.arctouchtmdb.database.models.MovieRealmModel;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.GridItemViewHolder> {
    private List<MovieRealmModel> movies;
    private Context context;
    private AdapterView.OnItemClickListener itemClickListener;

    public RecycleAdapter(Context context, List<MovieRealmModel> movies) {
        this.movies = movies;
        this.context = context;
    }

    @Override
    public GridItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new GridItemViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(GridItemViewHolder holder, int position) {
        String imageBaseUrl = context.getString(R.string.image_base_url);
        MovieRealmModel movie = movies.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(movie.getGenres());
        holder.tvReleaseDate.setText(movie.getRelease_date());

        Glide.with(holder.ivPoster.getContext())
                .load(imageBaseUrl + movie.getPoster_path())
                .into(holder.ivPoster);
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