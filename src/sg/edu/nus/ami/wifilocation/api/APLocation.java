package sg.edu.nus.ami.wifilocation.api;

/**
 * This is our location class for standard addressing the indoor location
 * @author qinfeng
 *
 */

public class APLocation {
	
	String building;
	String ap_name;
	String ap_location;
	double accuracy;
	double ap_lat;
	double ap_long;
	
	
	public APLocation() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * construct an APLocation with five parameters plus the accuracy
	 * @param building
	 * @param ap_name
	 * @param ap_location
	 * @param accuracy
	 * @param ap_lat
	 * @param ap_long
	 */
	public APLocation(String building, String ap_name, String ap_location,
			double accuracy, double ap_lat, double ap_long) {
		super();
		this.building = building;
		this.ap_name = ap_name;
		this.ap_location = ap_location;
		this.accuracy = accuracy;
		this.ap_lat = ap_lat;
		this.ap_long = ap_long;
	}
	
	/**
	 * construct an APLocation with five parameters
	 * @param building
	 * @param ap_name
	 * @param ap_location
	 * @param ap_lat
	 * @param ap_long
	 */
	public APLocation(String building, String ap_name, String ap_location,
			double ap_lat, double ap_long) {
		super();
		this.building = building;
		this.ap_name = ap_name;
		this.ap_location = ap_location;
		this.ap_lat = ap_lat;
		this.ap_long = ap_long;
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		this.building = building;
	}
	public String getAp_name() {
		return ap_name;
	}
	public void setAp_name(String ap_name) {
		this.ap_name = ap_name;
	}
	public String getAp_location() {
		return ap_location;
	}
	public void setAp_location(String ap_location) {
		this.ap_location = ap_location;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	public double getAp_lat() {
		return ap_lat;
	}
	public void setAp_lat(double ap_lat) {
		this.ap_lat = ap_lat;
	}
	public double getAp_long() {
		return ap_long;
	}
	public void setAp_long(double ap_long) {
		this.ap_long = ap_long;
	}
	
	/**
	 * Override the toString() method
	 */
	@Override
	public String toString() {
		return "APLocation [building=" + building + ", ap_name=" + ap_name
				+ ", ap_location=" + ap_location + ", accuracy=" + accuracy
				+ ", ap_lat=" + ap_lat + ", ap_long=" + ap_long + "]";
	}
	
	
}
