package com.sloppie.mediawellbeing.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.sloppie.mediawellbeing.db.dao.MediaFileDao;
import com.sloppie.mediawellbeing.db.dao.UserDao;
import com.sloppie.mediawellbeing.db.model.MediaFile;
import com.sloppie.mediawellbeing.db.model.User;

@Database(entities = {User.class, MediaFile.class}, version = 2)
public abstract class AppDb extends RoomDatabase {
    public static AppDb instance = null;

    public abstract UserDao userDao();
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
