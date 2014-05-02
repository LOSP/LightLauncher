package us.shandian.launcher.settings;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class IconPackHelper {

    public final static String[] sSupportedActions = new String[] {
        "org.adw.launcher.THEMES",
        "com.gau.go.launcherex.theme",
        "home.solo.launcher.free.ACTION_ICON",
        "home.solo.launcher.free.THEMES"
    };

    public static final String[] sSupportedCategories = new String[] {
        "com.fede.launcher.THEME_ICONPACK",
        "com.anddoes.launcher.THEME",
        "com.teslacoilsw.launcher.THEME"
    };
    
    // Icon Tags
    private static final String ICON_MASK = "iconmask";
    private static final String ICON_BACK = "iconback";
    private static final String ICON_UPON = "iconupon";
    private static final String ICON_SCALE = "scale";
    
    // Fonts
    private static final String THEME_FONT = "themefont.ttf";
    
    // List of resource names for all apps button
    private static final String[] ICON_DRAWER = new String[] {
        // Mostly used
        "all_apps_button_icon",
        
        // Collection of some other names of the icon
        "drawer",
        "app_drawer",
        "appdrawer",
        "appdrawer1",
        "apps",
        "all",
        "allapps",
        "allapp"
    };

    // Holds package/class -> drawable
    private Map<String, String> mIconPackResources;
    private final Context mContext;
    private String mLoadedIconPackName;
    private Resources mLoadedIconPackResource;
    
    // Icon Mask/Back/Upon/Scale
    private Drawable mIconMasks[], mIconBacks[], mIconUpons[];
    private float mIconScale;
    
    // Drawer icon
    private Drawable mDrawerIcon;
    
    // Font
    private Typeface mFont;
    
    // Random object used for icons
    private Random mRandom = new Random();

    public IconPackHelper(Context context) {
        mContext = context;
        mIconPackResources = new HashMap<String, String>();
    }

    public static Map<String, IconPackInfo> getSupportedPackages(Context context) {
        Intent i = new Intent();
        Map<String, IconPackInfo> packages = new HashMap<String, IconPackInfo>();
        PackageManager packageManager = context.getPackageManager();
        for (String action : sSupportedActions) {
            i.setAction(action);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPackInfo info = new IconPackInfo(r, packageManager);
                packages.put(r.activityInfo.packageName, info);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        for (String category : sSupportedCategories) {
            i.addCategory(category);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                IconPackInfo info = new IconPackInfo(r, packageManager);
                packages.put(r.activityInfo.packageName, info);
            }
            i.removeCategory(category);
        }
        return packages;
    }

    private static void loadResourcesFromXmlParser(XmlPullParser parser,
                                                   Map<String, String> iconPackResources) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        do {

            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }
            
            if (parser.getName().equalsIgnoreCase(ICON_MASK) ||
                parser.getName().equalsIgnoreCase(ICON_BACK) ||
                parser.getName().equalsIgnoreCase(ICON_UPON))
            {
                if (parser.getAttributeCount() > 0) {
                    String icons = new String();
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        icons += parser.getAttributeValue(i);
                        
                        if (i < parser.getAttributeCount() - 1) {
                            icons += "|";
                        }
                    }
                    iconPackResources.put(parser.getName().toLowerCase(), icons);
                }
                continue;
            }
            
            if (parser.getName().equalsIgnoreCase(ICON_SCALE)) {
                String factor = parser.getAttributeValue(null, "factor");
                if (factor == null) {
                    if (parser.getAttributeCount() == 1) {
                        factor = parser.getAttributeValue(0);
                    }
                }
                iconPackResources.put(parser.getName().toLowerCase(), factor);
                continue;
            }

            if (!parser.getName().equalsIgnoreCase("item")) {
                continue;
            }

            String component = parser.getAttributeValue(null, "component");
            String drawable = parser.getAttributeValue(null, "drawable");

            // Validate component/drawable exist
            if (TextUtils.isEmpty(component) || TextUtils.isEmpty(drawable)) {
                continue;
            }

            // Validate format/length of component
            if (!component.startsWith("ComponentInfo{") || !component.endsWith("}")
                || component.length() < 16) {
                continue;
            }

            // Sanitize stored value
            component = component.substring(14, component.length() - 1).toLowerCase();

            ComponentName name = null;
            if (!component.contains("/")) {
                // Package icon reference
                iconPackResources.put(component, drawable);
            } else {
                name = ComponentName.unflattenFromString(component);
                if (name != null) {
                    iconPackResources.put(name.getPackageName(), drawable);
                    iconPackResources.put(name.getClassName(), drawable);
                }
            }
        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
    }

    private static void loadApplicationResources(Context context,
                                                 Map<String, String> iconPackResources, String packageName) {
        Field[] drawableItems = null;
        try {
            Context appContext = context.createPackageContext(packageName,
                                                              Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            drawableItems = Class.forName(packageName+".R$drawable",
                                          true, appContext.getClassLoader()).getFields();
        } catch (Exception e){
            return;
        }

        for (Field f : drawableItems) {
            String name = f.getName();

            String icon = name.toLowerCase();
            name = name.replaceAll("_", ".");

            iconPackResources.put(name, icon);

            int activityIndex = name.lastIndexOf(".");
            if (activityIndex <= 0 || activityIndex == name.length() - 1) {
                continue;
            }

            String iconPackage = name.substring(0, activityIndex);
            if (TextUtils.isEmpty(iconPackage)) {
                continue;
            }
            iconPackResources.put(iconPackage, icon);

            String iconActivity = name.substring(activityIndex + 1);
            if (TextUtils.isEmpty(iconActivity)) {
                continue;
            }
            iconPackResources.put(iconPackage + "." + iconActivity, icon);
        }
    }

    public boolean loadIconPack(String packageName) {
        mIconPackResources = getIconPackResources(mContext, packageName);
        Resources res = null;
        try {
            res = mContext.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        mLoadedIconPackResource = res;
        mLoadedIconPackName = packageName;
        
        // Drawer Icon
        int drawerIconId = 0;
        for (String resName : ICON_DRAWER) {
            drawerIconId = getResourceIdForDrawable(resName);
            
            if (drawerIconId > 0) {
                break;
            }
        }
        
        if (drawerIconId > 0) {
            mDrawerIcon = mLoadedIconPackResource.getDrawable(drawerIconId);
        }
        
        mIconMasks = getDrawablesForName(ICON_MASK);
        mIconBacks = getDrawablesForName(ICON_BACK);
        mIconUpons = getDrawablesForName(ICON_UPON);
        String scale = mIconPackResources.get(ICON_SCALE);
        try {
            mIconScale = Float.valueOf(scale);
        } catch (Exception e) {
            // OK, you win...
            // Back to default
            mIconScale = 1f;
        }
        
        // Get themefont
        try {
            Context appContext = mContext.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
            mFont = Typeface.createFromAsset(appContext.getAssets(), THEME_FONT);
        } catch (Exception e) {
            mFont = null;
        }
        
        return true;
    }
    
    public static Map<String, String> getIconPackResources(Context context, String packageName) {
        return getIconPackResources(context, packageName, false);
    }

    public static Map<String, String> getIconPackResources(Context context, String packageName, boolean loadFromResources) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        Resources res = null;
        try {
            res = context.getPackageManager().getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        XmlPullParser parser = null;
        InputStream inputStream = null;
        Map<String, String> iconPackResources = new HashMap<String, String>();
        
        if (!loadFromResources) {
            try {
                inputStream = res.getAssets().open("appfilter.xml");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                parser = factory.newPullParser();
                parser.setInput(inputStream, "UTF-8");
            } catch (Exception e) {
                // Catch any exception since we want to fall back to parsing the xml/
                // resource in all cases
                return getIconPackResources(context, packageName, true);
            }
        } else {
            int resId = res.getIdentifier("appfilter", "xml", packageName);
            if (resId != 0) {
                parser = res.getXml(resId);
            }
        }

        if (parser != null) {
            try {
                loadResourcesFromXmlParser(parser, iconPackResources);
                if (!loadFromResources && !iconPackResources.containsKey(ICON_BACK) &&
                    !iconPackResources.containsKey(ICON_MASK) &&
                    !iconPackResources.containsKey(ICON_UPON) &&
                    !iconPackResources.containsKey(ICON_SCALE))
                {
                    // No icon masks? Maybe the author put them in resources
                    // So let's fall back
                    Map<String, String> res2 = getIconPackResources(context, packageName, true);
                    if (!res2.isEmpty()) {
                        return res2;
                    }
                }
                return iconPackResources;
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Cleanup resources
                if (parser instanceof XmlResourceParser) {
                    ((XmlResourceParser) parser).close();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // Application uses a different theme format (most likely launcher pro)
        int arrayId = res.getIdentifier("theme_iconpack", "array", packageName);
        if (arrayId == 0) {
            arrayId = res.getIdentifier("icon_pack", "array", packageName);
        }

        if (arrayId != 0) {
            String[] iconPack = res.getStringArray(arrayId);
            for (String entry : iconPack) {

                if (TextUtils.isEmpty(entry)) {
                    continue;
                }

                String icon = entry.toLowerCase();
                entry = entry.replaceAll("_", ".");

                iconPackResources.put(entry, icon);

                int activityIndex = entry.lastIndexOf(".");
                if (activityIndex <= 0 || activityIndex == entry.length() - 1) {
                    continue;
                }

                String iconPackage = entry.substring(0, activityIndex);
                if (TextUtils.isEmpty(iconPackage)) {
                    continue;
                }
                iconPackResources.put(iconPackage, icon);

                String iconActivity = entry.substring(activityIndex + 1);
                if (TextUtils.isEmpty(iconActivity)) {
                    continue;
                }
                iconPackResources.put(iconPackage + "." + iconActivity, icon);
            }
        } else {
            loadApplicationResources(context, iconPackResources, packageName);
        }
        return iconPackResources;
    }
    
    public void unloadIconPack() {
        mLoadedIconPackResource = null;
        mLoadedIconPackName = null;
        mIconMasks = null;
        mIconBacks = null;
        mIconUpons = null;
        mIconScale = 1f;
        mDrawerIcon = null;
        mFont = null;
        if (mIconPackResources != null) {
            mIconPackResources.clear();
        }
    }

    

    boolean isIconPackLoaded() {
        return mLoadedIconPackResource != null &&
            mLoadedIconPackName != null &&
            mIconPackResources != null;
    }

    private int getResourceIdForDrawable(String resource) {
        int resId = mLoadedIconPackResource.getIdentifier(resource, "drawable", mLoadedIconPackName);
        return resId;
    }
    
    private Drawable[] getDrawablesForName(String name) {
        if (isIconPackLoaded()) {
            String itemText = mIconPackResources.get(name);
            
            if (itemText == null) {
                return null;
            }
            
            String[] items = itemText.split("\\|");
            Drawable[] drawables = new Drawable[items.length];
            for (int i = 0; i < items.length; i++) {
                if (!TextUtils.isEmpty(items[i])) {
                    int id = getResourceIdForDrawable(items[i]);
                    if (id != 0) {
                        drawables[i] = mLoadedIconPackResource.getDrawable(id);
                    }
                }
            }
            
            return drawables;
        }
        return null;
    }
    
    /**
     *
     * Some icon packs may provide a mask for icons
     * In order to make all icons match their style.
     *
     * Returns a random icon mask provided by icon pack
     * Null if not provided
     */
    public Drawable getIconMask() {
        if (mIconMasks != null) {
            return mIconMasks[randomIntWithMaxValue(mIconMasks.length)];
        } else {
            return null;
        }
    }
    
    
    /**
     * Returns a random icon back provided by icon pack
     * Null if not provided
     */
    public Drawable getIconBack() {
        if (mIconBacks != null) {
            return mIconBacks[randomIntWithMaxValue(mIconBacks.length)];
        } else {
            return null;
        }
    }
    
    /**
     * Returns a random icon upon provided by icon pack
     * Null if not provided
     */
    public Drawable getIconUpon() {
        if (mIconUpons != null) {
            return mIconUpons[randomIntWithMaxValue(mIconUpons.length)];
        } else {
            return null;
        }
    }
    
    /**
     * Returns icon scale provided by icon pack
     * Null if not provided
     */
    public float getIconScale() {
        return mIconScale;
    }
    
    public Drawable getDrawerIcon() {
        return mDrawerIcon;
    }
    
    public Typeface getFont() {
        if (SettingsProvider.getBoolean(mContext, SettingsProvider.KEY_INTERFACE_ICONPACK_USE_FONT, true)) {
            return mFont;
        } else {
            return null;
        }
    }
    
    private int randomIntWithMaxValue(int max) {
        return Math.abs(mRandom.nextInt()) % max;
    }
    
    public Resources getIconPackResources() {
        return mLoadedIconPackResource;
    }

    public int getResourceIdForActivityIcon(ActivityInfo info) {
        String drawable = mIconPackResources.get(info.name.toLowerCase());
        if (drawable == null) {
            // Icon pack doesn't have an icon for the activity, fallback to package icon
            drawable = mIconPackResources.get(info.packageName.toLowerCase());
            if (drawable == null) {
                return 0;
            }
        }
        return getResourceIdForDrawable(drawable);
    }

    static class IconPackInfo {
        String packageName;
        CharSequence label;
        Drawable icon;

        IconPackInfo(ResolveInfo r, PackageManager packageManager) {
            packageName = r.activityInfo.packageName;
            icon = r.loadIcon(packageManager);
            label = r.loadLabel(packageManager);
        }

        IconPackInfo(){
        }

        public IconPackInfo(String label, Drawable icon, String packageName) {
            this.label = label;
            this.icon = icon;
            this.packageName = packageName;
        }
    }

}
