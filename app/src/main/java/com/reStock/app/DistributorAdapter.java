package com.reStock.app;

import android.content.Context;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class DistributorAdapter extends RecyclerView.Adapter<DistributorAdapter.DistributorViewHolder> {
    private Context mContext;
    private List<Distributor> mDistributor;
    private com.reStock.app.DistributorAdapter.OnItemClickListener mListener;
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    public DistributorAdapter(Context context, List<Distributor> distributors) {
        mContext = context;
        mDistributor = distributors;
    }

    @Override
    public DistributorAdapter.DistributorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.distributor_item, parent, false);
        return new DistributorAdapter.DistributorViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final DistributorAdapter.DistributorViewHolder holder, int position) {
        final Distributor distributorCurrent = mDistributor.get(position);
        holder.Name.setText(distributorCurrent.getName());
        holder.Email.setText(distributorCurrent.getEmail());

        final StorageReference storageReference = storage.getReferenceFromUrl(distributorCurrent.getProfile());

        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                CircularProgressDrawable circularProgressDrawable;
                circularProgressDrawable = new CircularProgressDrawable(mContext);
                circularProgressDrawable.start();

                GlideApp.with(mContext)
                        .load(storageReference)
                        .fitCenter()
                        //.signature(new ObjectKey(storageMetadata.getCreationTimeMillis()))
                        .placeholder(circularProgressDrawable)
                        .into(holder.Profile);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDistributor.size();
    }

    public class DistributorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView Name;
        public TextView Email;
        public ImageView Profile;

        public DistributorViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            //itemView.setOnCreateContextMenuListener(this);

            Name = itemView.findViewById(R.id.textViewDistributorName);
            Email = itemView.findViewById(R.id.textViewDistributorEmail);
            Profile = itemView.findViewById(R.id.imageViewDistributorProfilePicture);
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

    public void setOnItemClickListener(DistributorAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
}
