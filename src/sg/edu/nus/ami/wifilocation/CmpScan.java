package sg.edu.nus.ami.wifilocation;


	import java.util.Comparator;

	import android.net.wifi.ScanResult;

	public class CmpScan implements Comparator<ScanResult> {

		public int compare(ScanResult o1, ScanResult o2) {
				
			return o2.level - o1.level;
		}
	}
