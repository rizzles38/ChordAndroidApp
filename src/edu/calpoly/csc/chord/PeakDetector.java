package edu.calpoly.csc.chord;

import java.util.ArrayList;

public class PeakDetector {

	public static ArrayList<Peak> getPeaks(double[] fft, double minValue) {
		ArrayList<Peak> peaks = new ArrayList<Peak>();
		double val2 = fft[0];
		double val1 = fft[1];
		for(int i=2; i<fft.length; i++) {
			double val = fft[i];
			if(val1 >= minValue &&	val1 >= val2 && val1 > val){
				double position = i - 1 + (val - val2) / (2.0 * val1 - val - val2) / 2.0;
				peaks.add(new Peak(position, val1));
			}
			val2 = val1;
			val1 = val;
		}
		return peaks;
	}

	public static class Peak {
		double position;
		double value;
		public Peak(double position, double value) {
			this.position = position;
			this.value = value;
		}
	}

}
