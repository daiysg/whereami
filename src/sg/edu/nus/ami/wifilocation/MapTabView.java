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
import com.google.android.maps.OverlayItem;

public class MapTabView extends MapActivity {
	/** Called when the activity is first created. */
	MapView map;
	long start;
	long stop;
	MyLocationOverlay compass;
	MapController controller;
	int x, y;
	GeoPoint toutchedPoint;
	Drawable defautlmarker;
	List<Overlay> overlayList;
	LocationManager lm;

	MyItemizedOverlay itemizedOverlay;
	List<Overlay> mapOverlays;

	MyLocationOverlay locationoverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maptabview);
		map = (MapView) findViewById(R.id.mapview);
		map.setBuiltInZoomControls(true);
		mapOverlays = map.getOverlays();
		controller = map.getController();

		defautlmarker = getResources().getDrawable(R.drawable.pin);
		itemizedOverlay = new MyItemizedOverlay(defautlmarker, this);
		
		//============show a pin at i3 building=================//
		double lat = 1.292409;
		double lon = 103.775707;
		GeoPoint i3 = new GeoPoint((int) (lat* 1000000), (int) (lon*1000000));
		OverlayItem i3building = new OverlayItem(i3, "I3 building", "21 Heng Meng Keng Terrace");
		itemizedOverlay.addOverlay(i3building);
		mapOverlays.add(itemizedOverlay);
		//=========== end of showing pin at i3 building =========== //		

		locationoverlay = new MyLocationOverlay(this, map);
		mapOverlays.add(locationoverlay);
		GeoPoint point = locationoverlay.getMyLocation();
		if(point != null){
			controller.animateTo(point);
			controller.setZoom(17);
		}else{
			controller.animateTo(i3);
			controller.setZoom(15);
		}
	}

	@Override
	protected void onPause() {
		locationoverlay.disableCompass();
		locationoverlay.disableMyLocation();
		super.onPause();

	}

	@Override
	protected void onResume() {
		locationoverlay.enableCompass();
		locationoverlay.enableMyLocation();
		super.onResume();

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
