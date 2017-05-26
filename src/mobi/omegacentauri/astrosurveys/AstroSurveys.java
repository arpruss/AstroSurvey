package mobi.omegacentauri.astrosurveys;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class AstroSurveys extends Activity {
//	private int width;
//	private int height;
	protected WebView webview;
	private SharedPreferences options;
	private FOV fov = null;
	protected FOV newFOV = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		options = PreferenceManager.getDefaultSharedPreferences(this);
		
//		width = getWindowManager().getDefaultDisplay().getWidth();
//		height = getWindowManager().getDefaultDisplay().getHeight();
//		
//		if (height < 64)
//			height = 64;
//		if (height > 2048)
//			height = 2048;
//		if (width < 64)
//			width = 64;
//		if (width > 2048)
//			width = 2048;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (options.getBoolean(Options.PREF_FULL_SCREEN, true))
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		webview = new WebView(this);
		webview.setBackgroundColor(0x00000000);
		setContentView(webview);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (newFOV == null) {
			newFOV = new FOV(0.,0.,1.);
		}
		
		if (! newFOV.equals(fov)) {
			fov = newFOV;
			newFOV = null;
			Log.v("AstroSurveys", "download task");
			new DownloadTask(this, webview).execute(fov);
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.options:
			startActivity(new Intent(this, Options.class));	
			return true;
		case R.id.share:
			webview.loadUrl("javascript:share()");
			return true;
		default:
			return false;
		}
	}
	
	public static double currentJD() {
		long t = new Date().getTime();
		return t / 86400000. + 2440587.5;
//		return Time.getJulianDay(t, 0) + ((t - 86400000 / 2 ) % 86400000)/86400000.;
	}

	public void share(double ra, double dec, double size) {
		Log.v("AstroSurveys", "share: "+ra+" "+dec+" "+size);		
    	Intent i = new Intent(Intent.ACTION_VIEW);
    	i.setType("text/astro_position");
  
    	i.putExtra("RA", ra);
    	i.putExtra("Declination", dec);
    	i.putExtra("Size", size);
    	startActivity(Intent.createChooser(i, "Choose application to view coordinates"));
	}

}

