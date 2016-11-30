package dev.lfspersson.arctouchtmdb.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.lfspersson.arctouchtmdb.R;
import dev.lfspersson.arctouchtmdb.database.AuthenticationModel;
import dev.lfspersson.arctouchtmdb.database.DiscoverModel;
import dev.lfspersson.arctouchtmdb.network.RestService;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    RestService service;
    Context context;
    private static String API_KEY;

    @AfterViews
    void initialize() {
        context = getApplicationContext();
        API_KEY = getString(R.string.api_key);

        service = RestService.retrofit.create(RestService.class);
        //restAuthentication();
        restDiscoverMovies();
    }

    private String getDeviceLanguage() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        return language + "-" + country;
    }

    @Background
    public void restAuthentication() {
        final Call<AuthenticationModel> call = service.getAuthentication(API_KEY);
        call.enqueue(new Callback<AuthenticationModel>() {
            @Override
            public void onResponse(Response<AuthenticationModel> response, Retrofit retrofit) {
                AuthenticationModel auth = response.body();

                restDiscoverMovies();
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, t.getMessage().toString(), Toast.LENGTH_LONG).show();
                //progressDialog.dismiss();
            }
        });
    }

    @Background
    public void restDiscoverMovies() {
        String sort_by = "popularity.desc";
        boolean include_adult = false;
        boolean include_video = false;
        int page = 1;

        final Call<DiscoverModel> call = service.getMovies(API_KEY,
                                                            getDeviceLanguage(),
                                                            sort_by,
                                                            include_adult,
                                                            include_video,
                                                            page);
        call.enqueue(new Callback<DiscoverModel>() {
            @Override
            public void onResponse(Response<DiscoverModel> response, Retrofit retrofit) {
                DiscoverModel discoverModel = response.body();

            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(context, t.getMessage().toString(), Toast.LENGTH_LONG).show();
                //progressDialog.dismiss();
            }
        });
    }
}
