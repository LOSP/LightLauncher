package us.shandian.launcher.settings;

import android.os.Bundle;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

import us.shandian.launcher.R;

public class LauncherSettingsActivity extends PreferenceActivity implements OnPreferenceClickListener
{
    private Preference mRestart;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        // Initialize the preferences
        mRestart = findPreference(SettingsProvider.KEY_SETTINGS_RESTART);
        mRestart.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        if (preference == mRestart) {
            // I am Launcher, so I will restart after killing
            System.exit(0);
            return true;
        } else {
            return false;
        }
    }
}
