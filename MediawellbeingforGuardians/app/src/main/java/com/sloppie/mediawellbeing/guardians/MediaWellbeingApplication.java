package com.sloppie.mediawellbeing.guardians;

import android.app.Application;

public class MediaWellbeingApplication extends Application {
    private static String ACTIVE_USER_PROFILE;

    public static String getActiveUserProfile() {
        return ACTIVE_USER_PROFILE;
    }

    public static void setActiveUserProfile(String activeUserProfile) {
        ACTIVE_USER_PROFILE = activeUserProfile;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
