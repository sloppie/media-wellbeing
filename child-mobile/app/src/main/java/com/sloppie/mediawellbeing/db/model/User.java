package com.sloppie.mediawellbeing.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @NonNull
    @PrimaryKey
    private String id;

    public User(String id) {
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }
}
