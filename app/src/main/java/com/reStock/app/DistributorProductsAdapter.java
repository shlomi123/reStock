package com.reStock.app;

import android.content.Context;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class DistributorProductsAdapter extends RecyclerView.Adapter<DistributorProductsAdapter.DistributorProductsHolder> {
    private Context mContext;
    private List<Product> mProducts;
    private com.reStock.app.DistributorProductsAdapter.OnItemClickListener mListener;
    private CircularProgressDrawable circularProgressDrawable;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public DistributorProductsAdapter(Context context, List<Product> Products) {
        mContext = context;
        mProducts = Products;
    }

    @Override
    public DistributorProductsAdapter.DistributorProductsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new DistributorProductsHolder(v);
    }

    @Override
    public void onBindViewHolder(final DistributorProductsHolder holder, int position) {
        circularProgressDrawable = new CircularProgressDrawable(mContext);
        circularProgressDrawable.setStrokeWidth(10f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        Product ProductCurrent = mProducts.get(position);
        holder.textViewName.setText(ProductCurrent.getName());
        holder.textViewCost.setText("Cost/Unit: " + ProductCurrent.getCost() + "$");
        holder.textViewUnits.setText("Units/Case: " + ProductCurrent.getUnits_per_package());
        holder.imageView.setVisibility(View.INVISIBLE);

        final StorageReference storageReference = storage.getReferenceFromUrl(ProductCurrent.getImageUrl());
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                try{
                    GlideApp.with(mContext)
                            .load(storageReference)
                            .fitCenter()
                            .signature(new ObjectKey(storageMetadata.getCreationTimeMillis()))
                            .placeholder(circularProgressDrawable)
                            .into(holder.imageView);

                    holder.placeholder.setVisibility(View.INVISIBLE);
                    holder.imageView.setVisibility(View.VISIBLE);
                }catch(Exception e){
                    Log.d("blaaaa", e.getMessage());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    public class DistributorProductsHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textViewName;
        public TextView textViewCost;
        public TextView textViewUnits;
        public ImageView imageView;
        public ImageView placeholder;

        public DistributorProductsHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewCost = itemView.findViewById(R.id.text_view_cost);
            textViewUnits = itemView.findViewById(R.id.text_view_units);
            imageView = itemView.findViewById(R.id.image_view_product);
            placeholder = itemView.findViewById(R.id.image_view_product_placeholder);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(DistributorProductsAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
}
