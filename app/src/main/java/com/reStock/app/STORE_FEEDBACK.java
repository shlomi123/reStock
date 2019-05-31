package com.reStock.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class STORE_FEEDBACK extends AppCompatActivity {
    private EditText mEditTextSubject;
    private EditText mEditTextMessage;
    private SharedPreferences sharedPreferences;
    private String store_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store__feedback);

        mEditTextSubject = findViewById(R.id.store_edit_text_subject);
        mEditTextMessage = findViewById(R.id.store_edit_text_message);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        store_name = sharedPreferences.getString("STORE_NAME", null);

        Button buttonSend = findViewById(R.id.store_button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMail();
            }
        });
    }

    private void sendMail() {
        String recipientList = "info@techfficient.software";

        String subject = mEditTextSubject.getText().toString();
        String message = mEditTextMessage.getText().toString();

        SendMail sendMail = new SendMail(STORE_FEEDBACK.this, recipientList, subject, message, store_name, 0);
        sendMail.execute();
    }
}
