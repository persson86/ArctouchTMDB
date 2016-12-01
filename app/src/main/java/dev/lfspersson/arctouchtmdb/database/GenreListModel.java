package dev.lfspersson.arctouchtmdb.database;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by LFSPersson on 01/12/16.
 */

public class GenreListModel {
    @SerializedName("genres")
    private List<GenreModel> genreList;

    public List<GenreModel> getGenreList() {
        return genreList;
    }

    public void setGenreList(List<GenreModel> genreList) {
        this.genreList = genreList;
    }
}
