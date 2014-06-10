package ch.zhaw.seminar.soundcloud.stream.app.ui.adapter;

import java.util.List;
import java.util.Map;

import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListType;
import ch.zhaw.seminar.soundcloud.stream.app.ui.tasks.TrackListUpdateListener;

public class BaseTrackListUpdateListener implements TrackListUpdateListener {

	private final TrackListAdapter adapter;
	private final TrackListType type;

	public BaseTrackListUpdateListener(TrackListAdapter adapter, TrackListType type) {
		this.adapter = adapter;
		this.type = type;
	}
	
	@Override
	public void reloaded(Map<TrackListType, List<TrackListItem>> items) {
		if (adapter.getCount() != 0) {
			adapter.clear();
		}
		
		List<TrackListItem> tracklistItems = items.get(type);
		if (tracklistItems == null) {
			return;
		}
		
		for (TrackListItem trackListItem : tracklistItems) {
			adapter.add(trackListItem);
		}
		
		adapter.notifyDataSetChanged();
	}

	@Override
	public void artworksUpdated(Map<TrackListType, List<TrackListItem>> items) {
		if (adapter.getCount() == 0) {
			return;
		}
		
		for (int i = 0; i < adapter.getCount(); i++) {
			TrackListItem adapterItem = adapter.getItem(i);
			if (items.size() < (i+1) || items.get(type) == null || items.get(type).get(i) == null) {
				continue;
			}
			
			adapterItem.setArtwork(items.get(type).get(i).getArtwork());
		}
		
		adapter.notifyDataSetChanged();
	}

}
