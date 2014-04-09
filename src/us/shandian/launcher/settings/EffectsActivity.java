package us.shandian.launcher.settings;

import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.Bundle;

import java.util.List;
import java.util.Arrays;

import us.shandian.launcher.R;
import us.shandian.launcher.TransitionEffect;
import us.shandian.launcher.LauncherAppState;

public class EffectsActivity extends PreferenceActivity implements OnPreferenceChangeListener
{

    private CheckBoxPreference mAutoRotate;
    private ListPreference mTransEffect;
    
    private List<String> mTransValues;
    private List<String> mTransEntries;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable up
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Add them all
        addPreferencesFromResource(R.xml.settings_effects);
        
        // Find
        mAutoRotate = (CheckBoxPreference) findPreference(SettingsProvider.KEY_EFFECTS_GLOBAL_AUTO_ROTATE);
        mTransEffect = (ListPreference) findPreference(SettingsProvider.KEY_EFFECTS_WORKSPACE_TRANSITION_EFFECT);
        
        // Initialize
        boolean checked = SettingsProvider.getBoolean(this, 
                              SettingsProvider.KEY_EFFECTS_GLOBAL_AUTO_ROTATE,
                              false);
        mAutoRotate.setChecked(checked);
        mAutoRotate.setOnPreferenceChangeListener(this);
        
        mTransValues = Arrays.asList(getResources().getStringArray(R.array.transition_effect_values));
        mTransEntries = Arrays.asList(getResources().getStringArray(R.array.transition_effect_entries));
        String value = SettingsProvider.getString(this,
                           SettingsProvider.KEY_EFFECTS_WORKSPACE_TRANSITION_EFFECT,
                           TransitionEffect.TRANSITION_EFFECT_NONE);
        mTransEffect.setValue(value);
        mTransEffect.setSummary(mTransEffect.getEntry());
        mTransEffect.setOnPreferenceChangeListener(this);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean needsRestart = false;
        boolean ret = false;
        if (preference == mAutoRotate) {
            SettingsProvider.putBoolean(this, 
                SettingsProvider.KEY_EFFECTS_GLOBAL_AUTO_ROTATE,
                (Boolean) newValue);
            ret = true;
        } else if (preference == mTransEffect) {
            SettingsProvider.putString(this,
                SettingsProvider.KEY_EFFECTS_WORKSPACE_TRANSITION_EFFECT,
                (String) newValue);
            mTransEffect.setValue((String) newValue);
            mTransEffect.setSummary(mTransEntries.get(mTransValues.indexOf(newValue)));
            LauncherAppState.getInstance().getLauncher().reloadTransitionEffect();
        } else {
            ret = false;
        }
        
        if (needsRestart) {
            Toast.makeText(this, R.string.message_needs_restart, 1000);
        }
        
        return ret;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
