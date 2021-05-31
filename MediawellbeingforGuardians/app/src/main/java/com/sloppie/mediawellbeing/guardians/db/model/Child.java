package com.sloppie.mediawellbeing.guardians.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.sloppie.mediawellbeing.guardians.api.model.Profile;
import com.sloppie.mediawellbeing.guardians.db.Util;

@Entity (tableName = "child")
public class Child {
    @NonNull
    @PrimaryKey
    private String id;
    private String name;

    public Child(String id) {
        this.id = id;
        setName(Util.generateChildName(id));
    }

    public Child(Profile profile) {
        this.id = profile.getId();
        this.name = Util.generateChildName(profile.getId());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
