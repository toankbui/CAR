package com.car.carsquad.carapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import com.google.android.gms.tasks.OnCompleteListener;

/**
 * A forgot password screen.
 */
public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    //UI references
    private Button resetPasswordSendEmailButton;
    private EditText inputEmailForReset;

    //Firebase Object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //initialize firebase object
        firebaseAuth = FirebaseAuth.getInstance();

        resetPasswordSendEmailButton = (Button) findViewById(R.id.buttonForget);
        inputEmailForReset = (EditText) findViewById(R.id.email_reset_password);

        resetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = inputEmailForReset.getText().toString();

                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Please enter your Email", Toast.LENGTH_SHORT).show();
                } else {
                    //send reset link to user
                    firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("RESET PASSWORD EMAIL SENT");
                                builder.setMessage("Please check your email for reset password link");

                                builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                    }
                                });
                                builder.show();

                                //Toast.makeText(ForgotPasswordActivity.this,
                                 //       "Please check your mailbox for password reset link", Toast.LENGTH_SHORT).show();
                                //go back to loginActivity, allow user to login using new password
                                //startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            }
                            else {
                                Toast.makeText(ForgotPasswordActivity.this,
                                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
    //DO NOT REMOVE THIS (needed for inheritance)
    @Override
    public void onClick(View view) {}
}

