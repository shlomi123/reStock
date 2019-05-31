package com.reStock.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductAdapter.OnItemClickListener {

    private ImageButton addProduct;
    private SharedPreferences sharedPreferences;
    private FirebaseStorage mStorage;
    private RecyclerView mRecyclerView;
    private ProductAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private List<Product> mProducts;
    private String email;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_fragment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        email = mAuth.getCurrentUser().getEmail();
        addProduct = view.findViewById(R.id.button_add_product);
        mStorage = FirebaseStorage.getInstance();
        mProgressCircle = view.findViewById(R.id.progress_circle_products);
        mRecyclerView = view.findViewById(R.id.recycler_view_products);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProducts = new ArrayList<>();
        mAdapter = new ProductAdapter(getActivity(), mProducts);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ProductsFragment.this);


        db.collection("Companies")
                .document(email)
                .collection("Products")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            mProducts.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                Product product = documentSnapshot.toObject(Product.class);
                                mProducts.add(product);
                            }

                            Collections.sort(mProducts, new Helper.sortProductsByName());

                            mAdapter.notifyDataSetChanged();
                            mProgressCircle.setVisibility(View.INVISIBLE);
                        } else {
                            mProgressCircle.setVisibility(View.INVISIBLE);
                            mProducts.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
        });

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ADMIN_ADD_PRODUCT.class));
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getActivity(), "long press to show options", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteProduct(int position) {
        Product product = mProducts.get(position);

        deleteProduct(product);
    }

    @Override
    public void onEditProduct(int position) {
        Product product = mProducts.get(position);

        Gson gson = new Gson();
        String json = gson.toJson(product);

        Intent intent = new Intent(getActivity(), ADMIN_EDIT_PRODUCT.class);
        intent.putExtra("JSON", json);
        startActivity(intent);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.product_sort, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_product_sort_by_name:
                Collections.sort(mProducts, new Helper.sortProductsByName());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.item_product_sort_by_cost:
                Collections.sort(mProducts, new Helper.sortProductsByCost());
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }

    private void deleteProduct(Product product) {
        final String productName = product.getName();

        StorageReference storageReference = mStorage.getReferenceFromUrl(product.getImageUrl());
        //delete from storage
        storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // get the document of the product that needs to be deleted
                    db.collection("Companies").document(email).collection("Products").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = null;
                                for (DocumentSnapshot currentDocumentSnapshot : task.getResult()) {
                                    Product currProduct = currentDocumentSnapshot.toObject(Product.class);
                                    if (currProduct.getName().equals(productName)) {
                                        // this is the document that needs to be deleted
                                        documentSnapshot = currentDocumentSnapshot;
                                    }
                                }
                                // deletion....
                                db.collection("Companies")
                                        .document(email)
                                        .collection("Products")
                                        .document(documentSnapshot.getId())
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
                    });
                }
            }
        });
    }
}
