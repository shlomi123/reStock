package com.reStock.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoreOrdersFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar mProgressCircle;
    private SharedPreferences sharedPreferences;
    private RecyclerView mRecyclerView;
    private StoreOrderAdapter mAdapter;
    private List<Order> mOrders;
    private String store_email;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.store_orders_fragment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        store_email = sharedPreferences.getString("STORE_EMAIL", null);
        mRecyclerView = view.findViewById(R.id.recycler_view_store_orders);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProgressCircle = view.findViewById(R.id.progress_circle_store_orders);
        mOrders = new ArrayList<>();
        mAdapter = new StoreOrderAdapter(getContext(), mOrders);
        mRecyclerView.setAdapter(mAdapter);

        db.collection("Stores")
                .document(store_email)
                .collection("Orders")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            mOrders.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                Order order = documentSnapshot.toObject(Order.class);
                                mOrders.add(order);
                            }

                            Collections.sort(mOrders, new Helper.sortOrdersByDate());

                            mAdapter.notifyDataSetChanged();
                            mProgressCircle.setVisibility(View.INVISIBLE);
                        } else {
                            Toast.makeText(getContext(), "No orders made", Toast.LENGTH_SHORT).show();
                            mProgressCircle.setVisibility(View.INVISIBLE);
                            mOrders.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.orders_sort, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_orders_sort_by_distributor:
                Collections.sort(mOrders, new Helper.sortOrdersByDistributor());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.item_orders_sort_by_name:
                Collections.sort(mOrders, new Helper.sortOrdersByProductName());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.item_orders_sort_by_cost:
                Collections.sort(mOrders, new Helper.sortOrdersByTotalCost());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.item_orders_sort_by_date:
                Collections.sort(mOrders, new Helper.sortOrdersByDate());
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }
}