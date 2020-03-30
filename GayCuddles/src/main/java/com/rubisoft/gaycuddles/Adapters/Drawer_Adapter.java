package com.rubisoft.gaycuddles.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rubisoft.gaycuddles.Adapters.Drawer_Adapter.ViewHolder;
import com.rubisoft.gaycuddles.Classes.Drawer_Item;
import com.rubisoft.gaycuddles.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Drawer_Adapter extends RecyclerView.Adapter<ViewHolder> {

    private final ArrayList<Drawer_Item> drawerItems;

    // Provide a suitable constructor (depends on the kind of dataset)
    public Drawer_Adapter(ArrayList<Drawer_Item> mDrawerItems) {
		this.drawerItems = mDrawerItems;
	}

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TextView itemTitle = holder.itemView.findViewById(R.id.Drawer_item_TextView_title);
        ImageView itemIcon = holder.itemView.findViewById(R.id.Drawer_item_ImageView);

        itemTitle.setText(this.drawerItems.get(position).getItemTitle());
        itemIcon.setImageDrawable(this.drawerItems.get(position).getItemIcon());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return this.drawerItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View view) {
            super(view);
        }
    }
}