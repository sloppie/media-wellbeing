package com.sloppie.mediawellbeing.guardians;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.sloppie.mediawellbeing.guardians.db.AppDb;
import com.sloppie.mediawellbeing.guardians.db.ParentDao;
import com.sloppie.mediawellbeing.guardians.db.model.Parent;

import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {
    public static final String USER_ID = "USER_ID";

    private ParentDao parentDao = null;
    private String userId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        checkUserStatus();
    }

    private void checkUserStatus() {
        if (parentDao == null) {
            parentDao = AppDb.getInstance(getApplicationContext()).parentDao();
        }
        new Thread(() -> {
            List<Parent> parents = parentDao.getAllParents();

            runOnUiThread(() -> {
                if (parents.size() > 0) {
                    userId = parents.get(0).getId();
                    navigate(true);
                } else {
                    navigate(false);
                }
            });
        }).start();
    }

    private void navigate(boolean isLoggedIn) {
        Intent activityIntent = (isLoggedIn) ?
                new Intent(this, Profiles.class) :
                new Intent(this, SignUpActivity.class);

        if (userId != null) {
            activityIntent.putExtra(SplashScreenActivity.USER_ID, userId);
        }

        startActivity(activityIntent);
        finish();
    }
}