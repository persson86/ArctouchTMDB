package dev.lfspersson.arctouchtmdb.network;

import dev.lfspersson.arctouchtmdb.BuildConfig;
import dev.lfspersson.arctouchtmdb.database.models.DiscoverModel;
import dev.lfspersson.arctouchtmdb.database.models.GenreListModel;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by LFSPersson on 30/11/16.
 */

public interface RestService {
    @GET("discover/movie")
    Call<DiscoverModel> getMovies(@Query("api_key") String api_key,
                                  @Query("language") String language,
                                  @Query("sort_by") String sort_by,
                                  @Query("include_adult") boolean include_adult,
                                  @Query("include_video") boolean include_video,
                                  @Query("page") int page);

    @GET("genre/movie/list")
    Call<GenreListModel> getGenres(@Query("api_key") String api_key,
                                   @Query("language") String language);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_END_POINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
