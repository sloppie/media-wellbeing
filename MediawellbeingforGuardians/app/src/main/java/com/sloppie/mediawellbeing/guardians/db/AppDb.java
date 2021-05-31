package com.sloppie.mediawellbeing.guardians.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.sloppie.mediawellbeing.guardians.db.model.Child;
import com.sloppie.mediawellbeing.guardians.db.model.MediaFile;
import com.sloppie.mediawellbeing.guardians.db.model.Parent;

@Database(entities = {Parent.class, Child.class, MediaFile.class}, version = 1)
public abstract class AppDb extends RoomDatabase {
    private static AppDb instance;

    // Daos
    public abstract ChildDao childDao();
    public abstract ParentDao parentDao();
    public abstract MediaFileDao mediaFileDao();

    public static synchronized AppDb getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDb.class,
                    "media_wellbeing_parents_database"
            )
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }
}
