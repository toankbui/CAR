package com.car.carsquad.carapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;


public class RiderActivity extends AppCompatActivity implements View.OnClickListener {

    //Firebase object
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;

    //UI references
    private TextView textviewUserEmail;
    private CircleImageView navProfile;
    private TextView profileName;
    private DatabaseReference userRef;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private RecyclerView mPostList;
    private TextView mRating;

    LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("post");
        mDatabase.keepSynced(true);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mPostList = (RecyclerView) findViewById(R.id.rider_post_view);
        mPostList.setHasFixedSize(true);
        //mPostList.setLayoutManager(new LinearLayoutManager(this));
        mPostList.setLayoutManager(mLayoutManager);

        //initialize fireBase
        firebaseAuth = firebaseAuth.getInstance();
        //if user is not logged in yet
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        //for the sidebar
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);



        NavigationView navigationView = findViewById(R.id.nav_view);
        View navViewWithHeader = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfile = navViewWithHeader.findViewById(R.id.nav_profile_image);
        profileName =  navViewWithHeader.findViewById(R.id.nav_name);
        mRating = navViewWithHeader.findViewById(R.id.nav_ratings);

        userRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(firebaseAuth.getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String firstName = dataSnapshot.child("firstName").getValue().toString();
                    String lastName = dataSnapshot.child("lastName").getValue().toString();
                    String name = firstName + " " + lastName;
                    profileName.setText(name);
                    Object url = dataSnapshot.child("profile_image").getValue();
                    if(url != null)
                    {
                        String image = url.toString();
                        if (image != null)
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfile);
                        else
                            navProfile.setImageResource(R.drawable.profile);
                    }
                    else
                        navProfile.setImageResource(R.drawable.profile);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myProfileActivity = new Intent(getApplicationContext(),UpdateUserInfoActivity.class);
                startActivity(myProfileActivity);
            }
        });
        userRef.child("riderRating").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Double.class) != null) {
                    Double pts = ((double) Math.round(dataSnapshot.getValue(Double.class) * 100)/100);
                    mRating.setText(String.valueOf(pts));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // close drawer when item is tapped
                        menuItem.setChecked(false);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            //logout from menu bar
                            case R.id.nav_logout:
                                logout();
                                break;
                            case R.id.nav_switch_to_driver:
                                AlertDialog.Builder builder = new AlertDialog.Builder(RiderActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("You are about to enter DRIVER mode");
                                builder.setMessage("Do you wish to proceed?");
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //menuItem.setChecked(false);
                                        dialogInterface.cancel();
                                    }
                                });
                                builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DatabaseReference databaseUser =
                                             FirebaseDatabase.getInstance().getReference("users");
                                        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        //skip if user already signed up as DRIVER
                                        databaseUser.child(userId).child("isDriver").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String isDriver;
                                                isDriver = dataSnapshot.getValue(String.class);
                                                if (Objects.equals(isDriver, "true")) {
                                                    finish();
                                                    startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                                                    HashMap<String, Object> result = new HashMap<>();
                                                    result.put("currentMode", "driver");
                                                    FirebaseDatabase.getInstance().getReference().child("users")
                                                         .child(userId).updateChildren(result);
                                                 } else {
                                                    startActivity(new Intent(RiderActivity.this, DriverProfileActivity.class));
                                                 }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError){ }
                                        });
                                    }
                                });
                                builder.show();
                                break;
                            case R.id.messages:
                                menuItem.setChecked(false);
                                startActivity(new Intent(RiderActivity.this, MessageActivity.class));
                                break;
                            case R.id.nav_account:
                                menuItem.setChecked(false);
                                startActivity(new Intent(RiderActivity.this, UpdateUserInfoActivity.class));
                                break;
                            case R.id.nav_trips:
                                menuItem.setChecked(false);
                                startActivity(new Intent(RiderActivity.this, MainCurrentRidesHolder.class));
                                break;
                        }
                        menuItem.setChecked(false);
                        return true;
                    }
                });
    }


    @Override
    protected void onStart(){
        super.onStart();
        final FirebaseRecyclerAdapter<Post,RiderActivity.PostViewHolder> firebaseRecyclerAdapter =
                //mDatabase is database of POSTS
                new FirebaseRecyclerAdapter<Post, RiderActivity.PostViewHolder>
                        (Post.class, R.layout.post_cardview_rider, RiderActivity.PostViewHolder.class, mDatabase){
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
                                Intent intent = new Intent(RiderActivity.this, RiderPostDetails.class);
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

        FirebaseDatabase.getInstance().getReference().child("post").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mPostList.scrollToPosition(firebaseRecyclerAdapter.getItemCount()-1);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    //for the side bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Enter a starting location...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                firebaseSearch(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
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
        /*public void setDetours(String detours){
            TextView post_detours = (TextView)mView.findViewById(R.id.post_detours);
            post_detours.setText(detours + " stops along the way");
        }*/
        public void setTime(String depTime){
            TextView post_dep_time = (TextView)mView.findViewById(R.id.post_time);
            post_dep_time.setText("TIME: " + depTime);
        }
    }

    //LOGOUT of the app
    private void logout() {
        //TODO REMOVE FCM TOKEN
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(userId)
                .child("fcmToken").setValue("");

        //sign user out
        firebaseAuth.signOut();
        //end current activity and go back to main screen
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
    @Override
    public void onClick(View view) {}
    //prevent user from pressing the back button to go back from the main app screen
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    //search for posts
    private void firebaseSearch(String searchText){
        String query = searchText.toLowerCase();
        Query firebaseSearchQuery = mDatabase.orderByChild("startPt")
                .startAt(query).endAt(query + "\uf8ff");
        FirebaseRecyclerAdapter<Post,RiderActivity.PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, RiderActivity.PostViewHolder>
                        (Post.class, R.layout.post_cardview_rider, RiderActivity.PostViewHolder.class, firebaseSearchQuery){
                    @Override
                    protected void populateViewHolder(RiderActivity.PostViewHolder viewHolder, final Post model, int position){
                        viewHolder.setStart(model.getStartPt().toUpperCase());
                        viewHolder.setDest(model.getEndPt().toUpperCase());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setCost(model.getCost());
                        viewHolder.setTime(model.getTime());
                        //TODO
                        //viewHolder.setDetours("NULL");

                        //Go to next activity on click
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(RiderActivity.this, RiderPostDetails.class);
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

    private void populateSV(){
        FirebaseRecyclerAdapter<Post,RiderActivity.PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, RiderActivity.PostViewHolder>
                        (Post.class, R.layout.post_cardview_rider, RiderActivity.PostViewHolder.class, mDatabase){
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
                                Intent intent = new Intent(RiderActivity.this, RiderPostDetails.class);
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
}
