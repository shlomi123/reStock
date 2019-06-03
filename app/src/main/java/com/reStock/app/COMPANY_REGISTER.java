package com.reStock.app;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class COMPANY_REGISTER extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Button verify;
    private EditText email;
    private EditText password;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressBar spinner;
    private EditText verify_password;
    private TextView logIn;
    private SharedPreferences sharedPreferences;
    private CircleImageView mImageView;
    private ImageView placeholder;
    private Uri mImageUri;
    private Boolean imageFlag = false;
    private EditText name;
    private Button chooseFile;
    SharedPreferences.Editor editor;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company__register);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        mStorageRef = FirebaseStorage.getInstance().getReference("Profiles");

        mImageView = (CircleImageView) findViewById(R.id.imageView_company_profile);
        placeholder = (ImageView) findViewById(R.id.imageView_company_profile_placeholder);
        verify = (Button) findViewById(R.id.ButtonCompanyVerify);
        email = (EditText) findViewById(R.id.editTextCompanyEmail);
        name = (EditText) findViewById(R.id.editTextCompanyName);
        password = (EditText) findViewById(R.id.editTextCompanyPassword);
        verify_password = (EditText) findViewById(R.id.editTextCompanyPasswordVerify);
        logIn = (TextView) findViewById(R.id.textView_log_in);
        chooseFile = (Button) findViewById(R.id.button_open_file_chooser_for_profile);
        spinner=(ProgressBar)findViewById(R.id.progressBar_company_profile_upload);
        spinner.setVisibility(View.GONE);
        mImageView.setVisibility(View.INVISIBLE);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create user
                if (name.getText().toString().trim().equals(""))
                {
                    Toast.makeText(getApplicationContext(), "enter company name", Toast.LENGTH_SHORT).show();
                } else if (!imageFlag){
                    Toast.makeText(getApplicationContext(), "pick a profile picture", Toast.LENGTH_SHORT).show();
                } else {
                    spinner.setVisibility(View.VISIBLE);
                    verify.setVisibility(View.INVISIBLE);
                    password.setVisibility(View.INVISIBLE);
                    verify_password.setVisibility(View.INVISIBLE);
                    email.setVisibility(View.INVISIBLE);
                    logIn.setVisibility(View.INVISIBLE);
                    mImageView.setVisibility(View.INVISIBLE);
                    chooseFile.setVisibility(View.INVISIBLE);
                    name.setVisibility(View.INVISIBLE);

                    mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful())
                                    {
                                        spinner.setVisibility(View.INVISIBLE);
                                        verify.setVisibility(View.VISIBLE);
                                        password.setVisibility(View.VISIBLE);
                                        verify_password.setVisibility(View.VISIBLE);
                                        email.setVisibility(View.VISIBLE);
                                        logIn.setVisibility(View.VISIBLE);
                                        mImageView.setVisibility(View.VISIBLE);
                                        chooseFile.setVisibility(View.VISIBLE);
                                        name.setVisibility(View.VISIBLE);
                                        Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(COMPANY_REGISTER.this, COMPANY_SIGN_IN.class));
            }
        });

        chooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        // listens for user sign in
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    //send verification mail
                    sendVerificationEmail();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            imageFlag = true;
            Picasso.with(this).load(mImageUri).into(mImageView);
            placeholder.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            //Toast.makeText(getApplicationContext(), "verification email sent", Toast.LENGTH_LONG).show();
                            uploadCompany();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                            spinner.setVisibility(View.INVISIBLE);
                            verify.setVisibility(View.VISIBLE);
                            password.setVisibility(View.VISIBLE);
                            verify_password.setVisibility(View.VISIBLE);
                            email.setVisibility(View.VISIBLE);
                            logIn.setVisibility(View.VISIBLE);
                            mImageView.setVisibility(View.VISIBLE);
                            chooseFile.setVisibility(View.VISIBLE);
                            name.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void uploadCompany(){
        final StorageReference fileReference = mStorageRef.child(name.getText().toString().trim() + "." + getFileExtension(mImageUri));


        db.collection("Companies").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            // check that company name doesn't already exist
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        //if company name exists return
                        if (document != null)
                        {
                            if (document.getString("Name").toLowerCase().equals(name.getText().toString().trim().toLowerCase())) {
                                Toast.makeText(getApplicationContext(), "that company name already exists.", Toast.LENGTH_LONG).show();
                                spinner.setVisibility(View.INVISIBLE);
                                verify.setVisibility(View.VISIBLE);
                                password.setVisibility(View.VISIBLE);
                                verify_password.setVisibility(View.VISIBLE);
                                email.setVisibility(View.VISIBLE);
                                logIn.setVisibility(View.VISIBLE);
                                mImageView.setVisibility(View.VISIBLE);
                                chooseFile.setVisibility(View.VISIBLE);
                                name.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                    }

                    //upload profile picture
                    fileReference.putFile(mImageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //get path of uploaded product
                                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            //upload store properties to database
                                            final String path = uri.toString();
                                            final Map<String, Object> map = new HashMap<>();
                                            map.put("Name", name.getText().toString());
                                            map.put("Email", email.getText().toString());
                                            map.put("Profile", path);



                                            //add company to database
                                            db.collection("Companies").document(email.getText().toString().trim()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        //save company id
                                                        db.collection("Companies").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    for (DocumentSnapshot currentDocumentSnapshot : task.getResult())
                                                                    {
                                                                        String current_name = currentDocumentSnapshot.getString("Name");
                                                                        if (current_name.equals(name.getText().toString().trim()))
                                                                        {
                                                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                                    .setDisplayName(name.getText().toString().trim())
                                                                                    .build();

                                                                            user.updateProfile(profileUpdates)
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                editor = sharedPreferences.edit();
                                                                                                editor.putString("COMPANY_PROFILE", path);
                                                                                                editor.putString("COMPANY_EMAIL", email.getText().toString().trim());
                                                                                                editor.apply();
                                                                                                mAuth.removeAuthStateListener(mAuthListener);
                                                                                                FirebaseAuth.getInstance().signOut();

                                                                                                Dialog dialog = new Dialog(COMPANY_REGISTER.this);
                                                                                                dialog.setContentView(R.layout.custom_alert_dialog);
                                                                                                Button ok = (Button) dialog.findViewById(R.id.dialog_ok);
                                                                                                ok.setOnClickListener(new View.OnClickListener() {
                                                                                                    @Override
                                                                                                    public void onClick(View view) {
                                                                                                        startActivity(new Intent(COMPANY_REGISTER.this, COMPANY_SIGN_IN.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                                                                        finish();
                                                                                                    }
                                                                                                });
                                                                                                TextView textView = dialog.findViewById(R.id.dialog_text);
                                                                                                textView.setText("Verification mail has been sent");
                                                                                                dialog.setCancelable(false);
                                                                                                dialog.show();

                                                                                                /*AlertDialog.Builder builder = new AlertDialog.Builder(COMPANY_REGISTER.this);
                                                                                                builder.setMessage("Verification email has been sent")
                                                                                                        .setCancelable(false)
                                                                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                                                startActivity(new Intent(COMPANY_REGISTER.this, COMPANY_SIGN_IN.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                                                                                finish();
                                                                                                            }
                                                                                                        });
                                                                                                AlertDialog alert = builder.create();
                                                                                                alert.show();*/
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    spinner.setVisibility(View.INVISIBLE);
                                    verify.setVisibility(View.VISIBLE);
                                    password.setVisibility(View.VISIBLE);
                                    verify_password.setVisibility(View.VISIBLE);
                                    email.setVisibility(View.VISIBLE);
                                    logIn.setVisibility(View.VISIBLE);
                                    mImageView.setVisibility(View.VISIBLE);
                                    chooseFile.setVisibility(View.VISIBLE);
                                    name.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                //update progress bar during upload
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    spinner.setProgress((int) progress);
                                }
                            });
                } else {
                    spinner.setVisibility(View.INVISIBLE);
                    verify.setVisibility(View.VISIBLE);
                    password.setVisibility(View.VISIBLE);
                    verify_password.setVisibility(View.VISIBLE);
                    email.setVisibility(View.VISIBLE);
                    logIn.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    chooseFile.setVisibility(View.VISIBLE);
                    name.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
