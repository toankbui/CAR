package com.car.carsquad.carapp;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiderPostDetails extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mDatabase;
    private DatabaseReference mReference;
    private DatabaseReference databaseUser;
    private DatabaseReference databaseCar;

    private Button mMessageDriver;
    private Button mRequestRide;
    String driverFirstName;
    String driverLastName;
    Double driverRating;
    private String driverID;
    private String myID;
    private String postID;
    String riderFirstName;
    String riderLastName;
    User requestingRider;
    Post requestedRide;
    private String startPt;
    private String endPt;
    private String activityOrigin = "";

    private TextView carBrand;
    private TextView licenseNo;
    private TextView seatsAvailable;

    //0 = not friend. 1 = request received
    int currentState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(actionBar).hide();
        setContentView(R.layout.activity_rider_post_details);

        carBrand = (TextView) findViewById(R.id.car_text_view);
        licenseNo = (TextView) findViewById(R.id.license_text_view);
        seatsAvailable = (TextView) findViewById(R.id.seats_available_text_view);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("post");
        mDatabase.keepSynced(true);

        mReference = FirebaseDatabase.getInstance().getReference();

        mMessageDriver = (Button) findViewById(R.id.message_driver_button);
        mMessageDriver.setOnClickListener(this);

        mRequestRide = (Button) findViewById(R.id.join_ride_button);
        mRequestRide.setOnClickListener(this);

        getIncomingIntent();
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //requestingRider = new User(myID, riderFirstName, riderLastName, "","",0.0);
        databaseUser = FirebaseDatabase.getInstance().getReference("users");

        databaseCar = FirebaseDatabase.getInstance().getReference("car");

        //TODO set CAR INFO
        databaseCar.child(driverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                carBrand.setText("Brand: " + dataSnapshot.child("model").getValue(String.class));
                licenseNo.setText("License No: " + dataSnapshot.child("licensePlate").getValue(String.class));

                //get number of available seats
                FirebaseDatabase.getInstance().getReference().child("seatsAvailable").child(postID).child("seatsAvail")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue(Integer.class)!= null){
                                    seatsAvailable.setText(dataSnapshot.getValue(Integer.class).toString() + " seats available");
                                } else {
                                    seatsAvailable.setText("Ride Canceled");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                //seatsAvailable.setText(dataSnapshot.child("numSeats").getValue(Integer.class).toString() + " seats available");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(driverID);
        final CircleImageView pic = findViewById(R.id.post_details_image_r);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Object url = dataSnapshot.child("profile_image").getValue();
                    if(url != null)
                    {
                        String image = url.toString();
                        if (image != null)
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(pic);
                        else
                            pic.setImageResource(R.drawable.profile);
                    }
                    else
                        pic.setImageResource(R.drawable.profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //REMOVE YOURSELF
        mReference.child("post").child(postID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mReference.child("accepted").child(myID).child(postID).removeValue();
                mReference.child("accepted_obj").child(myID).child(postID).removeValue();
                mReference.child("request").child(myID).child(postID).removeValue();
                mReference.child("request_obj").child(myID).child(postID).removeValue();

                mReference.child("accepted").child(postID).child(myID).removeValue();
                mReference.child("accepted_obj").child(postID).child(myID).removeValue();
                mReference.child("request").child(postID).child(myID).removeValue();
                mReference.child("request_obj").child(postID).child(myID).removeValue();
            }
        });


        //mReference.keepSynced(true);
        mReference.child("request").child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(myID)) {
                    String request = dataSnapshot.child(myID).child("request_type").getValue().toString();
                    if(request.equals("received")) {
                        mRequestRide.setEnabled(true);
                        mRequestRide.setText("Cancel Ride");
                        currentState = 1;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mReference.child("accepted").child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(myID)) {
                    String request = dataSnapshot.child(myID).child("accept_type").getValue().toString();
                    if(request.equals("accepted_rider")) {
                        mRequestRide.setEnabled(true);
                        mRequestRide.setText("Cancel Ride");
                        currentState = 1;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //TODO LISTENING TO DATABASE
        //rider is either rejected or accepted (request will be removed in both cases)
        mReference.child("request").child(myID).child(postID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mReference.child("accepted").child(postID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //IF NOT ACCEPTED, THEN SET BUTTON TO REQUEST AGAIN
                        if(!dataSnapshot.hasChild(myID)) {
                            mRequestRide.setEnabled(true);
                            mRequestRide.setText("REQUEST RIDE");
                            currentState = 0;
                        }
                        //IF ACCEPTED, THEN DO NOTHING
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        mReference.child("accepted").child(myID).child(postID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mReference.child("accepted").child(postID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(myID)) {
                            if(activityOrigin.equals("RiderRequestedFragment")) {
                                Toast.makeText(RiderPostDetails.this, "Your ride has been accepted!",
                                        Toast.LENGTH_LONG).show();

                            }
                            //finish();
                            //startActivity(new Intent(RiderPostDetails.this, MainCurrentRidesHolder.class));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            //RIDER should be kicked out of post details activity if ACCEPTED THEN REJECTED
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mReference.child("accepted").child(postID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //IF REMOVED, THEN KICK OUT
                        if(!dataSnapshot.hasChild(myID)) {
                            Toast.makeText(RiderPostDetails.this,"You have been removed from the ride",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            //startActivity(new Intent(RiderPostDetails.this, MainCurrentRidesHolder.class));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }
        });

        //TODO RIDER kicked out of post details activity if post is removed
        mReference.child("post").child(postID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                /*AlertDialog.Builder builder = new AlertDialog.Builder(RiderPostDetails.this);
                builder.setCancelable(true);
                builder.setTitle("RIDE DELETED");
                builder.setMessage("The driver has removed the ride");
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();*/
                Toast.makeText(RiderPostDetails.this, "The driver has removed the ride",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra("postID") && getIntent().hasExtra("startPt") && getIntent().hasExtra("endPt") &&
                getIntent().hasExtra("date") && getIntent().hasExtra("time") && getIntent().hasExtra("cost") &&
                getIntent().hasExtra("driverID")){

            postID = getIntent().getStringExtra("postID");
            startPt = getIntent().getStringExtra("startPt");
            endPt = getIntent().getStringExtra("endPt");
            String date = getIntent().getStringExtra("date");
            String time = getIntent().getStringExtra("time");
            String cost = "$" + getIntent().getStringExtra("cost");
            driverID = getIntent().getStringExtra("driverID");

            //call setDetails
            setDetails(postID, startPt, endPt, date, time, cost, driverID);
        }
        if(getIntent().hasExtra("originActivity")){
            activityOrigin = getIntent().getStringExtra("originActivity");
        }
    }

    private void setDetails(String postID,String startPt,String endPt,String date,String time, String cost,String driverID){
        TextView startTV = (TextView) findViewById(R.id.start_text_view);
        startTV.setText(startPt.toUpperCase());
        TextView endTV = (TextView) findViewById(R.id.end_text_view);
        endTV.setText(endPt.toUpperCase());
        TextView dateTV = (TextView) findViewById(R.id.date_text_view);
        dateTV.setText(date);
        TextView timeTV = (TextView) findViewById(R.id.time_text_view);
        timeTV.setText(time);
        TextView costTV = (TextView) findViewById(R.id.cost_text_view);
        costTV.setText(cost);

        final String driverId = driverID;

        DatabaseReference databaseUser = FirebaseDatabase.getInstance().getReference("users");
        databaseUser.child(driverID).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverFirstName = dataSnapshot.getValue(String.class);
                TextView driverNameTV = (TextView) findViewById(R.id.driver_name_text_view);
                String name = driverFirstName + " ";
                driverNameTV.setText(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        databaseUser.child(driverID).child("lastName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverLastName = dataSnapshot.getValue(String.class);
                TextView driverNameTV = (TextView) findViewById(R.id.driver_name_text_view);
                driverNameTV.append(driverLastName);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        databaseUser.child(driverID).child("driverRating").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverRating = dataSnapshot.getValue(Double.class);
                TextView ratingTV = (TextView) findViewById(R.id.rating_text_view);
                if(dataSnapshot.getValue(Double.class) != null) {
                    Double pts = ((double) Math.round(driverRating * 100)/100);
                    ratingTV.setText(String.valueOf(pts));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    private void requestRide() {
        mRequestRide.setEnabled(false);
        if (currentState == 0) {
            mReference.child("request").child(postID).child(myID).child("request_type")
                    .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    mReference.child("request").child(myID).child(postID).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRequestRide.setEnabled(true);
                            mRequestRide.setText("Cancel Ride");
                            currentState = 1;
                        }
                    });
                }
            });
            //RETRIEVE MY INFO
            FirebaseDatabase.getInstance().getReference().child("users").child(myID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    requestingRider = dataSnapshot.getValue(User.class);
                    mReference.child("request_obj").child(postID).child(myID).setValue(requestingRider);

                    //TODO ADD POST TO REQUEST_OBJ POSTS
                    //RETRIEVE POST INFO
                    FirebaseDatabase.getInstance().getReference().child("post").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            requestedRide = dataSnapshot.getValue(Post.class);
                            mReference.child("request_obj").child(myID).child(postID).setValue(requestedRide);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        if (currentState == 1) {
            //RIDE IS CANCELED. ADD SEATS BACK

            FirebaseDatabase.getInstance().getReference().child("seatsAvailable").child(postID).child("seatsAvail")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int seatsAvail = dataSnapshot.getValue(Integer.class);
                            seatsAvail = seatsAvail + 1;
                            FirebaseDatabase.getInstance().getReference().child("seatsAvailable")
                                    .child(postID).child("seatsAvail").setValue(seatsAvail);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

            /*databaseCar.child(driverID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int seatsAvail = dataSnapshot.child("numSeats").getValue(Integer.class);
                    seatsAvail = seatsAvail + 1;
                    databaseCar.child(driverID).child("numSeats").setValue(seatsAvail);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });*/

            mReference.child("request").child(postID).child(myID).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mReference.child("request").child(myID).child(postID).child("request_type")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mRequestRide.setEnabled(true);
                                    mRequestRide.setText("Request Ride");
                                    currentState = 0;
                                }
                            });
                        }
                    });
            mReference.child("request_obj").child(postID).child(myID).removeValue();
            mReference.child("request_obj").child(myID).child(postID).removeValue();

            FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).child(myID).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseDatabase.getInstance().getReference().child("accepted").child(myID).child(postID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //Toast.makeText(DriverPostDetails.this, "rejected successfully", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    });
            FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(myID).removeValue();
            FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(myID).child(postID).removeValue();

        }
    }

    private void messageDriver() {
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference();
        chatRef.child("chatroom").child(myID).child(startPt.toUpperCase()
                + " - " + endPt.toUpperCase() + " - " + driverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //ONLY INITIATE BRAND NEW ROOM IF CURRENT ROOM IS NULL
                if(dataSnapshot == null) {
                    chatRef.child("chatroom").child(myID).child(startPt.toUpperCase()
                            + " - " + endPt.toUpperCase() + " - " + driverID).setValue(driverID);
                    chatRef.child("chatroom").child(driverID).child(startPt.toUpperCase()
                            + " - " + endPt.toUpperCase() + " - " + myID).setValue(myID);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Intent intent = new Intent(RiderPostDetails.this, ChatRoomActivity.class);
        intent.putExtra("driverID", driverID);
        intent.putExtra("startPt", startPt);
        intent.putExtra("endPt", endPt);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if (view == mRequestRide) {
            if(!(myID.equals(driverID))) {
                //have not requested
                String title = "";
                String message = "Do you wish to proceed?";
                if(currentState == 0){
                    title = "Request Ride";
                } else if (currentState == 1) {
                    title = "Cancel Ride";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(RiderPostDetails.this);
                builder.setCancelable(true);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestRide();
                        finish();
                        startActivity(new Intent(RiderPostDetails.this, MainCurrentRidesHolder.class));
                    }
                });
                builder.show();
            }
            //can't request your own ride
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(RiderPostDetails.this);
                builder.setCancelable(true);
                builder.setTitle("REQUEST FAILED");
                builder.setMessage("You cannot request your own ride");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        Intent intent = new Intent(RiderPostDetails.this, RiderActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        }
        else if (view == mMessageDriver){
            if(!(myID.equals(driverID))) {
                messageDriver();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(RiderPostDetails.this);
                builder.setCancelable(true);
                builder.setTitle("MESSAGE FAILED");
                builder.setMessage("You cannot message yourself");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        Intent intent = new Intent(RiderPostDetails.this, RiderActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed()
    {
        if(activityOrigin.equals("RiderRequestedFragment") || activityOrigin.equals("RiderAcceptedFragment")){
            finish();
            startActivity(new Intent(RiderPostDetails.this, MainCurrentRidesHolder.class));
        }
        // code here to show dialog
        super.onBackPressed();  // optional depending on your needs
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}



