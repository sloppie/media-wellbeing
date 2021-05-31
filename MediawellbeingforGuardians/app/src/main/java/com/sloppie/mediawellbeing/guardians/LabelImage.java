package com.sloppie.mediawellbeing.guardians;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sloppie.mediawellbeing.guardians.api.ECD;
import com.sloppie.mediawellbeing.guardians.api.model.UserActivity;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LabelImage extends AppCompatActivity {
    public static final String STRINGIFIED_USER_ACTIVITY = "STRINGIFIED_USER_AC";
    public static final String USER_ID = "USER_ID";

    private Retrofit retrofit;
    private ECD ecd;

    private ConstraintLayout constraintLayout;
    private ChipGroup labelChipGroup;
    private ImageView imageView;
    private Button submitLabelButton;

    private UserActivity userActivity;
    private String userId;
    private boolean isExplicit = false;
    private String imgUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_image);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ecd = retrofit.create(ECD.class);

        labelChipGroup = findViewById(R.id.image_label_chip_group);
        constraintLayout = findViewById(R.id.label_image_container);
        imageView = findViewById(R.id.viewed_image);
        submitLabelButton = findViewById(R.id.submit_label_button);

        Intent userLogActivityIntent = getIntent();

        if (userLogActivityIntent != null) {
            String stringifiedUserActivity =
                    userLogActivityIntent.getStringExtra(LabelImage.STRINGIFIED_USER_ACTIVITY);

            // restore the JSON object
            Type userActivityType = new TypeToken<UserActivity>() {}.getType();
            userActivity = new Gson().fromJson(stringifiedUserActivity, userActivityType);
            userId = userLogActivityIntent.getStringExtra(USER_ID);

            imgUrl = "http://192.168.43.196:5000/api/cdn/photo/" + userActivity.getFileName();

            Picasso.get()
                    .load(imgUrl)
                    .into(imageView);

            if (userActivity.getStatus() == "REVIEWED") {
                labelChipGroup.setEnabled(false);
                submitLabelButton.setEnabled(false);
            } else {

                labelChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    isExplicit = checkedId == R.id.is_explicit_chip;
                });

                submitLabelButton.setOnClickListener((v) -> labelImage());
            }

        }
    }

    private void labelImage() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", userId);

        int newLabel = isExplicit? 1: 0;
        userActivity.setLabel(newLabel);

        // disable button
        submitLabelButton.setEnabled(false);

        Call<UserActivity> reviewActivityCall = ecd.reviewActivity(headerMap, userActivity);

        reviewActivityCall.enqueue(new Callback<UserActivity>() {
            @Override
            public void onResponse(Call<UserActivity> call, Response<UserActivity> response) {
                if (response.isSuccessful()) {
                    UserActivity responseUserActivity = response.body();

                    if (responseUserActivity != null) {
                        Snackbar.make(
                                constraintLayout,
                                "Image labelled",
                                Snackbar.LENGTH_SHORT
                        ).show();
                    } else {
                        runOnUiThread(() -> submitLabelButton.setEnabled(true));
                        Snackbar.make(
                                constraintLayout,
                                "Image not labelled",
                                Snackbar.LENGTH_SHORT
                        ).show();
                    }
                } else {
                    runOnUiThread(() -> submitLabelButton.setEnabled(true));
                    Snackbar.make(
                            constraintLayout,
                            "Image not labelled",
                            Snackbar.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<UserActivity> call, Throwable t) {
                Log.d(".LabelImage", t.toString());
                runOnUiThread(() -> submitLabelButton.setEnabled(true));

                Snackbar.make(
                        constraintLayout,
                        "Unable to complete action. Please check your internet connection",
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        });
    }
}