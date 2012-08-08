package sg.edu.nus.ami.wifilocation;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MapTabView extends MapActivity implements LocationListener {
	/** Called when the activity is first created. */
	MapView map;
	long start;
	long stop;
	MyLocationOverlay compass;
	MapController controller;
	int x, y;
	GeoPoint toutchedPoint;
	Drawable d;
	List<Overlay> overlayList;
	LocationManager im;
	String towers;
	int lat = 0;
	int longi = 0;
	Drawable drawable;

	MyItemizedOverlay itemizedOverlay;
	List<Overlay> mapOverlays;

	MyLocationOverlay locationoverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maptabview);
		map = (MapView) findViewById(R.id.mapview);
		map.setBuiltInZoomControls(true);

		compass = new MyLocationOverlay(this, map);
		map.getOverlays().add(compass);
		controller = map.getController();

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				// makeUseOfNewLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
				locationListener);
		Location currentLocation = lm
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		// SharedPreferences preferences =
		// getSharedPreferences(BasicWifiLocation.PREFERENCES, MODE_PRIVATE);
		// double lat =
		// Double.valueOf(preferences.getString(BasicWifiLocation.LOCATION_AP_LAT,
		// ""));
		// double lon =
		// Double.valueOf(preferences.getString(BasicWifiLocation.LOCATION_AP_LONG,
		// ""));

		double lat = 1.292570;
		double lon = 103.775919;
		double lat2 = 1.292580;

		// GeoPoint point = new GeoPoint((int) (lat* 1000000), (int) (lon*
		// 1000000));
		// GeoPoint point2 = new GeoPoint((int) (lat2* 1000000), (int) (lon*
		// 1000000));

		// GeoPoint point2 = new GeoPoint(143860,11552297);
		// OverlayItem overlayItem2 = new OverlayItem(point2, "whats upppp",
		// "second");

		// working below
		if (currentLocation != null) {

			GeoPoint point = new GeoPoint(
					(int) (currentLocation.getLatitude() * 1000000),
					(int) (currentLocation.getLongitude() * 1000000));

			controller.animateTo(point);
			controller.setZoom(17);
			d = getResources().getDrawable(R.drawable.pin);

			// built in overlay
			locationoverlay = new MyLocationOverlay(this, map);
			map.getOverlays().add(locationoverlay);

		}

	}

	@Override
	protected void onPause() {
		compass.disableCompass();
		locationoverlay.disableMyLocation();
		super.onPause();

	}

	@Override
	protected void onResume() {

		// enable for overlay to work
		compass.enableCompass();
		locationoverlay.enableMyLocation();
		super.onResume();

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onLocationChanged(Location l) {

		map.getOverlays().add(locationoverlay);
	}

	public void onProviderDisabled(String arg0) {

	}

	public void onProviderEnabled(String arg0) {

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}
}
