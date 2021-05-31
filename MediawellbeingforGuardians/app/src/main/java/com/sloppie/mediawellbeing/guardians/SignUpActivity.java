package com.sloppie.mediawellbeing.guardians;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.sloppie.mediawellbeing.guardians.api.ECD;
import com.sloppie.mediawellbeing.guardians.api.model.Guardian;
import com.sloppie.mediawellbeing.guardians.db.Util;
import com.sloppie.mediawellbeing.guardians.util.CredentialValidator;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUpActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private ECD ecd;

    private TextView username;
    private TextView email;
    private TextView password;
    private TextView confirmPassword;
    private Button signUpButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ecd = retrofit.create(ECD.class);

        username = findViewById(R.id.sign_up_username);
        email = findViewById(R.id.sign_up_email);
        password = findViewById(R.id.sign_up_password);
        confirmPassword = findViewById(R.id.confirm_sign_up_password);
        signUpButton = findViewById(R.id.sign_up_button);
    }

    private void signUpNewUser() {
        signUpButton.setEnabled(false);
        String inputUsername = username.getText().toString();
        String inputEmail = email.getText().toString();
        String inputPassword = password.getText().toString();
        String inputConfirmPassword = confirmPassword.getText().toString();

        if (
                CredentialValidator.validateName(inputUsername) &&
                        CredentialValidator.validateEmail(inputEmail) &&
                        CredentialValidator.validatePassword(inputPassword) &&
                        (inputPassword.compareTo(inputConfirmPassword) == 0)
        ) {
            Log.d("SignUpActivity", "Signung up user");
            Map<String, String> fieldMap = new HashMap<>();
            fieldMap.put("email", inputEmail);
            fieldMap.put("password", inputPassword);
            fieldMap.put("name", inputUsername);

            Call<Guardian> signUpCall = ecd.createGuardian(fieldMap);
            signUpCall.enqueue(new Callback<Guardian>() {
                @Override
                public void onResponse(Call<Guardian> call, Response<Guardian> response) {
                    if (response.isSuccessful()) {
                        Guardian guardian = response.body();
                        if (guardian != null) {
                            Util.setParentCredentials(getApplicationContext(), guardian);
                            onSuccessfulSignUp(guardian.getId());
                        } else {
                            signUpButton.setEnabled(true);
                            Snackbar infoSnackbar = Snackbar.make(
                                    findViewById(R.id.sign_up_container),
                                    "Sign up attempt failed",
                                    Snackbar.LENGTH_INDEFINITE
                            );

                            infoSnackbar.setAction("Retry", (v) -> {
                                infoSnackbar.dismiss();
                                signUpNewUser();
                            });

                            infoSnackbar.show();
                        }
                    }
                    signUpButton.setEnabled(true);
                }

                @Override
                public void onFailure(Call<Guardian> call, Throwable t) {
                    signUpButton.setEnabled(true);
                    Log.d("SignUpActivity.Retrofit", t.toString());
                    Snackbar infoSnackbar = Snackbar.make(
                            findViewById(R.id.sign_up_container),
                            "Sign up attempt failed",
                            Snackbar.LENGTH_INDEFINITE
                    );

                    infoSnackbar.setAction("Retry", (v) -> {
                        infoSnackbar.dismiss();
                        signUpNewUser();
                    });

                    infoSnackbar.show();
                }
            });
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSuccessfulSignUp(String userId) {
        Intent navigateToProfiles = new Intent(this, Profiles.class);
        navigateToProfiles.putExtra(SplashScreenActivity.USER_ID, userId);
        startActivity(navigateToProfiles);
        finish();
    }

    public void onSignUp(View view) {
        signUpNewUser();
    }

    public void navigateToLogin(View view) {
        Intent navigateToLoginIntent = new Intent(this, LoginActivity.class);
        startActivity(navigateToLoginIntent);
        finish();
    }
}
