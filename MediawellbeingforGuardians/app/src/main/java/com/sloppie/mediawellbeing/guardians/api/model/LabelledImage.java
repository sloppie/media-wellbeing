package com.sloppie.mediawellbeing.guardians.api.model;

import com.google.gson.annotations.SerializedName;

public class LabelledImage {
    @SerializedName("image_id")
    private String imageId;
    private int label;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }
}
