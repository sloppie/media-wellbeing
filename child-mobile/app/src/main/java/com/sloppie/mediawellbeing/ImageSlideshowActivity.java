package com.sloppie.mediawellbeing;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.sloppie.mediawellbeing.adapter.ImageSlideshowAdapter;
import com.sloppie.mediawellbeing.api.ECD;
import com.sloppie.mediawellbeing.api.ImageScanner;
import com.sloppie.mediawellbeing.api.model.ModelResponse;
import com.sloppie.mediawellbeing.api.model.UserActivity;
import com.sloppie.mediawellbeing.db.AppDb;
import com.sloppie.mediawellbeing.db.dao.MediaFileDao;
import com.sloppie.mediawellbeing.db.model.MediaFile;
import com.sloppie.mediawellbeing.fragment.OpenImageDialogFragment;
import com.sloppie.mediawellbeing.models.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

public class ImageSlideshowActivity extends AppCompatActivity implements ImageScanner, OpenImageDialogFragment.SelectedImage {
    // constants
    private static final int STORAGE_PERMISSION_CODE = 16062;
    public static final String USER_ID = "USER_ID";

    // app database
    private AppDb appDb;

    private Retrofit retrofit;
    private ECD ecd;

    private ConstraintLayout imageSlideshowContainer;
    private RecyclerView recyclerView;
    private ArrayList<Photo> allImages;
    private Photo selectedImage;

    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slideshow);
        appDb = AppDb.getInstance(getApplicationContext());

        // create retrofit query-er
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ecd = retrofit.create(ECD.class);
//        Check if permission is granted before attempting to inflate RecyclerView
        if (
                ContextCompat.checkSelfPermission(
                        ImageSlideshowActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) {
            Log.d("ImageSlideshow", "Permission Granted");
        } else {
            requestPermission();
        }
        allImages = getAllShownImagesPath();

        imageSlideshowContainer = findViewById(R.id.image_slideshow_container);
        recyclerView = findViewById(R.id.image_slideshow);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new ImageSlideshowAdapter(this, allImages));

        // get the user Id
        Intent previousActivityIntent = getIntent();

        if (previousActivityIntent != null) {
            userId = previousActivityIntent.getStringExtra(USER_ID);
        }
    }

    @Override
    public void scanImage(Photo newPhoto, int idx) {

        new Thread(() -> {
            MediaFileDao mediaFileDao = appDb.mediaFileDao();
            List<MediaFile> mediaFiles =  mediaFileDao.fetchMediaFile(newPhoto.getId());

            if (mediaFiles.size() == 0) {
                fetchModelOutput(newPhoto, idx);
            } else {
                MediaFile mediaFile = mediaFiles.get(0);
                newPhoto.setIsExplicit(mediaFile.getIsExplicit());
                newPhoto.setScanState(Photo.SUCCESSFUL_SCAN);
                // update with the cached record
                runOnUiThread(() -> recyclerView.getAdapter().notifyItemChanged(idx));
                // check if the image is up for review
                if (mediaFile.getStatus().compareTo("PENDING_REVIEW") == 0) {
                    checkForReview(newPhoto, idx);
                }
            }
        }).start();
    }

    @Override
    public void rescanImage(Photo newPhoto, int idx) {
        // update the state of the image before continuing
        newPhoto.setScanState(Photo.PENDING_SCAN);
        recyclerView.getAdapter().notifyItemChanged(idx);

        // begin re-fetch
        fetchModelOutput(newPhoto, idx);
    }

    @Override
    public ArrayList<Photo> getAllImages() {
        return allImages;
    }

    @Override
    public void openImage(Photo photo) {
        selectedImage = photo;
        // means that the model thinks that the photo is explicit but the guardian incharge is yet
        // to review the photo
        if (photo.getIsExplicit() == 1 && photo.getStatus().compareTo("REVIEWED") != 0) {
            if (photo.getStatus().compareTo("NOT_REVIEWED") == 0) {
                OpenImageDialogFragment openImageDialogFragment = new OpenImageDialogFragment();
                openImageDialogFragment.show(getSupportFragmentManager(), OpenImageDialogFragment.TAG);
            } else {
                Intent openImageIntent = new Intent(this, ImagePreview.class);
                openImageIntent.putExtra("file_name", photo.getFileName());
                openImageIntent.putExtra("is_explicit", photo.getIsExplicit());
                openImageIntent.putExtra("file_location", photo.getAbsolutePath());
                openImageIntent.putExtra("review_status", photo.getStatus());
                openImageIntent.putExtra(ImageSlideshowActivity.USER_ID, userId);

                startActivity(openImageIntent);
            }
        } else if (photo.getIsExplicit() != 1) {
            // image may or may not be reviewed, but the model thinks that it is not explicit
            Intent openImageIntent = new Intent(this, ImagePreview.class);
            openImageIntent.putExtra("file_name", photo.getFileName());
            openImageIntent.putExtra("is_explicit", photo.getIsExplicit());
            openImageIntent.putExtra("file_location", photo.getAbsolutePath());
            openImageIntent.putExtra("review_status", photo.getStatus());
            openImageIntent.putExtra(ImageSlideshowActivity.USER_ID, userId);

            startActivity(openImageIntent);
        } else {
            // this must mean that the image has been reviewed and contains explicit content
            Snackbar viewingNotAllowedSnackbar = Snackbar.make(
                    imageSlideshowContainer,
                    R.string.viewing_not_allowed,
                    Snackbar.LENGTH_LONG
            );

            viewingNotAllowedSnackbar.setAction(
                    R.string.dismiss, v -> viewingNotAllowedSnackbar.dismiss());

            viewingNotAllowedSnackbar.show();
        }
    }

    private void fetchModelOutput(Photo newPhoto, int idx) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("file_id", newPhoto.getId());
        queryParams.put("user_id", MainApplication.getUserId());
        try {
            File mediaFile = new File(newPhoto.getAbsolutePath());
            String[] nameSplits = newPhoto.getFileName().split("\\.");
            String fileExt = nameSplits[nameSplits.length - 1];
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt)),
                    mediaFile);
            MultipartBody.Part sendableFile = MultipartBody.Part
                    .createFormData("file", newPhoto.getFileName(), requestBody);

            Call<ModelResponse> scanImageCall = ecd.scanImage(queryParams, sendableFile);

            scanImageCall.enqueue(new Callback<ModelResponse>() {
                @Override
                public void onResponse(Call<ModelResponse> call, Response<ModelResponse> response) {
                    if (!response.isSuccessful()) {
                        newPhoto.setScanState(Photo.FAILED_SCAN);
                        return;
                    }
                    ModelResponse modelResponse = response.body();
                    newPhoto.setIsExplicit(Integer.parseInt(modelResponse.getIsExplicit()));
                    newPhoto.setScanState(Photo.SUCCESSFUL_SCAN);

                    new Thread(() -> {
                        MediaFileDao mediaFileDao = appDb.mediaFileDao();
                        MediaFile newMediaFile = MediaFile.fromPhoto(newPhoto);
                        List<MediaFile> mediaFileList = mediaFileDao.fetchMediaFile(newPhoto.getId());
                        if (mediaFileList.size() == 0) {
                            appDb.mediaFileDao().insert(newMediaFile);
                        } else {
                            mediaFileDao.update(newMediaFile);
                        }
                    }).start();

                    runOnUiThread(() -> {
                        // notify item change
                        recyclerView.getAdapter().notifyItemChanged(idx);
                    });
                }

                @Override
                public void onFailure(Call<ModelResponse> call, Throwable t) {
                    newPhoto.setScanState(Photo.FAILED_SCAN);
                    runOnUiThread(() -> {
                        recyclerView.getAdapter().notifyItemChanged(idx);
                        Log.d(
                                "ImageSlideshowActivity",
                                newPhoto.getFileName() + "" + t.toString());
                    });
                }
            });
        } catch (Exception e) {
            Log.d("ImageSlideshow", "Unable to scan image: " + newPhoto.getFileName());
            Log.d("ImageSlideshow", e.toString());
        }
    }

    @Override
    public void sendImageForReview(Photo newPhoto) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", userId);

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
                Log.d("ImageSlideshow", t.getMessage());
                Toast.makeText(
                        getApplicationContext(),
                        "Image upload failed. Please check internet connectivity",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void checkForReview(Photo newPhoto, int idx) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", userId);
        // query map for file details
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("file_id", newPhoto.getId());
        Call<UserActivity> checkForActivityReviewCall = ecd.checkActivityReviewStatus(
                headerMap,
                queryMap
        );

        checkForActivityReviewCall.enqueue(new Callback<UserActivity>() {
            @Override
            public void onResponse(Call<UserActivity> call, Response<UserActivity> response) {
                if (response.isSuccessful()) {
                    UserActivity userActivity = response.body();
                    // prevent NullPointerExceptions
                    if (userActivity != null) {
                        // update status
                        MediaFile mediaFile = MediaFile.fromUserActivity(userActivity);
                        mediaFile.setStatus(userActivity.getStatus());
                        newPhoto.setStatus(userActivity.getStatus());
                        // to prevent being able to view an image that has not yet been labelled,
                        // only REVIEWED images will have their label changed.
                        if (userActivity.getStatus().compareTo("REVIEWED") == 0) {
                            newPhoto.setIsExplicit(userActivity.getLabel());
                        }
                        runOnUiThread(() -> {
                            recyclerView.getAdapter().notifyItemChanged(idx);
                        });

                        // update the MediaFile with the new status and the updated label
                        new Thread(() -> {
                            MediaFileDao mediaFileDao = appDb.mediaFileDao();
                            List<MediaFile> mediaFileList =
                                    mediaFileDao.fetchMediaFile(userActivity.getId());

                            if (mediaFileList.size() > 0) {
                                MediaFile foundMediaFile = mediaFileList.get(0);
                                foundMediaFile.setStatus(userActivity.getStatus());

                                // to prevent overriding control without approval of guardian, the
                                // explicit toggle is only changed when the file has already been
                                // REVIEWED
                                if (userActivity.getStatus().compareTo("REVIEWED") == 0) {
                                    foundMediaFile.setIsExplicit(userActivity.getLabel());
                                }

                                // save update to local database
                                mediaFileDao.update(foundMediaFile);
                            }
                        }).start();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserActivity> call, Throwable t) {
                // pass
            }
        });
    }

    @Override
    public String getUserId() {
        return userId;
    }

    private void requestPermission() {
        if (
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
        ) {
            new AlertDialog.Builder(ImageSlideshowActivity.this)
                    .setTitle(R.string.storage_permission_title)
                    .setMessage(R.string.storage_permission_rationale)
                    .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                        ActivityCompat.requestPermissions( // request permission
                                ImageSlideshowActivity.this,
                                new String[] {
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                },
                                ImageSlideshowActivity.STORAGE_PERMISSION_CODE
                        );
                    })
                    .setNegativeButton(R.string.revoke_permission, ((dialog, which) -> {
                        Toast.makeText(
                                ImageSlideshowActivity.this,
                                R.string.permission_not_granted,
                                Toast.LENGTH_SHORT
                        ).show();
                    }))
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions( // request permission
                    ImageSlideshowActivity.this,
                    new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    ImageSlideshowActivity.STORAGE_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ImageSlideshowActivity.STORAGE_PERMISSION_CODE) {
            if (permissions.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                            this,
                            R.string.fetching_stored_images,
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            this,
                            R.string.permission_not_granted,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

    // fetch all files
    private ArrayList<Photo> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<Photo> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        cursor = getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(new Photo(absolutePathOfImage));
        }
        // reverse list so that the newer images appear first
        Collections.reverse(listOfAllImages);

        return listOfAllImages;
    }

    @Override
    public Photo getSelectedImage() {
        return selectedImage;
    }
}
