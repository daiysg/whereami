package sg.edu.nus.ami.wifilocation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Splash extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		Thread timer = new Thread(){
			public void run(){
				try{
					sleep(1000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}finally{
					Intent i = new Intent("sg.edu.nus.ami.wifilocation");
					startActivity(i);
					Splash.this.finish();
				}
			}
		};
		timer.start();
	}
}
