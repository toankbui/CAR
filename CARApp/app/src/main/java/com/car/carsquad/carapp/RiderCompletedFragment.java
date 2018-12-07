package com.car.carsquad.carapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RiderCompletedFragment extends Fragment {
    DatabaseReference mCompletedRidesRef;
    private String myID;
    private RecyclerView mCompletedList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rider_completed, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        mCompletedList = (RecyclerView) getView().findViewById(R.id.rider_post_completed_view);
        mCompletedList.setHasFixedSize(true);
        mCompletedList.setLayoutManager(new LinearLayoutManager(getActivity()));

        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCompletedRidesRef = FirebaseDatabase.getInstance().getReference().child("completed").child(myID);

        FirebaseRecyclerAdapter<Post, RiderCompletedFragment.PostViewHolder> firebaseRecyclerAdapter =
                //mDatabase is database of POSTS
                new FirebaseRecyclerAdapter<Post, RiderCompletedFragment.PostViewHolder>
                        (Post.class, R.layout.ride_completed_cardview, RiderCompletedFragment.PostViewHolder.class, mCompletedRidesRef) {
                    @Override
                    protected void populateViewHolder(final RiderCompletedFragment.PostViewHolder viewHolder, final Post model, int position) {
                        viewHolder.setDest(model.getEndPt().toUpperCase());
                        viewHolder.setDate(model.getDate());

                        final String postID = model.getPostID();

                        //retrieve DRIVERID:
                        FirebaseDatabase.getInstance().getReference().child("completed").child(myID).child(postID)
                                .child("userID").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final String driverID = dataSnapshot.getValue(String.class);
                                //set driver's name
                                FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(driverID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                                        String name = firstName + " " + lastName;
                                        viewHolder.setDriverName(name);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        //Go to next activity on click
                        viewHolder.mRateDriver.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //retrieve DRIVERID:
                                FirebaseDatabase.getInstance().getReference().child("completed").child(myID).child(postID)
                                        .child("userID").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String driverID = dataSnapshot.getValue(String.class);
                                        //set driver's name
                                        FirebaseDatabase.getInstance().getReference().child("users")
                                                .child(driverID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                final Intent intent = new Intent(getActivity(), RatingActivity.class);
                                                //send information to next activity
                                                intent.putExtra("driverID", driverID);
                                                intent.putExtra("postID",postID);
                                                getActivity().finish();
                                                startActivity(intent);
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        });
                    }
                };
        mCompletedList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Button mRateDriver;
        TextView nameTV;

        public PostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mRateDriver = (Button) itemView.findViewById(R.id.button_rateDriver);
        }

        public void setDest(String dest) {
            TextView post_dest = (TextView) mView.findViewById(R.id.destination);
            post_dest.setText("Your trip to " + dest);
        }

        public void setDate(String depDate) {
            TextView post_date = (TextView) mView.findViewById(R.id.date);
            post_date.setText("DATE: " + depDate);
        }

        public void setDriverName(String driverName) {
            nameTV = (TextView) mView.findViewById(R.id.comp_driver_name);
            nameTV.setText(driverName);
        }
    }
}
