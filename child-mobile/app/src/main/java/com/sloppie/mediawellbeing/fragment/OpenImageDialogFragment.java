package com.sloppie.mediawellbeing.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.sloppie.mediawellbeing.ImagePreview;
import com.sloppie.mediawellbeing.ImageSlideshowActivity;
import com.sloppie.mediawellbeing.R;
import com.sloppie.mediawellbeing.models.Photo;
import com.squareup.picasso.Picasso;

import java.io.File;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class OpenImageDialogFragment extends AppCompatDialogFragment {
    public static final String TAG = "com.sloppie.mediawellbeing.fragment.OpenImageDialogFragment";

    public interface SelectedImage {
        Photo getSelectedImage();
        void sendImageForReview(Photo newPhoto);
        String getUserId();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_confirm_view_action, null);

        ImageView imageView = view.findViewById(R.id.dialog_image_view);
        SelectedImage selectedImage = (SelectedImage) getActivity();
        Photo selectedPhoto = selectedImage.getSelectedImage();

        Picasso.get()
                .load(new File(selectedPhoto.getAbsolutePath()))
                .transform(new BlurTransformation(getActivity(), 20))
                .into(imageView);

        alertDialog.setView(view)
                .setTitle("Confirm Opening Image")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // does nothing if cancelled
                    }
                })
                .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("ImageSlideshow", "Open image");
                        // intent to open image confirmed
                        Intent openImageIntent = new Intent(getContext(), ImagePreview.class);
                        openImageIntent.putExtra("file_name", selectedPhoto.getFileName());
                        openImageIntent.putExtra("is_explicit", selectedPhoto.getIsExplicit());
                        openImageIntent.putExtra("file_location", selectedPhoto.getAbsolutePath());
                        openImageIntent.putExtra("review_status", selectedPhoto.getStatus());
                        openImageIntent.putExtra(ImageSlideshowActivity.USER_ID, selectedImage.getUserId());

                        // send image to server for review
//                        selectedImage.sendImageForReview(selectedPhoto);
                        startActivity(openImageIntent);
                    }
                });

        return alertDialog.create();
    }

}
