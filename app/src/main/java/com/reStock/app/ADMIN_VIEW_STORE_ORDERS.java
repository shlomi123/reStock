package com.reStock.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ADMIN_VIEW_STORE_ORDERS extends AppCompatActivity implements OrderAdapter.OnItemClickListener{

    private RecyclerView mRecyclerView;
    private OrderAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private String store_name;
    private String store_email;
    private SharedPreferences sharedPreferences;
    private String company_name;
    private String company_email;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Order> mOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__view__store__orders);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        company_name = mAuth.getCurrentUser().getDisplayName();
        company_email = mAuth.getCurrentUser().getEmail();

        mRecyclerView = findViewById(R.id.recycler_view_orders);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle_orders);

        mOrders = new ArrayList<>();
        store_name = getIntent().getStringExtra("NAME");
        store_email = getIntent().getStringExtra("EMAIL");

        db.collection("Stores")
                .document(store_email)
                .collection("Orders")
                .whereEqualTo("_distributor", company_name)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty())
                        {
                            Toast.makeText(getApplicationContext(), "No Orders Were Made", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            mOrders.add(documentSnapshot.toObject(Order.class));
                        }

                        mAdapter = new OrderAdapter(ADMIN_VIEW_STORE_ORDERS.this, mOrders);

                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.setOnItemClickListener(ADMIN_VIEW_STORE_ORDERS.this);
                        mProgressCircle.setVisibility(View.INVISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(int position)
    {
        Order order = mOrders.get(position);
        Toast.makeText(getApplicationContext(), order.get_product(), Toast.LENGTH_SHORT).show();
    }
}
