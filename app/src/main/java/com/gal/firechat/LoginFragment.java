package com.gal.firechat;


import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {


    public LoginFragment() {
        // Required empty public constructor
    }

    TextView login, register, title;
    EditText email, password, passwordconfirm;
    Button send;
    FirebaseAuth authentication;
    FirebaseDatabase database;
    User self;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        setupReferences(v);
        authentication = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        getActivity().findViewById(R.id.navigation).setVisibility(View.GONE);
        return v;
    }

    private void setupReferences(View v){
        login = (TextView) v.findViewById(R.id.login);
        register = (TextView) v.findViewById(R.id.register);
        title = (TextView) v.findViewById(R.id.title);
        email = (EditText) v.findViewById(R.id.email);
        password = (EditText) v.findViewById(R.id.password);
        passwordconfirm = (EditText) v.findViewById(R.id.passwordconfirm);
        send = (Button) v.findViewById(R.id.send);

        login.setOnClickListener(changeListener);
        register.setOnClickListener(changeListener);
        send.setOnClickListener(loginListener);


    }

    View.OnClickListener changeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getTag().toString().equalsIgnoreCase(getString(R.string.logintext))){
                login.setVisibility(View.GONE);
                register.setVisibility(View.VISIBLE);
                passwordconfirm.setVisibility(View.GONE);
                title.setText(getString(R.string.login));
                send.setText(getString(R.string.login));
            }
            else{
                login.setVisibility(View.VISIBLE);
                register.setVisibility(View.GONE);
                passwordconfirm.setVisibility(View.VISIBLE);
                title.setText(getString(R.string.register));
                send.setText(getString(R.string.register));
            }
        }
    };

    View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String mail = email.getText().toString();
            String pass = password.getText().toString();
            String passconfirm = passwordconfirm.getText().toString();

            if(mail.isEmpty()){
                email.setError(getString(R.string.emailcantempty));
                return;
            }
            if(pass.isEmpty()){
                password.setError(getString(R.string.passcantempty));
                return;
            }
            if(register.getVisibility() == View.VISIBLE){
                authentication.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(getActivity(),
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = authentication.getCurrentUser();
                                self = new User(user.getEmail());
                                Toast.makeText(getActivity(),getString(R.string.loggedinas) + self.getEmail(),Toast.LENGTH_LONG).show();
                                getActivity().findViewById(R.id.navigation).setVisibility(View.VISIBLE);
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.content, new ChatFragment()).addToBackStack(null)
                                        .commit();
                            }
                            else {
                                Toast.makeText(getActivity(), R.string.loginfaile,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
            else {
                if(!pass.equals(passconfirm)){
                    passwordconfirm.setError(getString(R.string.passdontmatch));
                    return;
                }
                authentication.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(getActivity(),
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = authentication.getCurrentUser();
                                Toast.makeText(getActivity(),getString(R.string.createduser) + user.getEmail(),Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getActivity(), getString(R.string.usercreatefailed),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        }
    };


}

