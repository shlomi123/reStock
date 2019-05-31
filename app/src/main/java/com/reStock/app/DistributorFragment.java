package com.reStock.app;

import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DistributorFragment extends Fragment implements DistributorAdapter.OnItemClickListener{

    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private RecyclerView mRecyclerView;
    private DistributorAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private List<Distributor> mDistributors;
    private String store_email;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.distributor_fragment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        store_email = sharedPreferences.getString("STORE_EMAIL", null);

        mProgressCircle = view.findViewById(R.id.progress_circle_distributors);

        mRecyclerView = view.findViewById(R.id.recycler_view_distributors);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDistributors = new ArrayList<>();
        mAdapter = new DistributorAdapter(getActivity(), mDistributors);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(DistributorFragment.this);


        db.collection("Stores")
                .document(store_email)
                .collection("Distributors")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            mDistributors.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                Distributor distributor = documentSnapshot.toObject(Distributor.class);
                                mDistributors.add(distributor);
                            }

                            Collections.sort(mDistributors, new Helper.sortDistributorByName());

                            mAdapter.notifyDataSetChanged();
                            mProgressCircle.setVisibility(View.INVISIBLE);
                        } else {
                            mProgressCircle.setVisibility(View.INVISIBLE);
                            mDistributors.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.distributor_sort, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_distributor_sort_by_name:
                Collections.sort(mDistributors, new Helper.sortDistributorByName());
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.item_distributor_sort_by_email:
                Collections.sort(mDistributors, new Helper.sortDistributorByEmail());
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }

    @Override
    public void onItemClick(int position) {
        Distributor distributor = mDistributors.get(position);
        Intent intent = new Intent(getActivity(), STORE_SHOW_DISTRIBUTOR_PRODUCTS.class);
        intent.putExtra("DISTRIBUTOR_EMAIL", distributor.getEmail());
        intent.putExtra("DISTRIBUTOR_NAME", distributor.getName());
        startActivity(intent);
    }
}
