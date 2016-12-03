package dev.lfspersson.arctouchtmdb.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

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
import dev.lfspersson.arctouchtmdb.database.DatabaseHelper;
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
    private Context context;
    private RestService service;
    private RecycleAdapter recycleAdapter;
    private LinearLayoutManager layoutManager;
    private ProgressDialog progressDialog;

    private DiscoverModel discoverModel;
    private List<MovieModel> movieModelList;
    private GenreListModel genreListModel;
    private List<MovieRealmModel> movieRealmModelList;

    private static String API_KEY;
    private int page = 1;
    private boolean moviesInProcess = false;
    private int visibleItemPosition = 0;

    @Bean
    MovieDAO movieDAO;
    @Bean
    DatabaseHelper dbHelper;

    @ViewById
    RecyclerView rvList;
    @ViewById
    Toolbar toolbar;
    @ViewById
    TextView tvToolbarTitle;

    @AfterViews
    void initialize() {
        setActivityConfig();
        startDialog();
        setScreenConfig();
        setRestConfig();
    }

    private void setActivityConfig() {
        context = getApplicationContext();
        API_KEY = getString(R.string.api_key);
    }

    private void startDialog() {
        progressDialog = new ProgressDialog(this, R.style.CustomProgressDialog);
        //progressDialog.setTitle(getResources().getString(R.string.msg_retrieving_data));
        progressDialog.setMessage(getResources().getString(R.string.msg_wait));
        progressDialog.show();
    }

    private void setScreenConfig() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        loadToolbar();
    }

    private void loadToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        tvToolbarTitle.setText(R.string.toolbar_title);
    }

    private void setRestConfig() {
        movieDAO.deleteMovies();
        service = RestService.retrofit.create(RestService.class);
        restGetGenres();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem actionMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) actionMenuItem.getActionView();
        //searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<MovieRealmModel> filteredMoviesList = movieDAO.getMoviesBySearchTitle(query);
                setRecycleViewConfig(true, filteredMoviesList);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    setRecycleViewConfig(true, movieRealmModelList);
                } else {
                    List<MovieRealmModel> filteredMoviesList = movieDAO.getMoviesBySearchTitle(newText);
                    setRecycleViewConfig(true, filteredMoviesList);
                }
                return true;
            }
        });

        return true;
    }

    @Background
    public void restGetGenres() {
        final Call<GenreListModel> call = service.getGenres(API_KEY, getDeviceLanguage());
        call.enqueue(new Callback<GenreListModel>() {
            @Override
            public void onResponse(Response<GenreListModel> response, Retrofit retrofit) {
                genreListModel = response.body();
                restGetMovies();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(getString(R.string.log_error), t.getMessage());
            }
        });
    }

    @Background
    public void restGetMovies() {
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
                List<MovieModel> movieModelListRest = discoverModel.getResults();

                loadMovies(movieModelListRest);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(getString(R.string.log_error), t.getMessage());
                progressDialog.dismiss();
            }
        });
    }

    @UiThread
    public void loadMovies(List<MovieModel> movies) {
        if (movieModelList == null)
            movieModelList = new ArrayList<>();

        for (MovieModel m : movies) {
            movieModelList.add(m);
            saveMovieRealm(m);
        }

        //Save movies list as object using Realm
        movieDAO.saveMovies(movieRealmModelList);

        if (page == 1)
            setRecycleViewConfig(true, movieRealmModelList);
        else
            setRecycleViewConfig(false, movieRealmModelList);

        progressDialog.dismiss();
        moviesInProcess = false;
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
        model.setSearch_title(m.getTitle().toLowerCase());

        if (movieRealmModelList == null)
            movieRealmModelList = new ArrayList<>();

        movieRealmModelList.add(model);
    }

    private void setRecycleViewConfig(boolean newList, List<MovieRealmModel> contentList) {
        if (newList) {
            layoutManager = new GridLayoutManager(MainActivity.this, 2);
            rvList.setLayoutManager(layoutManager);
            rvList.setHasFixedSize(true);
            rvList.addOnScrollListener(createInfiniteScrollListener());
            recycleAdapter = new RecycleAdapter(context, contentList);
            rvList.setAdapter(recycleAdapter);
        } else {
            recycleAdapter.notifyItemInserted(contentList.size());
            rvList.scrollToPosition(visibleItemPosition + 2);
        }

        onClickListenerMovie(contentList);
    }

    private void onClickListenerMovie(final List<MovieRealmModel> contentList) {
        recycleAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieDetailActivity_.intent(context)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .extra("movieId", contentList.get(position).getId())
                        .start();

                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

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

            boolean genreNotInList = true;
            for (GenreModel g : genreListModel.getGenreList()) {
                pos++;
                if (g.getId() == id) {
                    pos = pos - 1;
                    genreNotInList = false;
                    break;
                }
            }

            if (genreNotInList)
                continue;

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
                restGetMovies();
                visibleItemPosition = firstVisibleItemPosition;
            }
        };
    }
}