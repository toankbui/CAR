package com.car.carsquad.carapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverPostDetails extends AppCompatActivity implements View.OnClickListener{

    private Button mDeletePost;
    private DatabaseReference mRiderRef, mFriendsRef;
    String postID;
    private String riderID;
    private String currentRiderID;
    private String myID;
    RecyclerView riderRequest, riderAccepted;
    String riderFirstName;
    String riderLastName;
    User requestingRider;
    Post acceptedRide;
    ToggleButton showRequested;
    ToggleButton showAccepted;
    TextView seatsAvailable;
    private DatabaseReference databaseCar;
    private String startPt;
    private String endPt;
    private String driverID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(actionBar).hide();
        setContentView(R.layout.activity_driver_post_details);

        seatsAvailable = (TextView) findViewById(R.id.seats_available_text_view);

        mDeletePost = (Button) findViewById(R.id.delete_ride_button);
        mDeletePost.setOnClickListener(this);

        showRequested = (ToggleButton) findViewById(R.id.button_show_requested);
        showRequested.setOnClickListener(this);
        showAccepted = (ToggleButton) findViewById(R.id.button_show_accepted);
        showAccepted.setOnClickListener(this);

        getIncomingIntent();

        mRiderRef = FirebaseDatabase.getInstance().getReference().child("user");
        mFriendsRef = FirebaseDatabase.getInstance().getReference().child("friends");
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseCar = FirebaseDatabase.getInstance().getReference("car");

        //TODO set CAR INFO
        FirebaseDatabase.getInstance().getReference().child("seatsAvailable").child(postID).child("seatsAvail")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue(Integer.class) != null) {
                            seatsAvailable.setText(dataSnapshot.getValue(Integer.class).toString() + " seats available");
                        } else {
                            seatsAvailable.setText("Ride Canceled");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //recycler view for user requests
        riderRequest = (RecyclerView) findViewById(R.id.request_list);
        riderRequest.setHasFixedSize(true);
        riderRequest.setLayoutManager(new LinearLayoutManager(this));

        riderAccepted = (RecyclerView) findViewById(R.id.accepted_list);
        riderAccepted.setHasFixedSize(true);
        riderAccepted.setLayoutManager(new LinearLayoutManager(this));

        riderRequest.setVisibility(View.GONE);
        riderAccepted.setVisibility(View.GONE);

        //showRequestList();

    }
    @Override
    protected void onStart(){
        super.onStart();

        DatabaseReference requestUserRef = FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID);
        FirebaseRecyclerAdapter<User,DriverPostDetails.RequestViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<User, DriverPostDetails.RequestViewHolder>
                        (User.class, R.layout.ride_request_cardview, DriverPostDetails.RequestViewHolder.class, requestUserRef){
                    @Override
                    protected void populateViewHolder(DriverPostDetails.RequestViewHolder viewHolder, final User model, int position){
                        String name = model.getFirstName() + " " + model.getLastName();
                        viewHolder.setRiderName(name);

                        Double rating = model.getRiderRating();
                        Double pts = ((double) Math.round(rating * 100)/100);
                        viewHolder.setRiderRating(pts);

                        /*final String*/ riderID = model.getUserID();
                        viewHolder.setImage(riderID,1);

                        //ACCEPT REQUEST
                        viewHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO UPDATE RIDER_ID
                                riderID = model.getUserID();

                                FirebaseDatabase.getInstance().getReference().child("users").child(riderID).child("fcmToken")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot1) {

                                                final String token = dataSnapshot1.getValue(String.class);

                                                FirebaseDatabase.getInstance().getReference().child("users").child(driverID)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                String sender = dataSnapshot.child("firstName").getValue(String.class) + " "
                                                                        + dataSnapshot.child("lastName").getValue(String.class);
                                                                String message_text = "Your request has been accepted";
                                                                Message message = new Message(sender, token, message_text);
                                                                Message.sendMessage(message, DriverPostDetails.this);
                                                            }
                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                                        });
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                        });

                                //ACCEPT RIDER
                                acceptRider();

                                //REMOVE RIDER FROM REQUESTS (since already accepted)
                                /*FirebaseDatabase.getInstance().getReference().child("request").child(postID).child(riderID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseDatabase.getInstance().getReference().child("request").child(riderID).child(postID).child("request_type")
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID).child(riderID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(riderID).child(postID).removeValue();*/
                            }
                        });
                        //REJECT REQUEST
                        viewHolder.btnReject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                riderID = model.getUserID();

                                FirebaseDatabase.getInstance().getReference().child("users").child(riderID).child("fcmToken")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot1) {

                                                final String token = dataSnapshot1.getValue(String.class);

                                                FirebaseDatabase.getInstance().getReference().child("users").child(driverID)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                String sender = dataSnapshot.child("firstName").getValue(String.class) + " "
                                                                        + dataSnapshot.child("lastName").getValue(String.class);

                                                                String message_text = "Your request has been rejected";
                                                                Message message = new Message(sender, token, message_text);
                                                                Message.sendMessage(message, DriverPostDetails.this);
                                                            }
                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                        });


                                //REMOVE USER'S REQUEST IF DRIVER REJECTED
                                //not myID, rather riderID
                                FirebaseDatabase.getInstance().getReference().child("request").child(postID).child(riderID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseDatabase.getInstance().getReference().child("request").child(riderID).child(postID).child("request_type")
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {}
                                                });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID).child(riderID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(riderID).child(postID).removeValue();
                            }
                        });
                    }
                };
        riderRequest.setAdapter(firebaseRecyclerAdapter);


        //ACCEPTED RIDERS
        DatabaseReference acceptedUserRef = FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID);
        FirebaseRecyclerAdapter<User,DriverPostDetails.RequestViewHolder> firebaseRecyclerAdapter2 =
                new FirebaseRecyclerAdapter<User, DriverPostDetails.RequestViewHolder>
                        (User.class, R.layout.ride_accepted_cardview, DriverPostDetails.RequestViewHolder.class, acceptedUserRef){
                    @Override
                    protected void populateViewHolder(DriverPostDetails.RequestViewHolder viewHolder, final User model, int position){
                        String name = model.getFirstName() + " " + model.getLastName();
                        viewHolder.setRiderName(name);


                        Double rating = model.getRiderRating();
                        Double pts = ((double) Math.round(rating * 100)/100);
                        viewHolder.setRiderRating(pts);

                        /*final String*/ riderID = model.getUserID();
                        viewHolder.setImage(riderID,0);
                        //MESSAGE ACCEPTED RIDER
                        viewHolder.btnMessage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                riderID = model.getUserID();

                                // TODO INITIATE CHAT ROOM
                                final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference();

                                chatRef.child("chatroom").child(riderID).child(startPt.toUpperCase()
                                        + " - " + endPt.toUpperCase() + " - " + driverID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //ONLY INITIATE BRAND NEW ROOM IF CURRENT ROOM IS NULL
                                        if(dataSnapshot == null) {
                                            chatRef.child("chatroom").child(riderID).child(startPt.toUpperCase()
                                                    + " - " + endPt.toUpperCase() + " - " + driverID).setValue(driverID);
                                            chatRef.child("chatroom").child(driverID).child(startPt.toUpperCase()
                                                    + " - " + endPt.toUpperCase() + " - " + riderID).setValue(riderID);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });

                                Intent intent = new Intent(DriverPostDetails.this, ChatRoomActivity.class);
                                intent.putExtra("driverID", riderID);
                                intent.putExtra("startPt", startPt);
                                intent.putExtra("endPt", endPt);
                                startActivity(intent);

                            }
                        });
                        //REMOVE ACCEPTED RIDER
                        viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                riderID = model.getUserID();


                                FirebaseDatabase.getInstance().getReference().child("users").child(riderID).child("fcmToken")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot1) {

                                                final String token = dataSnapshot1.getValue(String.class);

                                                FirebaseDatabase.getInstance().getReference().child("users").child(driverID)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                String sender = dataSnapshot.child("firstName").getValue(String.class) + " "
                                                                        + dataSnapshot.child("lastName").getValue(String.class);

                                                                String message_text = "You have been removed by the driver";
                                                                Message message = new Message(sender, token, message_text);
                                                                Message.sendMessage(message, DriverPostDetails.this);
                                                            }
                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                        });


                                //Toast.makeText(DriverPostDetails.this, "RiderID: "+riderID, Toast.LENGTH_LONG).show();

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
                                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                                });

                                //REMOVE USER'S REQUEST IF DRIVER REJECTED
                                //not myID, rather riderID
                                FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).child(riderID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseDatabase.getInstance().getReference().child("accepted").child(riderID).child(postID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                //Toast.makeText(DriverPostDetails.this, "rejected successfully", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(riderID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(riderID).child(postID).removeValue();
                            }
                        });

                        //FINISH RIDER RIDE
                        viewHolder.btnFinish.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //finish();
                                Intent intent = new Intent(DriverPostDetails.this, DriverFinishedRideActivity.class);
                                intent.putExtra("postID", postID);
                                intent.putExtra("riderID", model.getUserID());
                                startActivity(intent);
                            }
                        });
                    }
                };
        riderAccepted.setAdapter(firebaseRecyclerAdapter2);

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Button btnAccept;
        Button btnReject;
        Button btnMessage;
        Button btnRemove;
        Button btnFinish;
        String currentID;
        ValueEventListener vel;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            btnAccept = (Button) itemView.findViewById(R.id.button_accept_request);
            btnReject = (Button) itemView.findViewById(R.id.button_reject_request);

            btnMessage = (Button) itemView.findViewById(R.id.button_message);
            btnRemove = (Button) itemView.findViewById(R.id.button_remove);
            btnFinish = (Button) itemView.findViewById(R.id.button_finish);


            //currentID = currentRiderID;
        }

        public void setRiderName(String name) {
            TextView rider_name = (TextView) mView.findViewById(R.id.rider_name);
            rider_name.setText(name);
        }

        public void setRiderRating(Double rating) {
            TextView rider_rating = (TextView) mView.findViewById(R.id.rider_rating);
            rider_rating.setText(Double.toString(rating));
        }

        public void setImage(String riderID,int isRequested) {
            final CircleImageView pic;
            if(isRequested == 1)
                pic = mView.findViewById(R.id.request_image);
            else
                pic = mView.findViewById(R.id.accepted_image);
            final DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(riderID);
            riderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Object url = dataSnapshot.child("profile_image").getValue();
                        if (url != null) {
                            String image = url.toString();
                            if (image != null) {
                                if (image != null)
                                    Picasso.get().load(image).placeholder(R.drawable.profile).into(pic);
                                }
                                else
                                    pic.setImageResource(R.drawable.profile);
                        } else
                                pic.setImageResource(R.drawable.profile);
                    }
                }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

    private void acceptRider() {
        FirebaseDatabase.getInstance().getReference().child("seatsAvailable").child(postID).child("seatsAvail")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int seatsAvail = dataSnapshot.getValue(Integer.class);
                if(seatsAvail > 0) {
                    seatsAvail = seatsAvail - 1;
                    FirebaseDatabase.getInstance().getReference().child("seatsAvailable").child(postID).child("seatsAvail").setValue(seatsAvail);

                    FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).child(riderID).child("accept_type")
                            .setValue("accepted_rider").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            FirebaseDatabase.getInstance().getReference().child("accepted").child(riderID).child(postID).child("accept_type")
                                    .setValue("accepted_post").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) { }
                            });
                        }
                    });
                    //RETRIEVE RIDER INFO
                    FirebaseDatabase.getInstance().getReference().child("users").child(riderID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            requestingRider = dataSnapshot.getValue(User.class);
                            FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(riderID).setValue(requestingRider);

                            //RETRIEVE POST INFO
                            FirebaseDatabase.getInstance().getReference().child("post").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    acceptedRide = dataSnapshot.getValue(Post.class);
                                    FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(riderID).child(postID).setValue(acceptedRide);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });

                    //remove rider from request database
                    FirebaseDatabase.getInstance().getReference().child("request").child(postID).child(riderID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseDatabase.getInstance().getReference().child("request").child(riderID).child(postID).child("request_type")
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                                }
                            });
                    FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID).child(riderID).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("request_obj").child(riderID).child(postID).removeValue();

                }
                else if(seatsAvail == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DriverPostDetails.this);
                    builder.setCancelable(true);
                    builder.setTitle("CANNOT ADD RIDER");
                    builder.setMessage("You have no available seats!");
                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
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

            setDetails(postID, startPt, endPt, date, time, cost, driverID);
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
    }

    private void deletePost(){

        AlertDialog.Builder builder = new AlertDialog.Builder(DriverPostDetails.this);
        builder.setCancelable(true);
        builder.setTitle("DELETING POST");
        builder.setMessage("Do you want to proceed?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Delete Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //TODO REMOVE POST'S SEATSAVAIL
                FirebaseDatabase.getInstance().getReference().child("seatsAvailable")
                        .child(postID).removeValue();

                //REMOVE ALL RIDERS ASSOCIATED WITH POST
                final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
                //TODO STEP 1: POPULATE ARRAY STORING RIDER IDS
                mReference.child("accepted").child(postID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //TODO ARRAY POPULATE
                                final ArrayList<String> userIdArray = new ArrayList<>();

                                for(DataSnapshot idSnapshot : dataSnapshot.getChildren()){
                                    userIdArray.add(idSnapshot.getValue(String.class));
                                }

                                //Toast.makeText(DriverPostDetails.this, userIdArray.get(0), Toast.LENGTH_SHORT).show();
                                //TODO STEP 2: DELETION LOOP
                                for(String uid : userIdArray){
                                    mReference.child("request_obj").child(uid).child(postID).removeValue();
                                    mReference.child("request").child(uid).child(postID).removeValue();
                                    mReference.child("accepted_obj").child(uid).child(postID).removeValue();
                                    mReference.child("accepted").child(uid).child(postID).removeValue();

                                    //Toast.makeText(DriverPostDetails.this, uid, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                mReference.child("request").child(postID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //TODO ARRAY POPULATE
                                final ArrayList<String> userIdArray = new ArrayList<>();

                                for(DataSnapshot idSnapshot : dataSnapshot.getChildren()){
                                    userIdArray.add(idSnapshot.getValue(String.class));
                                }
                                //Toast.makeText(DriverPostDetails.this, "Array empty: " + userIdArray.isEmpty(),Toast.LENGTH_LONG).show();

                                //TODO STEP 2: DELETION LOOP
                                for(String uid : userIdArray){
                                    mReference.child("request_obj").child(uid).child(postID).removeValue();
                                    mReference.child("request").child(uid).child(postID).removeValue();
                                    mReference.child("accepted_obj").child(uid).child(postID).removeValue();
                                    mReference.child("accepted").child(uid).child(postID).removeValue();

                                    //Toast.makeText(DriverPostDetails.this, uid, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                //TODO STEP 3: REMOVE POSTS
                mReference.child("request").child(postID).removeValue();
                mReference.child("request_obj").child(postID).removeValue();
                mReference.child("accepted").child(postID).removeValue();
                mReference.child("accepted_obj").child(postID).removeValue();


                /*
                mReference.child("request").child(postID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mReference.child("request").child(riderID).child(postID).child("request_type")
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {}
                                });
                            }
                        });

                mReference.child("request_obj").child(riderID).child(postID).removeValue();


                FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseDatabase.getInstance().getReference().child("accepted").child(riderID).child(postID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {}
                                        });
                            }
                        });

                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(riderID).child(postID).removeValue();
*/

                //REMOVE POST FROM DATABASE
                DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference().child("post").child(postID);
                dbPost.setValue(null);

                //GO BACK TO DRIVER ACTIVITY
                Intent intent = new Intent(DriverPostDetails.this, DriverActivity.class);
                finish();
                startActivity(intent);
            }
        });
        builder.show();
    }
    @Override
    public void onClick(View view) {
        if(view == mDeletePost) {
            deletePost();
        }
        else if(view == showRequested) {
            toggleRequested();
        }
        else if(view == showAccepted) {
            toggleAccepted();
        }
    }

    private void toggleRequested(){
        if(riderRequest.getVisibility() != View.GONE) {
            riderRequest.setVisibility(View.GONE);
        } else if (riderRequest.getVisibility() == View.GONE){
            riderRequest.setVisibility(View.VISIBLE);
        }
    }
    private void toggleAccepted(){
        if(riderAccepted.getVisibility() != View.GONE) {
            riderAccepted.setVisibility(View.GONE);
        } else if (riderAccepted.getVisibility() == View.GONE) {
            riderAccepted.setVisibility(View.VISIBLE);
        }
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