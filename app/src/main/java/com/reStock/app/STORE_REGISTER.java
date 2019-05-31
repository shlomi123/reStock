package com.reStock.app;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class STORE_REGISTER extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Button verify;
    private EditText email;
    private EditText name;
    private EditText password;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Boolean sign_in_flag = false;
    private ProgressBar spinner;
    private EditText verify_password;
    private TextView logIn;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store__register);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);


        verify = (Button) findViewById(R.id.ButtonStoreVerify);
        email = (EditText) findViewById(R.id.editTextStoreEmail);
        name = (EditText) findViewById(R.id.editTextStoreName);
        password = (EditText) findViewById(R.id.editTextStorePassword);
        verify_password = (EditText) findViewById(R.id.editTextStorePasswordVerify);
        logIn = (TextView) findViewById(R.id.textView_store_log_in);
        spinner = (ProgressBar) findViewById(R.id.progressBarStoreLogIn);
        spinner.setVisibility(View.INVISIBLE);


        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "enter store name", Toast.LENGTH_LONG).show();
                } else {
                    spinner.setVisibility(View.VISIBLE);
                    verify.setVisibility(View.INVISIBLE);
                    password.setVisibility(View.INVISIBLE);
                    verify_password.setVisibility(View.INVISIBLE);
                    email.setVisibility(View.INVISIBLE);
                    name.setVisibility(View.INVISIBLE);
                    logIn.setVisibility(View.INVISIBLE);
                    mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        spinner.setVisibility(View.INVISIBLE);
                                        verify.setVisibility(View.VISIBLE);
                                        password.setVisibility(View.VISIBLE);
                                        verify_password.setVisibility(View.VISIBLE);
                                        email.setVisibility(View.VISIBLE);
                                        logIn.setVisibility(View.VISIBLE);
                                        name.setVisibility(View.VISIBLE);
                                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.removeAuthStateListener(mAuthListener);
                startActivity(new Intent(STORE_REGISTER.this, STORE_SIGN_IN.class));
            }
        });

        // listens for user sign in
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    sign_in_flag = true;
                    // User is signed in
                    //send verification mail
                    sendVerificationEmail();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            //Toast.makeText(getApplicationContext(), "verification email sent", Toast.LENGTH_SHORT).show();

                            Store store = new Store(name.getText().toString().trim(), email.getText().toString().trim());

                            db.collection("Stores")
                                    .document(email.getText().toString())
                                    .set(store)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                editor = sharedPreferences.edit();
                                                editor.putString("STORE_EMAIL", email.getText().toString().trim());
                                                editor.putString("STORE_NAME", name.getText().toString().trim());
                                                editor.putInt("USER_TYPE", 2);
                                                editor.apply();
                                                mAuth.removeAuthStateListener(mAuthListener);

                                                Dialog dialog = new Dialog(STORE_REGISTER.this);
                                                dialog.setContentView(R.layout.custom_alert_dialog);
                                                Button ok = (Button) dialog.findViewById(R.id.dialog_ok);
                                                ok.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        startActivity(new Intent(STORE_REGISTER.this, STORE_SIGN_IN.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                        finish();
                                                    }
                                                });
                                                dialog.setCancelable(false);
                                                dialog.show();

                                                /*AlertDialog.Builder builder = new AlertDialog.Builder(STORE_REGISTER.this);
                                                builder.setMessage("Verification email has been sent")
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                startActivity(new Intent(STORE_REGISTER.this, STORE_SIGN_IN.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                                finish();
                                                            }
                                                        });
                                                AlertDialog alert = builder.create();
                                                alert.show();*/

                                            }else {
                                                spinner.setVisibility(View.INVISIBLE);
                                                verify.setVisibility(View.VISIBLE);
                                                password.setVisibility(View.VISIBLE);
                                                verify_password.setVisibility(View.VISIBLE);
                                                email.setVisibility(View.VISIBLE);
                                                logIn.setVisibility(View.VISIBLE);
                                                name.setVisibility(View.VISIBLE);
                                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });


                        } else {
                            spinner.setVisibility(View.INVISIBLE);
                            verify.setVisibility(View.VISIBLE);
                            password.setVisibility(View.VISIBLE);
                            verify_password.setVisibility(View.VISIBLE);
                            email.setVisibility(View.VISIBLE);
                            logIn.setVisibility(View.VISIBLE);
                            name.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
