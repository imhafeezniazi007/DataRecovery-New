package com.example.datarecovery.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.datarecovery.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

private static class GridViewHolder extends RecyclerView.ViewHolder {

    TextView txt_label;

    public GridViewHolder(View itemView) {
        super(itemView);
//        txt_label = (TextView) itemView.findViewById(R.id.txt_label);
    }

}

private class ListViewHolder extends RecyclerView.ViewHolder {

    TextView txt_label;

    public ListViewHolder(View itemView) {
        super(itemView);
//        txt_label = (TextView) itemView.findViewById(R.id.txt_label);
    }

}

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private List<AbstractItem> mItems;

    public  MyAdapter(Context context, List<AbstractItem> items) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == AbstractItem.GRID_TYPE) {
            View itemView = mLayoutInflater.inflate(R.layout.item_image_layout, parent, false);
            // work here if you need to control height of your items
            // keep in mind that parent is RecyclerView in this case
            int height = parent.getMeasuredHeight() / 4;
            itemView.setMinimumHeight(height);
            return new GridViewHolder(itemView);
        } else {
            View itemView = mLayoutInflater.inflate(R.layout.view_more_layout, parent, false);
            return new ListViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        int type = getItemViewType(position);
        if (type == AbstractItem.GRID_TYPE) {
            GridItem item = (GridItem) mItems.get(position);
            GridViewHolder holder = (GridViewHolder) viewHolder;
//            holder.txt_label.setText(item.getLabel());
        } else {
            ListItem item = (ListItem) mItems.get(position);
            ListViewHolder holder = (ListViewHolder) viewHolder;
//            holder.txt_label.setText(item.getLabel());
        }
    }
}
