package com.car.carsquad.carapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // UI references
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonForgotPassword;
    private ProgressDialog progressDialog;

    //firebase authentication object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initializing FirebaseAuth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if User is already logged in, skip this login activity
        /*if(firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified()) {
            //start profile activity
            finish();
            startActivity(new Intent(getApplicationContext(), RiderActivity.class));
        }*/

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //if User is already logged in, skip this activity
        if (user != null && user.isEmailVerified()) {
            DatabaseReference databaseUser =
                    FirebaseDatabase.getInstance().getReference("users");
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            databaseUser.child(userId).child("currentMode").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String currentMode;
                    currentMode = dataSnapshot.getValue(String.class);
                    if (Objects.equals(currentMode, "driver")) {
                        startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                    } else if (Objects.equals(currentMode, "rider")) {
                        startActivity(new Intent(LoginActivity.this, RiderActivity.class));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }


        // Link to UI elements
        progressDialog = new ProgressDialog(this);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonForgotPassword = findViewById(R.id.forgot_password_button);

        //wait until Register button is clicked
        buttonLogin.setOnClickListener(this);
        buttonForgotPassword.setOnClickListener(this);
    }

    //method to register user to Firebase server
    private void userLogin(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //empty email check
        if(TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show();
            //stop the function
            return;
        }
        //empty password check
        if(TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            //stop the function
            return;
        }
        //valid email check
        if(!isEmailValid(email)){
            //email is invalid
            Toast.makeText(this, "Please enter a valid UCSD Email",
                    Toast.LENGTH_SHORT).show();
            //stop the function
            return;
        }
        //valid password check
        if(!isPasswordValid(password)){
            //password is invalid
            Toast.makeText(this, "Password should contain more than 4 characters",
                    Toast.LENGTH_SHORT).show();
            //stop the function
            return;
        }

        //email and password OKAY
        //show a progress bar
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        //allow user to login with email and password
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            //progressDialog.dismiss();
                            if(firebaseAuth.getCurrentUser().isEmailVerified()){
                                //go to main app screen if sign in successfully
                                DatabaseReference databaseUser =
                                        FirebaseDatabase.getInstance().getReference("users");
                                final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                //TODO ADD FCM TOKEN
                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                String token = task.getResult().getToken();
                                                //TODO UPDATE USER's FCM TOKEN
                                                FirebaseDatabase.getInstance().getReference().child("users").child(userId)
                                                        .child("fcmToken").setValue(token);
                                            }
                                        });

                                databaseUser.child(userId).child("currentMode").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String currentMode;
                                        currentMode = dataSnapshot.getValue(String.class);
                                        if (Objects.equals(currentMode, "driver")) {
                                            //progressDialog.dismiss();
                                            startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                                        } else if (Objects.equals(currentMode, "rider")){
                                            //progressDialog.dismiss();
                                            startActivity(new Intent(getApplicationContext(), RiderActivity.class));
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                                //startActivity(new Intent(getApplicationContext(), RiderActivity.class));
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("ACCOUNT NOT VERIFIED");
                                builder.setMessage("Please check the verification link sent to your email");

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                builder.show();
                            }
                        }
                        else {
                            Toast.makeText(LoginActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //check if email is valid
    private boolean isEmailValid(String email) {
        return email.contains("@ucsd.edu");
    }
    //check is email is valid
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    //when the user clicks register
    @Override
    public void onClick(View view) {
        if(view == buttonLogin) {
            userLogin();
        } else if (view == buttonForgotPassword) {
            finish();
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        }
    }
}

