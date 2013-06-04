package sg.edu.nus.ami.wifilocation;

public class CirIntersec {

	private final double originShift;
	private final double d2r;

	public CirIntersec() {
		originShift = 2 * Math.PI * 6378137 / 2.0;
		d2r= (Math.PI / 180.0);
	}

	//  Converts given lat/lon in WGS84 to XY in Spherical Mercator

	public double[] LatLonToMeters(double lat, double lon) {

		double mx = lon * originShift / 180.0;
		double my = Math.log(Math.tan((90 + lat) * Math.PI / 360.0))
				/ (Math.PI / 180.0);
		my = my * originShift / 180.0;

		return new double[] { mx, my };
	}

	// Converts given  XY to lat/lon in WGS84  in Spherical Mercator

	public double[] MetersToLatLon(double mx, double my) {

		double lon = (mx / originShift) * 180.0;
		double lat = (my / originShift) * 180.0;

		lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);

		return new double[] { lat, lon };
	}

	public double[] Intersectcenter(double r1, double r2,double x1, double y1,double x2, double y2,double d) {

		//d is distance betwee center of two circle
		double d1 = (r1*r1 - r2*r2 + d*d) / (2*d);
		
		//height from center from of intersecion.
		double h = Math.sqrt((r1*r1 - d1*d1));

		//center of intersection 
		double x3 = x1 + (d1 * (x2 - x1)) / d;
		double y3 = y1 + (d1 * (y2 - y1)) / d;
		//radius of intersection
		double R=r1-d1;

		return new double[] { x3,y3,R };
	}
	//haversine distance between two circle
	public	double geoDist_m(double lat1, double long1, double lat2, double long2)
	{
		double dlong = (long2 - long1) * d2r;
		double dlat = (lat2 - lat1) * d2r;
		double a = Math.pow(Math.sin(dlat/2.0), 2) + Math.cos(lat1*d2r) * Math.cos(lat2*d2r) * Math.pow(Math.sin(dlong/2.0), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = 6371 * c*1000;

		return d;
	}
	// find the distance between two circle using xy coordinate
	public	double geoDist_xy(double []pt1,double []pt2)
	{
		double dxy=Math.sqrt((pt1[0]-pt2[0])*(pt1[0]-pt2[0])+(pt1[1]-pt2[1])*(pt1[1]-pt2[1]));
		return dxy;

	}
	//given signal strength,find approx Distance
	public double SignalD(double x){
		double Dist;

		//formula 1
		//Dist = (float) (-0.0007*x*x*x - 0.0898*x*x - 4.0864*x - 62.719);

		//formula 2 
		//Dist = (float) (-0.0006*x*x*x- 0.1056*x*x - 6.9062*x - 150.93);

		Dist = (double) (-0.0007*x*x*x - 0.0898*x*x - 4.0864*x - 67.719);
		if(Dist<0)
			Dist=0;
		System.out.println("AP: "+Dist+"m");

		return Dist;
	}

	//check if two given circle intersect each other
	public boolean CheckInt(double r1,double r2,double d){

		
		if((r1+r2)>d & r2<(d+r1))
			return true;
		else 
			return false;
		
	}
}
