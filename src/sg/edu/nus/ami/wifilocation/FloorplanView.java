package sg.edu.nus.ami.wifilocation;

import java.io.InputStream;
import java.net.URL;

import sg.edu.nus.ami.wifilocation.api.APLocation;
import sg.edu.nus.ami.wifilocation.api.RequestMethod;
import sg.edu.nus.ami.wifilocation.api.RestClient;
import sg.edu.nus.ami.wifilocation.api.ServiceLocation;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FloorplanView extends Activity {
	private static final String DEBUG_TAG = "FloorplanView";

	MyImageView imageView;
	Drawable floorplan;
	String bdg_floor;
	BroadcastReceiver locationReceiver;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.floorplanview);

		Log.d(DEBUG_TAG, "floorplanview tab");
		imageView = new MyImageView(getApplicationContext());

//		imageView = (MyImageView) findViewById(R.id.imageView_01);
		
		if(locationReceiver==null){
			locationReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					Bundle bundle = intent.getExtras();
					Gson gson = new GsonBuilder().serializeNulls().create();
					APLocation apLocation = new APLocation();
					apLocation = gson.fromJson(bundle.getString("ap_location"), APLocation.class);
					String APname = apLocation.getAp_name();
					String temp_bdg_floor = APname.substring(0, APname.indexOf("AP") - 1);
					if(bdg_floor==null|| !temp_bdg_floor.equals(bdg_floor)){
						bdg_floor = temp_bdg_floor;
						Log.v(DEBUG_TAG, bdg_floor+" updated ");
						
						String[] _s = bdg_floor.split("[-]+");
						Log.v(DEBUG_TAG, floorplan.toString());
						floorplan = LoadImageFromWebOperations(getURL(_s[0], _s[1]));
						Log.v(DEBUG_TAG, floorplan.toString());
						Handler h = new Handler();
						h.post(new Runnable() {
							
							public void run() {
								imageView.setImageDrawable(floorplan);								
							}
						});
						
					}else{
						//do nothing	
						Log.v(DEBUG_TAG, bdg_floor+" is the same");
					}
					
					Log.v(DEBUG_TAG, "receive location service "+APname);
				}
			};
		}

		floorplan = getResources().getDrawable(R.drawable.nofloormap);
		imageView.setImageDrawable(floorplan);
		imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ScaleType.MATRIX);
		// enable touch
		imageView.setOnTouchListener(new Touch());
		
		setContentView(imageView);

	}
	
	public void onResume(){
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ServiceLocation.BROADCAST_ACTION);
		registerReceiver(locationReceiver, filter);
		
	}
	
	public void onPause(){
		super.onPause();
		unregisterReceiver(locationReceiver);
	}

	private Drawable LoadImageFromWebOperations(String url) {
		try {
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is, "src name");
			Log.v(DEBUG_TAG, url);
			return d;
		} catch (Exception e) {
			System.out.println("Exc=" + e);
		}
		
		return getResources().getDrawable(R.drawable.nofloormap);
	}

	public String getURL(String building, String floor) {

		String url = "http://172.18.101.125:8080/geoserver/wms?"
				+ "Layers=nus%3Afloors%2Cnus%3Arooms%2Cnus%3Alinks"
				+ "&service=wms" + "&request=getmap" + "&format=image%2Fpng"
				+ "&srs=EPSG%3A4326" + "&version=1.1.1";

		// try to append more parameters to the URL
		try {

			String filter = "building=%27" + building + "%27%20and%20floor=%27"
					+ floor + "%27";

			String bbox;
			int width;
			int height;
			int zoomlevel = 2;

			// get bbox from webservice
			// http://172.18.101.125:8080/api1/MapBbox?building=CCE
			String bbox_url = "http://172.18.101.125:8080/api1/MapBbox";
			RestClient client = new RestClient(bbox_url);
			client.AddParam("building", building);
			client.Execute(RequestMethod.GET);

			Bbox box = new Bbox();
			Gson gson = new GsonBuilder().serializeNulls().create();
			box = gson.fromJson(client.getResponse(), Bbox.class);
			bbox = box.toString();

			width = (int) ((box.maxX - box.minX) * 1000000) * zoomlevel;
			height = (int) ((box.maxY - box.minY) * 1000000) * zoomlevel;

			url = url + "&width=" + String.valueOf(width) + "&height="
					+ String.valueOf(height) + "&cql_filter=" + filter + "%3B"
					+ filter + "%3B" + filter + "&bbox=" + bbox;

			Toast.makeText(
					this,
					"Loading Floor Plan : " + building + "-" + floor
							+ "\nPlease Wait !", Toast.LENGTH_LONG).show();
			Log.d("locationap ", building + "," + floor);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return url;
	}

	public class Bbox {
		double minX;
		double minY;
		double maxX;
		double maxY;

		public String toString() {
			return String.valueOf(minX) + "," + String.valueOf(minY) + ","
					+ String.valueOf(maxX) + "," + String.valueOf(maxY);
		}
	}

}
