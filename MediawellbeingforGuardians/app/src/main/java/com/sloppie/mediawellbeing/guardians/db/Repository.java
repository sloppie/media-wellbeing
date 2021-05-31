package com.sloppie.mediawellbeing.guardians.db;

import android.app.Application;

public class Repository {
    private final AppDb db;
    private final ParentDao parentDao;
    private final ChildDao childDao;
    private final MediaFileDao mediaFileDao;

    public Repository(Application application) {
        db = AppDb.getInstance(application);
        parentDao = db.parentDao();
        childDao = db.childDao();
        mediaFileDao = db.mediaFileDao();
    }

    public ParentDao getParentDao() {
        return parentDao;
    }

    public ChildDao getChildDao() {
        return childDao;
    }

    public MediaFileDao getMediaFileDao() {
        return mediaFileDao;
    }
}
