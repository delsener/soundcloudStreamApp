package ch.zhaw.seminar.soundcloud.stream.app.ui.fragments;

import ch.zhaw.seminar.soundcloud.stream.app.ui.tasks.TrackListUpdateListener;

public interface SCFragment extends TrackListUpdateListener {

	public void selectAll(boolean select);
	
}
