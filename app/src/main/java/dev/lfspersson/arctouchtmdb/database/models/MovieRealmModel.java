package dev.lfspersson.arctouchtmdb.database.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmObject;

/**
 * Created by LFSPersson on 30/11/16.
 */

public class MovieRealmModel extends RealmObject {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("backdrop_path")
    private String backdrop_path;
    @SerializedName("poster_path")
    private String poster_path;
    @SerializedName("overview")
    private String overview;
    @SerializedName("release_date")
    private String release_date;
    private String search_title;

    private String genres;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getSearch_title() {
        return search_title;
    }

    public void setSearch_title(String search_title) {
        this.search_title = search_title;
    }
}
