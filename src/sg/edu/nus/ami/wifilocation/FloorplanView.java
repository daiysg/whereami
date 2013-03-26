package sg.edu.nus.ami.wifilocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
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
	Bitmap bm_floorplan;
	String APname;
	double accuracy;
	BroadcastReceiver locationReceiver;
	
	Drawable[] layers; 
	LayerDrawable ld;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(DEBUG_TAG, "floorplanview tab");
		imageView = new MyImageView(getApplicationContext());
		layers = new Drawable[2];
		ld = null;
		
		if(locationReceiver==null){
			locationReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					Bundle bundle = intent.getExtras();
					Gson gson = new GsonBuilder().serializeNulls().create();
					APLocation apLocation = new APLocation();
					apLocation = gson.fromJson(bundle.getString("ap_location"), APLocation.class);
					
					new UpdateFloorplanImageView().execute(apLocation);
				}
			};
		}

		floorplan = getResources().getDrawable(R.drawable.gettingfloormap);
		imageView.setImageDrawable(floorplan);
		imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ScaleType.MATRIX);
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

	public String getURL(String apname, String accuracy, String floorplan) {
		
		try {		
			String url = Baseurl+"/api/api/GeoserverURLGetter";
			RestClient client = new RestClient(url);
			if(apname != null)
				client.AddParam("apname", apname);
			if(accuracy != null)
				client.AddParam("accuracy", accuracy);
			if(floorplan != null)
				client.AddParam("floorplan", floorplan);
			client.Execute(RequestMethod.GET);
			Log.d(DEBUG_TAG, client.getResponse());
			return client.getResponse();
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private class UpdateFloorplanImageView extends AsyncTask<APLocation, Void, LayerDrawable>{

		@Override
		protected LayerDrawable doInBackground(APLocation... params) {

			APLocation apLocation = params[0];
			
			String temp_APname = apLocation.getAp_name();
			double temp_accuracy = apLocation.getAccuracy();
			
			if(APname == null ||!temp_APname.equals(APname)){
				APname = apLocation.getAp_name();
				File file1 = getApplicationContext().getFileStreamPath(APname+".png");
				Log.d(DEBUG_TAG, file1.getAbsolutePath());
				if(!file1.exists()){
					Log.i(DEBUG_TAG, "file does not exist");
					try {
						file1.createNewFile();
						Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(getURL(APname, null, null)).getContent());
						FileOutputStream fos = new FileOutputStream(file1);
						bitmap.compress(CompressFormat.PNG, 0, fos);
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}{
					Log.i(DEBUG_TAG, "file does exist");
				}
				
				try {
					BitmapFactory.Options o1 = new BitmapFactory.Options();
					o1.inSampleSize = 4;							
					Drawable d0 = BitmapDrawable.createFromResourceStream(getResources(), null, new FileInputStream(file1), null, o1);
					
					accuracy = temp_accuracy;
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = 4;
					Bitmap bitmap;
					bitmap = BitmapFactory.decodeStream((InputStream) new URL(getURL(APname, String.valueOf(accuracy), null)).getContent(), null, o);
					BitmapDrawable d = new BitmapDrawable(getResources(),bitmap);
					layers[0] = d0;
					layers[1] = d;
					
					ld = new LayerDrawable(layers);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}else if (Math.abs(accuracy - temp_accuracy)>2) {
				Log.v(DEBUG_TAG, "same ap, diff accuracy: "+ (accuracy - temp_accuracy));
				try {
					accuracy = temp_accuracy;
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = 4;
					Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(getURL(APname, String.valueOf(accuracy), null)).getContent(), null, o);
					BitmapDrawable d = new BitmapDrawable(getResources(),bitmap);
					layers[1] = d;
					ld = new LayerDrawable(layers);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.v(DEBUG_TAG, "receive location service "+APname+" , "+accuracy+", Thread id: "+Process.myTid());
			
			return ld;
		}

		@Override
		protected void onPostExecute(LayerDrawable result) {
			super.onPostExecute(result);
			if(result == null){
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.gettingfloormap));
			}else{
				imageView.setImageDrawable(result);
			}
		}
		
	}
	

}
