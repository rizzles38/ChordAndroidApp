package edu.calpoly.csc.chord;

public class Utils {

	final static double SCALE_DOUBLE_TO_SHORT = 2000.0;

	public static void normalizePositiveMax(double[] fft) {
		double max = 0.0;
		for(int i=0; i<fft.length; i++) {
			max = max < fft[i] ? fft[i] : max;
		}
		for(int i=0; i<fft.length; i++) {
			fft[i] /= max;
		}
	}
	
	public static double getAve(double[] fft) {
		double ave = 0.0;
		for(int i=0; i<fft.length; i++) {
			ave += fft[i];
		}
		ave /= fft.length;
		return ave;
	}

	public static double findMax(double[] val) {
		double max = -Double.MAX_VALUE;
		int p=-1;
		for(int i=0; i<val.length; i++) if(max < val[i]){
			p = i;
			max = val[i];
		}
		if(p==0 || p==val.length-1){
			return (double)p;
		} else {
			return (double)p + (val[p+1] - val[p-1]) / (2.0 * val[p] - val[p+1] - val[p-1]) / 2.0;
		}
	}

	public static void convertShortArrayToReIm(short[] audioData,	double[] re, double[] im) {
		for(int i = 0; i < audioData.length; i++){
			re[i] = (double)audioData[i] / Utils.SCALE_DOUBLE_TO_SHORT;
			im[i] = 0.0;
		}
	}

	
	public static int findSynchShift(double[] data, double[] old) {
		// data is bigger than old, find a position somewhere within data having a good correlation with old
		final int RANGE = 200; // choose so that it's big enough (better sync), but no performance impact
		int posMax = 0;
		double max = -Double.MAX_VALUE;
		for(int pos = 0; pos < RANGE; pos++){
			double corr = 0.0;
			for(int i=0; i<old.length; i++){
				corr += old[i] * data[i + pos];
			}
			if(corr>max){
				posMax = pos;
				max = corr;
			}
		}
		return posMax;
	}


}
