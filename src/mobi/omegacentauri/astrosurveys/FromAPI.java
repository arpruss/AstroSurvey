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
import java.util.Collection;
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
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class FromAPI extends AstroSurveys {
	Intent intent;
	
	@Override
	protected void onCreate(Bundle bundle) {
		intent = getIntent();
		super.onCreate(bundle);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		Log.v("AstroSurveys", "FromAPI.onNewIntent");
		this.intent = intent;
	}
	
	@Override
	protected void onResume() {
		double ra2000Radians = intent.getDoubleExtra("RA", 0.);
		double dec2000Radians = intent.getDoubleExtra("Declination", 0.);
		double sizeRadians = intent.getDoubleExtra("Size", Math.toRadians(2.));
		
		Log.v("AstroSurveys", "Intent:"+intent+" "+ra2000Radians+" "+dec2000Radians+" "+sizeRadians);
		
		newFOV = new FOV(Math.toDegrees(ra2000Radians),
					Math.toDegrees(dec2000Radians),
					Math.toDegrees(sizeRadians)	);
		
		super.onResume();
	}
}

