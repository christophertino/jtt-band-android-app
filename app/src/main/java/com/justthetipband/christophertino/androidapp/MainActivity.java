package com.justthetipband.christophertino.androidapp;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedHashMap;

/**
 * Main Activity
 *
 * @author Christopher Tino
 */
public class MainActivity extends ActionBarActivity { //ActionBarActivity extends from FragmentActivity
	private static final String TAG = "MainActivity";
	private static final int NUM_FRAGMENTS = 4; //how many fragments are we making?
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		CustomFragmentAdapter mAdapter = new CustomFragmentAdapter(getSupportFragmentManager());

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);

		//create hash map of menu titles and icons
		String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
		String[] drawerIcons = getResources().getStringArray(R.array.drawer_icons);
		LinkedHashMap<String, String> drawerMap = new LinkedHashMap<>();
		for(int i= 0; i < drawerTitles.length; i++){
			drawerMap.put(drawerTitles[i], drawerIcons[i]);
		}

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START); //shadow overlay
		drawerList.setAdapter(new NavDrawerAdapter(this, drawerMap));
		drawerList.setOnItemClickListener(new DrawerItemClickListener());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true); //let actionBar icon toggle drawer
		getSupportActionBar().setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(mAdapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		drawerToggle.onConfigurationChanged(newConfig);
	}

	/*
	 * DrawerItemClickListener
	 * Sets click listener for list view within the nav drawer
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			if (drawerLayout.isDrawerOpen(drawerList)) {
				drawerLayout.closeDrawer(drawerList);
			}
			//use FragmentPagerAdapter to swap current fragment
			viewPager.setCurrentItem(position);
		}
	}

	/*
	 * Build the Fragment Pager Adapter
	 * This is called from onCreate() and each time the fragment changes
	 * Some methods override from PagerAdapter parent class
	 * NOTE: On first load, FragmentPagerAdapter calls getItem(0) and getItem(1) by default
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
			//Based on the position, we call a particular fragment
			Fragment returnFrag = null;
			switch (index) {
				case 0:
					returnFrag = WebsiteBlogPostsFragment.newInstance(index);
					break;
				case 1:
					returnFrag = ScheduleFragment.newInstance(index);
					break;
				case 2:
					returnFrag = YouTubeFragment.newInstance(index);
					break;
				case 3:
					returnFrag = TwitterFragment.newInstance(index);
					break;
			}
			return returnFrag;
		}
	}

	/*
	 * NavDrawerAdapter
	 * Custom BaseAdapter used to populate the navigation drawer
	 * @param   HashMap<String, String>
	 * @return  Rendered content in drawer_list_item
	 */
	private static class NavDrawerAdapter extends BaseAdapter {
		private LinkedHashMap<String, String> hashMap;
		private String[] titles;
		private Context mContext;

		private static class ViewHolder {
			TextView drawerItemText;
			ImageView drawerItemIcon;
		}

		public NavDrawerAdapter(Context context, LinkedHashMap<String, String> hashMap) {
			this.mContext = context;
			this.hashMap  = hashMap;
			this.titles = this.hashMap.keySet().toArray(new String[hashMap.size()]);
		}

		@Override
		public int getCount() {
			return hashMap.size();
		}

		@Override
		public Object getItem(int position) {
			return hashMap.get(titles[position]);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			String title = titles[position];
			String icon = getItem(position).toString();

			ViewHolder viewHolder;

			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.drawer_list_item, parent, false);
				viewHolder.drawerItemText = (TextView) convertView.findViewById(R.id.drawer_item_text);
				viewHolder.drawerItemIcon = (ImageView) convertView.findViewById(R.id.drawer_item_icon);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.drawerItemText.setText(title);
			viewHolder.drawerItemIcon.setImageResource(mContext.getResources().getIdentifier(icon, "drawable", mContext.getPackageName()));

			// Return the completed view to render on screen
			return convertView;
		}
	}
}
