package ch.zhaw.seminar.soundcloud.stream.app.ui.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListType;
import ch.zhaw.seminar.soundcloud.stream.app.ui.activities.MainNavigationActivity;
import ch.zhaw.seminar.soundcloud.stream.app.ui.adapter.BaseTrackListUpdateListener;
import ch.zhaw.seminar.soundcloud.stream.app.ui.adapter.TrackListAdapter;
import ch.zhaw.seminar.soundcloud.stream.app.ui.tasks.TrackDeletionAsyncTask;
import ch.zhaw.seminar.soundcloud.stream.app.ui.tasks.TrackListUpdateListener;
import ch.zhaw.seminar.soundcloud.stream.app.ui.views.TrackListView;
import ch.zhaw.seminar.soundcloudStreamApp.R;

public class DownloadedTrackListFragment extends Fragment implements SCFragment {

	private TrackDeletionAsyncTask deletionTask;
	private TrackListAdapter adapter;
	private TrackListUpdateListener updateListener;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TrackListType type = (TrackListType) getArguments().get("type");

		View rootView = inflater.inflate(R.layout.downloaded_list_layout,
				container, false);
		final TrackListView listview = (TrackListView) rootView
				.findViewById(R.id.downloaded_list_view);
		
		if (adapter == null) {
			adapter = new TrackListAdapter(R.id.downloaded_list_view, inflater, R.layout.downloaded_list_item_layout);
			adapter.addAll(MainNavigationActivity.activity
					.getTracklistItems(type));
		}
		listview.setAdapter(adapter);

		if (updateListener == null) {
			updateListener = new BaseTrackListUpdateListener(adapter, type);
		}
		
		// add button listener
		Button downloadButton = (Button) rootView
				.findViewById(R.id.button_delete_tracks);
		if (downloadButton != null) {
			downloadButton.setClickable(true);
			downloadButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					delete();
				}
			});
		}
		
		return rootView;
	}

	public void delete() {
		if ((deletionTask != null && deletionTask.getStatus() != Status.FINISHED)
				|| adapter.getCount() == 0) {
			return;
		}
		
		List<TrackListItem> items = new ArrayList<TrackListItem>();
		for (int i = 0; i < adapter.getCount(); i++) {
			TrackListItem item = adapter.getItem(i);
			if (!item.isSelected()) {
				continue;
			}
			items.add(item);
		}
		
		if (items.isEmpty()) {
			return;
		}
		
		deletionTask = new TrackDeletionAsyncTask(adapter.getContext());
		deletionTask.execute(items.toArray(new TrackListItem[items.size()]));
	}
	
	@Override
	public void selectAll(boolean select) {
		for (int i = 0; i < adapter.getCount(); i++) {
			TrackListItem item = adapter.getItem(i);
			item.setSelected(select);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void reloaded(Map<TrackListType, List<TrackListItem>> items) {
		if (updateListener != null) {
			updateListener.reloaded(items);
		}
	}

	@Override
	public void artworksUpdated(Map<TrackListType, List<TrackListItem>> items) {
		if (updateListener != null) {
			updateListener.artworksUpdated(items);
		}
	}
}
