package mobi.omegacentauri.astrosurveys;

import mobi.omegacentauri.astrosurveys.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Options extends PreferenceActivity {
	public static final String PREF_FULL_SCREEN = "fullScreen";
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
	
		addPreferencesFromResource(R.xml.options);
	}

}
