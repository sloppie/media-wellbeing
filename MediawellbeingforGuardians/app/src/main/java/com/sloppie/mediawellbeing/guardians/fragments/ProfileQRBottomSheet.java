package com.sloppie.mediawellbeing.guardians.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sloppie.mediawellbeing.guardians.R;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ProfileQRBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = "com.sloppie.mediawellbeing.guardian.ProfileQRBottomSheet";

    private ImageView qrCodeImageView;
    private final String input;

    public ProfileQRBottomSheet(String input) {
        this.input = input;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.qr_share_sheet, container);
        qrCodeImageView = view.findViewById(R.id.child_qr_code);
        generateQRCode();

        Button closeLinkSheetButton = view.findViewById(R.id.close_link_sheet_button);
        // add listener to close the link sheet as soon as user is done linking
        closeLinkSheetButton.setOnClickListener((v) -> dismiss());

        return view;
    }

    private void generateQRCode() {
        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(input, null, QRGContents.Type.TEXT, 300);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.v("ProfileQRBottomSheet", e.toString());
        }
    }
}
