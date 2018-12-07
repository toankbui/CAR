package com.car.carsquad.carapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateUserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    //firebase references
    private DatabaseReference databaseUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private StorageReference userImageRef;
    private DatabaseReference currUserRef;

    //UI references
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mPhoneNo;
    private CircleImageView profileImage;
    final static int requestGallery = 1;

    private Button mSubmitInfo;
    private Button mCancel;
    private String userId;
    private String currFirst;
    private String currLast;
    private String currPhone;
    private double driverRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);

        //Database instance
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userId = Objects.requireNonNull(user).getUid();
        databaseUser = FirebaseDatabase.getInstance().getReference("users");
        currUserRef = databaseUser.child(userId);
        userImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        //UI References
        mFirstName = (EditText) findViewById(R.id.user_first_name);
        mLastName = (EditText) findViewById(R.id.user_last_name);
        mPhoneNo = (EditText) findViewById(R.id.user_phone_number);
        mSubmitInfo = (Button) findViewById(R.id.submit_user_info);
        mCancel = (Button) findViewById(R.id.cancel_update_info);
        profileImage = (CircleImageView) findViewById(R.id.post_image_rider) ;
        DatabaseReference currUser = databaseUser.child(userId);
        currUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currFirst = dataSnapshot.child("firstName").getValue().toString();
                    currLast = dataSnapshot.child("lastName").getValue().toString();
                    currPhone = dataSnapshot.child("phoneNo").getValue().toString();
                    mFirstName.setText(currFirst,TextView.BufferType.EDITABLE);
                    mLastName.setText(currLast,TextView.BufferType.EDITABLE);
                    mPhoneNo.setText(currPhone,TextView.BufferType.EDITABLE);
                    Object url = dataSnapshot.child("profile_image").getValue();
                    if(url != null){
                        String image = url.toString();
                        if(image != null && !image.equals("0"))
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                        else
                            profileImage.setImageResource(R.drawable.profile);
                    }
                    else
                        profileImage.setImageResource(R.drawable.profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSubmitInfo.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,requestGallery);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == requestGallery && resultCode == RESULT_OK && !data.equals(null))
        {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1).start(this);


        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                final StorageReference filePath = userImageRef.child(userId+".jpg");
                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();
                            final String downloadUrl = downloadUri.toString();
                            currUserRef.child("profile_image").setValue(downloadUrl);
                            DatabaseReference currUser = databaseUser.child(userId);
                            currUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        String image = dataSnapshot.child("profile_image").getValue().toString();
                                        if(image != null)
                                            Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });


            }
        }
    }

    private void updateInfo(){
        final String firstName = mFirstName.getText().toString().trim();
        final String lastName = mLastName.getText().toString().trim();
        final String phoneNo = mPhoneNo.getText().toString().trim();

        if(phoneNo.length() != 10) {
            Toast.makeText(UpdateUserInfoActivity.this, "Please enter a valid phone number", Toast.LENGTH_LONG).show();
            return;
        }
        if(!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(phoneNo)) {

            HashMap<String, Object> firstN = new HashMap<>();
            firstN.put("firstName", firstName);
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userId).updateChildren(firstN);

            HashMap<String, Object> lastN = new HashMap<>();
            lastN.put("lastName", lastName);
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userId).updateChildren(lastN);

            HashMap<String, Object> phoneN = new HashMap<>();
            phoneN.put("phoneNo", phoneNo);
            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(userId).updateChildren(phoneN);

            databaseUser.child(userId).child("currentMode").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String currentMode;
                    currentMode = dataSnapshot.getValue(String.class);
                    if (Objects.equals(currentMode, "driver")) {
                        finish();
                        startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                    } else if (Objects.equals(currentMode, "rider")){
                        finish();
                        startActivity(new Intent(getApplicationContext(), RiderActivity.class));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            //startActivity(new Intent(getApplicationContext(), RiderActivity.class));
            Toast.makeText(UpdateUserInfoActivity.this, "Your profile information has been updated", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Please fill out all the required fields", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == mCancel){
            databaseUser.child(userId).child("currentMode").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String currentMode;
                    currentMode = dataSnapshot.getValue(String.class);
                    if (Objects.equals(currentMode, "driver")) {
                        finish();
                        startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                    } else if (Objects.equals(currentMode, "rider")){
                        finish();
                        startActivity(new Intent(getApplicationContext(), RiderActivity.class));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            //finish();
            //startActivity(new Intent(this, RiderActivity.class));
        } else if(view == mSubmitInfo){
            updateInfo();
        }
    }
}
