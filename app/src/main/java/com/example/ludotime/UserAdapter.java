package com.example.ludotime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<User> users;

    public UserAdapter(ArrayList<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_user, parent, false);
        return new UserViewHolder(userView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User currentUser = users.get(position);
        holder.nameTextView.setText(currentUser.getName());
        holder.scoreTextView.setText(currentUser.getScore());
        holder.iconImageView.setImageResource(
                holder.nameTextView.getResources().getIdentifier(currentUser.getIcon(),
                        "drawable",
                        holder.nameTextView.getContext().getPackageName()
                )
        );
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView scoreTextView;
        public ImageView iconImageView;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textview_list_user_name);
            scoreTextView = itemView.findViewById(R.id.textview_list_user_score);
            iconImageView = itemView.findViewById(R.id.imageview_list_user_icon);

        }
    }
}
