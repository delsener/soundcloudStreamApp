package ch.zhaw.seminar.soundcloud.stream.app.models;

import android.graphics.drawable.Drawable;

public class TrackListItem {

	private final int id;
	private final String title;
	private final String artist;
	private final String artworkURL;
	
	private String streamURL;
	
	private Drawable artwork;
	private boolean selected;
	
	private final TrackListType trackListType;

	private String soundcloudTrackURL;
	
	public TrackListItem(int id, TrackListType trackListType, String title, String artist, String artworkURL) {
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.artworkURL = artworkURL;
		this.trackListType = trackListType;
	}

	public String getTitle() {
		return title;
	}
	
	public String getArtist() {
		return artist;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Drawable getArtwork() {
		return artwork;
	}

	public void setArtwork(Drawable artwork) {
		this.artwork = artwork;
	}

	public String getArtworkURL() {
		return artworkURL;
	}

	public int getId() {
		return id;
	}

	public String getStreamURL() {
		return streamURL;
	}

	public void setStreamURL(String streamURL) {
		this.streamURL = streamURL;
	}

	public TrackListType getTrackListType() {
		return trackListType;
	}

	public String getSoundcloudTrackURL() {
		return soundcloudTrackURL;
	}

	public void setSoundcloudTrackURL(String soundcloudTrackURL) {
		this.soundcloudTrackURL = soundcloudTrackURL;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrackListItem other = (TrackListItem) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
