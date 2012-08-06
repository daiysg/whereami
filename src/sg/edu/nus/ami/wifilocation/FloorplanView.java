package sg.edu.nus.ami.wifilocation;



import java.io.InputStream;
import java.net.URL;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class FloorplanView extends Activity {
	private static final String DEBUG_TAG = "FloorplanView";
	
	ImageView imageView;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.floorplanview);
		
		Log.d(DEBUG_TAG, "floorplanview tab");
		
		imageView = (ImageView) findViewById(R.id.imageView_01);
		SharedPreferences preferences = getSharedPreferences(BasicWifiLocation.PREFERENCES, MODE_PRIVATE);

		
		String APlocation=preferences.getString(BasicWifiLocation.LOCATION_AP_NAME, " ").toUpperCase();
		int Pos =APlocation.indexOf("AP");
		String location = APlocation.substring( 0, Pos-1 );
		
		Toast.makeText(this,"Downloading Floor Plan : "+location +"\nPlease Wait !",Toast.LENGTH_LONG).show();
		Log.d("locationap ", preferences.getString(BasicWifiLocation.LOCATION_AP_NAME, "I3-02"));
		
		//download image file
		Drawable drawable = LoadImageFromWebOperations("http://172.18.101.125/html/floorplan/"+location+".PNG");
	    imageView.setImageDrawable(drawable);
		
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
	        return null;
	      }
	    }

}
