package com.reStock.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ADMIN_ADD_STORE extends AppCompatActivity implements StoreSearchAdapter.OnItemClickListener{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText search;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private String company_name;
    private String company_email;
    private String company_profile;
    private ProgressBar mProgressCircle;
    private RecyclerView mRecyclerView;
    private StoreSearchAdapter mAdapter;
    private List<Store> mStores;
    private SearchView searchView;
    final private List<String> mExistingStores = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__add__store);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#00574B"));

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        company_name = mAuth.getCurrentUser().getDisplayName();
        company_email = mAuth.getCurrentUser().getEmail();
        company_profile = sharedPreferences.getString("COMPANY_PROFILE", null);

        //search = (EditText) findViewById(R.id.editText_search);
        mProgressCircle = findViewById(R.id.progress_circle_add_store);

        mRecyclerView = findViewById(R.id.recycler_view_add_store);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mStores = new ArrayList<>();
        mAdapter = new StoreSearchAdapter(getApplicationContext(), mStores);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ADMIN_ADD_STORE.this);

        db.collection("Companies")
                .document(company_email)
                .collection("Stores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                mExistingStores.add(documentSnapshot.toObject(Store.class).get_email());
                            }

                            db.collection("Stores")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                return;
                                            }
                                            // create recycler view to show stores and their data
                                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                                mStores.clear();
                                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                    Store store = document.toObject(Store.class);
                                                    if (!mExistingStores.contains(store.get_email())){
                                                        mStores.add(store);
                                                    }
                                                }

                                                mAdapter.notifyDataSetChanged();

                                                mProgressCircle.setVisibility(View.INVISIBLE);
                                            } else {
                                                mProgressCircle.setVisibility(View.INVISIBLE);
                                                mStores.clear();
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }else{
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.search_menu, menu);

        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    private void addStoreToDataBase(final String name, final String email) {
        //add new store to databased
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressCircle.setVisibility(View.VISIBLE);
        final Store s = new Store(name, email);
        db.collection("Companies")
                .document(company_email)
                .collection("Stores")
                .document(email)
                .set(s)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        final Distributor distributor = new Distributor(company_name, company_email, company_profile);

                        db.collection("Stores")
                                .document(email)
                                .collection("Distributors")
                                .document(company_email)
                                .set(distributor)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(getApplicationContext(), "Succesfuly added", Toast.LENGTH_LONG).show();
                                            finish();
                                        }else{
                                            Toast.makeText(getApplicationContext(), "Error, wasn't added", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("blaaaa", "Error adding document", e);
                        Toast.makeText(getApplicationContext(), "Error, wasn't added", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    @Override
    public void onItemClick(Store store) {
        addStoreToDataBase(store.get_name(), store.get_email());
    }
}
