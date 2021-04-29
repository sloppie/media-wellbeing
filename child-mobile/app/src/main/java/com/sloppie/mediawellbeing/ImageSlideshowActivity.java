package com.sloppie.mediawellbeing;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sloppie.mediawellbeing.adapter.ImageSlideshowAdapter;

public class ImageSlideshowActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slideshow);
        RecyclerView imageSlideShow = findViewById(R.id.image_slideshow);
        imageSlideShow.setAdapter(new ImageSlideshowAdapter(this));
        imageSlideShow.setLayoutManager(new GridLayoutManager(this, 2));
    }
}
