package us.shandian.launcher.settings;

import android.os.Bundle;
import android.content.Context;
import android.view.MenuItem;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

import us.shandian.launcher.R;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener
{
    private Preference mRestart;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        // Initialize the preferences
        mRestart = findPreference(SettingsProvider.KEY_SETTINGS_RESTART);
        mRestart.setOnPreferenceClickListener(this);
        
        // Show "up" button
        getActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
