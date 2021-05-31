package com.sloppie.mediawellbeing.guardians;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.sloppie.mediawellbeing.guardians.api.ECD;
import com.sloppie.mediawellbeing.guardians.api.model.Guardian;
import com.sloppie.mediawellbeing.guardians.db.AppDb;
import com.sloppie.mediawellbeing.guardians.db.model.Parent;
import com.sloppie.mediawellbeing.guardians.util.CredentialValidator;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private ECD ecd;

    private TextView email;
    private TextView password;
    private Button loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ecd = retrofit.create(ECD.class);

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
    }

    private void loginUser() {
        loginButton.setEnabled(false);
        String inputEmail = email.getText().toString();
        String inputPassword = password.getText().toString();

        if (
                        CredentialValidator.validateEmail(inputEmail) &&
                        CredentialValidator.validatePassword(inputPassword)
        ) {
            Map<String, String> fieldMap = new HashMap<>();
            fieldMap.put("email", inputEmail);
            fieldMap.put("password", inputPassword);

            Call<Guardian> signUpCall = ecd.loginUser(fieldMap);
            signUpCall.enqueue(new Callback<Guardian>() {
                @Override
                public void onResponse(Call<Guardian> call, Response<Guardian> response) {
                    loginButton.setEnabled(true);
                    if (response.isSuccessful()) {
                        Guardian guardian = response.body();
                        if (guardian != null) {
                            Parent parent = new Parent(
                                    guardian.getId(), guardian.getName(), guardian.getEmail());
                            onSuccessfulLogin(parent);
                        } else {
                            // null means that no user object was found
                            Snackbar infoSnackbar = Snackbar.make(
                                    findViewById(R.id.login_container),
                                    "Login attempt failed",
                                    Snackbar.LENGTH_INDEFINITE
                            );

                            infoSnackbar.setAction("Retry", (v) -> {
                                infoSnackbar.dismiss();
                                loginUser();
                            });

                            infoSnackbar.show();
                            loginButton.setEnabled(true);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Guardian> call, Throwable t) {
                    Snackbar infoSnackbar = Snackbar.make(
                            findViewById(R.id.login_container),
                            "Login attempt failed",
                            Snackbar.LENGTH_INDEFINITE
                    );

                    infoSnackbar.setAction("Retry", (v) -> {
                        infoSnackbar.dismiss();
                        loginUser();
                    });

                    infoSnackbar.show();
                    loginButton.setEnabled(true);
                }
            });
        }
    }

    private void onSuccessfulLogin(Parent parent) {
        new Thread(() -> {
            AppDb.getInstance(getApplicationContext()).parentDao().insert(parent);
            runOnUiThread(() -> {
                Intent navigateToProfilesIntent = new Intent(
                        getApplicationContext(), Profiles.class);
                // set the userId for the profiles
                navigateToProfilesIntent.putExtra(SplashScreenActivity.USER_ID, parent.getId());
                startActivity(navigateToProfilesIntent);
                finish();
            });
        }).start();
    }

    public void onLogin(View view) {
        loginUser();
    }

    public void navigateToSignUp(View view) {
        Intent navigateToSignUpIntent = new Intent(this, SignUpActivity.class);
        startActivity(navigateToSignUpIntent);
        finish();
    }
}
