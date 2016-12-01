package dev.lfspersson.arctouchtmdb.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
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
    RestService service;
    Context context;

    private RecycleAdapter recycleAdapter;

    private DiscoverModel discoverModel;
    private List<MovieModel> responseMovies;
    private List<MovieModel> movieModelList;
    private GenreListModel genreListModel;
    private List<MovieRealmModel> movieRealmModel;

    private static String API_KEY;
    private int page = 1;

    @Bean
    MovieDAO movieDAO;

    @ViewById
    RecyclerView rvList;
    @ViewById
    Button btLoadData;

    @AfterViews
    void initialize() {
        setActivityConfig();
        setScreenConfig();
        setRestConfig();
    }

    private void setActivityConfig(){
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Click
    void btLoadData() {
        page++;
        restGetMovies(page);
    }

    private void onClickListenerCard() {
        recycleAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(context, movieModelList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDeviceLanguage() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        return language + "-" + country;
    }

    @Background
    public void restGetMovies(int page) {
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

                restGetGenres();
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, t.getMessage().toString(), Toast.LENGTH_LONG).show();
                //progressDialog.dismiss();
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
                loadCards(responseMovies);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, t.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @UiThread
    public void loadCards(List<MovieModel> movies) {
        page = discoverModel.getPage();

        if (movieModelList == null) {
            movieModelList = new ArrayList<>();
            for (MovieModel m : movies) {
                movieModelList.add(m);
                saveMovieRealm(m);
            }
        } else {
            for (MovieModel m : movies) {
                movieModelList.add(m);
                saveMovieRealm(m);
            }
            recycleAdapter.notifyItemInserted(movieModelList.size());
            //rvList.scrollToPosition(movieModelList.size() - 20);
        }

        //Save movies list as objetct using Realm
        movieDAO.saveMovies(movieRealmModel);

        rvList.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        recycleAdapter = new RecycleAdapter(context, this, movieRealmModel);
        rvList.setAdapter(recycleAdapter);

        onClickListenerCard();

        //swipeRefreshLayout.setRefreshing(false);
    }

    private void saveMovieRealm(MovieModel m) {
        MovieRealmModel model = new MovieRealmModel();
        model.setId(m.getId());
        model.setTitle(m.getTitle());
        model.setPoster_path(m.getPoster_path());
        model.setOverview(m.getOverview());
        model.setRelease_date(m.getRelease_date());
        model.setGenres(getGenreMovie(m));

        if (movieRealmModel == null)
            movieRealmModel = new ArrayList<>();

        movieRealmModel.add(model);
    }

    private String getGenreMovie(MovieModel movie) {
        String genreDescription = "";
        String separator = " - ";

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

    //-----------------------
    /*    @Background
    public void restAuthentication() {
        final Call<AuthenticationModel> call = service.getAuthentication(API_KEY);
        call.enqueue(new Callback<AuthenticationModel>() {
            @Override
            public void onResponse(Response<AuthenticationModel> response, Retrofit retrofit) {
                AuthenticationModel auth = response.body();

                //restDiscoverMovies();
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, t.getMessage().toString(), Toast.LENGTH_LONG).show();
                //progressDialog.dismiss();
            }
        });
    }*/


/*    private void swipeRefreshLayoutListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(context, "refresh!!!", Toast.LENGTH_SHORT).show();

                page = discoverModel.getPage() + 1;
            }
        });
    }*/
}
