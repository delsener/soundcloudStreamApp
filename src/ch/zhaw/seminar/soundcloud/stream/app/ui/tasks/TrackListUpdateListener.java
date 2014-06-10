package ch.zhaw.seminar.soundcloud.stream.app.ui.tasks;

import java.util.List;
import java.util.Map;

import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListType;

public interface TrackListUpdateListener {

	public void reloaded(Map<TrackListType, List<TrackListItem>> items);
	
	public void artworksUpdated(Map<TrackListType, List<TrackListItem>> items);
	
}
