package com.angaza;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class WipeDrawable extends Drawable{
	
	private Paint mPaint = new Paint();
	private Path mPath = new Path();
	private float mPct = 0.0f;
	
	//Store these fields here to avoid creating anew when drawing
	float w, h, whPct, whW, whH, sideA, sideB, sideC, sideD;
	
	public WipeDrawable(){
		mPaint.setDither(true);
		mPaint.setStyle(Style.FILL);
		mPaint.setAntiAlias(true);
	}
			
	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		updatePath();
	}
	
	private void updatePath(){
		if(getBounds() == null){
			return;
		}
		
		Rect bnd = getBounds();
		
		w = bnd.width();
		h = bnd.height();
		whPct = (w + h) * mPct;
		whW = whPct - w;
		whH = whPct - h;
		sideA = whW >= 0 ? whW : 0;
		sideB = whW < 0 ? whPct : w;
		sideC = whH < 0 ? whPct : h;
		sideD = whH >= 0 ? whH : 0;
		
		mPath.reset();
		mPath.moveTo(w - sideB, h - sideA);
		mPath.lineTo(w - sideB, h);
		mPath.lineTo(w, h);
		mPath.lineTo(w, h - sideC);
		mPath.lineTo(w - sideD, h - sideC);
		mPath.close();
		invalidateSelf();
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return android.graphics.PixelFormat.TRANSLUCENT;
	}
	
	public void setPercent(float pct){
		mPct = pct;
		updatePath();
	}
	
	public float getPercent(){
		return mPct;
	}
	
	public void setColor(int color){
		mPaint.setColor(color);
	}
	
	@Override
	public void draw(Canvas canvas) {
		canvas.drawPath(mPath, mPaint);
	}
}
