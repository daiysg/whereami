package sg.edu.nus.ami.wifilocation;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


import sg.edu.nus.ami.wifilocationApi.APLocation;
import sg.edu.nus.ami.wifilocationApi.Geoloc;
import sg.edu.nus.ami.wifilocationApi.NUSGeoloc;
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

public class AndroidWifiLocationActivity extends TabActivity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private static final String DEBUG_TAG = "AndroidWifiLocation";
	
	WifiManager wifimgr;
	BroadcastReceiver receiver;
	
	Button bt_location;
	Button bt_floorplan;
	
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
	TextView tv_building;
	TextView tv_loc;
	TextView tv_distance;
   //test changes
	Handler getPosHandler;
	APLocation apLocation;
	Vector<ScanResult> wifi;
	Geoloc geoloc;
	int maxLevel = -100;
	ScanResult nearestAP = null;
	Vector<ScanResult> wifinus = new Vector<ScanResult>();
	Vector<APLocation> v_apLocation;
	
	SharedPreferences preferences;
	CirIntersec CI;
	
	boolean shouldturnoffwifi = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        CI=new CirIntersec();
        preferences = getSharedPreferences(BasicWifiLocation.PREFERENCES, MODE_PRIVATE);
        
        //setup UI
        Resources res = getResources();
        TabHost tabHost = getTabHost();
        
        TabSpec spec = tabHost.newTabSpec("My Location");
        spec.setIndicator("my location", res.getDrawable(android.R.drawable.ic_menu_mylocation));
        spec.setContent(R.id.tab_mylocation);
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("Google Map");
        spec.setIndicator("google map", res.getDrawable(android.R.drawable.ic_menu_compass));
        
        Context ctx = getApplicationContext();
        Intent i = new Intent(ctx, MapTabView.class);
        
        spec.setContent(i);
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("Floorplan");
        spec.setIndicator("floorplan", res.getDrawable(android.R.drawable.ic_menu_directions));
//        spec.setContent(R.id.tab_floorplan);
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
        
        tv_building = (TextView) findViewById(R.id.textviewColumnBuilding);
        tv_loc= (TextView) findViewById(R.id.textviewColumnLocation);
        tv_distance = (TextView) findViewById(R.id.textviewColumnDis);
        
        //setup wifi
        wifimgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        if(wifimgr.isWifiEnabled()==false){
        	wifimgr.setWifiEnabled(true);
        	shouldturnoffwifi = true;
        }
        
        getPosHandler = new Handler();
        apLocation = new APLocation();
        geoloc = new Geoloc();
        
        //register broadcast receiver
        if(receiver == null){
        	receiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					
					//clear the nus official ap list record every time when refresh
					wifinus.removeAllElements();
					
					StringBuilder sb1 = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					StringBuilder sb3 = new StringBuilder();
					
					StringBuilder sb1_1 = new StringBuilder();
					StringBuilder sb2_1 = new StringBuilder();
					StringBuilder sb3_1 = new StringBuilder();
					
					List<ScanResult> wifilist = wifimgr.getScanResults();
					//haha...more intelligient sort...
					Collections.sort(wifilist,  new CmpScan());
					
					for (ScanResult wifipoint : wifilist){
						sb1.append(wifipoint.SSID + "\n");
						sb2.append(wifipoint.BSSID + "\n");
						sb3.append(wifipoint.level + "\n");
						
						if (wifipoint.SSID.equals("NUS") || wifipoint.SSID.equals("NUSOPEN")) {
							wifinus.add(wifipoint);
							sb1_1.append(wifipoint.SSID + "\n");
							sb2_1.append(wifipoint.BSSID + "\n");
							sb3_1.append(wifipoint.level + "\n");
						}
					}//for
					
					//non nus point
					tv_ssid_2.setText(sb1);
					tv_bssid_2.setText(sb2);
					tv_level_2.setText(sb3);
					
					//for nus point
					tv_ssid_1.setText(sb1_1);
					tv_bssid_1.setText(sb2_1);
					tv_level_1.setText(sb3_1);
					
					//check for duplicate address				
					 // wifi=(Vector<ScanResult>) wifinus.clone();
					  Log.d("check", "wifinus size before "+String.valueOf(wifinus.size()));
					for ( int k=0;k<wifinus.size();k++){

						for(int i=(k+1);i<wifinus.size();i++){

							if(wifinus.get(k).BSSID.substring(0, 16).equals(wifinus.get(i).BSSID.substring(0, 16))){
								wifinus.removeElementAt(i);
								i--;
							
								Log.d("check", "remove wifi at "+String.valueOf(i));
							}
						}

					}

					
//					//checking remove wifi
//					for (ScanResult wifipoint : wifinus){
//						sb1.append(wifipoint.SSID + "\n");
//						sb2.append(wifipoint.BSSID + "\n");
//						sb3.append(wifipoint.level + "\n");
//		
//					}//for
//					
//					//non nus point
//					tv_ssid_2.setText(sb1);
//					tv_bssid_2.setText(sb2);
//					tv_level_2.setText(sb3);
					
					if (wifinus.size()>0) {
						new Thread(getPosRunnable).start();
					}
					
					//loop the wifi scan
					//TODO: qinfeng to improve the delayed time
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						
						public void run() {
							wifimgr.startScan();
						}
					}, 2000);
				}
			};
			
			registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			Log.d(DEBUG_TAG, "onCreate()");
        }//if
    }
    
    private Runnable getPosRunnable = new Runnable() {
			
			public void run() {
				getPosHandler.post(new Runnable() {
					public void run() {
						tv_location.setText("Getting position");
					}
				});
				
				NUSGeoloc nusGeoloc = new NUSGeoloc();
				Vector<String> mac = new Vector<String>(wifinus.size());
				Vector<Double> strength = new Vector<Double>(wifinus.size());
				v_apLocation = new Vector<APLocation>(wifinus.size());
				
				//wifisort(mac, strength, wifinus);
				//haha...more intelligient sort...
//				Collections.sort(wifinus,  new CmpScan());
				
				for ( ScanResult temp_wifi:wifinus){
					mac.add(temp_wifi.BSSID);
					strength.add(Double.valueOf(temp_wifi.level));
				}
		
				
				v_apLocation = nusGeoloc.getLocationBasedOnAP(mac, strength);
				if(v_apLocation.isEmpty()){
					getPosHandler.post(new Runnable() {
						
						public void run() {
							tv_location.setText("No location is available");
						}
					});
				}else{
					
					apLocation = v_apLocation.firstElement();//choose the nearest location
					
					//int test = v_apLocation.size();// get second location??
					//tv_location.setText(String.valueOf(test));
					//update main UI via handler
					getPosHandler.post(new Runnable() {
						DecimalFormat df = new DecimalFormat("###.###");
						public void run() {
							//						tv_location.setText(apLocation.toString());

							//String text = "You are near the room of "+apLocation.getAp_location()+" in the building of "+apLocation.getBuilding()+"\n";
							//							String text = wifinus.get(0).level+" You are about: "+DfromCenter(wifinus.get(0).level)+" m from the room of "+apLocation.getAp_location()+" in the building of "+apLocation.getBuilding()+"\n";
							//							tv_location.setText(text);
							StringBuilder build=new StringBuilder();
							StringBuilder Loc=new StringBuilder();
							StringBuilder dist=new StringBuilder();
							int g=0;
							for(APLocation apoint:v_apLocation){

								build.append(apoint.getBuilding()+"\n"+apoint.getAp_lat()+"\n");
								Loc.append(apoint.getAp_location()+"\n"+apoint.getAp_long()+"\n");
								dist.append(String.valueOf(df.format(CI.SignalD(wifinus.get(g).level))+"m \n"+wifinus.get(g).level+"\n"));
								g++;
							}
							
							// find distance between two strongest point
						
							
							//double DT=CI.geoDist_m(v_apLocation.get(0).getAp_lat(),v_apLocation.get(0).getAp_long(),v_apLocation.get(1).getAp_lat(),v_apLocation.get(1).getAp_long());
							
							double []P1xy=new double[2] ;
							double []P2xy=new double[2] ;
							double []P3_inter= new double [3];
							double []P3_latlon= new double [2];
							double Distance,r1,r2;
							P1xy=CI.LatLonToMeters(v_apLocation.get(0).getAp_lat(),v_apLocation.get(0).getAp_long());
							P2xy=CI.LatLonToMeters(v_apLocation.get(1).getAp_lat(),v_apLocation.get(1).getAp_long());
							r1=CI.SignalD(wifinus.get(0).level);
							r2=CI.SignalD(wifinus.get(1).level);
							
							Distance=CI.geoDist_xy(P1xy, P2xy);
							//double XY=CI.geoDist_xy(v_apLocation.get(0).getAp_lat(),v_apLocation.get(0).getAp_long(),v_apLocation.get(1).getAp_lat(),v_apLocation.get(1).getAp_long());
							//df.format(DT);
							boolean check=CI.CheckInt(r1,r2, Distance);
							
							//if does intersect ,cal intersect point.
							if(check){
								P3_inter=CI.Intersectcenter(r1, r2, P1xy[0],P1xy[1] , P2xy[0],P2xy[1], Distance);
								P3_latlon=CI.MetersToLatLon(P3_inter[0],P3_inter[1]);
							}
								
						
							tv_location.setText("Dist between two APs:  "+String.valueOf(df.format(Distance)).replace(",",".")+"m "+check+"\nIntersection Pt:"+df.format(P3_latlon[0])+" "+df.format(P3_latlon[1])+" Radius: "+df.format(P3_inter[2]));	

							tv_building.setText(build);
							tv_loc.setText(Loc);
							tv_distance.setText(dist);


						}
					});
					
					SharedPreferences.Editor prefEditor = preferences.edit();
					prefEditor.putString(BasicWifiLocation.LOCATION_BUILDING, apLocation.getBuilding());
					prefEditor.putString(BasicWifiLocation.LOCATION_ACCURACY, String.valueOf(apLocation.getAccuracy()));
					prefEditor.putString(BasicWifiLocation.LOCATION_AP_NAME, apLocation.getAp_name());
					prefEditor.putString(BasicWifiLocation.LOCATION_AP_LOCATION, apLocation.getAp_location());
					prefEditor.putString(BasicWifiLocation.LOCATION_AP_LAT, String.valueOf(apLocation.getAp_lat()));
					prefEditor.putString(BasicWifiLocation.LOCATION_AP_LONG, String.valueOf(apLocation.getAp_long()));
					prefEditor.commit();
				}
			}
		};
		


	public void onClick(View v) {
		if(v.getId() == R.id.buttonLocation){
			Toast.makeText(this, "Scanning WIFI and searching location", Toast.LENGTH_LONG).show();
			wifimgr.startScan();
		}
	}

	@Override
	public void onRestart(){
		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		Log.d(DEBUG_TAG, "onRestart()");
		super.onRestart();
	}

	@Override
    public void onStop(){
    	unregisterReceiver(receiver);
    	Log.d(DEBUG_TAG, "onStop()");
    	super.onStop();
    }
	
	@Override
	public void onDestroy(){
		if(shouldturnoffwifi)
			wifimgr.setWifiEnabled(false);
		super.onDestroy();
	}

}