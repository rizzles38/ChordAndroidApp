package edu.calpoly.csc.chord;

import java.util.ArrayList;

import edu.calpoly.csc.chord.PeakDetector.Peak;

public class PitchDetector {
	
	public static final double A4_FREQUENCY = 440.0;
	public static final int A4_KEY_NUMBER = 49;
	public static final int C0_KEY_NUMBER = -8;

	private static final double HARMONIC_MAX_ERROR = 0.05;

	public static ArrayList<Pitch> getPitches(ArrayList<Peak> peaks, double sampleRate, double fftSize, double fundamental) {
		ArrayList<Pitch> pitches = new ArrayList<Pitch>();
		for(Peak peak : peaks) {
			double frequency = peak.position * sampleRate / (double)fftSize;
			int harmonic = 0;
			Pitch pitchFound = null;
			for(Pitch pitch : pitches) {
				double ratio = frequency/pitch.frequency;
				harmonic = (int)Math.round(ratio);
				if(Math.abs(ratio/harmonic-1)<HARMONIC_MAX_ERROR){
					pitchFound = pitch;
				}
			}
			if(pitchFound == null) {
				if(fundamental == 0.0 || (frequency >= fundamental && frequency <= fundamental * 2.0)){    
					double tone = 12.0 * Math.log(frequency / A4_FREQUENCY) / Math.log(2.0) + A4_KEY_NUMBER;
					pitches.add(new Pitch(frequency, peak.value, tone));
				}
			}
		}
		
		double sumX = 0.0;
		double sumY = 0.0;
		for(Pitch pitch : pitches) {
			double alpha = (pitch.tone - (int)Math.round(pitch.tone)) * 2.0 * Math.PI;
			sumX += pitch.volume * Math.cos(alpha);
			sumY += pitch.volume * Math.sin(alpha);
		}
		double chordErr = Math.atan2(sumY, sumX) / 2.0 / Math.PI;
		for(Pitch pitch : pitches) {
			pitch.chordErr = chordErr;
			pitch.keyNumber = (int)Math.round(pitch.tone - pitch.chordErr);
			pitch.err = pitch.tone - pitch.chordErr - pitch.keyNumber;
			
			pitch.note = (pitch.keyNumber - C0_KEY_NUMBER) % 12;
			pitch.octaveNumber = (pitch.keyNumber - C0_KEY_NUMBER) / 12;
			if(pitch.note < 0){
				pitch.note += 12;
				pitch.octaveNumber--;
			}
		}
		
		return pitches;
	}

	public static ArrayList<Pitch> getPitches(ArrayList<Peak> peaks, double sampleRate, double fftSize) {
		return getPitches(peaks, sampleRate, fftSize, 0.0);
	}
	
	public static class Pitch {
		double frequency;  // in Hz
		double volume;
		double tone;   // = frequency in log scale, 440Hz = 49 tone, octave = 12 tones 
		int keyNumber; // = rounded tone
		double chordErr;
		double err;    // - remainder error after chord-tuning: tone = keyNumber + chordErr + err
		int note;      // 0..11: C=0, D=2, E=4, F=5, G=7, A=9, B=11
		int octaveNumber;
		public Pitch(double frequency, double volume, double tone) {
			this.frequency = frequency;
			this.volume = volume;
			this.tone = tone;
		}
	}

}
