package edu.calpoly.csc.chord;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SignalPlotView extends View {

	public static final int SIGNAL_PLOT_SIZE = 500;
	private static final double UP_BORDER = 1.1;
	public double sampleRate;
	public double fftSize;
	public double[] signalPlot = new double[0];
	
	public SignalPlotView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		signalPlot = new double[w];
		Log.d("Chord", "SizeChanged " + w);
	}

	public void draw(Canvas canvas) {
		int plotWidth = getWidth();
		int plotHeight = getHeight();
		Paint fftPaint = new Paint(); fftPaint.setColor(Color.GREEN);
		Paint background = new Paint(); background.setColor(Color.GRAY);
		Paint pitchPaint = new Paint(); pitchPaint.setColor(Color.BLUE); pitchPaint.setStrokeWidth(3.0f);
		Paint pitchTopPaint = new Paint(); pitchTopPaint.setColor(Color.RED);
		canvas.drawRect(0.0f, 0.0f, (float)plotWidth, (float)plotHeight, background);
		for(int i = 0; i < signalPlot.length; i++) {
			float x = (float)i;
			float y0 = (float)(plotHeight / 2);
			float y = (float)(y0-signalPlot[i]*(plotHeight / 2) / UP_BORDER);
			canvas.drawLine(x, y0, x, y, fftPaint);
		}
	}
}
