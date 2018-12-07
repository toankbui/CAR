package com.car.carsquad.carapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class DriverProfileActivity extends AppCompatActivity implements View.OnClickListener {

    //firebase references
    private DatabaseReference databaseCar;
    private DatabaseReference databaseUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    //UI references
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mPhoneNo;
    private EditText mCarModel;
    private EditText mLicensePlate;
    private EditText mNumSeats;

    private Button mSubmitRiderSignup;
    private Button mCancel;
    private String userId;
    private String isDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        //Database instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userId = Objects.requireNonNull(user).getUid();

        databaseCar = FirebaseDatabase.getInstance().getReference("car");
        databaseUser = FirebaseDatabase.getInstance().getReference("users");

        //skip if user already signed up as DRIVER
       /* databaseUser.child(userId).child("isDriver").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isDriver = dataSnapshot.getValue(String.class);
                if(Objects.equals(isDriver, "true")){
                    finish();
                    startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                }
                else{
                    Toast.makeText(DriverProfileActivity.this, isDriver, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });*/

        //UI References
        //mFirstName = (EditText) findViewById(R.id.driver_first_name);
        //mLastName = (EditText) findViewById(R.id.driver_last_name);
        mPhoneNo = (EditText) findViewById(R.id.driver_phone_number);
        mCarModel = (EditText) findViewById(R.id.car_model);
        mLicensePlate = (EditText) findViewById(R.id.license_plate);
        mNumSeats = (EditText) findViewById(R.id.num_seats);
        mSubmitRiderSignup = (Button) findViewById(R.id.submit_driver_signup);
        mCancel = (Button) findViewById(R.id.cancel_signup);

        mSubmitRiderSignup.setOnClickListener(this);
        mCancel.setOnClickListener(this);

    }


    private void enrollRider(){
        userId = mAuth.getCurrentUser().getUid();
        //String firstName = mFirstName.getText().toString().trim();
        //String lastName = mLastName.getText().toString().trim();
        String phoneNo = mPhoneNo.getText().toString().trim();
        String carModel = mCarModel.getText().toString().trim();
        String licenseNo = mLicensePlate.getText().toString().trim();
        int numSeats = Integer.parseInt(mNumSeats.getText().toString().trim());
        int originalNum = numSeats;

        if(/*!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) &&*/
                !TextUtils.isEmpty(phoneNo) && !TextUtils.isEmpty(carModel) &&
                        !TextUtils.isEmpty(licenseNo)&& !TextUtils.isEmpty(mNumSeats.getText().toString().trim())) {

            //send car info to database
            String carId = databaseCar.push().getKey();
            Car newCar = new Car(originalNum, carId, numSeats,carModel,licenseNo);
            databaseCar.child(userId).setValue(newCar);

            //update user info

            /* User updatedUser = new User(userId, firstName, lastName, phoneNo, "true", 0);
            databaseUser.child(userId).setValue(updatedUser);*/

            HashMap<String, Object> mode = new HashMap<>();
            mode.put("currentMode", "driver");
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userId).updateChildren(mode);

            HashMap<String, Object> isDriver = new HashMap<>();
            isDriver.put("isDriver", "true");
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userId).updateChildren(isDriver);

            HashMap<String, Object> phone = new HashMap<>();
            phone.put("phoneNo", phoneNo);
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userId).updateChildren(phone);

            HashMap<String, Object> phoneN = new HashMap<>();
            phoneN.put("phoneNo", phoneNo);
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userId).updateChildren(phoneN);


            startActivity(new Intent(this, DriverActivity.class));
            Toast.makeText(this, "You have successfully enrolled as a driver", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Please fill out the required fields", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == mCancel){
            finish();
            startActivity(new Intent(this, RiderActivity.class));
        } else if(view == mSubmitRiderSignup){
            enrollRider();
        }
    }
}

