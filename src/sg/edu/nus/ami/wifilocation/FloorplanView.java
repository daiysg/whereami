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
import android.widget.ImageView.ScaleType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This Activity is part of the whereami android application
 * It is to show the floor map where the person is based on the WiFi location
 * input: wifi label in term of I3-02-02
 * output: floorplan of I3-02
 * 
 * @author qinfeng
 *
 */
public class FloorplanView extends Activity {
	private static final String DEBUG_TAG = "FloorplanView";
	private static final String Baseurl = "http://nuslivinglab.nus.edu.sg";


	MyImageView imageView;
	Drawable floorplan;
//	String bdg_floor;
	String APname;
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
					String temp_APname = apLocation.getAp_name();
//					String temp_bdg_floor = APname.substring(0, APname.indexOf("AP") - 1);
					if(APname==null|| !temp_APname.equals(APname)){
//						bdg_floor = temp_bdg_floor;
						APname = temp_APname;
//						
//						String[] _s = bdg_floor.split("[-]+");
						floorplan = LoadImageFromWebOperations(getURL(APname));
						Handler h = new Handler();
						h.post(new Runnable() {
							
							public void run() {
								imageView.setImageDrawable(floorplan);								
							}
						});
						
					}else{
						//do nothing	
					}
					
					Log.v(DEBUG_TAG, "receive location service "+APname);
				}
			};
		}

		floorplan = getResources().getDrawable(R.drawable.gettingfloormap);
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
		Log.d(DEBUG_TAG,"onResume, register locationrecevier");
		
	}
	
	public void onPause(){
		super.onPause();
		unregisterReceiver(locationReceiver);
		Log.d(DEBUG_TAG,"onPause, unregister locationreceiver");
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

	public String getURL(String apname) {
		
		try {		
			String url = Baseurl+"/api/api/GeoserverURLGetter";
			RestClient client = new RestClient(url);
			client.AddParam("apname", apname);
			client.Execute(RequestMethod.GET);
			return client.getResponse();
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
