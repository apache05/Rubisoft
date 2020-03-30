package com.rubisoft.menradar.RecyclersViews;

import android.R.attr;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


//Esta modificación de RecyclerView permite que el número de columnas del grid sea adaptativo
public class RecyclerView_AutoFit extends RecyclerView {
    private GridLayoutManager manager;
    private int columnWidth = -1;

    public RecyclerView_AutoFit(@NonNull Context context) {
        super(context);
        this.init(context, null);
    }

    public RecyclerView_AutoFit(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public RecyclerView_AutoFit(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {
                    attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            this.columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }

        this.manager = new GridLayoutManager(this.getContext(), 1);
        this.setLayoutManager(this.manager);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (this.columnWidth > 0) {
            int spanCount = Math.max(1, this.getMeasuredWidth() / this.columnWidth);
            this.manager.setSpanCount(spanCount);
        }
    }
}
