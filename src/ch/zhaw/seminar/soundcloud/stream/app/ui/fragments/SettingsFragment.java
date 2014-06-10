package ch.zhaw.seminar.soundcloud.stream.app.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import ch.zhaw.seminar.soundcloudStreamApp.R;

public class SettingsFragment extends PreferenceFragment {

	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  
	  // Load the preferences from an XML resource
	  addPreferencesFromResource(R.xml.preference_screen);
	 }

	}
