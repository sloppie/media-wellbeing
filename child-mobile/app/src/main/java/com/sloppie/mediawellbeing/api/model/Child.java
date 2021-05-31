package com.sloppie.mediawellbeing.api.model;

import com.google.gson.annotations.SerializedName;
import com.sloppie.mediawellbeing.db.model.User;

public class Child {
    @SerializedName("_id")
    private String id;

    @SerializedName("guardian")
    private String guardian;

    public static User toUser(Child childProfile) {

        return new User(childProfile.getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuardian() {
        return guardian;
    }

    public void setGuardian(String guardian) {
        this.guardian = guardian;
    }
}
