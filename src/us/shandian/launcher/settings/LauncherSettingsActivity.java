package us.shandian.launcher.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import us.shandian.launcher.R;

public class LauncherSettingsActivity extends PreferenceActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
