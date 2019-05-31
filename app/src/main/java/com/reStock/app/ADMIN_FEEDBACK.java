package com.reStock.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class ADMIN_FEEDBACK extends AppCompatActivity {
    private EditText mEditTextSubject;
    private EditText mEditTextMessage;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String company_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__feedback);

        mEditTextSubject = findViewById(R.id.edit_text_subject);
        mEditTextMessage = findViewById(R.id.edit_text_message);

        company_name = mAuth.getCurrentUser().getDisplayName();

        Button buttonSend = findViewById(R.id.button_send);
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

        SendMail sendMail = new SendMail(ADMIN_FEEDBACK.this, recipientList, subject, message, company_name, 1);
        sendMail.execute();
    }
}
