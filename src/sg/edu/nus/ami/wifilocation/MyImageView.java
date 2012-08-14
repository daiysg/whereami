package sg.edu.nus.ami.wifilocation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;


public class MyImageView extends ImageView
{

    /**
     * @param context
     */
    public MyImageView(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
        setBackgroundColor(Color.WHITE);
    }

    /**
     * @param context
     * @param attrs
     */
    public MyImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public MyImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Paint paint  = new Paint(Paint.LINEAR_TEXT_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12.0F);
//        canvas.drawText("Hello World in My view", 100, 100, paint);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event)
//    {
//        Log.d("Hello Android", "Got a touch event: " + event.getAction());
//        return super.onTouchEvent(event);
//
//    }
}
