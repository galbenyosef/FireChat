package com.gal.firechat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public  class UsersAdapter extends ArrayAdapter<User> {

    private final User self;

    public UsersAdapter(Context context, ArrayList<User> users, User self) {
        super(context, 0, users);
        this.self=self;
    }

    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.users_view, parent, false);
        }
        // Lookup view for data population
        TextView username = (TextView) convertView.findViewById(R.id.user_name);
        // Populate the data into the template view using the data object
        if (user != null) {
            username.setText(user.getNickname());
            if (user.getConnection().equals("OFFLINE")) {
                username.setTextColor(0xFFFF0000);
                username.setTextSize(18);
            }
            if (user.getConnection().equals("ONLINE")) {
                username.setTextColor(0xFF000000);
                username.setTextSize(22);
            }
            if (user.getEmail().equals(self.getEmail())) {
                username.setTextColor(0xFF009900);
                username.setTextSize(22);
            }
            if (user.getEmail().equals(User.getPublicRoom().getEmail()) ||
                    user.getEmail().equals(User.getLockedRoom().getEmail())) {
                username.setTextColor(0xFF0000FF);
                username.setTextSize(24);
            }
        }
        // Return the completed view to render on screen
        return convertView;
    }
}