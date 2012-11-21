package sg.edu.nus.ami.wifilocation.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import sg.edu.nus.ami.wifilocation.R;
import sg.edu.nus.ami.wifilocation.Splash;
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
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
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
	APLocation apLocation;
	Vector<ScanResult> wifi;
	Geoloc geoloc;

	Vector<ScanResult> wifinus;
	Vector<APLocation> v_apLocation;
	
	Handler h_wifiscantimer;
	Thread t_wifiscantimer;
	boolean b_wifiscantimer_continue = true;
	private boolean shouldturnoffwifi = false;

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
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		v_configuredNetID = new Vector<Integer>();
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

		Toast.makeText(this, "ServiceLocation Started.", Toast.LENGTH_SHORT)
				.show();

		// DO SOMETHING
//		int counter = intent.getExtras().getInt("counter");
		int counter = 0;
		new Thread(myThreads, new ServiceWorker(counter), "ServiceLocation")
				.start();
		
		t_wifiscantimer.start();
		Log.i(TAG, "wifiscantimer thread started, thread id:  "+ t_wifiscantimer.getId());

		return START_STICKY;
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
			final String TAG2 = "ServiceWorker:"
					+ Thread.currentThread().getId();

			wifinus = new Vector<ScanResult>();
			apLocation = new APLocation();
			geoloc = new Geoloc();

			if (wifimgr.isWifiEnabled() == false) {
				wifimgr.setWifiEnabled(true);
				shouldturnoffwifi = true;
			}
			
			acquireWifiLock();
			//disable wifi when mobile is available and connecting
			if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting())
				disableConfiguredNetwork();
			startWifiScan();

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

									Log.v(TAG2, "wifi_point = "
											+ wifipoint.SSID);
								}
							}
						}
						if (wifinus.size() > 0) {

							NUSGeoloc nusGeoloc = new NUSGeoloc();
							Vector<String> mac = new Vector<String>(
									wifinus.size());
							Vector<Double> strength = new Vector<Double>(
									wifinus.size());
							v_apLocation = new Vector<APLocation>(
									wifinus.size());

							for (ScanResult temp_wifi : wifinus) {
								mac.add(temp_wifi.BSSID);
								strength.add(Double.valueOf(temp_wifi.level));
							}

							v_apLocation = nusGeoloc.getLocationBasedOnAP(mac,
									strength);
							if (v_apLocation.isEmpty()) {
								Log.v(TAG2 + "NO_LOCATION",
										"No location is available");
							} else {
								// choose the nearest location
								apLocation = v_apLocation.firstElement();

								Intent return_intent = new Intent();
								return_intent.setAction(BROADCAST_ACTION);
								
								Gson gson = new GsonBuilder().serializeNulls().create();
								return_intent.putExtra("ap_location",
										gson.toJson(apLocation, APLocation.class));
								sendBroadcast(return_intent);

								Log.v(TAG2, "Sending Object over."+gson.toJson(apLocation, APLocation.class));

							}
						}


						lastResultTimetamp = System.currentTimeMillis();
						startWifiScan();
						Log.d(TAG, "loop wifi scan within location service, scan wifi at "+lastResultTimetamp);

					}
				};

			}
			
			registerReceiver(receiver, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			Log.v(TAG2, "run() in ServiceWorker in LocationService");

		}
	}
	
	Runnable wifiscantimer = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			if(System.currentTimeMillis()-lastResultTimetamp>WifiScanInterval){
				startWifiScan();
				Log.i(TAG,"wifi scan started by timer");
			}
			if(b_wifiscantimer_continue){
				wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				_wifiInfo = wifimgr.getConnectionInfo();
				Log.i(TAG, wifiInfo.getDetailedState().toString()+" wifimgr wifi info: "+_wifiInfo.getSupplicantState().toString()+
						"ip: "+_wifiInfo.getIpAddress());
				h_wifiscantimer.postDelayed(wifiscantimer, 1000);
				Log.i(TAG, "here is wifi timer, thread id: "+Thread.currentThread().getId());
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
		restoreConfiguredNetwork();
		if(shouldturnoffwifi)
			wifimgr.setWifiEnabled(false);
		Toast.makeText(this, "ServiceLocation Done.", Toast.LENGTH_LONG).show();
		myThreads.interrupt();
		b_wifiscantimer_continue = false;
		Log.i(TAG,"onDestroy, set wifiscantimer_continue flag to false");
		notifmgr.cancelAll();

	}
	
	/**
	 * this method is to tweak the wifiscan
	 * check wifi state before start wifi scan
	 * 
	 */
	public void startWifiScan(){
//		wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//		Log.i(TAG, wifiInfo.getDetailedState().toString());
		
		boolean startedsuccessfully = wifimgr.startScan();
		if(startedsuccessfully){
			Log.i(TAG, "wifi scan started successfully");
		}else{
			Log.e(TAG, "wifi scan failed");
		}
	}
	
	/**
	 * this method is to disable user configured network so that the phone will not
	 * auto connect to wifi
	 */
	public void disableConfiguredNetwork(){
		//first print out a list of configured network with the configure state:
		//disabled, enabled, current
		int state = wifimgr.getWifiState();
		if(state == WifiManager.WIFI_STATE_ENABLED){
			runDisableConfiguredNetwork();
		}else if(state == WifiManager.WIFI_STATE_ENABLING || state == WifiManager.WIFI_STATE_DISABLING){
			registerReceiver(new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
						try{
							unregisterReceiver(this);
							disableConfiguredNetwork();
						}catch(IllegalArgumentException e){
							Log.e(TAG, "unregister receiver more than once");
						}
					}
					
				}
			}, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		}else{
			//no thing else
		}
		
	}
	
	public void runDisableConfiguredNetwork(){
		List<WifiConfiguration> l_wificonf = wifimgr.getConfiguredNetworks();
		String s = "";
		for(WifiConfiguration wificonf : l_wificonf){
			s+="NetID: "+wificonf.networkId+"| BSSID: "+wificonf.BSSID + "| SSID: "+wificonf.SSID+"| State: "+wificonf.status+"\n";
			
		}
		Log.i(TAG,"disable network:"+s);
		
		
		for(WifiConfiguration wificonf : l_wificonf){
			if(WifiConfiguration.Status.DISABLED != wificonf.status){
				wifimgr.disableNetwork(wificonf.networkId);
				v_configuredNetID.add(wificonf.networkId);
				Log.i(TAG, "disable net id: "+wificonf.networkId);
			}			
		}
		
		s="";
		for(WifiConfiguration wificonf : l_wificonf){
			s+="NetID: "+wificonf.networkId+"| BSSID: "+wificonf.BSSID + "| SSID: "+wificonf.SSID+"| State: "+wificonf.status+"\n";
			
		}
		Log.i(TAG,s);
		
	}
	
	public void restoreConfiguredNetwork(){
		List<WifiConfiguration> l_wificonf = wifimgr.getConfiguredNetworks();
		String s = "";
		for(WifiConfiguration wificonf : l_wificonf){
			s+="NetID: "+wificonf.networkId+"| BSSID: "+wificonf.BSSID + "| SSID: "+wificonf.SSID+"| State: "+wificonf.status+"\n";
			
		}
		Log.i(TAG,"restore configure network::"+s);
		
		if(v_configuredNetID.size()>0){
			for(int i : v_configuredNetID){				
				wifimgr.enableNetwork(i, false);
			}
		}
		
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
		Intent intent = new Intent(this,Splash.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
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

}
