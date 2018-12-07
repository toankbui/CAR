package com.car.carsquad.carapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).hide();

        setContentView(R.layout.activity_main);

        //Firebase Authentication Objects
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        /*
        //if User is already logged in, skip this activity
        if(user != null && user.isEmailVerified()) {

            //finish();

            DatabaseReference databaseUser =
                    FirebaseDatabase.getInstance().getReference("users");
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            databaseUser.child(userId).child("currentMode").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String currentMode;
                    currentMode = dataSnapshot.getValue(String.class);
                    if (Objects.equals(currentMode, "driver")) {
                        //finish();
                        startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                    } else if (Objects.equals(currentMode, "rider")){
                        //finish();
                        startActivity(new Intent(MainActivity.this, RiderActivity.class));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });


            //start profile activity
            //finish();
            //startActivity(new Intent(getApplicationContext(), RiderActivity.class));

        }*/
    }
    //what activity to jump to
    public void nextActivity(View view) {
        int viewId = view.getId();
        Intent nextActivityIntent;
        switch(viewId) {
            case R.id.login_button:
                nextActivityIntent = new Intent(this, LoginActivity.class);
                startActivity(nextActivityIntent);
                break;
            case R.id.register_button:
                nextActivityIntent = new Intent(this, RegisterActivity.class);
                startActivity(nextActivityIntent);
                break;
        }
    }
    //prevent user from pressing the back button to go back from the main app screen
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


}
