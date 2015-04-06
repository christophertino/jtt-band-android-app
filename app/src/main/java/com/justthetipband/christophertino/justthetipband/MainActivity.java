package com.justthetipband.christophertino.justthetipband;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;

/**
 * Main Activity
 *
 * @author Christopher Tino
 */
public class MainActivity extends FragmentActivity implements OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int NUM_FRAGMENTS = 4; //how many fragments are we making?
    private CustomFragmentAdapter mAdapter;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAdapter = new CustomFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        //set click bindings for nav menu
        Button buttonBlog = (Button) findViewById(R.id.tab_blog);
        Button buttonSchedule = (Button) findViewById(R.id.tab_schedule);
        Button buttonVideos = (Button) findViewById(R.id.tab_videos);
        Button buttonTwitter = (Button) findViewById(R.id.tab_twitter);
        buttonBlog.setOnClickListener(this);
        buttonSchedule.setOnClickListener(this);
        buttonVideos.setOnClickListener(this);
        buttonTwitter.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Integer viewID = v.getId();
        //Route nav menu items to appropriate fragment
        switch (viewID) {
            case R.id.tab_blog:
                mPager.setCurrentItem(0);
                break;
            case R.id.tab_schedule :
                mPager.setCurrentItem(1);
                break;
            case R.id.tab_videos :
                mPager.setCurrentItem(2);
                break;
            case R.id.tab_twitter :
                mPager.setCurrentItem(3);
                break;
        }
    }

    /*
     * Build the Fragment Pager Adapter
     * This is called onCreate() and each time the fragment changes
     * Some methods overriden from PagerAdapter parent class
     */
    public static class CustomFragmentAdapter extends FragmentPagerAdapter {
        public CustomFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }

        @Override
        public Fragment getItem(int index) {
            Log.v(TAG, "index is " + index);
            Fragment returnFrag = null;
            //Based on the position, we call a particular fragment
            switch (index) {
                case 0:
                    returnFrag = WebsiteBlogPostsFragment.newInstance(index);
                    break;
            }
            //TODO: for now just show the only fragment we have
            return WebsiteBlogPostsFragment.newInstance(index);
        }
    }

}
