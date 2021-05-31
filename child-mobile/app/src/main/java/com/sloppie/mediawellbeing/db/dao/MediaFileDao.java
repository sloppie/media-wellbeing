package com.sloppie.mediawellbeing.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sloppie.mediawellbeing.db.model.MediaFile;

import java.util.List;

@Dao
public interface MediaFileDao {
    @Insert
    void insert(MediaFile mediaFile);
    @Update
    void update(MediaFile mediaFile);
    @Delete
    void delete(MediaFile mediaFile);

    @Query("SELECT * FROM media_file_table")
    List<MediaFile> getMediaFiles();

    @Query("SELECT * FROM media_file_table WHERE id = :id")
    List<MediaFile> fetchMediaFile(String id);

}
