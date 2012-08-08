package sg.edu.nus.ami.wifilocation;

import java.io.InputStream;
import java.net.URL;

import sg.edu.nus.ami.wifilocationApi.RequestMethod;
import sg.edu.nus.ami.wifilocationApi.RestClient;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FloorplanView extends Activity {
	private static final String DEBUG_TAG = "FloorplanView";
	
	ImageView imageView;
	Drawable floorplan;	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.floorplanview);
		
		Log.d(DEBUG_TAG, "floorplanview tab");
		
		imageView = (ImageView) findViewById(R.id.imageView_01);	
		
		
		String url = "http://172.18.101.125:8080/geoserver/wms?" +
		"Layers=nus%3Afloors%2Cnus%3Arooms%2Cnus%3Alinks" +
		"&service=wms" +
		"&request=getmap" +
		"&format=image%2Fpng" +
		"&srs=EPSG%3A4326" +
		"&version=1.1.1";
		
		//try to append more parameters to the URL
		try {
			SharedPreferences preferences = getSharedPreferences(BasicWifiLocation.PREFERENCES, MODE_PRIVATE);
			
			String APname=preferences.getString(BasicWifiLocation.LOCATION_AP_NAME, " ").toUpperCase();
			String location = APname.substring( 0, APname.indexOf("AP")-1 );
			String[] _s = location.split("[-]+");			
			String filter = "building=%27"+_s[0]+"%27%20and%20floor=%27"+_s[1]+"%27";
			
			String bbox;
			int width;
			int height;
			int zoomlevel = 2;
			
			// get bbox from webservice http://172.18.101.125:8080/api1/MapBbox?building=CCE			
			String bbox_url = "http://172.18.101.125:8080/api1/MapBbox";
			RestClient client = new RestClient(bbox_url);
			client.AddParam("building", _s[0]);
			client.Execute(RequestMethod.GET);
			
			Bbox box = new Bbox();
			Gson gson = new GsonBuilder().serializeNulls().create();
			box = gson.fromJson(client.getResponse(), Bbox.class);
			bbox = box.toString();
			
			width = (int) ((box.maxX-box.minX)*1000000)*zoomlevel;
			height = (int) ((box.maxY-box.minY)*1000000)*zoomlevel;			
			
			url = url+"&width="+String.valueOf(width)+"&height="+String.valueOf(height)+"&cql_filter="+filter+"%3B"+filter+"%3B"+filter+"&bbox="+bbox;
			
			Toast.makeText(this,"Loading Floor Plan : "+location +"\nPlease Wait !",Toast.LENGTH_LONG).show();
			Log.d("locationap ", preferences.getString(BasicWifiLocation.LOCATION_AP_NAME, "I3-02"));
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
		
		floorplan = LoadImageFromWebOperations(url);
	    imageView.setImageDrawable(floorplan);
		
	    //enable touch
		imageView.setOnTouchListener(new Touch());	        
	        
		}

	    private Drawable LoadImageFromWebOperations(String url)
	    {
	      try{
	        InputStream is = (InputStream) new URL(url).getContent();
	        Drawable d = Drawable.createFromStream(is, "src name");
	        return d;
	      }catch (Exception e) {
	        System.out.println("Exc="+e);
	        return getResources().getDrawable(R.drawable.nofloormap);
	      }
	    }
	    
	    public class Bbox {
	    	double minX;
	    	double minY;
	    	double maxX;
	    	double maxY;
	    	
	    	public String toString(){
	    		return String.valueOf(minX)+","+String.valueOf(minY)+","+String.valueOf(maxX)+","+String.valueOf(maxY);
	    	}

	    }

}
