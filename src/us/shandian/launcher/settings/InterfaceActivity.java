package us.shandian.launcher.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

import us.shandian.launcher.R;

public class InterfaceActivity extends PreferenceActivity implements OnPreferenceClickListener
{
    private static final String KEY_ICONPACK_CHOOSER = "interface_iconpack_chooser";
    private static final String KEY_ICONPACK_CURRENT = "interface_iconpack_current";
    
    private Preference mCurrent;
    private Preference mChooser;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_interface);
        
        // Initialize
        mCurrent = findPreference(KEY_ICONPACK_CURRENT);
        mChooser = findPreference(KEY_ICONPACK_CHOOSER);
        
        mChooser.setOnPreferenceClickListener(this);
        mCurrent.setSummary(SettingsProvider.getString(this, SettingsProvider.KEY_INTERFACE_ICONPACK, getResources().getString(R.string.interface_iconpack_current_summary_default)));
        
        // Show "home"
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }
}
