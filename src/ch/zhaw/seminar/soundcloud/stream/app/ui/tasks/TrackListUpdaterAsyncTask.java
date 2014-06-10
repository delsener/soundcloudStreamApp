package ch.zhaw.seminar.soundcloud.stream.app.ui.tasks;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListType;
import ch.zhaw.seminar.soundcloud.stream.app.utils.SoundcloudStreamAppUtil;

public class TrackListUpdaterAsyncTask extends
		AsyncTask<Void, TrackListItem, Void> {

	private static final String PREF_KEY_TARGET_PROFILE = "pref_target_profile";
	private static final String PREF_KEY_HOST = "pref_service_host";
	
	private Context context;
	private ProgressDialog processDialog;
	private final Map<TrackListType, List<TrackListItem>> trackListItems = new HashMap<TrackListType, List<TrackListItem>>();
	private final List<TrackListUpdateListener> listeners = new ArrayList<TrackListUpdateListener>();

	public TrackListUpdaterAsyncTask(Context context) {
		this.context = context;
		this.processDialog = new ProgressDialog(context);
	}

	@Override
	protected Void doInBackground(Void... params) {
		trackListItems.clear();

		String host = PreferenceManager.getDefaultSharedPreferences(
				context).getString(PREF_KEY_HOST, SoundcloudStreamAppUtil.SERVICE_URL);
		
		String targetProfile = PreferenceManager.getDefaultSharedPreferences(
				context).getString(PREF_KEY_TARGET_PROFILE, "wankeltruug");
		if (targetProfile == null) {
			return (null);
		}
		
		// fetch tracks online
		LinkedList<JSONObject> jsonObjects = new LinkedList<JSONObject>();

		try {
			String jsonResponse = SoundcloudStreamAppUtil.fetchExtendedStreamJSON(host, targetProfile);
			SoundcloudStreamAppUtil.parseJsonTrackList(jsonObjects, jsonResponse);
			for (JSONObject jsonObject : jsonObjects) {
				try {
					// check duration -> longer then 10min = set
					long duration = jsonObject.getLong("duration");
					int id = jsonObject.getInt("id");
					TrackListType type = TrackListType.TRACK;

					if (SoundcloudStreamAppUtil.findAudioFileId(id) != -1) {
						type = TrackListType.DOWNLOADED_CURRENT_PROFILE;
					} else if (duration > (10 * DateUtils.MILLIS_PER_MINUTE)) {
						type = TrackListType.SET;
					}

					String title = (String) jsonObject.get("title");
					String description = (String) ((JSONObject) jsonObject
							.get("user")).get("username");
					String artwork = null;
					if (jsonObject.get("artwork_url") instanceof String) {
						artwork = (String) jsonObject.get("artwork_url");
					}
					String streamUrl = null;
					if (jsonObject.has("stream_url")) {
						streamUrl = (String) jsonObject.get("stream_url");
					}
					
					String soundcloudTrackURL = null;
					if (jsonObject.has("permalink_url")) {
						soundcloudTrackURL = jsonObject.getString("permalink_url");
					}
					
					TrackListItem trackListItem = new TrackListItem(id, type,
							title, description, artwork);
					trackListItem.setStreamURL(streamUrl);
					trackListItem.setSoundcloudTrackURL(soundcloudTrackURL);

					if (trackListItems.get(type) == null) {
						trackListItems.put(type,
								new LinkedList<TrackListItem>());
					}
					trackListItems.get(type).add(trackListItem);
				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
			}
			
			// fetch local downloaded tracks
			List<TrackListItem> localItems = trackListItems.get(TrackListType.DOWNLOADED_LOCAL);
			if (localItems == null) {
				localItems = new LinkedList<TrackListItem>();
			}
			localItems.addAll(SoundcloudStreamAppUtil.findDownloadedItems());
			trackListItems.put(TrackListType.DOWNLOADED_LOCAL, localItems);
		} catch (Exception e) {
			// do something
		} finally {
			processDialog.dismiss();
			processDialog = null;

			// update UI
			new Handler(Looper.getMainLooper()).post(new Runnable() {

				@Override
				public void run() {
					for (TrackListUpdateListener listener : listeners) {
						listener.reloaded(trackListItems);
					}
				}
			});

		}

		return (null);
	}

	@Override
	protected void onPreExecute() {
		processDialog.setMessage("Loading data from soundcloud...");
		processDialog.show();
		processDialog.setCancelable(false);
	}

	@Override
	protected void onPostExecute(Void result) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Collection<List<TrackListItem>> entries = trackListItems
						.values();
				for (List<TrackListItem> itemList : entries) {
					for (TrackListItem item : itemList) {
						String artworkURL = item.getArtworkURL();
						if (artworkURL == null) {
							continue;
						}

						try {
							HttpURLConnection connection = (HttpURLConnection) new URL(
									artworkURL).openConnection();
							connection.connect();
							InputStream input = connection.getInputStream();

							Bitmap x = BitmapFactory.decodeStream(input);
							item.setArtwork(new BitmapDrawable(Resources
									.getSystem(), x));
						} catch (Exception ex) {
							// ups, could not load artwork
							ex.printStackTrace();
						}
					}
				}

				// update UI
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						for (TrackListUpdateListener listener : listeners) {
							listener.artworksUpdated(trackListItems);
						}
					}
				});
			}
		}).start();
	}

	public void addUpdateListener(TrackListUpdateListener listener) {
		listeners.add(listener);
	}

	public Map<TrackListType, List<TrackListItem>> getTrackListItems() {
		return trackListItems;
	}
}
