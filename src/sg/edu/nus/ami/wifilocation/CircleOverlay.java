package sg.edu.nus.ami.wifilocation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class CircleOverlay extends Overlay{

    Context context;
    double mLat;
    double mLon;
    double circleRadius;

    public double getmLat() {
		return mLat;
	}

	public void setmLat(double mLat) {
		this.mLat = mLat;
	}

	public double getmLon() {
		return mLon;
	}

	public void setmLon(double mLon) {
		this.mLon = mLon;
	}

	public double getCircleRadius() {
		return circleRadius;
	}

	public void setCircleRadius(double circleRadius) {
		this.circleRadius = circleRadius;
	}

	public CircleOverlay(Context _context){
    	context = _context;
    }
    
    public CircleOverlay(Context _context, double _lat, double _lon , double _radius) {
        context = _context;
        mLat = _lat;
        mLon = _lon;
        circleRadius = _radius;
    }

    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	    super.draw(canvas, mapView, shadow); 
	    Projection projection = mapView.getProjection();
	    Point pt = new Point();
	    GeoPoint geo = new GeoPoint((int) (mLat *1e6), (int)(mLon * 1e6));
	    projection.toPixels(geo ,pt);
	    Paint circlePainter;
		circlePainter = new Paint();
		circlePainter.setAntiAlias(true);
		circlePainter.setStrokeWidth(2.0f);
		circlePainter.setColor(0xff6666ff);
		circlePainter.setStyle(Style.FILL_AND_STROKE);
		circlePainter.setAlpha(70);
		int radius = metersToRadius((float) circleRadius, mapView, mLat);
	    canvas.drawCircle((float)pt.x, (float)pt.y, radius, circlePainter);
	    
	    Paint dotPainter;
	    dotPainter = new Paint();
	    dotPainter.setAntiAlias(true);
	    dotPainter.setColor(Color.RED);
	    dotPainter.setStyle(Style.FILL);
	    canvas.drawCircle((float)pt.x, (float)pt.y, 5.0f, dotPainter);
    }
    
    public static int metersToRadius(float meters, MapView map, double latitude) {
        return (int) (map.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
    }
}
