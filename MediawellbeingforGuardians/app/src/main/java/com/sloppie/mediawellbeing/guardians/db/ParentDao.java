package com.sloppie.mediawellbeing.guardians.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sloppie.mediawellbeing.guardians.db.model.Parent;

import java.util.List;

@Dao
public interface ParentDao {
    @Insert
    void insert(Parent parent);
    @Update
    void update(Parent parent);
    @Delete
    void delete(Parent parent);
    @Query("SELECT * FROM parent")
    List<Parent> getAllParents();
}
