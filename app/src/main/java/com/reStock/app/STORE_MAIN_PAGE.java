package com.reStock.app;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.Manifest.permission.CAMERA;

public class STORE_MAIN_PAGE extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int fragment_num = 1;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store__main__page);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        //add custom toolbar
        Toolbar toolbar = findViewById(R.id.store_main_page_toolbar);
        setSupportActionBar(toolbar);

        //drawer settings
        drawer = findViewById(R.id.store_drawer_layout);
        navigationView = findViewById(R.id.store_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.store_content_frame,
                    new DistributorFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_distributors);
            getSupportActionBar().setTitle("Suppliers");
        }

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
                getSupportFragmentManager().beginTransaction().replace(R.id.store_content_frame,
                        new DistributorFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_stores);
                getSupportActionBar().setTitle("Suppliers");
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //item clicked in drawer menu
        switch (item.getItemId()) {
            case R.id.nav_distributors:
                fragment_num = 1;
                getSupportFragmentManager().beginTransaction().replace(R.id.store_content_frame,
                        new DistributorFragment()).commit();
                getSupportActionBar().setTitle("Suppliers");
                break;
            case R.id.nav_scan:
                startActivity(new Intent(STORE_MAIN_PAGE.this, STORE_QR_SCAN.class));
                break;
            case R.id.nav_orders:
                getSupportFragmentManager().beginTransaction().replace(R.id.store_content_frame,
                        new StoreOrdersFragment()).commit();
                getSupportActionBar().setTitle("Orders");
                break;
            case R.id.nav_sign_out:
                mAuth.signOut();
                startActivity(new Intent(STORE_MAIN_PAGE.this, STORE_SIGN_IN.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
            case R.id.nav_feedback:
                startActivity(new Intent(STORE_MAIN_PAGE.this, STORE_FEEDBACK.class));
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
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed in order to use camera for scanning feature")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(STORE_MAIN_PAGE.this,
                                    new String[] {Manifest.permission.CAMERA}, 1);
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, 1);
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
}
