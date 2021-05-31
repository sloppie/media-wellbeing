package com.sloppie.mediawellbeing.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.sloppie.mediawellbeing.R;
import com.sloppie.mediawellbeing.api.ImageScanner;
import com.sloppie.mediawellbeing.models.Photo;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ImageSlideshowAdapter extends RecyclerView.Adapter<ImageSlideshowAdapter.ViewHolder> {
    private final ArrayList<String> imageLocations;
    private final Activity activity;
    private final ArrayList<Photo> images;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final private View cardView;
        private final ImageScanner imageScanner;
        private final ImageView imageView;

        public ViewHolder(View view, ImageScanner imageScanner) {
            super(view);
            cardView = view;
            this.imageScanner = imageScanner;
            imageView = cardView.findViewById(R.id.tile_image);

            cardView.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                Photo currentPhoto = this.imageScanner.getAllImages().get(getAdapterPosition());
                if (currentPhoto.getScanState() == Photo.FAILED_SCAN) {
                    imageScanner.rescanImage(currentPhoto, currentPosition);
                } else if (currentPhoto.getScanState() == Photo.SUCCESSFUL_SCAN) {
                    imageScanner.openImage(imageScanner.getAllImages().get(currentPosition));
                }
            });
        }

        public ImageView getImageView() {
            return imageView;
        }

        public ImageView getScanStatusImage() {
            return cardView.findViewById(R.id.scan_state_icon);
        }

        public TextView getScanStatusText() {
            return cardView.findViewById(R.id.scan_state_text);
        }

        public LinearLayout getScanStatusOverlay() {
            return cardView.findViewById(R.id.scanning_overlay);
        }

        public ConstraintLayout getImageTile() {
            return cardView.findViewById(R.id.tile);
        }
    }

    public ImageSlideshowAdapter(Activity activity, ArrayList<Photo> images) {
        this.imageLocations = getAllShownImagesPath((activity));
        this.images = images;
        this.activity = activity;
        Log.d("ImageSlideshowAdapter", "" + this.imageLocations.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_tile, parent, false);

        return  new ViewHolder(view, (ImageScanner) activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int isExplicit = images.get(position).getIsExplicit();
        // insert a differnt margin based on whether the image is even or odd in the grid
        if (isExplicit == -1 || isExplicit == 1) {
            Picasso.get()
                    .load(new File(images.get(position).getAbsolutePath()))
                    .error(R.drawable.ic_launcher_background)
                    .resize(200, 200)
                    .transform(new BlurTransformation(activity, 10))
                    .into(holder.getImageView());
            if (isExplicit == -1) {
//                holder.getImageCaption().setText(R.string.scanning_image);
                ImageScanner imageScanner = (ImageScanner) activity;
                if (images.get(position).getScanState() == Photo.FAILED_SCAN) {
                    holder.getScanStatusImage().setImageResource(R.drawable.ic_error_scanning);
                    holder.getScanStatusText().setText(R.string.err_scanning_image);
                } else {
                    imageScanner.scanImage(images.get(position), position);
                    holder.getScanStatusImage().setImageResource(R.drawable.ic_scan_image);
                }
            } else {
                holder.getScanStatusText().setText(R.string.contains_explicit_content);
                holder.getScanStatusImage().setImageResource(R.drawable.ic_explicit_image);
            }
        } else {
            Picasso.get()
                    .load(new File(images.get(position).getAbsolutePath()))
                    .error(R.drawable.ic_launcher_background)
                    .resize(200, 200)
                    .into(holder.getImageView());
//            holder.getImageCaption().setText(R.string.contains_neutral_content);
//            holder.getScanStatusOverlay().setVisibility(View.INVISIBLE);
            holder.getScanStatusImage().setImageDrawable(null);
            holder.getScanStatusText().setText("");
        }
    }

    @Override
    public int getItemCount() {
        return this.imageLocations.size();
    }

    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

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
