package com.reStock.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class STORE_QR_SCAN extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView scannerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String distributor_email;
    private String distributor_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store__qr__scan);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        scannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void handleResult(Result result) {
        String[] list = result.getText().split("##");
        distributor_email = list[1];
        distributor_name = list[0];

        db.collection("Companies")
                .whereEqualTo("Email", distributor_email)
                .whereEqualTo("Name", distributor_name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().size() > 0){
                                Intent intent = new Intent(STORE_QR_SCAN.this, STORE_SHOW_DISTRIBUTOR_PRODUCTS.class);
                                intent.putExtra("DISTRIBUTOR_NAME", distributor_name);
                                intent.putExtra("DISTRIBUTOR_EMAIL", distributor_email);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "invalid qr-code", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }
}
