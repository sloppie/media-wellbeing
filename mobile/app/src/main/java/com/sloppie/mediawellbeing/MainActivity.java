package com.sloppie.mediawellbeing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import com.sloppie.mediawellbeing.service.TestService;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        DisplayManager dm =
//                (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
//        Display[] displays = dm.getDisplays();
//
//        for (Display display: displays) {
//            Log.d(getPackageName() + ":DisplayName", display.getName());
//            Log.d(getPackageName() + ":DisplayName", String.valueOf(display.getState()));
//        }
        try {
//            AccessibilityNodeInfo rootInActiveWindow = new TestService().getRootInActiveWindow();
//            Log.d(getPackageName(), rootInActiveWindow.getViewIdResourceName());
//            SurfaceView surfaceView = new SurfaceView(getApplicationContext());
//            surfaceView.setAlpha((float) 0.2);
////            surfaceView.getSurfaceControl()
////            surfaceView.getHolder().lockCanvas();
//            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
//            transaction.setAlpha(surfaceView.getSurfaceControl(), (float) 0.2);
//            transaction.apply();
//            transaction.close();
        } catch (Exception e) {
            Log.d(getPackageName() + ":AccessibilityService", e.toString());
        }
    }

    public void button1Click(View view) {
        Log.d(getPackageName() + ":AccessibilityService", "Pressed!");
    }
}