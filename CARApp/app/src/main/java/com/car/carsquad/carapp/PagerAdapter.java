package com.car.carsquad.carapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                RiderRequestedFragment riderRequestedFrag = new RiderRequestedFragment();
                return riderRequestedFrag;
            case 1:
                RiderAcceptedFragment riderAcceptedFrag = new RiderAcceptedFragment();
                return riderAcceptedFrag;
            case 2:
                RiderCompletedFragment riderCompletedFrag = new RiderCompletedFragment();
                return riderCompletedFrag;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

