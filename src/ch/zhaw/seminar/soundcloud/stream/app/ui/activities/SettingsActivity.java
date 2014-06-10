package ch.zhaw.seminar.soundcloud.stream.app.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import ch.zhaw.seminar.soundcloud.stream.app.ui.fragments.SettingsFragment;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}