package com.car.carsquad.carapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.widget.RatingBar;

public class RatingActivity extends AppCompatActivity implements View.OnClickListener {

    public RatingBar ratingBar;
    private Button skipBtn;
    private Button submitBtn;
    private String driverId;
    private String postId;
    private int ratingCount;
    private double rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        // initialize ratingBar
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        skipBtn = (Button) findViewById(R.id.button_skip);
        skipBtn.setOnClickListener(this);
        submitBtn = (Button) findViewById(R.id.button_submit);
        submitBtn.setOnClickListener(this);

        getIncomingIntent();
    }

    @Override
    public void onClick(View view) {
        if (view == skipBtn) {
            String myId = FirebaseAuth.getInstance().getUid();
            FirebaseDatabase.getInstance().getReference().child("completed").child(myId).child(postId).removeValue();
            finish();
            startActivity(new Intent(this, MainCurrentRidesHolder.class));
        }
        else if (view == submitBtn) {
            //RATE DRIVER
            updateDriverRating();

            String myId = FirebaseAuth.getInstance().getUid();
            FirebaseDatabase.getInstance().getReference().child("completed").child(myId).child(postId).removeValue();
            finish();
            startActivity(new Intent(this, MainCurrentRidesHolder.class));
        }
    }
    private void updateDriverRating(){
        final DatabaseReference riderDB = FirebaseDatabase.getInstance().getReference().child("users");

        riderDB.child(driverId).child("driverRating").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rating = dataSnapshot.getValue(Double.class);

                riderDB.child(driverId).child("driverRatingCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ratingCount = dataSnapshot.getValue(Integer.class);
                        double total = rating * ratingCount;
                        double newRating = (ratingBar.getRating() + total)/(ratingCount+1);
                        riderDB.child(driverId).child("driverRating").setValue(newRating);
                        riderDB.child(driverId).child("driverRatingCount").setValue(ratingCount+1);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra("driverID") && getIntent().hasExtra("postID")){
            driverId = getIntent().getStringExtra("driverID");
            postId = getIntent().getStringExtra("postID");
        }
    }
}
