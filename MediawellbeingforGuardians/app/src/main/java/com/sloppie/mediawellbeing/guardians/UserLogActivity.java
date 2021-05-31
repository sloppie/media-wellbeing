package com.sloppie.mediawellbeing.guardians;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sloppie.mediawellbeing.guardians.adapters.UserActivityAdapter;
import com.sloppie.mediawellbeing.guardians.api.ECD;
import com.sloppie.mediawellbeing.guardians.api.model.UserActivity;
import com.sloppie.mediawellbeing.guardians.db.Util;
import com.sloppie.mediawellbeing.guardians.fragments.ProfileQRBottomSheet;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserLogActivity extends AppCompatActivity implements UserActivityAdapter.UserActivityManager {
    public static final String TAG = ".UserLogActivity";
    public static final String USER_ID = "USER_ID";

    private Retrofit retrofit;
    private ECD ecd;
    public List<UserActivity> userActivities;
    private String userId;

    private ConstraintLayout userActivityLogContainer;
    private RecyclerView recyclerView;
    private UserActivityAdapter userActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_log);
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ecd = retrofit.create(ECD.class);
        userActivities = new ArrayList<>();

        Intent profileActivityIntent = getIntent();

        if (profileActivityIntent != null) {
            userId = profileActivityIntent.getStringExtra(UserLogActivity.USER_ID);
        } else {
            userId = MediaWellbeingApplication.getActiveUserProfile();
            Toast.makeText(
                    getApplicationContext(),
                    "Unable to find selected user profile",
                    Toast.LENGTH_SHORT
            ).show();
        }

        if (userId == null) {
            userId = MediaWellbeingApplication.getActiveUserProfile();
        }

        // TODO: Add recyclerView fragments
        userActivityLogContainer = findViewById(R.id.user_activity_log_container);
        recyclerView = findViewById(R.id.user_activity_list);
        userActivityAdapter = new UserActivityAdapter(this, userActivities);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userActivityAdapter);

        // fetch all the user actions carried out
        fetchUserActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.user_profile_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.link_action:
                openQRBottomSheet();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void selectUserActivity(int position) {
        Log.d(TAG, "Selecting log for file: " + userActivities.get(position).getFileName());
        Gson userActivity = new Gson();
        Type userActivityType =  new TypeToken<UserActivity>() {}.getType();
        String stringifiedUserActivity =
                userActivity.toJson(userActivities.get(position), userActivityType);

        Intent labelImageIntent = new Intent(this, LabelImage.class);
        labelImageIntent.putExtra(LabelImage.STRINGIFIED_USER_ACTIVITY, stringifiedUserActivity);
        labelImageIntent.putExtra(LabelImage.USER_ID, userId);

        // to help recover it once the LabelImage activity is destroyed.
        MediaWellbeingApplication.setActiveUserProfile(userId);

        startActivity(labelImageIntent);
    }

    private void fetchUserActivity() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", userId);
        Call<List<UserActivity>> userActivityCall = ecd.fetchUserActivity(headerMap);

        userActivityCall.enqueue(new Callback<List<UserActivity>>() {
            @Override
            public void onResponse(
                    Call<List<UserActivity>> call, Response<List<UserActivity>> response) {
                if (response.isSuccessful()) {
                    List<UserActivity> fetchedUserActivities = response.body();

                    if (fetchedUserActivities != null) {
                        userActivities.addAll(fetchedUserActivities);
                        runOnUiThread(() -> userActivityAdapter.notifyDataSetChanged());
                    } else {
                        runOnUiThread(() -> Snackbar.make(
                                userActivityLogContainer,
                                "no activity present for " + Util.generateChildName(userId),
                                Snackbar.LENGTH_SHORT
                        ).show());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<UserActivity>> call, Throwable t) {
                // notify users about the failure in fetch
                runOnUiThread(() -> Snackbar.make(
                        userActivityLogContainer,
                        "Unable to fetch user activity for " + Util.generateChildName(userId),
                        Snackbar.LENGTH_SHORT
                ).show());
            }
        });
    }

    private void openQRBottomSheet() {
        if (userId != null) {
            ProfileQRBottomSheet profileQRBottomSheet = new ProfileQRBottomSheet(userId);
            profileQRBottomSheet.show(getSupportFragmentManager(), ProfileQRBottomSheet.TAG);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "No user id available for qr generation",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}