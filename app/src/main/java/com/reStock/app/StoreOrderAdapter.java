package com.reStock.app;

import android.content.Context;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class StoreOrderAdapter extends RecyclerView.Adapter<StoreOrderAdapter.OrderViewHolder> {
    private Context mContext;
    private List<Order> mOrders;
    private StoreOrderAdapter.OnItemClickListener mListener;
    private CircularProgressDrawable circularProgressDrawable;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public StoreOrderAdapter(Context context, List<Order> orders) {
        mContext = context;
        mOrders = orders;
    }

    @Override
    public StoreOrderAdapter.OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.order_item, parent, false);
        return new StoreOrderAdapter.OrderViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final StoreOrderAdapter.OrderViewHolder holder, int position) {
        circularProgressDrawable = new CircularProgressDrawable(mContext);
        circularProgressDrawable.start();

        final Order OrderCurrent = mOrders.get(position);

        holder.Name.setText(OrderCurrent.get_product());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        holder.Date.setText("Date: " + simpleDateFormat.format(OrderCurrent.get_date()));
        holder.Quantity.setText("Quantity: " + String.valueOf(OrderCurrent.get_quantity()));
        holder.Distributor.setText("Supplier: " + OrderCurrent.get_distributor());
        holder.TotalCost.setText("Total Cost: " + String.format("%.2f", OrderCurrent.get_total_cost()) + "$");

        StorageReference storageReference = storage.getReferenceFromUrl(OrderCurrent.get_url());
        GlideApp.with(mContext)
                .load(storageReference)
                .fitCenter()
                .placeholder(circularProgressDrawable)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView Name;
        public TextView Date;
        public TextView Quantity;
        public TextView Distributor;
        public TextView TotalCost;
        public ImageView imageView;


        public OrderViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            Name = itemView.findViewById(R.id.textViewProductName2);
            Date = itemView.findViewById(R.id.textViewProductOrderDate);
            Quantity = itemView.findViewById(R.id.textViewOrderQuantity);
            imageView = itemView.findViewById(R.id.imageViewProductImage);
            Distributor = itemView.findViewById(R.id.textViewOrderDistributor);
            TotalCost = itemView.findViewById(R.id.textViewOrderTotalCost);
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

    public void setOnItemClickListener(StoreOrderAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
}
