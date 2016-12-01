package dev.lfspersson.arctouchtmdb.database.models;

import io.realm.RealmObject;

/**
 * Created by LFSPersson on 01/12/16.
 */

public class RealmInterger extends RealmObject {
    private Integer val;

    public Integer getValue() {
        return val;
    }

    public void setValue(Integer value) {
        this.val = value;
    }
}

