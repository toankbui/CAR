package com.car.carsquad.carapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Driver;
import java.util.Calendar;
import java.util.Objects;

public class DriverPostActivity extends AppCompatActivity implements View.OnClickListener {

    //private AutoCompleteTextView mStartPoint;
    //private AutoCompleteTextView mEndPoint;
    private EditText mCost;
    private String startPt;
    private String endPt;
    private LatLng startLatLng;
    private LatLng endLatLng;
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
    private TextView mDisplayDate;
    private TextView mDisplayTime;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private Button mPostRide;
    private Button mCancelPost;
    String date;
    String time;
    int availableSeats;

    MyLatLng startLoc;
    MyLatLng endLoc;

    DatabaseReference databasePosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_post);

        //GOOGLE PLACES AUTOCOMPLETE
        PlaceAutocompleteFragment autocompleteFragment1 = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.post_start_point1);
        autocompleteFragment1.setHint("ENTER START POINT");
        ImageView startIcon = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        startIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_start));

        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //String placeID = place.getId();
                startPt = place.getName().toString().toLowerCase();
                startLatLng = place.getLatLng();
                startLat = startLatLng.latitude;
                startLng = startLatLng.longitude;
                startLoc = new MyLatLng();
                startLoc.setLatitude(startLat);
                startLoc.setLongitude(startLng);
            }
            @Override
            public void onError(Status status) {
            }
        });
        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.post_destination_point1);
        autocompleteFragment2.setHint("ENTER DESTINATION");
        ImageView endIcon = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        endIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_end));
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                endPt = place.getName().toString().toLowerCase();
                endLatLng = place.getLatLng();
                endLat = endLatLng.latitude;
                endLng = endLatLng.longitude;
                endLoc = new MyLatLng();
                endLoc.setLatitude(endLat);
                endLoc.setLongitude(endLng);
            }
            @Override
            public void onError(Status status) {
            }
        });

        databasePosts = FirebaseDatabase.getInstance().getReference("post");

        //UI References
        mDisplayDate = (TextView) findViewById(R.id.tvDate);
        mDisplayTime = (TextView) findViewById(R.id.tvTime);
        mPostRide = (Button) findViewById(R.id.confirm_post);
        mCancelPost = (Button) findViewById(R.id.post_cancel);
        mCost = (EditText) findViewById(R.id.ride_cost);

        //set action
        mPostRide.setOnClickListener(this);
        mCancelPost.setOnClickListener(this);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        DriverPostActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener,
                        year, month, day);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getDatePicker().setMinDate(System.currentTimeMillis()-10000);
                dialog.show();
            }
        });

        mDisplayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(
                        DriverPostActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mTimeSetListener,
                        hour, minute, true);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                Log.d("DriverPostActivity", "onDateSet: mm/dd/yyyy: " + month + "/" + day + "/" + year);
                date = month + "/" + day + "/" + year;
                mDisplayDate.setText("DATE: " + date);
            }
        };
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.d("DriverPostActivity", "onTimeSet: hh:mm: " + hour + "/" + minute);
                time = checkDigit(hour) + ":" + checkDigit(minute);
                mDisplayTime.setText("TIME: " + time);
            }
        };
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }
    private void postRide(){
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final String departureDate = date;//mDisplayDate.getText().toString().trim();
        final String departureTime = time;//mDisplayTime.getText().toString().trim();

        final String cost = mCost.getText().toString().trim();

        if(!TextUtils.isEmpty(startPt) && !TextUtils.isEmpty(endPt) &&
                !TextUtils.isEmpty(departureDate) && !TextUtils.isEmpty(departureTime)) {

            //TODO CREATE THE POST
            final String postId = databasePosts.push().getKey();
            Post newPost = new Post(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    postId,startPt,endPt,departureDate,departureTime, cost, startLoc, endLoc);

            databasePosts.child(Objects.requireNonNull(postId)).setValue(newPost);

            //TODO KEEP TRACK OF NUM SEATS

            FirebaseDatabase.getInstance().getReference().child("car").child(userId)
                    .child("originalNumSeats").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FirebaseDatabase.getInstance().getReference().child("seatsAvailable")
                            .child(postId).child("seatsAvail").setValue(dataSnapshot.getValue(Integer.class));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });

            finish();
            startActivity(new Intent(this, DriverActivity.class));
            Toast.makeText(this, "Your ride has been posted", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Please fill out the required fields", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == mCancelPost){
            finish();
            startActivity(new Intent(this, DriverActivity.class));
        } else if(view == mPostRide){
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("post");
            postRide();
        }
    }

}
