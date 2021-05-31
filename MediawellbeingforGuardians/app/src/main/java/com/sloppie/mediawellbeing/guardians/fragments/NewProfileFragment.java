package com.sloppie.mediawellbeing.guardians.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.WriterException;
import com.sloppie.mediawellbeing.guardians.R;
import com.sloppie.mediawellbeing.guardians.adapters.ProfileAdapter;
import com.sloppie.mediawellbeing.guardians.db.AppDb;
import com.sloppie.mediawellbeing.guardians.db.Util;
import com.sloppie.mediawellbeing.guardians.db.model.Child;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class NewProfileFragment extends BottomSheetDialogFragment {
    public static final String TAG = "com.sloppie.mediawellbeing.parent.NewProfileFragment";

    private final ProfileAdapter.ProfileManager profileManager;
    private ImageView qrCodeImageView;
    private EditText profileName;
    private TextView qrStatusTextView;

    private final String userId;

    // TODO: Create an interface to create profiles from the Profiles activity to ensure that the
    // changes are instantly updated to the profiles RecyclerView
    public NewProfileFragment(ProfileAdapter.ProfileManager profileManager, String userId) {
        this.profileManager = profileManager;
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View newProfileView = inflater.inflate(R.layout.fragment_new_profile, container, false);
        Button createProfileButton =  newProfileView.findViewById(R.id.create_new_profile_button);
        qrCodeImageView = newProfileView.findViewById(R.id.qr_code_image);
        qrStatusTextView = newProfileView.findViewById(R.id.qr_code_generation_status);
        profileName = newProfileView.findViewById(R.id.profile_name_edit_text);

        createProfileButton.setOnClickListener((v) -> {
            // show the image of the generated QR
            createQRCode(profileName.getText().toString());
            createNewProfile(profileName.getText().toString());
        });

        return newProfileView;
    }

    private void createQRCode(String input) {
        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        qrStatusTextView.setText("Generating QR Code...");
        String childId = userId + ":" + input;
        QRGEncoder qrgEncoder = new QRGEncoder(childId, null, QRGContents.Type.TEXT, 300);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            qrCodeImageView.setVisibility(View.VISIBLE);
            qrCodeImageView.setImageBitmap(bitmap);
            qrStatusTextView.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Log.v("NewProfileFragment", e.toString());
            qrStatusTextView.setText("Unable to generate QR code");
        }
    }

    private void createNewProfile(String name) {
        // create the ID of the child
        Util.createNewProfile(profileManager, name);
        dismiss();
    }
}
