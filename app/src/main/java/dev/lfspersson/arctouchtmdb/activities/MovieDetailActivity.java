package dev.lfspersson.arctouchtmdb.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import dev.lfspersson.arctouchtmdb.R;
import dev.lfspersson.arctouchtmdb.database.dao.MovieDAO;
import dev.lfspersson.arctouchtmdb.database.models.MovieRealmModel;

@EActivity(R.layout.activity_movie_detail)
public class MovieDetailActivity extends AppCompatActivity {
    private int movieId;
    private Context context;

    @Bean
    MovieDAO movieDAO;

    @ViewById
    ImageView ivPoster;
    @ViewById
    ImageView ivBackdrop;
    @ViewById
    TextView tvTitle;
    @ViewById
    TextView tvGenre;
    @ViewById
    TextView tvReleaseDate;
    @ViewById
    TextView tvOverview;

    @AfterViews
    void initialize() {
        movieId = (Integer) getIntent().getSerializableExtra("movieId");
        context = getApplicationContext();

        loadInfoScreen();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void loadInfoScreen(){
        String imageBaseUrl = context.getString(R.string.image_base_url);
        MovieRealmModel movie = movieDAO.getMovieById(movieId);

        Glide.with(ivPoster.getContext())
                .load(imageBaseUrl + movie.getPoster_path())
                .into(ivPoster);

        Glide.with(ivBackdrop.getContext())
                .load(imageBaseUrl + movie.getBackdrop_path())
                .into(ivBackdrop);

        tvTitle.setText(movie.getTitle());
        tvGenre.setText(movie.getGenres());
        tvReleaseDate.setText(movie.getRelease_date());
        tvOverview.setText(movie.getOverview());
    }

}
