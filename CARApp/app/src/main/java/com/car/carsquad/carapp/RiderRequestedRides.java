package com.car.carsquad.carapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RiderRequestedRides extends AppCompatActivity implements View.OnClickListener {

    private Button mBack;
    DatabaseReference mCurrentRidesRef;
    private String myID;
    private RecyclerView mPostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_current_rides);

        mBack = (Button) findViewById(R.id.back_btn);
        mBack.setOnClickListener(this);

        mPostList = (RecyclerView) findViewById(R.id.rider_post_view);
        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(new LinearLayoutManager(this));

        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mCurrentRidesRef = FirebaseDatabase.getInstance().getReference().child("request_obj").child(myID);
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Post,RiderActivity.PostViewHolder> firebaseRecyclerAdapter =
                //mDatabase is database of POSTS
                new FirebaseRecyclerAdapter<Post, RiderActivity.PostViewHolder>
                        (Post.class, R.layout.post_cardview_rider, RiderActivity.PostViewHolder.class, mCurrentRidesRef){
                    @Override
                    protected void populateViewHolder(RiderActivity.PostViewHolder viewHolder, final Post model, int position){
                        viewHolder.setStart(model.getStartPt().toUpperCase());
                        viewHolder.setDest(model.getEndPt().toUpperCase());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setCost(model.getCost());
                        //TODO
                        //viewHolder.setDetours("NULL");
                        viewHolder.setTime(model.getTime());

                        //Go to next activity on click
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(RiderRequestedRides.this, RiderPostDetails.class);
                                //send information to next activity
                                intent.putExtra("postID", model.getPostID());
                                intent.putExtra("startPt", model.getStartPt());
                                intent.putExtra("endPt", model.getEndPt());
                                intent.putExtra("date", model.getDate());
                                intent.putExtra("time", model.getTime());
                                intent.putExtra("cost", model.getCost());
                                intent.putExtra("driverID", model.getUserID());
                                //Toast.makeText(RiderActivity.this, "DriverID: " + model.getUserID(), Toast.LENGTH_LONG).show();
                                startActivity(intent);
                            }
                        });
                    }
                };
        mPostList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public PostViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Context context = v.getContext();
                    Intent intent = new Intent(context, RiderPostDetails.class);
                    context.startActivity(intent);
                }
            });
        }
        public void setStart(String start){
            TextView post_start = (TextView)mView.findViewById(R.id.post_start);
            post_start.setText(start);
        }
        public void setDest(String dest){
            TextView post_dest = (TextView)mView.findViewById(R.id.post_dest);
            post_dest.setText(dest);
        }
        public void setDate(String depDate){
            TextView post_date = (TextView)mView.findViewById(R.id.post_date);
            post_date.setText("DATE: " +depDate);
        }
        public void setCost(String cost){
            TextView post_dep_date = (TextView)mView.findViewById(R.id.post_cost);
            post_dep_date.setText("$" + cost);
        }
        public void setDetours(String detours){
            TextView post_detours = (TextView)mView.findViewById(R.id.post_detours);
            post_detours.setText(detours + " stops along the way");
        }
        public void setTime(String depTime){
            TextView post_dep_time = (TextView)mView.findViewById(R.id.post_time);
            post_dep_time.setText("TIME: " + depTime);
        }
    }

    @Override
    public void onClick(View view) {
        if(view == mBack){
            finish();
            startActivity(new Intent(RiderRequestedRides.this, RiderActivity.class));
        }
    }
}
