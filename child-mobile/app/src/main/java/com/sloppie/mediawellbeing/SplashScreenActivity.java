package com.sloppie.mediawellbeing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.sloppie.mediawellbeing.db.AppDb;
import com.sloppie.mediawellbeing.db.dao.UserDao;
import com.sloppie.mediawellbeing.db.model.User;

import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        checkForUser();
    }

    private void checkForUser() {

        new Thread(() -> {
            AppDb appDb = AppDb.getInstance(getApplicationContext());
            UserDao userDao = appDb.userDao();
            List<User> users = userDao.getAllUsers();

            if (users.size() > 0) {
                User user = users.get(0);
                Intent navigationIntent = new Intent(getApplicationContext(), ImageSlideshowActivity.class);
                navigationIntent.putExtra(ImageSlideshowActivity.USER_ID, user.getId());

                MainApplication.setUserId(user.getId());

                runOnUiThread(() -> {
                    startActivity(navigationIntent);
                    finish();
                });
            } else {
                Intent navigationIntent = new Intent(
                        getApplicationContext(), LinkParentApplicationActivity.class);

                runOnUiThread(() -> {
                    startActivity(navigationIntent);
                    finish();
                });
            }
        }).start();
    }

}