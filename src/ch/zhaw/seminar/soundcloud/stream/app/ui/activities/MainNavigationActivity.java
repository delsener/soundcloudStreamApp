package ch.zhaw.seminar.soundcloud.stream.app.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListType;
import ch.zhaw.seminar.soundcloud.stream.app.ui.fragments.DownloadedTrackListFragment;
import ch.zhaw.seminar.soundcloud.stream.app.ui.fragments.SCFragment;
import ch.zhaw.seminar.soundcloud.stream.app.ui.fragments.SetListFragment;
import ch.zhaw.seminar.soundcloud.stream.app.ui.fragments.TrackListFragment;
import ch.zhaw.seminar.soundcloud.stream.app.ui.tasks.TrackListUpdateListener;
import ch.zhaw.seminar.soundcloud.stream.app.ui.tasks.TrackListUpdaterAsyncTask;
import ch.zhaw.seminar.soundcloudStreamApp.R;

public class MainNavigationActivity extends FragmentActivity {

	public static MainNavigationActivity activity;
	
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	private final List<Fragment> fragments = new ArrayList<Fragment>();
	private final SparseBooleanArray lastSelectionMap = new SparseBooleanArray();
	private TrackListUpdaterAsyncTask currentUpdateTask = null;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		
		setContentView(R.layout.main_navigation);

		fragments.add(new TrackListFragment());
		fragments.add(new SetListFragment());
		
		Bundle args1 = new Bundle();
		args1.putSerializable("type", TrackListType.DOWNLOADED_LOCAL);
		DownloadedTrackListFragment downloadedLocalListFragment = new DownloadedTrackListFragment();
		downloadedLocalListFragment.setArguments(args1);
		fragments.add(downloadedLocalListFragment);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		updateTrackList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sc_main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (R.id.action_refresh == item.getItemId()) {
			updateTrackList();
			return true;
		} else if (R.id.action_select_all == item.getItemId()) {
			int currentItem = mViewPager.getCurrentItem();
			SCFragment currentFragment = (SCFragment) fragments.get(currentItem);
			
			Boolean lastSelection = lastSelectionMap.get(currentItem);
			if (lastSelection == null) {
				lastSelection = Boolean.FALSE;
			}
			
			currentFragment.selectAll(!lastSelection);
			lastSelectionMap.put(currentItem, !lastSelection);
			return true;
		}  else if (R.id.action_settings == item.getItemId()) {
			Intent myIntent = new Intent(MainNavigationActivity.this, SettingsActivity.class);
			MainNavigationActivity.this.startActivity(myIntent);
		}
		
		return false;
	}
	
	public void updateTrackList() {
		if (currentUpdateTask != null
				&& currentUpdateTask.getStatus() != Status.FINISHED) {
			return;
		}
		currentUpdateTask = new TrackListUpdaterAsyncTask(mViewPager.getContext());
		
		for (Fragment fragment : fragments) {
			if (!(fragment instanceof TrackListUpdateListener)) {
				continue;
			}
			currentUpdateTask.addUpdateListener((TrackListUpdateListener) fragment);
		}
		
		currentUpdateTask.execute();
	}
	
	public List<TrackListItem> getTracklistItems(TrackListType type) {
		if (currentUpdateTask != null
				&& currentUpdateTask.getStatus() == Status.FINISHED) {
			List<TrackListItem> items = currentUpdateTask.getTrackListItems().get(type);
			if (items == null) {
				return new ArrayList<TrackListItem>(0);
			}
			return items;
		}
		return new ArrayList<TrackListItem>(0);
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}
		
		@Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
		
		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getResources().getString(R.string.navigation_tracks);
			case 1:
				return getResources().getString(R.string.navigation_sets);
			case 2:
				return getResources().getString(R.string.navigation_downloads_local);
			}
			return null;
		}
	}
}
