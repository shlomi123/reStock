package com.reStock.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ENTRY_POINT extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry__point);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("USER_TYPE", 0);
        int user_type = sharedPreferences.getInt("USER_TYPE", 0);

        switch (user_type)
        {
            case 0:
                startActivity(new Intent(ENTRY_POINT.this, CHOOSE_USER_TYPE.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
            case 1:
                //admin main page
                startActivity(new Intent(ENTRY_POINT.this, ADMIN_MAIN_PAGE.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
            case 2:
                //client main page
                startActivity(new Intent(ENTRY_POINT.this, STORE_MAIN_PAGE.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
            case 3:
                //go to company sign in
                startActivity(new Intent(ENTRY_POINT.this, COMPANY_SIGN_IN.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
            case 4:
                //go to store sign in
                startActivity(new Intent(ENTRY_POINT.this, STORE_SIGN_IN.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
                break;
        }
    }
}
