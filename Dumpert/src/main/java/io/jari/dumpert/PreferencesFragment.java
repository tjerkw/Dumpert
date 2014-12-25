package io.jari.dumpert;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * JARI.IO
 * Date: 25-12-14
 * Time: 2:44
 */
public class PreferencesFragment extends PreferenceFragment {
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("theme")) {
                    Main main = (Main) getActivity();
                    main.preferences.edit().putBoolean("switchtosettings", true).commit();
                    main.finish();
                    startActivity(main.getIntent());
                    main.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };

//        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }
}
