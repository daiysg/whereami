<<<<<<< HEAD
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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

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

	private ImageView imageView;
	private Drawable floorplan;
	private ImageButton zoominButton;
	private ImageButton zoomoutButton;
	private ImageView compassView;
	int scale = 1;
	CharSequence height = "1200";
	CharSequence width = "1000";
	int imagesize = 0;
	boolean scalemodified = true;
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
		setContentView(R.layout.floorplanview);
		imageView = (ImageView) findViewById(R.id.imageView_01);
		imageView.setBackgroundColor(Color.WHITE);
		zoominButton = (ImageButton) findViewById(R.id.zoomin);
		zoomoutButton = (ImageButton) findViewById(R.id.zoomout);
		compassView=(ImageView) findViewById(R.id.compass);
		zoominButton.setVisibility(View.GONE);
		zoomoutButton.setVisibility(View.GONE);
		compassView.setVisibility(View.GONE);
		
		layers = new Drawable[2];
		ld = null;

		if (locationReceiver == null) {
			locationReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					Bundle bundle = intent.getExtras();
					if (bundle.getString("ap_location") != null) {
						Gson gson = new GsonBuilder().serializeNulls().create();
						APLocation apLocation = new APLocation();
						apLocation = gson.fromJson(
								bundle.getString("ap_location"),
								APLocation.class);
						new UpdateFloorplanImageView().execute(apLocation);
					}
				}
			};
		}

		floorplan = getResources().getDrawable(R.drawable.gettingfloormap);
		imageView.setImageDrawable(floorplan);
		imageView.setAdjustViewBounds(true);
		imageView.setOnTouchListener(new Touch());
		// imageView.setScaleType(ScaleType.FIT_XY);

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

	public String getURL(String apname, String accuracy, String floorplan) {

		try {
			String url = Baseurl + "/api/api/GeoserverURLGetter";
			RestClient client = new RestClient(url);
			if (apname != null)
				client.AddParam("apname", apname);
			if (accuracy != null)
				client.AddParam("accuracy", accuracy);
			if (floorplan != null)
				client.AddParam("floorplan", floorplan);
			client.Execute(RequestMethod.GET);
			Log.d(DEBUG_TAG, client.getResponse());
			String result = client.getResponse();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private class UpdateFloorplanImageView extends
			AsyncTask<APLocation, Void, LayerDrawable> {

		@SuppressLint("NewApi")
		@Override
		protected LayerDrawable doInBackground(APLocation... params) {

			APLocation apLocation = params[0];

			String temp_APname = apLocation.getAp_name();
			double temp_accuracy = apLocation.getAccuracy();

			if (scalemodified || APname == null || !temp_APname.equals(APname)) {

				APname = apLocation.getAp_name();
				File file1 = getApplicationContext().getFileStreamPath(
						APname + ".jpg");
				Log.d(DEBUG_TAG, file1.getAbsolutePath());
				if (!file1.exists()) {
					Log.i(DEBUG_TAG, "file does not exist");
					try {

						file1.createNewFile();
						String floormapPng = changeResolution(getURL(APname, null,
								null));
						String floormap=floormapPng.replace("png","jpeg");
						Bitmap bitmap = BitmapFactory
								.decodeStream((InputStream) new URL(floormap)
										.getContent());
						FileOutputStream fos = new FileOutputStream(file1);
						bitmap.compress(CompressFormat.JPEG, 0, fos);
						imagesize=bitmap.getByteCount();
						fos.close();
					    bitmap.recycle();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				{
					Log.i(DEBUG_TAG, "file does exist");
				}

				try {
					BitmapFactory.Options o1 = new BitmapFactory.Options();
					o1.inSampleSize = scale;
					Drawable d0 = BitmapDrawable.createFromResourceStream(
							getResources(), null, new FileInputStream(file1),
							null, o1);
					accuracy = temp_accuracy;
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scale;
					Bitmap bitmap = null;
					String currentpoint = changeResolution(getURL(APname,
							String.valueOf(accuracy), null));
					bitmap = BitmapFactory.decodeStream((InputStream) new URL(
							currentpoint).getContent(), null, o);

					BitmapDrawable d = new BitmapDrawable(getResources(),
							bitmap);

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
			} else if (Math.abs(accuracy - temp_accuracy) > 2) {
				Log.v(DEBUG_TAG, "same ap, diff accuracy: "
						+ (accuracy - temp_accuracy));
				try {
					accuracy = temp_accuracy;
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scale;
					Bitmap bitmap = BitmapFactory.decodeStream(
							(InputStream) new URL(getURL(APname,
									String.valueOf(accuracy), null))
									.getContent(), null, o);
					BitmapDrawable d = new BitmapDrawable(getResources(),
							bitmap);
					layers[1] = d;
					ld = new LayerDrawable(layers);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Log.v(DEBUG_TAG, "receive location service " + APname + " , "
					+ accuracy + ", Thread id: " + Process.myTid());

			return ld;
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(LayerDrawable result) {
			super.onPostExecute(result);
			if (result == null) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.nofloormap));
				// imageView.setScaleType(ScaleType.FIT_XY);
			} else {

				zoominButton.setVisibility(View.VISIBLE);
				zoomoutButton.setVisibility(View.VISIBLE);
				compassView.setVisibility(View.VISIBLE);
				imageView.setScaleType(ScaleType.MATRIX);
				// Drawable drawable = imageView.getDrawable();
				// Rect imageBounds = drawable.getBounds();
				imageView.setImageDrawable(result);
				/*
				 * Matrix matrix=imageView.getMatrix(); RectF drawableRect = new
				 * RectF(0, 0, imageView.getWidth(), imageView.getHeight());
				 * RectF viewRect = new RectF(0, 0, size.x, size.y);
				 * matrix.setRectToRect(drawableRect, viewRect,
				 * Matrix.ScaleToFit.CENTER); imageView.setImageMatrix(matrix);
				 */

				if (scalemodified) {
					
					Matrix m = imageView.getImageMatrix();
					RectF drawableRect = new RectF(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());
					RectF viewRect = new RectF(0, 0, imageView.getWidth(), imageView.getHeight());
					m.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
					imageView.setImageMatrix(m);
					Context context = getApplicationContext();
					// int finalsize
					int finalsize = imagesize / (scale * scale);
					CharSequence text = "Resolution:" + height + "*" + width
							+ "Scale:" + scale + " Size:" + finalsize / 1024
							+ "KB";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
					scalemodified = false;
				}
			}
		}

	}

	public void zoominClick(View view) {
		if (scale > 1 && scale <= 16) {
			scale /= 2;
			Context context = getApplicationContext();
			CharSequence text = "Zoom in!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			scalemodified = true;
			// new UpdateFloorplanImageView().execute();
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
			scalemodified = true;
			// new UpdateFloorplanImageView().execute();
		} else {
			Context context = getApplicationContext();
			CharSequence text = "You have reached the smallest scale. Cannot zoom out!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	public String changeResolution(String url) {
		String result1 = url.replace("1790", width);
		return result1.replace("1858", height);
		
	}
}
=======
package sg.edu.nus.ami.wifilocation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

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
			 final int heightRatio = Math.round((float) o.outWidth/ (float) REQUIRED_SIZE);
			 final int widthRatio = Math.round((float) o.outHeight / (float) REQUIRED_SIZE);            
			 scale = heightRatio < widthRatio ? heightRatio : widthRatio;
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
>>>>>>> bdd973fdb4d6fbbf9246ab45b0a8a3c5bfc2b34c
