package com.sloppie.mediawellbeing.guardians.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "parent")
public class Parent {
    @NonNull
    @PrimaryKey
    private String id;
    private String name;
    private String email;

    public Parent(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
