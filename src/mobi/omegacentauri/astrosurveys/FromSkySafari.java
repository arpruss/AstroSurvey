package mobi.omegacentauri.astrosurveys;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class FromSkySafari extends AstroSurveys {
	 //3.154713494000000e+01
	static final int DEFAULT_FRACTION_DIGITS = 15;
	static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("0.000000000000000E+00");

	private static final String[] SKYSAFARI_PATHS = 
		{ 
		"/SkySafari 5 Pro/Saved Settings/[CurrentSettings].skyset", 
		"/SkySafari 4 Pro/Saved Settings/[CurrentSettings].skyset", 
		"/SkySafari Pro/Saved Settings/[CurrentSettings].skyset", 
		"/SkySafari 5 Plus/Saved Settings/[CurrentSettings].skyset",
		"/SkySafari 4 Plus/Saved Settings/[CurrentSettings].skyset",
		"/SkySafari Plus/Saved Settings/[CurrentSettings].skyset",
		"/SkySafari 5/Saved Settings/[CurrentSettings].skyset",
		"/SkySafari 4/Saved Settings/[CurrentSettings].skyset",
		"/SkySafari/Saved Settings/[CurrentSettings].skyset"
		};
	private static final String[] SKYSAFARI_PACKAGES =
		{
		"com.simulationcurriculum.skysafari5pro",
		"com.simulationcurriculum.skysafari4pro",
		"com.southernstars.skysafari_pro",
		"com.simulationcurriculum.skysafari5plus",
		"com.simulationcurriculum.skysafari4plus",
		"com.southernstars.skysafari_plus",
		"com.simulationcurriculum.skysafari5",
		"com.simulationcurriculum.skysafari4",
		"com.southernstars.skysafari_lite"
		};
	private static final String DISPLAY_AZ = "DisplayCenterLon=";
	private static final String DISPLAY_ALT = "DisplayCenterLat=";
	private static final String DISPLAY_FOV = "DisplayFOV=";
	private static final String LAT = "Latitude=";
	private static final String LON = "Longitude=";
	private static final String JULIAN_DATE = "JulianDate=";
	private static final String REAL_TIME = "RealTime=";
	private static final String OBJECT_LOCKED = "SelectedObjectLocked=";
	private static final double DEG2RAD = Math.PI/180;
	private boolean realTime;
	private int currentSkySafari;
	private String sd;
	private static final String TEMP_CONFIG = "AstroSurveysTempConfig";
	private FOV inFOV = null;

	@Override
	protected void onResume() {
		sd = Environment.getExternalStorageDirectory().getPath();
		currentSkySafari = getMostRecent(sd, SKYSAFARI_PATHS);		
		File skySafariConfig = currentSkySafari < 0 ? null : new File(sd + SKYSAFARI_PATHS[currentSkySafari]);
		newFOV = getFOV_SkySafari(skySafariConfig);
		if (newFOV == null) 
			Toast.makeText(this, "Cannot find SkySafari data.", Toast.LENGTH_LONG).show();
		
		super.onResume();
	}
	
	private FOV getFOV_SkySafari(File skySafariConfig) {
		if (null == skySafariConfig)
			return null;
		
		inFOV = new FOV();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(skySafariConfig));
			
			String line;
			
			while (null != (line = reader.readLine())) {
				if (line.startsWith(DISPLAY_AZ)) 
					inFOV.az = getDouble(line, DISPLAY_AZ, DEG2RAD);
				else if (line.startsWith(DISPLAY_ALT)) 
					inFOV.alt = getDouble(line, DISPLAY_ALT, DEG2RAD);
				else if (line.startsWith(DISPLAY_FOV)) 
					inFOV.sizeDegrees = getDouble(line, DISPLAY_FOV, 1.);
				else if (line.startsWith(LON))
					inFOV.lon = getDouble(line, LON, DEG2RAD);
				else if (line.startsWith(LAT)) 
					inFOV.lat = getDouble(line, LAT, DEG2RAD);
				else if (line.startsWith(JULIAN_DATE)) {
					inFOV.jd = getDouble(line, JULIAN_DATE, 1.);
				}
				else if (line.startsWith(REAL_TIME)) {
					realTime = 0 != getInteger(line, REAL_TIME);
				}
			}
			
			reader.close();
		} catch (IOException e) {
			return null;
		}
		
		if (inFOV.preValid()) 
			inFOV.computeRaDec2000FromAltAz();
		else
			inFOV = null;
		return inFOV;
	}

	private double getDouble(String line, String code, double d) {
		try {
			return Double.parseDouble(line.substring(code.length())) * d;
		} 
		catch (NumberFormatException e) {
			return Double.NaN;
		}
	}

	private int getInteger(String line, String code) {
		try {
			return Integer.parseInt(line.substring(code.length()));
		} 
		catch (NumberFormatException e) {
			return 0;
		}
	}

	int getMostRecent(String prefix, String[] paths) {
		if (paths.length == 0)
			return -1;
		
		long t1 = System.currentTimeMillis();
		long bestT = -1;
		int bestIndex = -1;
		
		for (int i = 0 ; i < paths.length ; i++ ) {
			File f = new File(prefix+paths[i]);
			if (f.exists()) {
				long t = f.lastModified();
				if (bestIndex < 0 || (bestT < t && t <= t1)) {
					bestT = t;
					bestIndex = i;
				}
			}
		}
		
		return bestIndex;
	}

	public void shareSkySafari(double ra, double dec, double size) {
		Log.v("AstroSurveys", "shareSS");
		if (currentSkySafari < 0 || inFOV == null) {
			Toast.makeText(this, "Cannot find SkySafari data.", Toast.LENGTH_LONG).show();
			return;
		}
		
		FOV out = new FOV();
		out.jd = realTime ? currentJD() : inFOV.jd;
		out.ra2000Degrees = Math.toDegrees(ra);
		out.dec2000Degrees = Math.toDegrees(dec);
		out.sizeDegrees = Math.toDegrees(size);
		out.lat = inFOV.lat;
		out.lon = inFOV.lon;
		out.computeAltAzFromRaDec2000();
		
		Log.v("AstroSurveys", "configuring");
		String config = sd + SKYSAFARI_PATHS[currentSkySafari];
		File orig = new File(config);
		File backup = new File(config+".backup");
		if (orig.renameTo(backup)) 
			orig = backup;
		
		File temp = new File(orig.getParent() + "/" + TEMP_CONFIG);
		BufferedReader r = null;
		BufferedWriter w = null;

		Log.v("AstroSurveys", "copying and modifying");
		try {
			r = new BufferedReader(new FileReader(orig));
			w = new BufferedWriter(new FileWriter(temp));
			
			String line;
			
			Log.v("AstroSurveys", "writing");
			
			while (null != (line = r.readLine())) {
				if (line.startsWith(DISPLAY_AZ)) 
					line = fixLine(line, DISPLAY_AZ, Math.toDegrees(out.az));
				else if (line.startsWith(DISPLAY_ALT)) 
					line = fixLine(line, DISPLAY_ALT, Math.toDegrees(out.alt));
				else if (line.startsWith(DISPLAY_FOV)) 
					line = fixLine(line, DISPLAY_FOV, out.sizeDegrees);
				else if (realTime && line.startsWith(JULIAN_DATE))
					line = fixLine(line, JULIAN_DATE, out.jd);
				else if (line.startsWith(OBJECT_LOCKED)) 
					line = OBJECT_LOCKED + "0";
				w.write(line);
				w.write("\n");
			}
			
			w.close();
			w = null;
			r.close();
			r = null;
			
			Log.v("AstroSurveys", "renaming");
			temp.renameTo(new File(config));
			
			Log.v("AstroSurveys", "old: "+Math.toDegrees(inFOV.alt)+" "+Math.toDegrees(inFOV.az));
			Log.v("AstroSurveys", "old: "+Math.toDegrees(inFOV.ra)+" "+Math.toDegrees(inFOV.dec));
			Log.v("AstroSurveys", "old: "+inFOV.jd);
			Log.v("AstroSurveys", "new: "+Math.toDegrees(out.alt)+" "+Math.toDegrees(out.az));
			Log.v("AstroSurveys", "new: "+Math.toDegrees(out.ra)+" "+Math.toDegrees(out.dec));
			Log.v("AstroSurveys", "new: "+out.jd);
			
			Intent i = getPackageManager().getLaunchIntentForPackage(SKYSAFARI_PACKAGES[currentSkySafari]);
			i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
			
			startActivity(i);
		} catch (IOException e) {
			Log.v("AstroSurveys", "Error");
			Log.v("AstroSurveys", ""+e);
			if (r != null) {
				try {
					r.close();
				} catch (IOException e1) {
				}
			}
			if (w != null) {
				try {
					w.close();
				} catch (IOException e1) {
				}
			}
			temp.delete();
		}
	}
	
	// format number in such a way as not to change line length
	private String fixLine(String line, String head, double value) {
		int oldLength = line.length() - head.length();
		
		String formatted = DEFAULT_FORMAT.format(value);
		
		int newLength = formatted.length();
		
		if (newLength == oldLength)
			return head + formatted.toLowerCase();
		
		int nOld = DEFAULT_FORMAT.getMinimumFractionDigits();
		int nNew;

		nNew = nOld + oldLength - newLength;
		
		if (nNew < 0)
			nNew = 0;

		String format = "0.";
		for (int i=0; i<nNew ; i++)
			format += "0";
		format += "E+00";
		
		Log.v("AstroSurveys", format);
		
		return head + new DecimalFormat(format).format(value).toLowerCase();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item))
			return true;
		
		switch(item.getItemId()) {
		case R.id.share_skysafari:
			webview.loadUrl("javascript:shareSkySafari()");
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_skysafari, menu);
		
		return true;
	}


}

