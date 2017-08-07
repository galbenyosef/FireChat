package com.gal.firechat;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.TextView;

import java.text.Bidi;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessagesAdapter extends ArrayAdapter<Message> {

    private final User self;

    public MessagesAdapter(Context context, ArrayList<Message> messages,User self) {
        super(context, 0, messages);
        this.self=self;
    }

    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Message message = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.messages_view, parent, false);
        }
        // Lookup view for data population
        TextView sender = (TextView) convertView.findViewById(R.id.msg_sender);
        TextView content = (TextView) convertView.findViewById(R.id.msg_content);
        TextView date = (TextView) convertView.findViewById(R.id.msg_date);
        // Populate the data into the template view using the data object
        if (message!=null) {
            sender.setText(message.getSender());
            content.setText(message.getMessage());
            date.setText(message.getDateShort());
            Bidi bidi = new Bidi(getItem(position).getMessage(), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
            if(bidi.getBaseLevel() == 0){
                if (message.getSender().equals(self.getEmail())) {
                    content.setGravity(Gravity.START);
                    content.setTextColor(0xFF0000FF);
                    content.setTextSize(18);
                    content.setBackgroundColor(0xFFFFF68F);
                    date.setGravity(Gravity.END);
                    date.setBackgroundColor(0xFFFFF68F);
                    date.setTextSize(14);
                }
                else {
                    content.setGravity(Gravity.END);
                    content.setTextColor(0xFFFF0000);
                    content.setTextSize(18);
                    content.setBackgroundColor(0xFFF0E68C);//F0E68C
                    date.setGravity(Gravity.START);
                    date.setBackgroundColor(0xFFF0E68C);
                    date.setTextSize(14);
                }
            }
            else {
                if (message.getSender().equals(self.getEmail())) {
                    content.setGravity(Gravity.END);
                    content.setTextColor(0xFF0000FF);
                    content.setTextSize(18);
                    content.setBackgroundColor(0xFFFFF68F);
                    date.setGravity(Gravity.END);
                    date.setBackgroundColor(0xFFFFF68F);
                    date.setTextSize(14);

                }
                else {
                    content.setGravity(Gravity.START);
                    content.setTextColor(0xFFFF0000);
                    content.setTextSize(18);
                    content.setBackgroundColor(0x00FFFFFF);
                    content.setBackgroundColor(0x00F0E68C);
                    date.setGravity(Gravity.START);
                    date.setBackgroundColor(0x00F0E68C);
                    date.setTextSize(14);
                }
            }

        }
        // Return the completed view to render on screen
        return convertView;
    }
}