package sg.edu.nus.ami.wifilocation;


import android.graphics.Matrix;  
import android.graphics.PointF;  
import android.util.FloatMath;  
import android.view.MotionEvent;  
import android.view.View;  
import android.view.View.OnTouchListener;  
import android.widget.ImageView;  

public class Touch implements OnTouchListener {  

	// These matrices used to move and zoom image  
	Matrix matrix = new Matrix();  
	Matrix savedMatrix = new Matrix();  

	//  3 states  
	static final int NONE = 0;  
	static final int DRAG = 1;  
	static final int ZOOM = 2;  
	int mode = NONE;  

	// Remember some things for zooming  
	PointF start = new PointF();  
	PointF mid = new PointF();  
	float oldDist = 1f;  


	public boolean onTouch(View v, MotionEvent event) {  
		ImageView view = (ImageView) v;  
		// Handle touch events   
		switch (event.getAction() & MotionEvent.ACTION_MASK) {  
		case MotionEvent.ACTION_DOWN:  
			savedMatrix.set(matrix);  
			start.set(event.getX(), event.getY());  
			mode = DRAG;  
			break;  
		case MotionEvent.ACTION_POINTER_DOWN:  
			oldDist = spacing(event);  
			if (oldDist > 10f) {  
				savedMatrix.set(matrix);  
				midPoint(mid, event);  
				mode = ZOOM;  
			}  
			break;  
		case MotionEvent.ACTION_UP:  
		case MotionEvent.ACTION_POINTER_UP:  
			mode = NONE;  
			break;  
		case MotionEvent.ACTION_MOVE:  
			if (mode == DRAG) {  
				// ...      
				matrix.set(savedMatrix);  
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);      
			} else if (mode == ZOOM) {  
				float newDist = spacing(event);  
				if (newDist > 10f) {  
					matrix.set(savedMatrix);  
					float scale = newDist / oldDist;  
					matrix.postScale(scale, scale, mid.x, mid.y);  
				}  
			}  
			break;  
		}  

		view.setImageMatrix(matrix);  
		return true; // indicate event was handled  
	}  


	/** Determine the space between the first two fingers */  
	private float spacing(MotionEvent event) {  
		float x = event.getX(0) - event.getX(1);  
		float y = event.getY(0) - event.getY(1);  
		return FloatMath.sqrt(x * x + y * y);  
	}  

	/** Calculate the mid point of the first two fingers */  
	private void midPoint(PointF point, MotionEvent event) {  
		float x = event.getX(0) + event.getX(1);  
		float y = event.getY(0) + event.getY(1);  
		point.set(x / 2, y / 2);  
	}  
}  