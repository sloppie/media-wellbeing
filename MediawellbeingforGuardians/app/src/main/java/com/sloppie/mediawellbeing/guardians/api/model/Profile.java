package com.sloppie.mediawellbeing.guardians.api.model;

import com.google.gson.annotations.SerializedName;
import com.sloppie.mediawellbeing.guardians.db.Util;
import com.sloppie.mediawellbeing.guardians.db.model.Child;

public class Profile {
    @SerializedName("_id")
    private String id;
    private String guardian;

    public static Profile fromChild(Child newChild) {
        Profile newProfile = new Profile();
        newProfile.setId(newChild.getId());
        newProfile.setGuardian(Util.extractGuardianId(newChild.getId()));

        return newProfile;
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
