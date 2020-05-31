package com.rubisoft.bisexcuddles.FrameLayouts;

/* Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.rubisoft.bisexcuddles.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A layout that draws something in the insets passed to {@link #fitSystemWindows(Rect)}, i.e. the area above UI chrome
 * (status and navigation bars, overlay action bars).
 */
public class ScrimInsetsFrameLayout extends FrameLayout {
    private final Rect mTempRect = new Rect();
    @Nullable
    private Drawable mInsetForeground;
    private Rect mInsets;

	public ScrimInsetsFrameLayout(@NonNull Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScrimInsetsFrameLayout(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScrimInsetsFrameLayout(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs, defStyle);
    }

    private void init(@NonNull Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrimInsetsFrameLayout, defStyle, 0);
        if (a == null) {
            return;
        }
        this.mInsetForeground = a.getDrawable(R.styleable.ScrimInsetsView_insetForeground2);
        a.recycle();

        this.setWillNotDraw(true);
    }

   /* @Override
    protected boolean fitSystemWindows(Rect insets) {
        this.mInsets = new Rect(insets);
        this.setWillNotDraw(this.mInsetForeground == null);
        ViewCompat.postInvalidateOnAnimation(this);
        if (this.mOnInsetsCallback != null) {
            this.mOnInsetsCallback.onInsetsChanged(insets);
        }
        return true; // consume insets
    }
*/
    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();
        if (this.mInsets != null && this.mInsetForeground != null) {
            int sc = canvas.save();
            canvas.translate(this.getScrollX(), this.getScrollY());

            // Top
            this.mTempRect.set(0, 0, width, this.mInsets.top);
            this.mInsetForeground.setBounds(this.mTempRect);
            this.mInsetForeground.draw(canvas);

            // Bottom
            this.mTempRect.set(0, height - this.mInsets.bottom, width, height);
            this.mInsetForeground.setBounds(this.mTempRect);
            this.mInsetForeground.draw(canvas);

            // Left
            this.mTempRect.set(0, this.mInsets.top, this.mInsets.left, height - this.mInsets.bottom);
            this.mInsetForeground.setBounds(this.mTempRect);
            this.mInsetForeground.draw(canvas);

            // Right
            this.mTempRect.set(width - this.mInsets.right, this.mInsets.top, width, height - this.mInsets.bottom);
            this.mInsetForeground.setBounds(this.mTempRect);
            this.mInsetForeground.draw(canvas);

            canvas.restoreToCount(sc);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mInsetForeground != null) {
            this.mInsetForeground.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mInsetForeground != null) {
            this.mInsetForeground.setCallback(null);
        }
    }

	private interface OnInsetsCallback {
        void onInsetsChanged(Rect insets);
    }
}
