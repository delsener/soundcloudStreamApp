package ch.zhaw.seminar.soundcloud.stream.app.ui.tasks;

import org.apache.commons.lang3.ArrayUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.ui.activities.MainNavigationActivity;
import ch.zhaw.seminar.soundcloud.stream.app.utils.SoundcloudStreamAppUtil;

public class TrackDeletionAsyncTask extends
		AsyncTask<TrackListItem, TrackListItem, Void> {

	private ProgressDialog processDialog;
	
	public TrackDeletionAsyncTask(Context context) {
		this.processDialog = new ProgressDialog(context);
	}

	@Override
	protected Void doInBackground(TrackListItem...params) {
		if (ArrayUtils.isEmpty(params)) {
			return (null);
		}

		try {
			for (TrackListItem trackItem : params) {
				SoundcloudStreamAppUtil.deleteMP3File(trackItem);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			processDialog.dismiss();
			processDialog = null;
			
			//MainNavigationActivity.activity.updateTrackList();
		}

		return (null);
	}

	@Override
	protected void onPreExecute() {
		processDialog.setMessage("Deleting selected tracks ...");
		processDialog.show();
		processDialog.setCancelable(false);
	}

	@Override
	protected void onPostExecute(Void result) {
		// update UI
		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				MainNavigationActivity.activity.updateTrackList();
			}
		});
	}
}
