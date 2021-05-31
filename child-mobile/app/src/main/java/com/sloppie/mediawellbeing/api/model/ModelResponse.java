package com.sloppie.mediawellbeing.api.model;

import com.google.gson.annotations.SerializedName;

public class ModelResponse {
    @SerializedName("is_explicit")
    private String isExplicit;
    @SerializedName("file_id")
    private String fileId;

    public String getIsExplicit() {
        return isExplicit;
    }

    public String getFileId() {
        return fileId;
    }
}
