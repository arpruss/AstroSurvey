package mobi.omegacentauri.astrosurveys;

import android.util.Log;

public class FOV {
	public static final double DEG2RAD = Math.PI/180;

	double alt = Double.NaN; // rad
	double az = Double.NaN;  // rad
	double sizeDegrees = Double.NaN;
	double lat = Double.NaN;
	double lon = Double.NaN;
	double jd = Double.NaN;
	double ra2000Degrees = Double.NaN;  //degrees
	double dec2000Degrees = Double.NaN; //degrees
	double dec; //radians
	double ra;  //radians

	public FOV() {
	}

	public FOV(double ra2000, double dec2000, double size) {
		Log.v("AstroSurvey", "FOV:"+ra2000+" "+dec2000+" "+size);
		this.ra2000Degrees = ra2000;
		this.dec2000Degrees = dec2000;
		this.sizeDegrees = size;
	}

	boolean equals(FOV b) {
		return b != null && b.ra2000Degrees == ra2000Degrees && b.dec2000Degrees == dec2000Degrees && b.sizeDegrees == sizeDegrees;
	}

	boolean preValid() {
		return ! Double.isNaN(alt) && ! Double.isNaN(az) && ! Double.isNaN(sizeDegrees) &&
				! Double.isNaN(lat) && ! Double.isNaN(lon) && ! Double.isNaN(jd);
	}

	boolean valid() {
		return ! Double.isNaN(ra2000Degrees) && ! Double.isNaN(dec2000Degrees);
	}
	
	double getLMST() {
		double jd0 = Math.floor(jd-0.5)+0.5;
		double H = (jd - jd0) * 24;
		double D0 = jd0 - 2451545.0;
		double T = (jd - 2451545.0) / 36525;
		double gmst = 6.697374558 + 0.06570982441908 * D0 + 1.00273790935 * H + 0.000026 * T * T; // in hours
		return (gmst + lon * 12/Math.PI) % 24;
	}

	void computeRADecFromAltAz() {
		Log.v("AstroSurveys", "lat="+lat/DEG2RAD+" lon="+lon/DEG2RAD+" az="+az/DEG2RAD+" alt="+alt/DEG2RAD);
		Log.v("AstroSurveys", "jd="+jd);
		dec = Math.asin(Math.sin(lat)*Math.sin(alt)+Math.cos(lat)*Math.cos(alt)*Math.cos(az));
		double ha = Math.atan2( -Math.sin(az)*Math.cos(alt), 
				-Math.cos(az)*Math.sin(lat)*Math.cos(alt)+Math.sin(alt)*Math.cos(lat));
		//GMST from http://aa.usno.navy.mil/faq/docs/GAST.php
		double lmst = getLMST();
		Log.v("AstroSurveys", "ha="+ha * 12/Math.PI);
		Log.v("AstroSurveys", "lmst="+lmst);
		ra = (lmst * Math.PI/12 - ha) % (2*Math.PI);
		if (ra < 0)
			ra += 2 * Math.PI;
	}
	
	void computeAltAzFromRaDec2000() {
		double y = (jd - 2451545.0)/365.25;

		ra = Math.toRadians( ra2000Degrees + (180 / 12) * (3.075 + 1.336 * Math.sin(Math.toRadians(ra2000Degrees)) * Math.tan(Math.toRadians(dec2000Degrees))) * y / (60 * 60));
		dec = Math.toRadians( dec2000Degrees + 20.04 * Math.cos(Math.toRadians(ra2000Degrees)) * y / (60 * 60) );
		
		Log.v("AstroSurveys", "ra="+ra+" dec="+dec);

		double ha = (getLMST() * Math.PI/12 - ra) % (2 * Math.PI);
		
		Log.v("AstroSurveys", "ha="+(ha * 12 / Math.PI));

		double celX = Math.cos(ha)*Math.cos(dec);
		double celY = Math.sin(ha)*Math.cos(dec);
		double celZ = Math.sin(dec);
		
		double horX = celX*Math.sin(lat)-celZ*Math.cos(lat);
		double horY = celY;
		double horZ = celX*Math.cos(lat)+celZ*Math.sin(lat);
		
		Log.v("AstroSurveys", ""+horX+" "+horY+" "+horZ);
		
		az = Math.atan2(-horY, -horX);
		
		if (az < 0)
			az += Math.PI * 2;
		
		alt = Math.asin(horZ);
	}

	void computeRaDec2000FromAltAz() {
		computeRADecFromAltAz();

		// These should be within about a second: http://www.cv.nrao.edu/~rfisher/Ephemerides/earth_rot.html
		//RA = RA(2000) + (3.075 + 1.336 * sin(RA) * tan(Dec)) * y
		//Dec = Dec(2000) + 20.04 * cos(RA) * y

		double y = (jd - 2451545.0)/365.25;

		ra2000Degrees = (ra * 12 / Math.PI - (3.075 + 1.336 * Math.sin(ra) * Math.tan(dec)) * y / (60 * 60)) * (180/12);
		dec2000Degrees = dec * 180 / Math.PI - 20.04 * Math.cos(ra) * y / (60 * 60);

		if (ra2000Degrees < 0)
			ra2000Degrees += 360;

		Log.v("AstroSurveys", "y="+y+" RA="+(ra*12/Math.PI)+" Dec="+(dec*180/Math.PI)+" JD="+jd+" RA2K="+ra2000Degrees*12/180+" Dec2K="+dec2000Degrees);
	}
}

