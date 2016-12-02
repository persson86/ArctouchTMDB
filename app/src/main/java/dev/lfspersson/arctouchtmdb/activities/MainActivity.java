package dev.lfspersson.arctouchtmdb.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.github.pwittchen.infinitescroll.library.InfiniteScrollListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.lfspersson.arctouchtmdb.R;
import dev.lfspersson.arctouchtmdb.adapters.RecycleAdapter;
import dev.lfspersson.arctouchtmdb.database.dao.MovieDAO;
import dev.lfspersson.arctouchtmdb.database.models.DiscoverModel;
import dev.lfspersson.arctouchtmdb.database.models.GenreListModel;
import dev.lfspersson.arctouchtmdb.database.models.GenreModel;
import dev.lfspersson.arctouchtmdb.database.models.MovieModel;
import dev.lfspersson.arctouchtmdb.database.models.MovieRealmModel;
import dev.lfspersson.arctouchtmdb.network.RestService;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private RestService service;
    private static String API_KEY;
    private int page = 1;

    private Context context;

    private RecycleAdapter recycleAdapter;
    private LinearLayoutManager layoutManager;
    private ProgressDialog progressDialog;

    private DiscoverModel discoverModel;
    private List<MovieModel> responseMovies;
    private List<MovieModel> movieModelList;
    private GenreListModel genreListModel;
    private List<MovieRealmModel> movieRealmModel;

    public boolean moviesInProcess = false;
    public int visibleItemPosition = 0;

    @Bean
    MovieDAO movieDAO;

    @ViewById
    RecyclerView rvList;

    @AfterViews
    void initialize() {
        startDialog();
        setActivityConfig();
        setScreenConfig();
        setRestConfig();
    }

    private void startDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle(getResources().getString(R.string.msg_retrieving_data));
        progressDialog.setMessage(getResources().getString(R.string.msg_wait));
        progressDialog.show();
    }

    private void setActivityConfig() {
        context = getApplicationContext();
        API_KEY = getString(R.string.api_key);
    }

    private void setScreenConfig() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setRestConfig() {
        movieDAO.deleteMovies();
        service = RestService.retrofit.create(RestService.class);
        restGetMovies(page);
    }

    @Background
    public void restGetMovies(final int page) {
        moviesInProcess = true;

        String sort_by = getString(R.string.sort_by);
        boolean include_adult = false;
        boolean include_video = false;

        final Call<DiscoverModel> call = service.getMovies(API_KEY,
                getDeviceLanguage(),
                sort_by,
                include_adult,
                include_video,
                page);

        call.enqueue(new Callback<DiscoverModel>() {
            @Override
            public void onResponse(Response<DiscoverModel> response, Retrofit retrofit) {
                discoverModel = response.body();

                if (responseMovies != null)
                    responseMovies.clear();

                responseMovies = discoverModel.getResults();

                if (page > 1) {
                    loadMovies(responseMovies);
                } else
                    restGetGenres();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(getString(R.string.log_error), t.getMessage());
                progressDialog.dismiss();
            }
        });
    }

    @Background
    public void restGetGenres() {
        final Call<GenreListModel> call = service.getGenres(API_KEY, getDeviceLanguage());
        call.enqueue(new Callback<GenreListModel>() {
            @Override
            public void onResponse(Response<GenreListModel> response, Retrofit retrofit) {
                genreListModel = response.body();
                loadMovies(responseMovies);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(getString(R.string.log_error), t.getMessage());
            }
        });
    }

    @UiThread
    public void loadMovies(List<MovieModel> movies) {
        page = discoverModel.getPage();

        if (movieModelList == null)
            movieModelList = new ArrayList<>();

        for (MovieModel m : movies) {
            movieModelList.add(m);
            saveMovieRealm(m);
        }

        //Save movies list as object using Realm
        movieDAO.saveMovies(movieRealmModel);

        setRecycleViewConfig(page);
        progressDialog.dismiss();
        moviesInProcess = false;
    }

    private void setRecycleViewConfig(int page) {
        if (page == 1) {
            layoutManager = new GridLayoutManager(MainActivity.this, 2);
            rvList.setLayoutManager(layoutManager);
            rvList.setHasFixedSize(true);
            rvList.addOnScrollListener(createInfiniteScrollListener());
            recycleAdapter = new RecycleAdapter(context, movieRealmModel);
            rvList.setAdapter(recycleAdapter);
        } else {
            recycleAdapter.notifyItemInserted(movieModelList.size());
            rvList.scrollToPosition(visibleItemPosition);
        }

        onClickListenerMovie();
    }

    private void onClickListenerMovie() {
        recycleAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieDetailActivity_.intent(context)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .extra("movieId", movieRealmModel.get(position).getId())
                        .start();

                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

    }

    private void saveMovieRealm(MovieModel m) {
        MovieRealmModel model = new MovieRealmModel();
        model.setId(m.getId());
        model.setTitle(m.getTitle());
        model.setPoster_path(m.getPoster_path());
        model.setBackdrop_path(m.getBackdrop_path());
        model.setOverview(m.getOverview());
        model.setRelease_date(m.getRelease_date());
        model.setGenres(getGenreMovie(m));

        if (movieRealmModel == null)
            movieRealmModel = new ArrayList<>();

        movieRealmModel.add(model);
    }

    private String getDeviceLanguage() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        return language + "-" + country;
    }

    private String getGenreMovie(MovieModel movie) {
        String genreDescription = "";
        String separator = getString(R.string.genre_separator);

        int count = 0;
        for (int id : movie.getGenre_ids()) {
            count++;
            int pos = 0;

            for (GenreModel g : genreListModel.getGenreList()) {
                pos++;
                if (g.getId() == id) {
                    pos = pos - 1;
                    break;
                }
            }

            if (count != movie.getGenre_ids().size()) {
                if (count == 1)
                    genreDescription = genreListModel.getGenreList().get(pos).getName() + separator;
                else {
                    genreDescription = genreDescription + genreListModel.getGenreList().get(pos).getName() + separator;
                }
            } else {
                genreDescription = genreDescription + genreListModel.getGenreList().get(pos).getName();
            }
        }
        return genreDescription;
    }

    private InfiniteScrollListener createInfiniteScrollListener() {
        return new InfiniteScrollListener(movieDAO.getMovies().size(), layoutManager) {
            @Override
            public void onScrolledToEnd(final int firstVisibleItemPosition) {
                if (moviesInProcess)
                    return;

                page++;
                restGetMovies(page);
                visibleItemPosition = firstVisibleItemPosition;
            }
        };
    }
}
