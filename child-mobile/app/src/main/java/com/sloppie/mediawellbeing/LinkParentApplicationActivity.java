package com.sloppie.mediawellbeing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.sloppie.mediawellbeing.api.ECD;
import com.sloppie.mediawellbeing.api.model.Child;
import com.sloppie.mediawellbeing.db.AppDb;
import com.sloppie.mediawellbeing.db.dao.UserDao;
import com.sloppie.mediawellbeing.db.model.User;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.sloppie.mediawellbeing.ImageSlideshowActivity.USER_ID;

public class LinkParentApplicationActivity extends AppCompatActivity {
    public static final String TAG = "LinkParentApplication";
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1111;

    private ConstraintLayout constraintLayout;
    private CodeScanner codeScanner;
    private CodeScannerView codeScannerView;
    private LinearLayout linearLayout;

    private Retrofit retrofit;
    private ECD ecd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_parent_application);

        constraintLayout = findViewById(R.id.link_parent_application_container);
        codeScannerView = findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(this, codeScannerView);
        linearLayout = findViewById(R.id.creating_user_profile);

        codeScanner.setDecodeCallback(result -> {
            Log.d(LinkParentApplicationActivity.TAG, result.getText());
            linkParentAccount(result.getText());
        });

        codeScanner.setErrorCallback(error -> runOnUiThread(()  ->Toast.makeText(
                getApplicationContext(),
                R.string.error_initialising_camera,
                Toast.LENGTH_SHORT
        ).show()));

        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.196:5000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ecd = retrofit.create(ECD.class);

        if (
                ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Granted");
        } else {
            requestPermission();
        }
    }

    private void linkParentAccount(String text) {
        runOnUiThread(() -> linearLayout.setVisibility(View.VISIBLE));
        Map<String, String> headerMap = new HashMap<>();

        // create header
        headerMap.put("Authorization", text);

        Call<Child> linkChildProfileCall = ecd.linkChildProfile(headerMap);

        linkChildProfileCall.enqueue(new Callback<Child>() {
            @Override
            public void onResponse(Call<Child> call, Response<Child> response) {
                if (response.isSuccessful()) {
                    Child childProfile = response.body();
                    if (childProfile != null) {
                        User linkedUser = Child.toUser(childProfile);

                        new Thread(() -> {
                            AppDb appDb = AppDb.getInstance(getApplicationContext());
                            UserDao userDao = appDb.userDao();

                            userDao.insert(linkedUser);
                            // user
                            Intent imageSlideShowIntent = new Intent(
                                    getApplicationContext(), ImageSlideshowActivity.class);

                            imageSlideShowIntent.putExtra(USER_ID, linkedUser.getId());

                            MainApplication.setUserId(linkedUser.getId());

                            runOnUiThread(() -> {
                                startActivity(imageSlideShowIntent);
                                finish();
                            });
                        }).start();
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(
                            constraintLayout,
                            "Something went wrong. Please check the scanned QR Code",
                            Snackbar.LENGTH_SHORT
                    );

                    runOnUiThread(() -> snackbar.show());
                }
            }

            @Override
            public void onFailure(Call<Child> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(
                        constraintLayout,
                        "Something went wrong. Please check your internet connectivity",
                        Snackbar.LENGTH_SHORT
                );

                runOnUiThread(() -> snackbar.show());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            codeScanner.startPreview();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        codeScanner.releaseResources();
    }

    private void requestPermission() {
        if (
                ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                )
        ) {
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle(R.string.camera_permission_title)
                    .setMessage(R.string.camera_permission_rationale)
                    .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                        ActivityCompat.requestPermissions( // request permission
                                this,
                                new String[] {
                                        Manifest.permission.CAMERA,
                                },
                                CAMERA_PERMISSION_REQUEST_CODE
                        );
                    })
                    .setNegativeButton(R.string.revoke_permission, ((dialog, which) -> {
                        Toast.makeText(
                                getApplicationContext(),
                                R.string.permission_not_granted,
                                Toast.LENGTH_SHORT
                        ).show();
                    }))
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions( // request permission
                    this,
                    new String[] {
                            Manifest.permission.CAMERA,
                    },
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    codeScanner.startPreview();
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
}