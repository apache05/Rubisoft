package com.rubisoft.menradar.ViewPagers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

//Esto es un viewpager especial que utilizamos en layout_amigos que desactiva el scroll horizontal para pasar de un tab a otro.
//lo utilizamos para que dentro pueda haber un recyclerview y podamos hacer fling con los perfiles
public class Viewpager_Especial extends ViewPager {

    private boolean isPagingEnabled;

    public Viewpager_Especial(Context context) {
        super(context);

    }

    public Viewpager_Especial(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        return isPagingEnabled && super.onTouchEvent(event);
    }
    @Override
    public boolean performClick() {
        // Calls the super implementation, which generates an AccessibilityEvent
        // and calls the onClick() listener on the view, if any
        super.performClick();

        // Handle the action for the custom click here

        return true;
    }
    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        return isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        isPagingEnabled = b;
    }
}