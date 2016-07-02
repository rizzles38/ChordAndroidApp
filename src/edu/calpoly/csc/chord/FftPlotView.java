package edu.calpoly.csc.chord;

import edu.calpoly.csc.chord.PitchDetector.Pitch;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FftPlotView extends View {

	public static final int PITCH_PLOT_SIZE = 10;
	private static final double UP_BORDER = 1.1;
	public double sampleRate;
	public double fftSize;
	public double[] fftPlot = new double[0];
	public Pitch[] pitchPlot = new Pitch[PITCH_PLOT_SIZE];
	
	public FftPlotView(Context context, AttributeSet attrs) {
		super(context, attrs);
		for(int i = 0; i < PITCH_PLOT_SIZE; i++) {
			pitchPlot[i] = new Pitch(-1, -1, -1);
		}
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		fftPlot = new double[w];
	}

	public void draw(Canvas canvas) {
		int plotWidth = getWidth();
		int plotHeight = getHeight();
		Paint fftPaint = new Paint(); fftPaint.setColor(Color.GREEN);
		Paint background = new Paint(); background.setColor(Color.GRAY);
		Paint pitchPaint = new Paint(); pitchPaint.setColor(Color.BLUE); pitchPaint.setStrokeWidth(3.0f);
		Paint pitchTopPaint = new Paint(); pitchTopPaint.setColor(Color.RED);
		canvas.drawRect(0.0f, 0.0f, (float)plotWidth, (float)plotHeight, background);
  		for(int i = 0; i < fftPlot.length; i++) {
			float x = (float)i;
			float y0 = (float)plotHeight;
			float y = (float)(plotHeight * (UP_BORDER-fftPlot[i]));
			canvas.drawLine(x, y0, x, y, fftPaint);
		}
		for(int i = 0; i < PITCH_PLOT_SIZE; i++) {
			float x = (float)(plotWidth * pitchPlot[i].frequency / sampleRate * fftSize / fftPlot.length);
			float y = (float)(plotHeight * (UP_BORDER-pitchPlot[i].volume));
			canvas.drawRect(x-3, y-3, x+3, y+3, pitchTopPaint);
		}
	}
}
