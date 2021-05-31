package com.sloppie.mediawellbeing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.sloppie.mediawellbeing.api.ECD;
import com.sloppie.mediawellbeing.api.model.UserActivity;
import com.sloppie.mediawellbeing.db.AppDb;
import com.sloppie.mediawellbeing.db.dao.MediaFileDao;
import com.sloppie.mediawellbeing.db.model.MediaFile;
import com.sloppie.mediawellbeing.models.Photo;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImagePreview extends AppCompatActivity {
    private AppDb appDb;

    private Retrofit retrofit;
    private ECD ecd;

    private CoordinatorLayout imagePreviewContainer;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        appDb = AppDb.getInstance(getApplicationContext());

        Intent openedImageIntent = getIntent();
        imagePreviewContainer = findViewById(R.id.image_preview_container);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ecd = retrofit.create(ECD.class);

        if (openedImageIntent != null) {
            ImageView imagePreview = findViewById(R.id.image_preview);
            String fileLocation = openedImageIntent.getStringExtra("file_location");
            String fileName = openedImageIntent.getStringExtra("file_name");
            int isExplicit = openedImageIntent.getIntExtra("is_explicit", 0);
            String status = openedImageIntent.getStringExtra("review_status");
            userId = openedImageIntent.getStringExtra(ImageSlideshowActivity.USER_ID);

            Picasso.get()
                    .load(new File(fileLocation))
                    .into(imagePreview);

            if (isExplicit == 1 && status.compareTo("PENDING_REVIEW") != 0) {
                Snackbar infoSnackbar = Snackbar.make(
                        imagePreviewContainer,
                        "Image may contain explicit content",
                        Snackbar.LENGTH_INDEFINITE);
                infoSnackbar.setAction("DISMISS", (v) -> {
                    infoSnackbar.dismiss();
                });
                infoSnackbar.show();
                sendImageForReview(new Photo(fileLocation));
            } else if (status.compareTo("PENDING_REVIEW") == 0) {
                Snackbar infoSnackbar = Snackbar.make(
                        imagePreviewContainer,
                        "Image review pending",
                        Snackbar.LENGTH_INDEFINITE);
                infoSnackbar.setAction("DISMISS", (v) -> {
                    infoSnackbar.dismiss();
                });
                infoSnackbar.show();
            }

        }
    }

    public void sendImageForReview(Photo newPhoto) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", MainApplication.getUserId());

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("file_id", newPhoto.getId());

        File mediaFile = new File(newPhoto.getAbsolutePath());
        String[] nameSplits = newPhoto.getFileName().split("\\.");
        String fileExt = nameSplits[nameSplits.length - 1];
        RequestBody requestBody = RequestBody.create(
                MediaType.parse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt)),
                mediaFile);
        MultipartBody.Part sendableFile = MultipartBody.Part
                .createFormData("file", newPhoto.getFileName(), requestBody);

        Call<UserActivity> sendImageForReviewCall = ecd.setImageForReview(
                headerMap, queryMap, sendableFile);

        sendImageForReviewCall.enqueue(new Callback<UserActivity>() {

            @Override
            public void onResponse(Call<UserActivity> call, Response<UserActivity> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Image uploaded and pending review",
                            Toast.LENGTH_SHORT
                    ).show();
                    new Thread(() -> {
                        MediaFileDao mediaFileDao = appDb.mediaFileDao();
                        UserActivity userActivity = response.body();
                        if (userActivity != null) {
                            List<MediaFile> mediaFileList =
                                    mediaFileDao.fetchMediaFile(userActivity.getId());
                            if (mediaFileList.size() > 0) {
                                // update the review of the media file to pending
                                MediaFile foundMediaFile = mediaFileList.get(0);
                                foundMediaFile.setStatus(userActivity.getStatus());
                                mediaFileDao.update(foundMediaFile);
                                runOnUiThread(() -> {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Image review status: " + foundMediaFile.getStatus(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "No entry found",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Image response empty ",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });

                        }
                    }).start();
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Image upload failed",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<UserActivity> call, Throwable t) {

                Log.d("ImageSlideshow", call.request().toString());
                Toast.makeText(
                        getApplicationContext(),
                        "Image upload failed. Please check internet connectivity",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

}