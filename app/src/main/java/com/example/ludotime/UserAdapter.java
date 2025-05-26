package com.example.ludotime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Adapter for displaying users in a RecyclerView
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> users;

    /**
     * Constructor for UserAdapter
     * @param users List of users to display
     */
    public UserAdapter(ArrayList<User> users) {
        this.users = users;
    }

    /**
     * Creates a new ViewHolder for user items
     * @param parent The parent ViewGroup
     * @param viewType The view type of the new View
     * @return A new UserViewHolder instance
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_user, parent, false);
        return new UserViewHolder(userView);
    }

    /**
     * Binds user data to the ViewHolder
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item in the dataset
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = users.get(position);
        holder.nameTextView.setText(currentUser.getName());
        holder.scoreTextView.setText(String.valueOf(currentUser.getScore()));        holder.iconImageView.setImageResource(
                holder.nameTextView.getResources().getIdentifier(currentUser.getIcon(),
                        "drawable",
                        holder.nameTextView.getContext().getPackageName()
                )
        );
    }

    /**
     * Returns the total number of items in the dataset
     * @return The size of the users list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder for user items in the RecyclerView
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView scoreTextView;
        public ImageView iconImageView;

        /**
         * Constructor for UserViewHolder
         * @param itemView The view of the item
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textview_list_user_name);
            scoreTextView = itemView.findViewById(R.id.textview_list_user_score);
            iconImageView = itemView.findViewById(R.id.imageview_list_user_icon);

        }
    }
}