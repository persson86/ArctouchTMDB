package dev.lfspersson.arctouchtmdb.database.dao;

import android.content.Context;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

import dev.lfspersson.arctouchtmdb.database.DatabaseHelper;
import dev.lfspersson.arctouchtmdb.database.models.MovieModel;
import dev.lfspersson.arctouchtmdb.database.models.MovieRealmModel;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by LFSPersson on 01/12/16.
 */

@EBean(scope = EBean.Scope.Singleton)
public class MovieDAO {
    @RootContext
    Context context;

    @Bean
    DatabaseHelper dbHelper;

    public void saveMovies(List<MovieRealmModel> model) {
        Realm realm = dbHelper.getRealm();
        realm.beginTransaction();
        realm.where(MovieRealmModel.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.beginTransaction();
        realm.copyToRealm(model);
        realm.commitTransaction();
    }

    public void saveMovie(MovieRealmModel model) {
        Realm realm = dbHelper.getRealm();
        realm.beginTransaction();
        realm.where(MovieRealmModel.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.beginTransaction();
        realm.copyToRealm(model);
        realm.commitTransaction();
    }

    public List<MovieRealmModel> getMovies() {
        return dbHelper.getRealm().where(MovieRealmModel.class).findAll();
    }

    public MovieRealmModel getMovieById(long id) {
        return dbHelper.getRealm().where(MovieRealmModel.class).equalTo("id", id).findFirst();
    }

    public void deleteMovies() {
        Realm realm = dbHelper.getRealm();
        realm.beginTransaction();
        realm.where(MovieRealmModel.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    public List<MovieRealmModel> getMoviesBySearchTitle(String query) {
        Realm realm = dbHelper.getRealm();
        RealmResults<MovieRealmModel> result = realm.where(MovieRealmModel.class)
                //.equalTo("title", query)
                .contains("title", query)
                .findAll();
        return result;
    }

}