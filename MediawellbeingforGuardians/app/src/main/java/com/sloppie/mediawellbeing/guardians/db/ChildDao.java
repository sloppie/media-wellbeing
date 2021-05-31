package com.sloppie.mediawellbeing.guardians.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sloppie.mediawellbeing.guardians.db.model.Child;

import java.util.List;

@Dao
public interface ChildDao {

    @Insert
    void insert(Child child);

    @Update
    void update(Child child);

    @Delete
    void delete(Child child);

    @Query("SELECT * FROM child")
    List<Child> getAllChildren();

    @Query("SELECT * FROM child WHERE id = :id")
    List<Child> getChild(String id);
}
