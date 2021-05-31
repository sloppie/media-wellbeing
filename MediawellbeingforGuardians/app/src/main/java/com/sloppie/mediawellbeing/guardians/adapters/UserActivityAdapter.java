package com.sloppie.mediawellbeing.guardians.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sloppie.mediawellbeing.guardians.R;
import com.sloppie.mediawellbeing.guardians.api.model.UserActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserActivityAdapter extends RecyclerView.Adapter<UserActivityAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView itemImage;
        private final TextView itemTitle;
        private final TextView itemDescription;
        private final LinearLayout reviewBadge;

        public ViewHolder(View view, UserActivityManager userActivityManager) {
            super(view);
            cardView = view.findViewById(R.id.user_activity_item);
            itemImage = view.findViewById(R.id.user_activity_image);
            itemTitle = view.findViewById(R.id.user_activity_tile);
            itemDescription = view.findViewById(R.id.user_activity_description);
            reviewBadge = view.findViewById(R.id.review_badge);

            cardView.setOnClickListener(
                    (v) -> userActivityManager.selectUserActivity(getAdapterPosition()));
        }

        public ImageView getItemImage() {
            return itemImage;
        }

        public TextView getItemTitle() {
            return itemTitle;
        }

        public TextView getItemDescription() {
            return itemDescription;
        }

        public LinearLayout getReviewBadge() {
            return reviewBadge;
        }
    }

    public interface UserActivityManager {
        void selectUserActivity(int position);
    }

    private final Activity activity;
    private final List<UserActivity> activities;

    public UserActivityAdapter(Activity activity, List<UserActivity> activities) {
        this.activity = activity;
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_activity_item, parent, false);
        return new ViewHolder(view, (UserActivityManager) activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get()
                .load("http://192.168.43.196:5000a/api/cdn/photo/" + this.activities.get(position).getFileName())
                .into(holder.getItemImage());
        holder.getItemTitle().setText(activities.get(position).getFileName());
        holder.getItemDescription().setText(activities.get(position).getDate());
        // if the image has already been reviewed, hide the badge
        if (activities.get(position).getStatus().compareTo("REVIEWED") == 0) {
            holder.getReviewBadge().setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }
}
