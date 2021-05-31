package com.sloppie.mediawellbeing.guardians;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.sloppie.mediawellbeing.guardians.adapters.ProfileAdapter;
import com.sloppie.mediawellbeing.guardians.api.ECD;
import com.sloppie.mediawellbeing.guardians.api.model.Profile;
import com.sloppie.mediawellbeing.guardians.db.AppDb;
import com.sloppie.mediawellbeing.guardians.db.Util;
import com.sloppie.mediawellbeing.guardians.db.model.Child;
import com.sloppie.mediawellbeing.guardians.db.model.Parent;
import com.sloppie.mediawellbeing.guardians.fragments.NewProfileFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Profiles extends AppCompatActivity implements ProfileAdapter.ProfileManager {

    private Retrofit retrofit;
    private ECD ecd;
    private List<Profile> profiles = new ArrayList<>();
    private Profile selectedProfile;

    private RecyclerView recyclerView;
    private ProfileAdapter profileAdapter;
    private NewProfileFragment npf;

    private String userId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ecd = retrofit.create(ECD.class);
        recyclerView = findViewById(R.id.profile_list);
        profileAdapter = new ProfileAdapter(this, profiles);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(profileAdapter);

        // get the user ID
        Intent previousActivityIntent = getIntent();

        if (previousActivityIntent != null) {
            userId = previousActivityIntent.getStringExtra(SplashScreenActivity.USER_ID);
        }

        // fetch the user profiles from the DB
        setUserProfiles();
    }

    public void getUserProfiles() {
        Map<String, String> headerMap = new HashMap<>();

        Call<List<Profile>> fetchUserProfiles = ecd.getProfiles(headerMap);

        fetchUserProfiles.enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (!response.isSuccessful()) {
                    return;
                } else {
                    List<Profile> fetchedProfiles = response.body();
                    if (profiles != null) {
                        profiles.addAll(fetchedProfiles);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {

            }
        });
    }

    public void setUserProfiles() {
        final Context appContext = getApplicationContext();

        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(() -> {
            List<Child> children = AppDb.getInstance(appContext).childDao().getAllChildren();

            if (children != null) {
                for (Child child: children) {
                    Profile profile = Profile.fromChild(child);
                    profile.setGuardian(Util.extractGuardianId(child.getId()));
                    profiles.add(profile);
                }

                runOnUiThread(() -> profileAdapter.notifyDataSetChanged());
            }
        });
    }

    @Override
    public void updateProfile() {
        runOnUiThread(() -> {
            profileAdapter.notifyDataSetChanged();
            npf.dismiss();
        });
    }

    @Override
    public void selectProfile(int position) {
        selectedProfile = profiles.get(position);
        Intent userLogActivityIntent = new Intent(getApplicationContext(), UserLogActivity.class);
        userLogActivityIntent.putExtra(UserLogActivity.USER_ID, selectedProfile.getId());
        startActivity(userLogActivityIntent);
    }

    @Override
    public void updateProfile(Profile newProfile) {
        profiles.add(newProfile);
        runOnUiThread(() -> profileAdapter.notifyItemInserted(profiles.size() - 1));
    }

    public void openNewProfileBottomSheet(View view) {
        npf = new NewProfileFragment(this, userId);
        npf.show(getSupportFragmentManager(), NewProfileFragment.TAG);
    }
}