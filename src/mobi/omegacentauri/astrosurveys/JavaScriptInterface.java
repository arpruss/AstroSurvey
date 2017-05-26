package mobi.omegacentauri.astrosurveys;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class JavaScriptInterface {
	AstroSurveys context;

	public JavaScriptInterface(AstroSurveys context) {
		this.context = context;
	}
	
	public void log(String message) {
		Log.v("AstroSurveys", "jsmsg:"+message);
	}

	public void share(String ra, String dec, String size) {
		context.share(Math.toRadians(Double.parseDouble(ra)), 
				Math.toRadians(Double.parseDouble(dec)),
				Math.toRadians(Double.parseDouble(size)));
	}
	
	public void shareSkySafari(String ra, String dec, String size) {
		((FromSkySafari)context).shareSkySafari(Math.toRadians(Double.parseDouble(ra)), 
				Math.toRadians(Double.parseDouble(dec)),
				Math.toRadians(Double.parseDouble(size)));
	}
	
	
}