package com.rubisoft.gayradar.Classes;

import android.graphics.drawable.Drawable;

public class Drawer_Item {

    private final Drawable ItemIcon;
    private final String ItemTitle;

    public Drawer_Item(Drawable un_itemIcon, String un_itemTitle) {

		this.ItemIcon = un_itemIcon;
		this.ItemTitle = un_itemTitle;

	}

    public Drawable getItemIcon() {
        return this.ItemIcon;
    }

    public String getItemTitle() {
        return this.ItemTitle;
    }
}