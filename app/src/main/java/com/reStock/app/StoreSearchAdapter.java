package com.reStock.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StoreSearchAdapter extends RecyclerView.Adapter<StoreSearchAdapter.StoreViewHolder> implements Filterable {
    private Context mContext;
    private List<Store> mStores;
    private List<Store> mStoresFull;
    private StoreSearchAdapter.OnItemClickListener mListener;

    public StoreSearchAdapter(Context context, List<Store> stores) {
        mContext = context;
        mStores = stores;
        mStoresFull = stores;
    }

    @Override
    public StoreSearchAdapter.StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.store_item, parent, false);
        return new StoreSearchAdapter.StoreViewHolder(v);

    }

    @Override
    public void onBindViewHolder(StoreSearchAdapter.StoreViewHolder holder, int position) {
        final Store StoreCurrent = mStoresFull.get(position);
        holder.Name.setText(StoreCurrent.get_name());
        holder.Email.setText(StoreCurrent.get_email());
        holder.Button.setText(Character.toString(StoreCurrent.get_name().charAt(0)));
        holder.Button.getBackground().setColorFilter(Color.parseColor(Helper.getRandomColor()), PorterDuff.Mode.LIGHTEN);
    }

    @Override
    public int getItemCount() {
        return mStoresFull.size();
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView Name;
        public TextView Email;
        public Button Button;


        public StoreViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            Name = itemView.findViewById(R.id.textViewStoreName);
            Email = itemView.findViewById(R.id.textViewStoreEmail);
            Button = itemView.findViewById(R.id.store_letter);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(mStoresFull.get(position));
                }
            }
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mStoresFull = mStores;
                } else {
                    List<Store> filteredList = new ArrayList<>();
                    for (Store row : mStores) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.get_name().toLowerCase().startsWith(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    mStoresFull = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mStoresFull;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mStoresFull = (ArrayList<Store>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface OnItemClickListener {
        void onItemClick(Store store);
    }

    public void setOnItemClickListener(StoreSearchAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
}
