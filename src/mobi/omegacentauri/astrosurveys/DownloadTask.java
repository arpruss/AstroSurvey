package mobi.omegacentauri.astrosurveys;

import java.io.File;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.WebView;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<FOV, Integer, Boolean> {
	static final String ALADIN_VERSION = "v2"; //v1 or v2
	//static final String ALADIN_JS_LINK= "http://aladin.u-strasbg.fr/AladinLite/api/v1/aladin.min.js";
	static final String ALADIN_JS_LINK = "http://aladin.u-strasbg.fr/AladinLite/api/v2/latest/aladin.min.js";
	static final String JQUERY_JS_LINK = "http://code.jquery.com/jquery-2.1.0.min.js"; // "http://code.jquery.com/jquery-1.10.1.min.js";
	static final String ALADIN_JS_LOCAL = "aladin.js";
	static final String JQUERY_JS_LOCAL = "jquery.js";
	
	private ProgressDialog progress;
	private Uri webviewUri;
	private WebView webView;
	private AstroSurveys context;
	private Cache cache;

	public DownloadTask(AstroSurveys context, WebView webView) {
		this.context = context;
		this.webView = webView;
		this.cache = new Cache(context);
	}

//	@Override
//	protected void onCancelled() {
//		super.onCancelled();
//	}

	@Override
	protected void onPreExecute() {
		progress = new ProgressDialog(context);
		progress.setCancelable(true);
		progress.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface arg0) {
				DownloadTask.this.cancel(true);					
			}});
		progress.setMessage("Please wait");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setIndeterminate(true);
		progress.show();
	}

	@Override
	protected Boolean doInBackground(FOV... fov) {			
		if (! cache.load(JQUERY_JS_LINK, JQUERY_JS_LOCAL, Cache.REFRESH_NEVER, null, this) ||
				! cache.load(ALADIN_JS_LINK, ALADIN_JS_LOCAL, 1, 
						new String[][] {
						{"\"mousedown\"", "\"touchdown\""},
						{"\"mouseout\"", "\"touchend\""},
						{"mouse(up|down|out|move)\\b\\s*",""} 
				}, this) )
			return false;

		webviewUri = getURI_AladinLite(fov[0]);
		if (webviewUri == null) 
			return false;

		return true;
	}
	
	private Uri getURI_AladinLite(FOV fov) {
		if (! cache.load(Cache.ASSET_URL+ALADIN_VERSION+"override.css", "override.css", Cache.REFRESH_NEVER, null, null) ||
				! cache.load(Cache.ASSET_URL+ALADIN_VERSION+"fullscreen.html",  "fullscreen.html",  
						Cache.REFRESH_ALWAYS, new String[][] {
						{ "\\[\\[fovDegrees\\]\\]", ""+fov.sizeDegrees },
						{ "\\[\\[ra2000Degrees\\]\\]", ""+fov.ra2000Degrees },
						{ "\\[\\[dec2000PMDegrees\\]\\]", (fov.dec2000Degrees >= 0 ? "+" : "")+fov.dec2000Degrees }
				}, null))
			return null;
		return Uri.fromFile(new File(cache.cachePath + "/fullscreen.html"));
	}

	@Override
	protected void onPostExecute(Boolean success) {
		progress.dismiss();
		if (!success) {
			Toast.makeText(context, "Error downloading, try later", Toast.LENGTH_LONG).show();
			return;
		}
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new JavaScriptInterface(context), "AndroidInterface");
		webView.loadUrl(webviewUri.toString());
	}
}
