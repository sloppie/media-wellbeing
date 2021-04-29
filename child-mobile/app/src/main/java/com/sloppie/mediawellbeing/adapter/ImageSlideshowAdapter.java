package com.sloppie.mediawellbeing.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sloppie.mediawellbeing.R;

import java.util.ArrayList;

public class ImageSlideshowAdapter extends RecyclerView.Adapter<ImageSlideshowAdapter.ViewHolder> {
    private final ArrayList<String> imageLocations;
    private final Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final private View cardView;
        public ViewHolder(View view) {
            super(view);
            cardView = view;
        }

        public ImageView getImageView() {
            return cardView.findViewById(R.id.tile_image);
        }

        public TextView getImageName() {
            return cardView.findViewById(R.id.tile_name);
        }

        public TextView getImageCaption() {
            return cardView.findViewById(R.id.tile_caption);
        }
    }

    public ImageSlideshowAdapter(Activity activity) {
        this.imageLocations = getAllShownImagesPath((activity));
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_tile, parent, false);

        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getImageName().setText(imageLocations.get(position));
        Glide.with(activity.getApplicationContext())
                .load(imageLocations.get(position))
                .into(holder.getImageView());
    }

    @Override
    public int getItemCount() {
        return this.imageLocations.size();
    }

    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }
}
