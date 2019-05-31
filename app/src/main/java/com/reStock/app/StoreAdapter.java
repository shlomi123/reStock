package com.reStock.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private Context mContext;
    private List<Store> mStores;
    private com.reStock.app.StoreAdapter.OnItemClickListener mListener;

    public StoreAdapter(Context context, List<Store> stores) {
        mContext = context;
        mStores = stores;
    }

    @Override
    public StoreAdapter.StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.store_item, parent, false);
        return new StoreAdapter.StoreViewHolder(v);

    }

    @Override
    public void onBindViewHolder(StoreAdapter.StoreViewHolder holder, int position) {
        final Store StoreCurrent = mStores.get(position);
        holder.Name.setText(StoreCurrent.get_name());
        holder.Email.setText(StoreCurrent.get_email());
        holder.button.setText(Character.toString(StoreCurrent.get_name().charAt(0)));
        holder.button.getBackground().setColorFilter(Color.parseColor(Helper.getRandomColor()), PorterDuff.Mode.LIGHTEN);
    }

    @Override
    public int getItemCount() {
        return mStores.size();
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener{
        public TextView Name;
        public TextView Email;
        public Button button;


        public StoreViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            Name = itemView.findViewById(R.id.textViewStoreName);
            Email = itemView.findViewById(R.id.textViewStoreEmail);
            button = itemView.findViewById(R.id.store_letter);
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
            MenuItem viewStore = menu.add(Menu.NONE, 1, 1, "view orders");
            MenuItem delete = menu.add(Menu.NONE, 2, 2, "delete");
            MenuItem excel = menu.add(Menu.NONE, 3, 3, "excel sheet");

            viewStore.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
            excel.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {

                    switch (item.getItemId()) {
                        case 1:
                            mListener.onViewStore(position);
                            return true;
                        case 2:
                            mListener.onDeleteStore(position);
                            return true;
                        case 3:
                            mListener.onExcelSheet(position);
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onViewStore(int position);

        void onDeleteStore(int position);

        void onExcelSheet(int position);
    }

    public void setOnItemClickListener(StoreAdapter.OnItemClickListener listener) {
        mListener = listener;
    }
}
