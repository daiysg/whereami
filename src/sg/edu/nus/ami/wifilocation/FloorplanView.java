package sg.edu.nus.ami.wifilocation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;

import javax.security.auth.PrivateCredentialPermission;

import sg.edu.nus.ami.wifilocation.api.APLocation;
import sg.edu.nus.ami.wifilocation.api.RequestMethod;
import sg.edu.nus.ami.wifilocation.api.RestClient;
import sg.edu.nus.ami.wifilocation.api.ServiceLocation;
import android.R.string;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
//import android.widget.ImageView;
//import android.widget.ImageView.ScaleType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This Activity is part of the whereami android application It is to show the
 * floor map where the person is based on the WiFi location input: wifi label in
 * term of I3-02-02 output: floorplan of I3-02
 * 
 * @author qinfeng
 * 
 */
public class FloorplanView extends Activity {
	private static final String DEBUG_TAG = "FloorplanView";
	private static final String Baseurl = "http://nuslivinglab.nus.edu.sg";

	// MyImageView imageView;
	private ImageView imageView;
	private Drawable floorplan;
	private Bitmap bm_floorplan;
	private ImageButton zoominButton;
	// String bdg_floor;
	private String APname;
	int scale = 1;
	private BroadcastReceiver locationReceiver;
	int REQUIRED_SIZE = 700;
	private ImageButton zoomoutButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.floorplanview);

		Log.d(DEBUG_TAG, "floorplanview tab");
		// imageView = new MyImageView(getApplicationContext());
		imageView = (ImageView) findViewById(R.id.imageView_01);
		zoominButton = (ImageButton) findViewById(R.id.zoomin);
		zoomoutButton = (ImageButton) findViewById(R.id.zoomout);
		zoominButton.setVisibility(View.GONE);
		zoomoutButton.setVisibility(View.GONE);
		if (locationReceiver == null) {
			locationReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					// String action = intent.getAction();
					Bundle bundle = intent.getExtras();
					Gson gson = new GsonBuilder().serializeNulls().create();
					APLocation apLocation = new APLocation();
					apLocation = gson.fromJson(bundle.getString("ap_location"),
							APLocation.class);
					String temp_APname = apLocation.getAp_name();
					// String temp_bdg_floor = APname.substring(0,
					// APname.indexOf("AP") - 1);
					if (APname == null || !temp_APname.equals(APname)) {
						// bdg_floor = temp_bdg_floor;
						APname = temp_APname;
						getFitScales(getURL(APname));
						// TODO: start an asynctask to update the imageview
						new BitmapWorkerTask(imageView).execute(getURL(APname));
					} else {
						// do nothing
					}

					Log.v(DEBUG_TAG, "receive location service " + APname);
				}
			};
		}

		floorplan = getResources().getDrawable(R.drawable.gettingfloormap);
		imageView.setImageDrawable(floorplan);
		imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ScaleType.MATRIX);
		// setContentView(imageView);

	}

	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ServiceLocation.BROADCAST_ACTION);
		registerReceiver(locationReceiver, filter);
		Log.d(DEBUG_TAG, "onResume, register locationrecevier");

	}

	public void onPause() {
		super.onPause();
		unregisterReceiver(locationReceiver);
		Log.d(DEBUG_TAG, "onPause, unregister locationreceiver");
	}

	@SuppressWarnings("unused")
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
			String url = Baseurl + "/api/api/GeoserverURLGetter";
			RestClient client = new RestClient(url);
			client.AddParam("apname", apname);
			client.Execute(RequestMethod.GET);
			return client.getResponse();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void getFitScales(String url) {
		InputStream is;
		try {			
			is = (InputStream) new URL(url).getContent();
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, o);
			// The new size we want to scale to
			final int REQUIRED_SIZE = 700;
			// Find the correct scale value. It should be the power of 2.
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Decode image size

	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeInputStream(String url) {
		try {
			// InputStream is = (InputStream) new URL(url).getContent();
			// Decode image size

			/*
			 * BitmapFactory.Options o = new BitmapFactory.Options();
			 * o.inJustDecodeBounds = true; BitmapFactory.decodeStream(is, null,
			 * o);
			 * 
			 * //The new size we want to scale to final int REQUIRED_SIZE=700;
			 */

			// Find the correct scale value. It should be the power of 2.
			/*
			 * while(o.outWidth/scale/2>=REQUIRED_SIZE &&
			 * o.outHeight/scale/2>=REQUIRED_SIZE) scale*=2;
			 */
			// Decode with inSampleSize

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			InputStream is1 = (InputStream) new URL(url).getContent();
			return BitmapFactory.decodeStream(is1, null, o2);
		} catch (Exception e) {
		}
		return BitmapFactory.decodeResource(getResources(),
				R.drawable.nofloormap);
	}

	class BitmapWorkerTask extends AsyncTask<String, Integer, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private int data = 0;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			zoominButton.setVisibility(View.VISIBLE);
			zoomoutButton.setVisibility(View.VISIBLE);
			// enable touch
			imageView.setOnTouchListener(new Touch());
		}

		// Decode image in background.
		protected Bitmap doInBackground(String... params) {
			Bitmap result = decodeInputStream(params[0]);
			return result;
		}

		protected void onProgressUpdate(Integer progress) {
			setProgress(progress);
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	public void zoominClick(View view) {
		if (scale > 1 && scale <=16) {
			scale /= 2;
			Context context = getApplicationContext();
			CharSequence text = "Zoom in!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			new BitmapWorkerTask(imageView).execute(getURL(APname));
		} else {
			Context context = getApplicationContext();
			CharSequence text = "You have reached the largest scale. Cannot zoom in!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	public void zoomoutClick(View view) {
		if (scale >= 1 && scale < 16) {
			scale *= 2;
			Context context = getApplicationContext();
			CharSequence text = "Zoom out!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			new BitmapWorkerTask(imageView).execute(getURL(APname));
		} else {
			Context context = getApplicationContext();
			CharSequence text = "You have reached the smallest scale. Cannot zoom out!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}
	 
}
