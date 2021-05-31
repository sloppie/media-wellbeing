package com.sloppie.mediawellbeing.api;

import com.sloppie.mediawellbeing.models.Photo;

import java.util.ArrayList;

public interface ImageScanner {
    void scanImage(Photo newPhoto, int idx);
    void rescanImage(Photo newPhoto, int idx);
    ArrayList<Photo> getAllImages();

    void openImage(Photo photo);
}
