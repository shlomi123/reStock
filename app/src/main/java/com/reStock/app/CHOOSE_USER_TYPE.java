package com.reStock.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class CHOOSE_USER_TYPE extends AppCompatActivity {

    private Button company;
    private Button store;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose__user__type);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        company = (Button) findViewById(R.id.buttonCompany);
        store = (Button) findViewById(R.id.buttonStore);

        company.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("USER_TYPE", 1);
                editor.apply();*/
                startActivity(new Intent(CHOOSE_USER_TYPE.this, COMPANY_REGISTER.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("USER_TYPE", 2);
                editor.apply();*/
                startActivity(new Intent(CHOOSE_USER_TYPE.this, STORE_REGISTER.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }
}