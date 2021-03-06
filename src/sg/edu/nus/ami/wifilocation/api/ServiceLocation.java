package sg.edu.nus.ami.wifilocation.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import sg.edu.nus.ami.wifilocation.AndroidWifiLocationActivity;
import sg.edu.nus.ami.wifilocation.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Service Class to call Localisation Services provided By : Qinfeng & Jianbin
 * Created as a Local Service as it is only required to run as a background task
 * Remote Services are for interprocess communication between applications
 * 
 * @author Cher Lia
 * 
 */
public class ServiceLocation extends Service {

	public static String BROADCAST_ACTION = "sg.edu.nus.ami.wifilocation.api.SHOW_LOCATION";

	private static final String TAG = "ServiceLocation";
	private final int WifiScanInterval = 6000; //scan wifi at least every 3 seconds, if doesnot receive a bcast
												// within 3 seconds, start another wifi scan
	long lastResultTimetamp = 0;
	
	private ThreadGroup myThreads;
	private NotificationManager notifmgr;

	WifiManager wifimgr;
	BroadcastReceiver receiver;
	WifiLock wifiLock;
	ConnectivityManager cm;
	NetworkInfo wifiInfo;
	WifiInfo _wifiInfo;
	Vector<Integer> v_configuredNetID;

	Handler getPosHandler;

	Vector<ScanResult> wifi;
	Vector<ScanResult> wifinus;
	
	Handler h_wifiscantimer;
	Thread t_wifiscantimer;
	boolean b_wifiscantimer_continue = true;
	
	public boolean started = false;

	@Override
	public void onCreate() {
		super.onCreate();

		myThreads = new ThreadGroup("ServiceWorker");
		
		h_wifiscantimer = new Handler();
		t_wifiscantimer = new Thread(wifiscantimer);

		Toast.makeText(this, "ServiceLocation CREATED", Toast.LENGTH_SHORT)
				.show();
		notifmgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		displayNotificationMessage("Background Service 'ServiceLocation' is running.");
		wifimgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * Local Service hence no need for binding, as binding is for remote
	 * services
	 */
	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(started){
			Log.i(TAG, "service started before");
			return START_STICKY;
		}else{
			
			Toast.makeText(this, "ServiceLocation Started.", Toast.LENGTH_SHORT)
			.show();
			
			int counter = 0;
			new Thread(myThreads, new ServiceWorker(counter), "ServiceLocation")
			.start();
			
			t_wifiscantimer.start();
			Log.i(TAG, "wifiscantimer thread started, thread id:  "+ t_wifiscantimer.getId());

			started = true;
			return START_STICKY;
			
		}
	}

	/**
	 * Cannot touch UI directly
	 * 
	 */
	class ServiceWorker implements Runnable {
		private int counter = -1;

		public ServiceWorker(int counter) {
			this.counter = counter;
		}

		public void run() {
			wifinus = new Vector<ScanResult>();

			acquireWifiLock();
			wifimgr.startScan();

			if (receiver == null) {
				receiver = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {

						// clear the nus official ap list record every time when
						// refresh
						wifinus.removeAllElements();

						List<ScanResult> wifilist = wifimgr.getScanResults();
						if (wifilist!=null) {
							Collections.sort(wifilist, new CmpScan());
							for (ScanResult wifipoint : wifilist) {
								if (wifipoint.SSID.equals("NUS")
										|| wifipoint.SSID.equals("NUSOPEN")) {
									wifinus.add(wifipoint);
								}
							}
						}
						if (wifinus.size() > 0) {
							new GetApLocation().execute(wifinus);
						}
						
						Intent return_intent = new Intent();
						return_intent.setAction(BROADCAST_ACTION);
						return_intent.putParcelableArrayListExtra("wifilist", (ArrayList<ScanResult>) wifilist);
						sendBroadcast(return_intent);
						Log.v(TAG, "Sending Object over. wifilist: "+wifilist.size());

						lastResultTimetamp = System.currentTimeMillis();
						wifimgr.startScan();
						Log.d(TAG, "loop wifi scan within location service, scan wifi at "+lastResultTimetamp+", Thread id: "+Thread.currentThread().getId());
					}
				};
			}
			
			registerReceiver(receiver, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			Log.v(TAG, "run() in ServiceWorker in LocationService, thread id: "+Thread.currentThread().getId());
		}
	}
	
	Runnable wifiscantimer = new Runnable() {
		
		public void run() {
			if(System.currentTimeMillis()-lastResultTimetamp>WifiScanInterval){
				wifimgr.startScan();
				Log.i(TAG,"wifi scan started by timer");
			}
			if(b_wifiscantimer_continue){
				h_wifiscantimer.postDelayed(wifiscantimer, 1000);
				Log.i(TAG, "wifi timer postdelayed, thread id: "+Thread.currentThread().getId());
			}
		}
	};

	@Override
	public void onDestroy() {
		try{
			unregisterReceiver(receiver);
		}catch (IllegalArgumentException e) {
			Log.e(TAG, "unregisterReceiver locationservice wifibroadcast receiver once more");
		}

		releaseWifiLock();
		Toast.makeText(this, "ServiceLocation Done.", Toast.LENGTH_SHORT).show();
		myThreads.interrupt();
		b_wifiscantimer_continue = false;
		Log.i(TAG,"onDestroy, set wifiscantimer_continue flag to false");
		notifmgr.cancelAll();
	}
	
	private void displayNotificationMessage(String message) {
		Notification notification = new Notification(
				R.drawable.blue_dot_circle, message, System.currentTimeMillis());

		// prevent user from clearing notification. Keep notification until we
		// destroy it ourselves
//		notification.flags = Notification.FLAG_NO_CLEAR;
		
		/**
		 * PendingIntent.getActivity : Retrieve a PendingIntent that will start a new activity, like calling Context.startActivity(Intent). 
		 * @params context 
		 * @params requestCode Private request code for the sender (currently not used). 
		 * @params intent Intent of the activity to be launched.
		 * @params flags
		 * 
		 */
		Intent intent = new Intent(this,AndroidWifiLocationActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		/**
		 * Deprecated 
		 * 
		 * @params context
		 * @params contentTitle The title that goes in the expanded entry
		 * @params contentText The text that goes in the expanded entry
		 * @params contentIntent
		 */
		notification.setLatestEventInfo(this, TAG, message, contentIntent);
		
		notifmgr.notify(0, notification);
	}
	
	private void acquireWifiLock(){
		wifiLock = wifimgr.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, ServiceLocation.class.getName());
		wifiLock.setReferenceCounted(false);
		wifiLock.acquire();
	}
	
	private void releaseWifiLock(){
		if(wifiLock != null){
			if(wifiLock.isHeld()){
				wifiLock.release();
			}
			wifiLock = null;
		}
	}
	
	public class CmpScan implements Comparator<ScanResult> {

		public int compare(ScanResult o1, ScanResult o2) {
				
			return o2.level - o1.level;
		}
	}
	
	private class GetApLocation extends AsyncTask<Vector<ScanResult>, Void, APLocation>{

		@Override
		protected APLocation doInBackground(Vector<ScanResult>... params) {
			Vector<ScanResult> wifinus = params[0];
			NUSGeoloc nusGeoloc = new NUSGeoloc();
			Vector<String> mac = new Vector<String>(
					wifinus.size());
			Vector<Double> strength = new Vector<Double>(
					wifinus.size());
			Vector<APLocation> v_apLocation = new Vector<APLocation>(
					wifinus.size());

			for (ScanResult temp_wifi : wifinus) {
				mac.add(temp_wifi.BSSID);
				strength.add(Double.valueOf(temp_wifi.level));
			}

			v_apLocation = nusGeoloc.getLocationBasedOnAP(mac,
					strength);
			if (v_apLocation.isEmpty()) {
				Log.v(TAG + "NO_LOCATION",
						"No location is available");
				return null;
			} else {
				// choose the nearest location
				APLocation apLocation = new APLocation();
				apLocation = v_apLocation.firstElement();
				return apLocation;
			}
		}

		@Override
		protected void onPostExecute(APLocation apLocation) {
			super.onPostExecute(apLocation);
			if(apLocation != null){
				Gson gson = new GsonBuilder().serializeNulls().create();
				Intent return_intent = new Intent();
				return_intent.setAction(BROADCAST_ACTION);
				return_intent.putExtra("ap_location",
						gson.toJson(apLocation, APLocation.class));
				sendBroadcast(return_intent);
				
				Log.v(TAG, "Sending Object over."+gson.toJson(apLocation, APLocation.class));
			}
		}
	}

}
