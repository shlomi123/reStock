package com.reStock.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import java.util.Collections;
import java.util.List;

public class StoresFragment extends Fragment implements StoreAdapter.OnItemClickListener {

    private ImageButton addStore;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private RecyclerView mRecyclerView;
    private StoreAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private List<Store> mStores;
    private String company_email;
    private String company_name;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stores_fragment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        company_email = mAuth.getCurrentUser().getEmail();
        company_name = mAuth.getCurrentUser().getDisplayName();
        addStore = (ImageButton) view.findViewById(R.id.button_add_store);
        mRecyclerView = view.findViewById(R.id.recycler_view_stores_main);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgressCircle = view.findViewById(R.id.progress_circle_stores_main);
        mStores = new ArrayList<>();

        mAdapter = new StoreAdapter(getActivity(), mStores);

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(StoresFragment.this);

        // get required company
        db.collection("Companies")
                .document(company_email)
                .collection("Stores")
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
                                mStores.add(store);
                            }

                            Collections.sort(mStores, new Helper.sortStoresByName());

                            mAdapter.notifyDataSetChanged();

                            mProgressCircle.setVisibility(View.INVISIBLE);
                        } else {
                            mProgressCircle.setVisibility(View.INVISIBLE);
                            mStores.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });

        addStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ADMIN_ADD_STORE.class));
            }
        });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.store_sort, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_store_sort_by_name:
                Collections.sort(mStores, new Helper.sortStoresByName());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.item_store_sort_by_email:
                Collections.sort(mStores, new Helper.sortStoresByEmail());
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getActivity(), "long press to show options", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExcelSheet(int position){
        final Store chosenStore = mStores.get(position);

        final List<Order> store_orders = new ArrayList<>();

        db.collection("Stores")
                .document(chosenStore.get_email())
                .collection("Orders")
                .whereEqualTo("_distributor", company_name)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Order order = documentSnapshot.toObject(Order.class);
                            store_orders.add(order);
                        }

                        if (Helper.saveExcelFile(getContext(), chosenStore, store_orders)){
                            Toast.makeText(getContext(), "excel sheet created - (saved to documents)", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "error creating excel sheet", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewStore(int position) {
        final Store chosenStore = mStores.get(position);

        Intent intent = new Intent(getActivity(), ADMIN_VIEW_STORE_ORDERS.class);
        intent.putExtra("NAME", chosenStore.get_name());
        intent.putExtra("EMAIL", chosenStore.get_email());
        startActivity(intent);
    }

    @Override
    public void onDeleteStore(int position) {
        final Store chosenStore = mStores.get(position);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Delete Store");
        alertDialog.setMessage("Are you sure you want to delete the store " + chosenStore.get_name() + "?");

        //if the user verifies the order
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // delete chosen store
                deleteStore(chosenStore.get_email());
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void deleteStore(final String storeEmail) {
        // get required company
        //TODO what if store is in the middle of ordering

        db.collection("Companies")
                .document(company_email)
                .collection("Stores")
                .document(storeEmail)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Error While Deleting", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        db.collection("Stores")
                .document(storeEmail)
                .collection("Distributors")
                .document(company_email)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Error While Deleting", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
