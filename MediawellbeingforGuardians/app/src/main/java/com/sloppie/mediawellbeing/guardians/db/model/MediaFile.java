package com.sloppie.mediawellbeing.guardians.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "media_table")
public class MediaFile {
    @NonNull
    @PrimaryKey
    private String id;
    private String name;
    @ColumnInfo(name = "is_explicit")
    private int isExplicit;
    private String status;

    public MediaFile(String id, String name, int isExplicit) {
        this.id = id;
        this.name = name;
        this.isExplicit = isExplicit;
        this.status = "NOT_REVIEWED";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIsExplicit(int isExplicit) {
        this.isExplicit = isExplicit;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIsExplicit() {
        return isExplicit;
    }

    public String getStatus() {
        return status;
    }
}
