package sg.edu.nus.ami.wifilocation;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;



//public class MapTabView extends MapActivity {
//	private static final String DEBUG_TAG = "AP_Location_MapActivity";
//	
//	MapView mapView;
//	MapController mapController;
//	MyItemizedOverlay itemizedOverlay;
//	Drawable drawable;
//	List<Overlay> mapOverlays;
//	CircleOverlay circleOverlay;
//	
//	//added by jianbin
//	MyLocationOverlay compass;
//	MyLocationOverlay locationoverlay;
//	
//	@Override
//	
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		setContentView(R.layout.maptabview);
//		
//		Log.d(DEBUG_TAG, "maptabview onCreate()");
//		
//		mapView = (MapView) findViewById(R.id.mapview);
//		
////		mapView.setBuiltInZoomControls(true);
//		mapController = mapView.getController();
////		GeoPoint pointIDMI = new GeoPoint(1292533, 103775788); 
////		mapController.setCenter(pointIDMI);
////		mapController.setZoom(15);
////		
//		//mapOverlays = mapView.getOverlays();
//		
////		Resources res = this.getResources();
////		drawable = res.getDrawable(R.drawable.pin);
////		itemizedOverlay = new MyItemizedOverlay(drawable,this);
////		circleOverlay = new CircleOverlay(this);
////		
////		SharedPreferences preferences = getSharedPreferences(BasicWifiLocation.PREFERENCES, MODE_PRIVATE);
////		if (preferences.contains(BasicWifiLocation.LOCATION_AP_LAT)){
////			double lat = Double.valueOf(preferences.getString(BasicWifiLocation.LOCATION_AP_LAT, ""));
////			double lon = Double.valueOf(preferences.getString(BasicWifiLocation.LOCATION_AP_LONG, ""));
////			GeoPoint mypoint = new GeoPoint((int)(lat*1000000),(int)(lon*1000000));
////			String snippet = preferences.getString(BasicWifiLocation.LOCATION_BUILDING,"")+"\n"+preferences.getString(BasicWifiLocation.LOCATION_AP_LOCATION, "")
////				+"\n"+preferences.getString(BasicWifiLocation.LOCATION_ACCURACY, "");
////			
////			OverlayItem overlayitem = new OverlayItem(mypoint, "My Point", snippet);
////			itemizedOverlay.addOverlay(overlayitem);
////			
////			circleOverlay.setmLat(lat);
////			circleOverlay.setmLon(lon);
////			circleOverlay.setCircleRadius(Math.abs(Double.valueOf(preferences.getString(BasicWifiLocation.LOCATION_ACCURACY, null))));
////		}
////		mapOverlays.add(itemizedOverlay);
////		mapOverlays.add(circleOverlay);
//		
//		
//		//----------------
//
//		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		Location currentLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		
//
//	      
//	    GeoPoint point = new GeoPoint((int) (currentLocation.getLatitude() * 1000000), (int) (currentLocation.getLongitude()* 1000000));
//			
//			//GeoPoint point = new GeoPoint(51643234, 7848593);
//			
//	    mapController.animateTo(point);
//	    mapController.setZoom(17);
//		
//		
//		locationoverlay=new MyLocationOverlay(this,mapView);
//		mapView.getOverlays().add(locationoverlay);
//		
//		compass = new MyLocationOverlay(this, mapView);
//		mapView.getOverlays().add(compass);
//	
//	}
//	@Override
//	protected void onResume() {
//		// TODO Auto-generated method stub
//		compass.enableCompass();
//		locationoverlay.enableMyLocation();
//		super.onResume();
//	
//	}
//	@Override
//	protected void onPause() {
//		// TODO Auto-generated method stub
//		compass.disableCompass();
//		locationoverlay.disableMyLocation();
//		super.onPause();
//
//	}
//	@Override
//	protected boolean isRouteDisplayed() {
//		return false;
//	}
//	
//}

public class MapTabView extends MapActivity implements LocationListener{
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
		map = (MapView)findViewById(R.id.mapview);
		map.setBuiltInZoomControls(true);

	String tag = "here";
	Log.d(tag, "where");
		compass = new MyLocationOverlay(this, map);
		map.getOverlays().add(compass);
		controller = map.getController();
		
		
	LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	Location currentLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	
	//SharedPreferences preferences = getSharedPreferences(BasicWifiLocation.PREFERENCES, MODE_PRIVATE);
	//double lat = Double.valueOf(preferences.getString(BasicWifiLocation.LOCATION_AP_LAT, ""));
    //double lon = Double.valueOf(preferences.getString(BasicWifiLocation.LOCATION_AP_LONG, ""));
    
    double lat = 1.292570;
    double lon = 103.775919;
    double lat2 = 1.292580;
     
//    GeoPoint point = new GeoPoint((int) (lat* 1000000), (int) (lon* 1000000));
//    GeoPoint point2 = new GeoPoint((int) (lat2* 1000000), (int) (lon* 1000000));

	//GeoPoint point2 = new GeoPoint(143860,11552297);
	//OverlayItem overlayItem2 = new OverlayItem(point2, "whats upppp", "second");
	
	//working below
		if(currentLocation!=null){
			
		  GeoPoint point = new GeoPoint((int) (currentLocation.getLatitude() * 1000000), (int) (currentLocation.getLongitude()* 1000000));
			
		controller.animateTo(point);
		controller.setZoom(17);
		d = getResources().getDrawable(R.drawable.pin);

		//built in overlay
		locationoverlay=new MyLocationOverlay(this,map);
		map.getOverlays().add(locationoverlay);
		
		}
	
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		compass.disableCompass();
		locationoverlay.disableMyLocation();
		super.onPause();

	}
	@Override
	protected void onResume() {

		
		//enable for overlay to work
		compass.enableCompass();
		locationoverlay.enableMyLocation();
		super.onResume();

	}
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void onLocationChanged(Location l) {
		// TODO Auto-generated method stub

		map.getOverlays().add(locationoverlay);
	}
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		/// Video 145 för att se hur man sätter denna i bruk!	
	}
}
