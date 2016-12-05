package dev.lfspersson.arctouchtmdb.database.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LFSPersson on 30/11/16.
 */

public class GenreModel {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
