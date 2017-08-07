package com.gal.firechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth authentication;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        try {
            authentication = FirebaseAuth.getInstance();
            user = authentication.getCurrentUser();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());return;}


        if(user == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, new LoginFragment()).addToBackStack(null)
                    .commit();
            Toast.makeText(this, R.string.hello, Toast.LENGTH_LONG).show();
        }
        else{
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, new ChatFragment()).addToBackStack(null)
                    .commit();
            Toast.makeText(this, getString(R.string.welcomeback) + user.getEmail(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        this.finish();
    }
}
