package com.sloppie.mediawellbeing.guardians.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sloppie.mediawellbeing.guardians.R;
import com.sloppie.mediawellbeing.guardians.api.model.Profile;
import com.sloppie.mediawellbeing.guardians.db.Util;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView profile;
        private final TextView profileTitle;
        private final TextView profileDescription;

        public ViewHolder(View view, ProfileManager profileManager) {
            super(view);
            profile = view.findViewById(R.id.profile_item);
            profileTitle = view.findViewById(R.id.profile_title);
            profileDescription = view.findViewById(R.id.profile_description);

            view.setOnClickListener((v) -> profileManager.selectProfile(getAdapterPosition()));
        }

        public CardView getProfile() {
            return profile;
        }

        public TextView getProfileTitle() {
            return profileTitle;
        }

        public TextView getProfileDescription() {
            return profileDescription;
        }
    }

    public interface ProfileManager {
        void updateProfile();
        void selectProfile(int position);
        Context getApplicationContext();
        void updateProfile(Profile newProfile);
    }

    private final Activity parentActivity;
    private final List<Profile> profiles;

    public ProfileAdapter(Activity parentActivity, List<Profile> profiles) {
        this.parentActivity = parentActivity;
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.profile_item, parent, false);

        return new ViewHolder(view, (ProfileManager) parentActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView projectTitle =  holder.getProfileTitle();
        TextView projectDescription = holder.getProfileDescription();

        projectTitle.setText(Util.generateChildName(profiles.get(position).getId()));
        projectDescription.setText(R.string.select_profile);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }
}
