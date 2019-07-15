package com.reStock.app;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class ADMIN_MAIN_PAGE extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int fragment_num = 1;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private SharedPreferences sharedPreferences;
    private String email;
    private String name;
    private String profile_path;
    private TextView profile_name;
    private CircleImageView profile_picture;
    private Button change_profile_picture;
    private CircularProgressDrawable circularProgressDrawable;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user;
    private Uri mImageUri;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__main__page);

        user = mAuth.getCurrentUser();
        // company id
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        profile_path = sharedPreferences.getString("COMPANY_PROFILE", null);
        email = user.getEmail();
        name = user.getDisplayName();

        setUpSlideMenu(savedInstanceState);

        //start service for new orders
        /*if (!isMyServiceRunning(NewOrderService.class)) {
            startService(new Intent(this, NewOrderService.class).putExtra("DISTRIBUTOR_EMAIL", email));
        }*/


        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.M)
        {
            if(!checkPermission())
            {
                requestPermission();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        switch (fragment_num){
            case 1:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new StoresFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_stores);
                getSupportActionBar().setTitle("Retailers");
                break;
            case 2:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new ProductsFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_products);
                getSupportActionBar().setTitle("Products");
                break;
            case 3:
                final StorageReference storageReference = storage.getReferenceFromUrl(profile_path);
                storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {

                        GlideApp.with(getApplicationContext())
                                .load(storageReference)
                                .fitCenter()
                                .signature(new ObjectKey(storageMetadata.getCreationTimeMillis()))
                                .placeholder(circularProgressDrawable)
                                .into(profile_picture);
                    }
                });

                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new StoresFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_stores);
                getSupportActionBar().setTitle("Retailers");
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //item clicked in drawer menu
        switch (item.getItemId()) {
            case R.id.nav_stores:
                //open store fragment
                fragment_num = 1;
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new StoresFragment()).commit();
                getSupportActionBar().setTitle("Retailers");
                break;
            case R.id.nav_products:
                //open product fragment
                fragment_num = 2;
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new ProductsFragment()).commit();
                getSupportActionBar().setTitle("Products");
                break;
            case R.id.nav_scan_feature:
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new ScanFragment()).commit();
                getSupportActionBar().setTitle("Scan Feature");
                break;
            case R.id.nav_orders:
                //open edit profile fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        new OrderFragment()).commit();
                getSupportActionBar().setTitle("Orders");
                break;
            case R.id.nav_sign_out:
                mAuth.signOut();
                startActivity(new Intent(ADMIN_MAIN_PAGE.this, COMPANY_SIGN_IN.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
            case R.id.nav_feedback:
                startActivity(new Intent(ADMIN_MAIN_PAGE.this, ADMIN_FEEDBACK.class));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed in order to upload images for products and save files to storage")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(ADMIN_MAIN_PAGE.this,
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    requestPermission();
                }
            }
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            profile_picture.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            StorageReference storageReference = storage.getReferenceFromUrl(profile_path);

            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        StorageReference newStorageReference = FirebaseStorage.getInstance()
                                .getReference("Profiles")
                                .child(name + "." + getFileExtension(mImageUri));

                        newStorageReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String path = uri.toString();

                                        db.collection("Companies")
                                                .document(email)
                                                .update("Profile", path)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                                            editor.putString("COMPANY_PROFILE", path);
                                                            editor.apply();

                                                            final StorageReference storageReference = storage.getReferenceFromUrl(profile_path);
                                                            storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                                                @Override
                                                                public void onSuccess(StorageMetadata storageMetadata) {

                                                                    GlideApp.with(getApplicationContext())
                                                                            .load(storageReference)
                                                                            .fitCenter()
                                                                            .signature(new ObjectKey(storageMetadata.getCreationTimeMillis()))
                                                                            .placeholder(circularProgressDrawable)
                                                                            .into(profile_picture);
                                                                }
                                                            });
                                                            profile_picture.setVisibility(View.VISIBLE);
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                        }
                                                    }
                                                });

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressBar.setProgress((int) progress);
                            }
                        });

                    }else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setUpSlideMenu(Bundle savedInstanceState){
        //add custom toolbar
        Toolbar toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);

        //drawer settings
        drawer = findViewById(R.id.main_page_drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressBar = navigationView.getHeaderView(0).findViewById(R.id.progressBar_company_new_profile_upload);
        progressBar.setVisibility(View.INVISIBLE);

        //add profile picture
        circularProgressDrawable = new CircularProgressDrawable(getApplicationContext());
        circularProgressDrawable.start();
        profile_picture = navigationView.getHeaderView(0).findViewById(R.id.profile_picture);
        final StorageReference storageReference = storage.getReferenceFromUrl(profile_path);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                GlideApp.with(getApplicationContext())
                        .load(storageReference)
                        .fitCenter()
                        .signature(new ObjectKey(storageMetadata.getCreationTimeMillis()))
                        .placeholder(circularProgressDrawable)
                        .into(profile_picture);
            }
        });

        //change profile picture
        change_profile_picture = navigationView.getHeaderView(0).findViewById(R.id.button_open_file_chooser_change_profile);
        change_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });


        //set company name
        profile_name = navigationView.getHeaderView(0).findViewById(R.id.textView_distributor_name);
        profile_name.setText(name);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                    new StoresFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_stores);
            getSupportActionBar().setTitle("Retailers");
        }
    }
}
