package sg.edu.nus.ami.wifilocation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import sg.edu.nus.ami.wifilocation.api.APLocation;
import sg.edu.nus.ami.wifilocation.api.NUSGeoloc;
import sg.edu.nus.ami.wifilocation.api.ServiceLocation;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidWifiLocationActivity extends TabActivity implements
		OnClickListener {
	/** Called when the activity is first created. */

	private static final String DEBUG_TAG = "AndroidWifiLocation";

	Button bt_location;

	TextView tv_location;

	TextView tv_ssid;
	TextView tv_bssid;
	TextView tv_level;

	TextView tv_ssid_1;
	TextView tv_bssid_1;
	TextView tv_level_1;

	TextView tv_ssid_2;
	TextView tv_bssid_2;
	TextView tv_level_2;

	WifiManager wifimgr;
	BroadcastReceiver receiver;
	Handler getPosHandler;
	APLocation apLocation;
	Vector<APLocation> v_apLocation;

	ScanResult nearestAP = null;
	Vector<ScanResult> wifi;
	Vector<ScanResult> wifinus = new Vector<ScanResult>();

	SharedPreferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		preferences = getSharedPreferences(BasicWifiLocation.PREFERENCES,
				MODE_PRIVATE);

		// setup UI
		Context ctx = getApplicationContext();
		Resources res = getResources();
		TabHost tabHost = getTabHost();

		TabSpec spec = tabHost.newTabSpec("My Location");
		spec.setIndicator("my location",
				res.getDrawable(android.R.drawable.ic_menu_mylocation));
		spec.setContent(R.id.tab_mylocation);
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("Google Map");
		spec.setIndicator("outdoor",
				res.getDrawable(android.R.drawable.ic_menu_compass));
		Intent i = new Intent(ctx, MapTabView.class);
		spec.setContent(i);
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("Floorplan");
		spec.setIndicator("indoor",
				res.getDrawable(android.R.drawable.ic_menu_directions));
		Intent i_floorplan = new Intent(ctx, FloorplanView.class);
		spec.setContent(i_floorplan);
		tabHost.addTab(spec);

		bt_location = (Button) findViewById(R.id.buttonLocation);
		bt_location.setOnClickListener(this);

		tv_location = (TextView) findViewById(R.id.textviewLocation);
		tv_ssid = (TextView) findViewById(R.id.textviewColumn1);
		tv_bssid = (TextView) findViewById(R.id.textviewColumn2);
		tv_level = (TextView) findViewById(R.id.textviewColumn3);

		tv_ssid_1 = (TextView) findViewById(R.id.textviewColumn1_1);
		tv_bssid_1 = (TextView) findViewById(R.id.textviewColumn2_1);
		tv_level_1 = (TextView) findViewById(R.id.textviewColumn3_1);

		tv_ssid_2 = (TextView) findViewById(R.id.textviewColumn1_2);
		tv_bssid_2 = (TextView) findViewById(R.id.textviewColumn2_2);
		tv_level_2 = (TextView) findViewById(R.id.textviewColumn3_2);

		// setup wifi
		wifimgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		getPosHandler = new Handler();
		apLocation = new APLocation();
		
		// register broadcast receiver
		if (receiver == null) {
			receiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {

					// clear the nus official ap list record every time when
					// refresh
					wifinus.removeAllElements();

					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					StringBuilder sb3 = new StringBuilder();

					StringBuilder sb1_1 = new StringBuilder();
					StringBuilder sb2_1 = new StringBuilder();
					StringBuilder sb3_1 = new StringBuilder();

					List<ScanResult> wifilist = wifimgr.getScanResults();

					Collections.sort(wifilist, new CmpScan());

					for (ScanResult wifipoint : wifilist) {
						sb1.append(wifipoint.SSID + "\n");
						sb2.append(wifipoint.BSSID + "\n");
						sb3.append(wifipoint.level + "\n");

						if (wifipoint.SSID.equals("NUS")
								|| wifipoint.SSID.equals("NUSOPEN")) {
							wifinus.add(wifipoint);
							sb1_1.append(wifipoint.SSID + "\n");
							sb2_1.append(wifipoint.BSSID + "\n");
							sb3_1.append(wifipoint.level + "\n");
						}
					}

					// non nus point
					tv_ssid_2.setText(sb1);
					tv_bssid_2.setText(sb2);
					tv_level_2.setText(sb3);

					// for nus point
					tv_ssid_1.setText(sb1_1);
					tv_bssid_1.setText(sb2_1);
					tv_level_1.setText(sb3_1);

					if (wifinus.size() > 0) {
						new Thread(getPosRunnable).start();
					}

					// loop the wifi scan
					// TODO: qinfeng to improve the delayed time
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {

						public void run() {
							wifimgr.startScan();
						}
					}, 2000);
				}
			};

		}// if

		Log.v(DEBUG_TAG, "onCreate()");

	}

	private Runnable getPosRunnable = new Runnable() {

		public void run() {
			getPosHandler.post(new Runnable() {
				public void run() {
					tv_location.setText("Getting position");
				}
			});

			// cannot use Toast directly in other thread
			// Toast.makeText(getApplicationContext(), "Getting position",
			// Toast.LENGTH_LONG);

			NUSGeoloc nusGeoloc = new NUSGeoloc();
			Vector<String> mac = new Vector<String>(wifinus.size());
			Vector<Double> strength = new Vector<Double>(wifinus.size());
			v_apLocation = new Vector<APLocation>(wifinus.size());

			for (ScanResult temp_wifi : wifinus) {
				mac.add(temp_wifi.BSSID);
				strength.add(Double.valueOf(temp_wifi.level));
			}

			v_apLocation = nusGeoloc.getLocationBasedOnAP(mac, strength);
			if (v_apLocation.isEmpty()) {
				getPosHandler.post(new Runnable() {

					public void run() {
						tv_location.setText("No location is available");
					}
				});
			} else {
				// choose the nearest location
				apLocation = v_apLocation.firstElement();
				// update main UI via handler
				getPosHandler.post(new Runnable() {
					public void run() {
						String text = "You are near "
								+ apLocation.getAp_location()
								+ " in the building of "
								+ apLocation.getBuilding() + "\n";

						tv_location.setText(text);
					}
				});

				SharedPreferences.Editor prefEditor = preferences.edit();
				prefEditor.putString(BasicWifiLocation.LOCATION_BUILDING,
						apLocation.getBuilding());
				prefEditor.putString(BasicWifiLocation.LOCATION_ACCURACY,
						String.valueOf(apLocation.getAccuracy()));
				prefEditor.putString(BasicWifiLocation.LOCATION_AP_NAME,
						apLocation.getAp_name());
				prefEditor.putString(BasicWifiLocation.LOCATION_AP_LOCATION,
						apLocation.getAp_location());
				prefEditor.putString(BasicWifiLocation.LOCATION_AP_LAT,
						String.valueOf(apLocation.getAp_lat()));
				prefEditor.putString(BasicWifiLocation.LOCATION_AP_LONG,
						String.valueOf(apLocation.getAp_long()));
				prefEditor.commit();
			}
		}
	};

	public void onClick(View v) {
		if (v.getId() == R.id.buttonLocation) {
			Toast.makeText(this, "Scanning WIFI and searching location",
					Toast.LENGTH_LONG).show();
			 wifimgr.startScan();

		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (wifimgr.isWifiEnabled() == false) {
			CreateAlertDialog dialog = new CreateAlertDialog();
			AlertDialog alert = dialog.newdialog(this);
			alert.show();
		} else {
			// start service
			// TODO: need test where the service can be called
			if (!isMyServiceRunning()) {
				int counter = 1;
				Intent ls_intent = new Intent(this, ServiceLocation.class);
				ls_intent.putExtra("counter", counter++);
				startService(ls_intent);
				Log.v(DEBUG_TAG, "onResume(), start location service");
			}
		}		
		
		registerReceiver(receiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		Log.d(DEBUG_TAG,
		"onResume(), create wifi broadcast receiver and register receiver");
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("sg.edu.nus.ami.wifilocation.api.ServiceLocation"
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onPause() {
		unregisterReceiver(receiver);
		Log.d(DEBUG_TAG, "onPause(), unregisterReceiver");
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(DEBUG_TAG, "onStop()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent service = new Intent(this, ServiceLocation.class);
		stopService(service);
		Log.v(DEBUG_TAG, "onDestroy, stop service");
		finish();
	}

	public class CmpScan implements Comparator<ScanResult> {

		public int compare(ScanResult o1, ScanResult o2) {

			return o2.level - o1.level;
		}
	}

}