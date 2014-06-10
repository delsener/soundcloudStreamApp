package ch.zhaw.seminar.soundcloud.stream.app.ui.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import ch.zhaw.seminar.soundcloud.stream.app.models.TrackListItem;
import ch.zhaw.seminar.soundcloud.stream.app.ui.activities.MainNavigationActivity;
import ch.zhaw.seminar.soundcloudStreamApp.R;

public class TrackListAdapter extends ArrayAdapter<TrackListItem> implements
		OnClickListener {

	/** The inflator used to inflate the XML layout */
	private LayoutInflater inflator;

	private final int rowItemLayout;

	public TrackListAdapter(int viewResource, LayoutInflater inflator,
			int rowItemLayout) {
		super(inflator.getContext(), viewResource);
		this.inflator = inflator;
		this.rowItemLayout = rowItemLayout;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		// We only create the view if its needed
		if (view == null) {
			view = inflator.inflate(rowItemLayout, null);

			// Set the click listener for the checkbox
			view.setClickable(true);
			view.setOnClickListener(this);
			view.setBackgroundColor(Resources.getSystem().getColor(
					android.R.color.white));
		}

		TrackListItem data = (TrackListItem) getItem(position);
		view.setTag(data);

		CheckBox checkbox = (CheckBox) view.findViewById(R.id.track_check_view);
		if (checkbox != null) {
			checkbox.setChecked(data.isSelected());
			checkbox.setOnClickListener(this);
			checkbox.setTag(data);
		}

		ImageButton playButton = (ImageButton) view
				.findViewById(R.id.track_play_button);
		if (playButton != null) {
			playButton.setOnClickListener(this);
			playButton.setTag(data);
		}

		ImageView coverView = (ImageView) view
				.findViewById(R.id.track_cover_view);

		if (data.getArtwork() != null) {
			coverView.setImageDrawable(data.getArtwork());
		} else {
			coverView.setImageResource(R.drawable.no_cover);
		}

		TextView titleView = (TextView) view
				.findViewById(R.id.track_title_view);
		titleView.setText(data.getTitle());

		TextView descriptionView = (TextView) view
				.findViewById(R.id.track_description_view);
		descriptionView.setText(data.getArtist());

		return view;
	}

	@Override
	/** Will be called when a checkbox has been clicked. */
	public void onClick(View view) {
		if (view instanceof CheckBox) {
			view = (View) view.getParent();

			ObjectAnimator animator = ObjectAnimator.ofObject(view,
					"backgroundColor", new ArgbEvaluator(), 0xff78c5f9,
					0xFFFFFFFF);
			animator.setDuration(300);
			animator.start();

			TrackListItem data = (TrackListItem) view.getTag();
			data.setSelected(!data.isSelected());
			notifyDataSetChanged();
		}

		if (view instanceof ImageButton) {
			TrackListItem data = (TrackListItem) view.getTag();
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(data
					.getSoundcloudTrackURL()));
			MainNavigationActivity.activity.startActivity(i);
		}
	}
}
