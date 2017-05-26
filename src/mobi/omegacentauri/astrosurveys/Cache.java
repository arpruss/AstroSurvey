package mobi.omegacentauri.astrosurveys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

class Cache {
	Context context;
	File cacheDir;
	String cachePath;
	public static final int REFRESH_NEVER = -1;
	public static final int REFRESH_ALWAYS = 0;
	public static final int LAST_VERSION = -1;
	public static final String ASSET_URL = "file:///android_asset/";
	
	public Cache(Context c) {
		context = c;
		cacheDir = c.getCacheDir();
		cacheDir.mkdir();
		cachePath = cacheDir.getPath();
		
		int curVersion = -1;
		
		try {
			curVersion = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			curVersion = 0;
		}
		
		SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(c);
		
		if (curVersion != options.getInt("Cache.curVersion", curVersion)) {
			clean();
			options.edit().putInt("Cache.curVersion", curVersion);
		}
	}
	
	private void clean() {
		File[] files = cacheDir.listFiles();
		for (File f : files)
			f.delete();
	}

	InputStream openStream(String url) throws IOException {
		if (url.startsWith(ASSET_URL)) {
			return context.getAssets().open(url.substring(ASSET_URL.length()));
		}
		else {
			return new URL(url).openStream();
		}
	}
	
	public boolean load(String url, String name, int refreshDays, String[][] patches, AsyncTask task) {
		Log.v("AstroSurveys", "requesting "+url+" to "+name);
		File out = new File(cachePath + "/" + name);
		
		if (out.exists() && ( refreshDays == REFRESH_NEVER || 
				( refreshDays > 0 && out.lastModified() + refreshDays * 86400000l >= System.currentTimeMillis() ))) {
			Log.v("AstroSurveys", name+" ready in cache");
			return true;
		}
		
		out.delete();
		
		File temp = null;
		
		try {
			temp = File.createTempFile("temp", ".tmp", cacheDir);
	
			if (patches != null) {
				BufferedReader r = new BufferedReader( new InputStreamReader( openStream(url) ) );
				
				String in = "";
				
				for (String line ; null != (line = r.readLine()); ) {
					if (task != null && task.isCancelled())
						throw new IOException("Cancel");
					in += line+"\n";
				}
				
				r.close();
				
				for (String[] patch: patches) {
					in = in.replaceAll(patch[0], patch[1]);
				}
				
				FileWriter w = new FileWriter(temp);
				w.write(in);
				w.close();
			}			
			else {
				byte[] buffer = new byte[32768];
				InputStream in = openStream(url);
				OutputStream w = new FileOutputStream(temp);
				
				int read;
				while (0 <= (read = in.read(buffer))) {
					if (task != null && task.isCancelled())
						throw new IOException("Cancel");
					w.write(buffer, 0, read);
				}
				in.close();
				w.close();
			}
			
			if (! temp.renameTo(out)) {
				temp.delete();
				return false;
			}
		}
		catch(IOException e) {
			if (temp != null)
				temp.delete();
			return false;
		}
		
		return true;
	}
}