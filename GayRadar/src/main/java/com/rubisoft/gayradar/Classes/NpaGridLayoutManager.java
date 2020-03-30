package com.rubisoft.gayradar.Classes;


import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * No Predictive Animations GridLayoutManager
 * Esta LayoutManager es necesario para que no pete al hacer reload sobre el RecyclerView GridLayout
 * Cuando se solucione el bug, dejar de utilizar Managers para que las imágenes se ajusten a la pantalla
 * y también podré quitar el itemdecoration
 */
public class NpaGridLayoutManager extends GridLayoutManager {
    public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NpaGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
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
