package edu.calpoly.csc.chord;

// Blackman-Harris window function

public class WindowFunction {
	
	final static double a0 = 0.35875;
	final static double a1 = 0.48829;
	final static double a2 = 0.14128;
	final static double a3 = 0.01168;
	
	double[] w; 
	
	WindowFunction(int size){
		w = new double[size];

		for(int i = 0; i < size; i++){
			w[i] = a0 - a1 * Math.cos(2.0 * Math.PI * (double)i / (double)size)
					  + a2 * Math.cos(4.0 * Math.PI * (double)i / (double)size)
					  - a3 * Math.cos(6.0 * Math.PI * (double)i / (double)size);
		}
	}
	
	public void smooth(double[] data) {
		if(data.length != w.length) {
			throw new RuntimeException("Window function size = " + w.length + " is not the same as data size = " + data.length);
		}
		
		for(int i = 0; i < data.length; i++) {
			data[i] *= w[i];
		}
	}
	
}
