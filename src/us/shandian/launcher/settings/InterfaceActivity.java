package us.shandian.launcher.settings;

import android.os.Bundle;
import android.widget.Toast;
import android.view.MenuItem;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.EditTextPreference;
import android.preference.Preference.OnPreferenceChangeListener;

import us.shandian.launcher.R;
import us.shandian.launcher.LauncherAppState;

public class InterfaceActivity extends PreferenceActivity implements OnPreferenceChangeListener
{
    private static final String KEY_ICONPACK_CHOOSER = "interface_iconpack_chooser";
    private static final String KEY_ICONPACK_CURRENT = "interface_iconpack_current";
    
    private Preference mCurrent;
    private Preference mChooser;
    
    private EditTextPreference mHomescreenIconSize;
    private EditTextPreference mHotseatIconSize;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_interface);
        
        // Initialize
        mCurrent = findPreference(KEY_ICONPACK_CURRENT);
        mChooser = findPreference(KEY_ICONPACK_CHOOSER);
        mHomescreenIconSize = (EditTextPreference) findPreference(SettingsProvider.KEY_INTERFACE_HOMESCREEN_DRAWER_ICON_SIZE);
        mHotseatIconSize = (EditTextPreference) findPreference(SettingsProvider.KEY_INTERFACE_HOTSEAT_ICON_SIZE);
        
        String iconPack = SettingsProvider.getString(this, SettingsProvider.KEY_INTERFACE_ICONPACK, getResources().getString(R.string.interface_iconpack_current_summary_default));
        try {
            mCurrent.setSummary(getPackageManager().getPackageInfo(iconPack, 0).applicationInfo.loadLabel(getPackageManager()));
        } catch (Exception e) {
            // So what?
        }
        
        int homescreenIconSize = SettingsProvider.getInt(this, SettingsProvider.KEY_INTERFACE_HOMESCREEN_DRAWER_ICON_SIZE, 48);
        mHomescreenIconSize.setSummary(homescreenIconSize + " dp");
        mHomescreenIconSize.setText(String.valueOf(homescreenIconSize));
        
        int hotseatIconSize = SettingsProvider.getInt(this, SettingsProvider.KEY_INTERFACE_HOTSEAT_ICON_SIZE, 48);
        mHotseatIconSize.setSummary(hotseatIconSize + " dp");
        mHotseatIconSize.setText(String.valueOf(hotseatIconSize));
        
        mHomescreenIconSize.setOnPreferenceChangeListener(this);
        mHotseatIconSize.setOnPreferenceChangeListener(this);
        
        
        // Show "home"
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mCurrent != null) {
            String iconPack = SettingsProvider.getString(this, SettingsProvider.KEY_INTERFACE_ICONPACK, getResources().getString(R.string.interface_iconpack_current_summary_default));
            try {
                mCurrent.setSummary(getPackageManager().getPackageInfo(iconPack, 0).applicationInfo.loadLabel(getPackageManager()));
            } catch (Exception e) {
                mCurrent.setSummary(iconPack);
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean ret = false;
        boolean needsRestart = false;
        if (preference == mHomescreenIconSize) {
            int size = newValue.equals("") ? 48 : Integer.parseInt((String) newValue);
            mHomescreenIconSize.setSummary(size + " dp");
            SettingsProvider.putInt(this, SettingsProvider.KEY_INTERFACE_HOMESCREEN_DRAWER_ICON_SIZE, size);
            ret = true;
            needsRestart = true;
        } else if (preference == mHotseatIconSize) {
            int size = newValue.equals("") ? 48 : Integer.parseInt((String) newValue);
            mHotseatIconSize.setSummary(size + " dp");
            SettingsProvider.putInt(this, SettingsProvider.KEY_INTERFACE_HOTSEAT_ICON_SIZE, size);
            ret = true;
            needsRestart = true;
        }
        
        if (ret) {
            // If anything changed, reload launcher when finishing this activity
            LauncherAppState.getInstance().getDynamicGrid().forceReload();
            LauncherAppState.getInstance().getIconCache().flush();
            LauncherAppState.getInstance().getModel().forceReload();
        }
        
        if (needsRestart) {
            // Show the message
            Toast.makeText(this, R.string.message_needs_restart, 1000).show();
        }
        
        return ret;
        
    }
}
