package uk.co.sundroid.util.astro.image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v7.widget.AppCompatImageView;

public class TrackerImageView extends AppCompatImageView {

	private float direction = 0f;
	private TrackerImage trackerImage;
    private Point center;

    public TrackerImageView(Context context) {
    	super(context);
	}

	@Override
	public void onDraw(Canvas canvas) {
    	if (trackerImage == null) {
    		return;
		}
		float height = this.getHeight();
		float width = this.getWidth();
		canvas.rotate(-direction, width/2, height/2);
        float cX = width/2;
        float cY = height/2;
        if (center != null) {
            cX = center.x;
            cY = center.y;
        }
		trackerImage.drawOnCanvas(this, canvas, cX, cY);
		super.onDraw(canvas);
	}

	public void setTrackerImage(TrackerImage trackerImage) {
		this.trackerImage = trackerImage;
		this.postInvalidate();
	}

	public void setDirection(float direction) {
		this.direction = direction;
		this.postInvalidate();
	}

    public void setCenter(Point center) {
        this.center = center;
        this.postInvalidate();
    }

}