package com.reStock.app;

import android.content.Context;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

//Product adapter
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Product> mProducts;
    private com.reStock.app.ProductAdapter.OnItemClickListener mListener;
    private CircularProgressDrawable circularProgressDrawable;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public ProductAdapter(Context context, List<Product> Products) {
        mContext = context;
        mProducts = Products;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, int position) {
        circularProgressDrawable = new CircularProgressDrawable(mContext);
        circularProgressDrawable.start();

        Product ProductCurrent = mProducts.get(position);
        holder.textViewName.setText(ProductCurrent.getName());
        holder.textViewCost.setText("Cost/Unit: " + ProductCurrent.getCost() + "$");
        holder.textViewUnits.setText("Units/Package: " + ProductCurrent.getUnits_per_package());

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

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener{
        public TextView textViewName;
        public TextView textViewCost;
        public TextView textViewUnits;
        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewCost = itemView.findViewById(R.id.text_view_cost);
            textViewUnits = itemView.findViewById(R.id.text_view_units);
            imageView = itemView.findViewById(R.id.image_view_product);
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

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuItem edit = menu.add(Menu.NONE, 1, 1, "edit");
            MenuItem delete = menu.add(Menu.NONE, 2, 2, "delete");

            edit.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    switch (item.getItemId()) {
                        case 1:
                            mListener.onEditProduct(position);
                            return true;
                        case 2:
                            mListener.onDeleteProduct(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onEditProduct(int position);

        void onDeleteProduct(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
