package com.rubisoft.womenradar.Adapters;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

public class ViewPagerTabs_Adapter extends FragmentStatePagerAdapter {
    //Crea el ViewPagerTabs_Adapter necesario para los fragments en tabs
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerTabs_Adapter(FragmentManager manager) {
        super(manager);
    }

    @NonNull
	@Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public String getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}