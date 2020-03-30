package com.rubisoft.womencuddles.Classes;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;

/**
 * No Predictive Animations GridLayoutManager
 * Esta LayoutManager es necesario para que no pete al hacer reload sobre el RecyclerView LinearLayout
 */
public class NpaLinearLayoutManager extends LinearLayoutManager {
    public NpaLinearLayoutManager(Context context) {
        super(context);
    }

    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}