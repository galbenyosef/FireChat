package com.gal.firechat;

import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.gal.firechat.Message.MessageDateComparator;
import static com.gal.firechat.User.getLockedRoom;
import static com.gal.firechat.User.getPublicRoom;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    public ChatFragment() {
        // Required empty public constructor
    }

    FirebaseDatabase database;
    ValueEventListener
            listener_public,
            listener_private,
            listener_users,
            public_reader,
            private_reader,
            private_msg_marker;
    ListView usersList, messagesList;
    UsersAdapter usersAdapter;
    MessagesAdapter messagesAdapter;
    ArrayList<User> users = new ArrayList<>();
    ArrayList<Message>
            messages = new ArrayList<>(),
            m_public = new ArrayList<>(),
            m_private = new ArrayList<>();
    EditText input;
    User self,receiver;
    ImageView fire;

    void showExitDialog() {
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(R.string.logoutquestion);
        b.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                database.getReference("users").child(self.getEmail()).child("connection").setValue("OFFLINE");
                database.getReference("users").removeEventListener(listener_users);
                database.getReference("messages").child("public").removeEventListener(listener_public);
                database.getReference("messages").child("private").child(self.getEmail()).removeEventListener(listener_private);
                auth.signOut();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new LoginFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        b.setNegativeButton(R.string.no, null);
        b.show();
    }

    void showLockedDialog() {
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(R.string.logoutquestion);
        final EditText input = new EditText(this.getContext());
        b.setView(input);
        b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                //well now you know the password for the locked room
                if (input.getText().toString().equals("555")){

                }
                else
                    return;
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        b.show();
    }


    void showNickDialog() {
        final AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setTitle(R.string.enternick);
        final EditText input = new EditText(getActivity());
        b.setView(input);
        b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString().length() > 1
                        && !input.getText().toString().equals(User.getPublicRoom().getEmail()))
                    database.getReference("users")
                            .child(self.getEmail())
                            .child("nickname")
                            .setValue(input.getText().toString());
            }
        });
        b.setNegativeButton(R.string.cancel, null);
        b.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        input = (EditText) v.findViewById(R.id.input);
        fire = (ImageView)v.findViewById(R.id.send);
        self=null;
        //Default after initialization receiver is Lobby therefore:
        receiver=getPublicRoom();
        String title = getString(R.string.nowtalkingto) + receiver.getNickname();
        ((TextView)v.findViewById(R.id.talking_to)).setText(title);

        database = FirebaseDatabase.getInstance();
        self = new User(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //init users list
        usersAdapter = new UsersAdapter(getActivity(),users,self);
        usersList = (ListView)v.findViewById(R.id.userList);
        usersList.setAdapter(usersAdapter);
        //init messages list
        messages.add(new Message(new User(getString(R.string.admin)),new User(getString(R.string.system)),getString(R.string.welcome)));
        messagesAdapter = new MessagesAdapter(getActivity(),messages,self);
        messagesList = (ListView)v.findViewById(R.id.messagesList);
        messagesList.setAdapter(messagesAdapter);

        //after initialization m_public is filled with all public messages
        public_reader = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Message>> data_base =
                        new GenericTypeIndicator<Map<String, Message>>(){};
                Map<String, Message> data_messages = dataSnapshot.getValue(data_base);
                if (data_messages == null) return;
                m_public.clear();
                if(data_messages.size() > 0) {
                    for (Message entry : data_messages.values()) {
                        m_public.add(entry);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //same for private
        private_reader = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Message>> data_base = new GenericTypeIndicator<Map<String, Message>>(){};
                Map<String, Message> data_messages = dataSnapshot.getValue(data_base);
                if (data_messages == null) return;
                m_private.clear();
                if(data_messages.size() > 0) {
                    for (String message : data_messages.keySet()) {
                        m_private.add(data_messages.get(message));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //function as database setter, marks as read all messages for specicif private user
        private_msg_marker = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Message>> data_base = new GenericTypeIndicator<Map<String, Message>>(){};
                Map<String, Message> data_messages = dataSnapshot.getValue(data_base);
                if (data_messages == null) return;
                if(data_messages.size() > 0) {
                    for (String key : data_messages.keySet()) {
                        Message m = data_messages.get(key);
                        if(m.getSender().equals(receiver.getEmail())|| m.getReceiver().equals (receiver.getEmail()))
                            database.getReference("messages").child("private").child(self.getEmail()).child(key).child("status").setValue("read");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //listens of updates for users list
        listener_users = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, User>> data_base = new GenericTypeIndicator<Map<String, User>>(){};
                Map<String, User> data_users_online = dataSnapshot.getValue(data_base);
                if (data_users_online == null) return;
                users.clear();
                if(data_users_online.size() > 0) {
                    for (User entry : data_users_online.values()) {
                        users.add(entry);
                    }
                    Collections.sort(users, User.UserComparator);
                    users.add(0, getPublicRoom());
                 //   users.add(1, getLockedRoom());
                    usersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //listens of updates for public messages
        listener_public = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Message>> data_base = new GenericTypeIndicator<Map<String, Message>>(){};
                Map<String, Message> data_messages = dataSnapshot.getValue(data_base);
                if (data_messages == null) return;
                m_public.clear();
                if(data_messages.size() > 0)  {
                    for (String message : data_messages.keySet()) {
                        Message m = data_messages.get(message);
                        m_public.add(m);
                    }
                }
                //if there's an update for public and public is also receiver(selected), refresh new updated list
                if (receiver.getEmail().equals(getPublicRoom().getEmail())){
                    messages.clear();
                    messages.addAll(m_public);
                    Collections.sort(messages,MessageDateComparator);
                    messagesAdapter.notifyDataSetChanged();
                }
                //else we have to notify user there's a new public message, introducing unread button
                else{
                    usersList.getChildAt(0).findViewById(R.id.unread_button).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        //listens of updates for private messages
        listener_private = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, Message>> data_base = new GenericTypeIndicator<Map<String, Message>>() {};
                Map<String, Message> data_messages = dataSnapshot.getValue(data_base);
                if (data_messages==null) return;
                m_private.clear();
                if (data_messages.size() > 0) {
                    for (String message : data_messages.keySet()) {
                        Message m = data_messages.get(message);
                        m_private.add(m);
                    }
                }
                ArrayList<Message> filtered = new ArrayList<>();
                for (Message m : m_private){
                    //if there's an update for private user and private user is also receiver(selected), filter relevant messages
                    if (m.getSender().equals(receiver.getEmail()) || m.getReceiver().equals(receiver.getEmail())) {
                        filtered.add(m);
                        //call private message marker
                        dataSnapshot.getRef().addListenerForSingleValueEvent(private_msg_marker);
                    }
                    else{
                        int i=0;
                        //for all users
                        for (;i<usersAdapter.getCount();i++) {
                            String user = ((User) usersList.getItemAtPosition(i)).getEmail();
                            //user received private message but is not selected so
                            if (user.equals(m.getSender()) &&
                                    !m.getSender().equals(self.getEmail()) && m.getStatus().equals("unread")) {
                                //show unread button on private user
                                usersList.getChildAt(i).findViewById(R.id.unread_button).setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                }
                //show filtered content
                if (!receiver.getEmail() .equals( getPublicRoom().getEmail() ) &&
                        !receiver.getEmail() .equals(User.getPublicRoom().getEmail())){
                    messages.clear();
                    messages.addAll(filtered);
                    Collections.sort(messages,MessageDateComparator);
                    messagesAdapter.notifyDataSetChanged();
                    //messagesList.setSelection(messagesAdapter.getCount() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                receiver = usersAdapter.getItem(position);

                if (receiver == null) return;
                //do nothing if self clicked
                String text = getString(R.string.nowtalkingto) + receiver.getNickname();
                ((TextView)getActivity().findViewById(R.id.talking_to)).setText(text);
                if (receiver.getEmail().equals(self.getEmail())) return;

                //turn off unread button (read)
                view.findViewById(R.id.unread_button).setVisibility(View.GONE);
                //introduce typing and send interface
                getActivity().findViewById(R.id.bottom_layout).setVisibility(View.VISIBLE);
                //set messages to public
                if (receiver.getEmail().equals(getPublicRoom().getEmail())){
                    messages.clear();
                    messages.addAll(m_public);
                }
                else {
                    //filter messages to private user
                    ArrayList<Message> filtered = new ArrayList<>();
                    for (Message m : m_private){
                        if (m.getSender().equals(self.getEmail())
                                && m.getReceiver().equals(receiver.getEmail())
                                || m.getSender().equals(receiver.getEmail())
                                && m.getReceiver().equals(self.getEmail())) {

                                    filtered.add(m);

                        }
                        //mark all private user messages as read
                        database.getReference("messages").child("private")
                                .child(self.getEmail()).getRef()
                                .addListenerForSingleValueEvent(private_msg_marker);
                    }
                    //set messages to private user
                    messages.clear();
                    messages.addAll(filtered);
                }
                Collections.sort(messages,MessageDateComparator);
                messagesAdapter.notifyDataSetChanged();
            }
        };

        usersList.setOnItemClickListener(onItemClickListener);

        v.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receiver.getEmail().equals(self.getEmail()))
                    return;
                if (input.getText().toString().length() < 2)
                    return;
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                Message msg = new Message(self,receiver,input.getText().toString());
                if (receiver.getEmail().equals(getPublicRoom().getEmail())) {
                    DatabaseReference r;
                    r = database.getReference("messages").child("public").push();
                    r.setValue(msg);
                }
                else if (receiver.getEmail().equals(getLockedRoom().getEmail())) {
                    DatabaseReference r;
                    r = database.getReference("messages").child("locked").push();
                    r.setValue(msg);
                }
                else {
                    DatabaseReference r;
                    r = database.getReference("messages").child("private").child(self.getEmail()).push();
                    r.setValue(msg);
                    r = database.getReference("messages").child("private").child(receiver.getEmail()).push();
                    r.setValue(msg);
                }
                ((EditText)getActivity().findViewById(R.id.input)).setText("");
            }
        });

        getActivity().findViewById(R.id.nav_changenick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showNickDialog();
            }
        });

        getActivity().findViewById(R.id.nav_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showExitDialog();
            }
        });

        getActivity().findViewById(R.id.navigation).setVisibility(View.VISIBLE);

        database.getReference("users").child(self.getEmail()).child("connection").setValue("ONLINE");
        database.getReference("users").child(self.getEmail()).child("email").setValue(self.getEmail());

        database.getReference("messages").child("public").addListenerForSingleValueEvent(public_reader);
        database.getReference("messages").child("private").child(self.getEmail()).addListenerForSingleValueEvent(private_reader);

        database.getReference("users").addValueEventListener(listener_users);

        database.getReference("messages").child("public").addValueEventListener(listener_public);
        database.getReference("messages").child("private").child(self.getEmail()).addValueEventListener(listener_private);



        new Handler().post(new Runnable() {
            @Override
            public void run() {
                AnimationDrawable fireButton = (AnimationDrawable)fire.getBackground();
                fireButton.start();
            }
        });
        return v;

    }
}