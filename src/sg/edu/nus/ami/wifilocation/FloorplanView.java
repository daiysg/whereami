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
import android.R.integer;
import android.R.string;
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
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
 * Modified in Aug 1: User can input the APname he wants to type in. Output: floorplan and aplocation of the user input floor.
 * 
 * @author qinfeng, Dai Yuan
 * 
 */
public class FloorplanView extends Activity implements OnTouchListener {
	private static final String DEBUG_TAG = "FloorplanView";
	private static final String Baseurl = "http://nuslivinglab.nus.edu.sg";
	// These matrices used to move and zoom image
	private Matrix floorMatrix = new Matrix();
	private Matrix compassMatrix = new Matrix();
	private Matrix savedFloorMatrix = new Matrix();
	private Matrix savedCompassMatrix = new Matrix();

	// 3 states
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;
	private float d = 0f;
	private float[] floorLastEvent = null;
	
	//Imageview: the imageview to display map
	private ImageView imageView;
	private Drawable floorplan;
	private ImageButton zoominButton;
	private ImageButton zoomoutButton;
	private ImageView compassView;
	private CheckBox locationLockCheckbox;
	private EditText buildingEditText;
	private EditText floorEditText;
	private EditText apEditText;
	private int scale = 1;
	private boolean scalemodified = true;
	private boolean locationlocked=false;
	
	private EditText editText01;
	private EditText editText02;
	private EditText editText03;
	private Button changeLocationButton;
	
	private String APname;
	private double accuracy;
	private BroadcastReceiver locationReceiver;

	Drawable[] layers;
	LayerDrawable ld;


	/**
	 * Set up the floorplan display activity 
	 * 
	 * @param imageView the main floorplan imageview
	 * @param locationLockCheckbox checkbox checked when the location is set by user
	 * @param buildingEditText edittext for user to type in the building name, like S16, BIZ2 etc.
	 * @param floorEditText edittext for user to type in the floor no. : like 02, B1 etc.
	 * @param apEditText edittext for user to type in the APName no
	 * @param compassView the view of the compass
	 * @param editText01 edittext of label "building", readonly
	 * @param editText02 edittext of label "floor", readonly
	 * @param editText03 edittext of label "APName", readonly
	 * @param changeLocationButton Onclick for user to change another floorplan to display
	 * @param zoominButton onclick for user to zoomin
	 * @param zoomoutButton onclick for user to zoomout
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(DEBUG_TAG, "floorplanview tab");
		setContentView(R.layout.floorplanview);
		imageView = (ImageView) findViewById(R.id.imageView_01);
		imageView.setBackgroundColor(Color.WHITE);
		locationLockCheckbox= (CheckBox) findViewById(R.id.LocationLock1);
		locationLockCheckbox.setChecked(false);
		locationLockCheckbox.setTextColor(Color.BLACK);
		buildingEditText=(EditText) findViewById(R.id.buidlingname);
		floorEditText=(EditText) findViewById(R.id.floorname);
		apEditText=(EditText)findViewById(R.id.apName);
		zoominButton = (ImageButton) findViewById(R.id.zoomin);
		zoomoutButton = (ImageButton) findViewById(R.id.zoomout);
		compassView = (ImageView) findViewById(R.id.compass);
		editText01=(EditText) findViewById(R.id.EditText01);
		editText02=(EditText) findViewById(R.id.EditText02);
		editText03=(EditText) findViewById(R.id.EditText03);
		changeLocationButton=(Button) findViewById(R.id.button1);
		zoominButton.setVisibility(View.GONE);
		zoomoutButton.setVisibility(View.GONE);
		compassView.setVisibility(View.GONE);
		buildingEditText.setVisibility(View.GONE);
		floorEditText.setVisibility(View.GONE);
		apEditText.setVisibility(View.GONE);
		locationLockCheckbox.setVisibility(View.GONE);
		editText01.setVisibility(View.GONE);
		editText02.setVisibility(View.GONE);
		editText03.setVisibility(View.GONE);
		changeLocationButton.setVisibility(View.GONE);
		

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
		floorMatrix.setTranslate(1f, 1f);
		imageView.setImageMatrix(floorMatrix);
		float scale = (float) 0.45;
		compassMatrix.setScale(scale, scale);

		compassView.setImageMatrix(compassMatrix);
		imageView.setOnTouchListener(this);
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

	/**
	 * convert an location to a URL can be used to download floorplan map
	 * 
	 * @param apname the apname that is calculated in NUS Geoloc.java; or the userinput in apEditText.
	 * @param accuracy the accuracy that is calculated in Geoloc.java 
	 * @param floorplan the floorplan that is calculated in NUS Geoloc.java, including building name and floor name; or the userinput in apEditText.
	 * @return a URL string that can be used to find the floormap of such location. Ref in: http://wiki.nus.edu.sg/display/nllapi/GeoserverURLGettter
	 */
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

	/**
	 * background running, update the floormap.
	 * @author qinfeng, Dai Yuan
	 * @param locationlocked: a boolean var. true for used-defined location, false for test location
	 * @param scalemodified: a boolean var. true for user-defined resolution
	 * 
	 */
	private class UpdateFloorplanImageView extends
			AsyncTask<APLocation, Void, LayerDrawable> {

		@SuppressLint("NewApi")
		@Override
		protected LayerDrawable doInBackground(APLocation... params) {

			APLocation apLocation = params[0];

			String temp_APname = apLocation.getAp_name();
			double temp_accuracy = apLocation.getAccuracy();

			if (scalemodified || locationlocked || APname == null || !temp_APname.equals(APname)) {

				if (locationlocked==false)
				{
				   APname = apLocation.getAp_name();
				}
				File file1 = getApplicationContext().getFileStreamPath(
						APname + ".png");
				Log.d(DEBUG_TAG, file1.getAbsolutePath());
				if (!file1.exists()) {
					Log.i(DEBUG_TAG, "file does not exist");
					try {

						file1.createNewFile();
						String floormapPng = changeResolution(getURL(APname,
								null, null));

						Bitmap bitmap = BitmapFactory
								.decodeStream((InputStream) new URL(floormapPng)
										.getContent());
						FileOutputStream fos = new FileOutputStream(file1);
						bitmap.compress(CompressFormat.PNG, 0, fos);
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
					layers[1] = d;
					layers[0] = d0;

					ld = new LayerDrawable(layers);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			} else if ((Math.abs(accuracy - temp_accuracy) > 2) && !locationlocked) {
				Log.v(DEBUG_TAG, "same ap, diff accuracy: "
						+ (accuracy - temp_accuracy));
				try {
					accuracy = temp_accuracy;
					BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scale;
					String currentpoint = changeResolution(getURL(APname,
							String.valueOf(accuracy), null));
					Bitmap bitmap = BitmapFactory.decodeStream(
							(InputStream) new URL(currentpoint).getContent(), null, o);
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
			} else {

				//visible all views in floorplan
				
				zoominButton.setVisibility(View.VISIBLE);
				zoomoutButton.setVisibility(View.VISIBLE);
				compassView.setVisibility(View.VISIBLE);
				buildingEditText.setVisibility(View.VISIBLE);
				floorEditText.setVisibility(View.VISIBLE);
				apEditText.setVisibility(View.VISIBLE);
				locationLockCheckbox.setVisibility(View.VISIBLE);
				editText01.setVisibility(View.VISIBLE);
				editText02.setVisibility(View.VISIBLE);
				editText03.setVisibility(View.VISIBLE);
				changeLocationButton.setVisibility(View.VISIBLE);
				
				imageView.setScaleType(ScaleType.MATRIX);
				compassView.setScaleType(ScaleType.MATRIX);

				imageView.setImageDrawable(result);

				if (scalemodified) {

					Matrix m = imageView.getImageMatrix();
					RectF drawableRect = new RectF(0, 0,
							result.getIntrinsicWidth(),
							result.getIntrinsicHeight());
					RectF viewRect = new RectF(0, 0, imageView.getWidth(),
							imageView.getHeight());
					m.setRectToRect(drawableRect, viewRect,
							Matrix.ScaleToFit.CENTER);
					imageView.setImageMatrix(m);
				
					scalemodified = false;
				}
			}
		}

	}

	/**
	 * the Onclick() function for zoominButton to click to zoomin resolution
	 * @param scale the scale of the floorplan. larger scale, smaller resolution
	 * @param scalemodified true if the scale is modified
	 */
	public void zoominClick(View view) {
		if (scale > 1 && scale <= 16) {
			scale /= 2;
			Context context = getApplicationContext();
			CharSequence text = "Zoom in!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			scalemodified = true;
		} else {
			Context context = getApplicationContext();
			CharSequence text = "You have reached the largest scale. Cannot zoom in!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	/**
	 * the Onclick() function for zoomoutButton to click to zoomout resolution
	 * @param scale the scale of the floorplan. larger scale, smaller resolution
	 * @param scalemodified true if the scale is modified
	 */
	public void zoomoutClick(View view) {
		if (scale >= 1 && scale < 16) {
			scale *= 2;
			Context context = getApplicationContext();
			CharSequence text = "Zoom out!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			scalemodified = true;
		} else {
			Context context = getApplicationContext();
			CharSequence text = "You have reached the smallest scale. Cannot zoom out!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	/**
	 * the Onclick() function for changing the floormap display to be the user-defined location
	 * @param locationLocakCheckbox the checkbox in floorplanview
	 * @param locationlocked true is the location is set by user
	 */
	public void locationChangeClick(View view)
	{
		if (locationLockCheckbox.isChecked())
		{
			locationlocked=true;
			APname=buildingEditText.getText().toString()+"-"+floorEditText.getText().toString()+"-AP"+apEditText.getText().toString();
		}
		else 
		{
			locationlocked=false;
		}
	}
	
	/**
	 * 
	 * Function to change the resolution
	 * @param url the url to display the floorplanview
	 * @param division the division index to the original map. eg. division=2 means the displayed map is the original map scaled by 2*2
	 * @return the modified url to display the floorplanview. The resolution is changed by the int var: division
	 * 
	 */
	public String changeResolution(String url) {
		
		int division=2;
		String[] urlpart1=url.split("&width=");
		int width=Integer.parseInt(urlpart1[1].substring(0, 4));
		int height=Integer.parseInt(urlpart1[1].substring(12,16));
		String result=url.replace(Integer.toString(width), Integer.toString((width)/division));
		
		return result.replace(Integer.toString(height), Integer.toString((height)/division));

	}

	/**
	 * The touch event controller
	 *
	 *  @param savedFloorMatrix the original map matrix 
	 *  @param savedCompassMatrix the original compass matrix
	 *  @param mode the touch mode: 0 for none; 1 for drage 2 for zoom;
	 *  @param floorLastEvent the last floor event 
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		// Handle touch events

		int value=event.getAction() & MotionEvent.ACTION_MASK;
		Log.d("ontouch", Integer.toString(value));
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedFloorMatrix.set(floorMatrix);
			savedCompassMatrix.set(compassMatrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			floorLastEvent = null;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedFloorMatrix.set(floorMatrix);
				savedCompassMatrix.set(compassMatrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			floorLastEvent = new float[4];
			floorLastEvent[0] = event.getX(0);
			floorLastEvent[1] = event.getX(1);
			floorLastEvent[2] = event.getY(0);
			floorLastEvent[3] = event.getY(1);

			d = rotation(event);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			floorLastEvent = null;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				// ...
				floorMatrix.set(savedFloorMatrix);
				float dx = event.getX() - start.x;
				float dy = event.getY() - start.y;
				floorMatrix.postTranslate(dx, dy);
			} else if (mode == ZOOM) {				
				float newDist = spacing(event);
				if (newDist > 10f) {
					floorMatrix.set(savedFloorMatrix);
					compassMatrix.set(savedCompassMatrix);
					float scale = newDist / oldDist;
					floorMatrix.postScale(scale, scale, mid.x, mid.y);
				}

				if (floorLastEvent != null) {	
					float newRot = rotation(event);
					float r = newRot - d;
					Log.d("rotationvalue","rotation value"+" "+Float.toString(r));
                    
					floorMatrix.postRotate(r, imageView.getWidth() / 2,
							imageView.getHeight() / 2);		
					compassMatrix.postRotate(r, compassView.getWidth() / 2,
								compassView.getHeight() / 2);				
				}				
			}
			break;
		}

		compassView.setImageMatrix(compassMatrix);			
    
		imageView.setImageMatrix(floorMatrix);
		return true; // indicate event was handled
	}

	/** Determine the space between the first two fingers
	 * 
	 *  @return the space between two fingers
	 *  
	 *  */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers
	 * 
	 *  
	 *  */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/**
	 * Calculate the degree to be rotated by.
	 * 
	 * @param event
	 * @return Degrees the rotated degrees compared to origin 
	 */
	private float rotation(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(delta_y, delta_x);
		return (float) Math.toDegrees(radians);
	}
	
	
}
