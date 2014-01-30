package us.shandian.launcher.settings;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView.OnItemClickListener;

import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import us.shandian.launcher.R;
import us.shandian.launcher.settings.IconPackHelper.IconPackInfo;

public class IconPackChooserActivity extends Activity implements OnItemClickListener
{
    private IconPackAdapter mAdapter;
    
    private ListView mList;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interface_iconpack_chooser);
        
        // Initialize the list
        mList = (ListView) findViewById(R.id.iconpack_list);
        mAdapter = new IconPackAdapter(this, IconPackHelper.getSupportedPackages(this));
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        
        // Show "up"
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= mAdapter.getCount()) {
            return;
        } else if (position == 0) {
            SettingsProvider.remove(this, SettingsProvider.KEY_INTERFACE_ICONPACK);
        } else {
            SettingsProvider.putString(this, SettingsProvider.KEY_INTERFACE_ICONPACK, mAdapter.getItem(position).toString());
        }
        
        // Restart launcher after changing icon pack
        System.exit(0);
    }
    
    private class IconPackAdapter extends BaseAdapter
    {
        private ArrayList<IconPackInfo> mPackages;
        private LayoutInflater mInflater;
        
        public IconPackAdapter(Context context, Map<String, IconPackInfo> pkgs) {
            mPackages = new ArrayList<IconPackInfo>(pkgs.values());
            mPackages.add(0, new IconPackInfo(context.getResources().getString(R.string.application_name), context.getResources().getDrawable(R.mipmap.ic_launcher_home), ""));
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public int getCount() {
            return mPackages.size();
        }

        @Override
        public Object getItem(int position) {
            return mPackages.get(position).packageName;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (position >= mPackages.size()) return convertView;
            IconPackInfo info = mPackages.get(position);
            View ret = mInflater.inflate(R.layout.interface_iconpack_chooser_item, null);
            ImageView icon = (ImageView) ret.findViewById(R.id.iconpack_icon);
            icon.setImageDrawable(info.icon);
            TextView label = (TextView) ret.findViewById(R.id.iconpack_name);
            label.setText(info.label);
            return ret;
        }

        
    }
}
