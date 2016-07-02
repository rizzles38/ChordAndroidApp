package edu.calpoly.csc.chord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.calpoly.csc.chord.PitchDetector.Pitch;

public class ChordDetector {

	public static final String[] notes = {"C", "C#", "D", "D#",  "E", "F", "F#", "G", "G#", "A", "A#", "B"}; 

	static final String[] chords = {"M", "m", "+", "dim"};
	static final int[][] chordIntervals = {
		{4, 3, 5, 4},   // major 
		{3, 4, 5, 3},   // minor 
		{4, 4, 4, 4},   // augmented 
		{3, 3, 6, 3}    // diminished 
	};

	static final String[] chords4 = {"dim7", "dim07", "m7", "mM7", "7", "M7", "+7", "+M7", "M6", "m6"};
	static final int[][] chord4Intervals = {
		{3, 3, 3, 3, 3, 3},   // diminished seventh 
		{3, 3, 4, 2, 3, 3},   // half-diminished seventh 
		{3, 4, 3, 2, 3, 4},   // minor seventh 
		{3, 4, 4, 1, 3, 4},   // minor major seventh  
		{4, 3, 3, 2, 4, 3},   // dominant seventh 
		{4, 3, 4, 1, 4, 3},   // major seventh 
		{4, 4, 2, 2, 4, 4},   // augmented seventh 
		{4, 4, 3, 1, 4, 4},   // augmented major seventh 
		{4, 3, 2, 3, 4, 3},   // major sixth - same as inverted minor seventh
		{3, 4, 2, 3, 3, 4}    // minor sixth - same as inverted half-diminished seventh
	};

	public static String detectChord(ArrayList<Pitch> pitches) {
		String chordName = null;
		ArrayList<Pitch> best;
		Pitch fundamentalPitch = pitches.get(0);
		
		Collections.sort(pitches, new PitchQualityComparator());

		if(pitches.size() >= 4) {
			best = getFirst(pitches, 4);
			chordName = matchChord(best);
			if(chordName != null) return chordName;
		}
		
		if(pitches.size() >= 3) {
			best = getFirst(pitches, 3);
			chordName = matchChord(best);
			if(chordName != null) return chordName;
		}
		
//		best = getFirst(pitches, 1);
//		return notes[best.get(0).note];
		return notes[fundamentalPitch.note];
	}

	private static String matchChord(ArrayList<Pitch> best) {
		Collections.sort(best, new PitchToneComparator());
		int n = best.size();

		// calculate intervals
		int intervals[] = new int[n-1];
		for(int i = 0; i < n-1; i++){
			intervals[i] = best.get(i+1).keyNumber - best.get(i).keyNumber;
		}
		
		for(int invert = 0; invert < n; invert++){

			for(int chord = 0; chord < (n==3 ? chordIntervals.length : chord4Intervals.length); chord++) {
				int[] pattern = (n==3 ? chordIntervals[chord] : chord4Intervals[chord]);
			
				// check if intervals[] matches intervals of a known chord 
				boolean match = true;
				for(int i = 0; i < n-1; i++){
					if(intervals[i] != pattern[i + invert]) {
						match = false;
						break;
					}
				}

				if(match){
					if(invert == 0) {
						return notes[best.get(0).note] + (n==3 ? chords[chord] : chords4[chord]);
					} else {
						int root = n - invert;
						return notes[best.get(root).note] + (n==3 ? chords[chord] : chords4[chord]) + "/" + notes[best.get(0).note];
					}
				}
				
			}
		}
		
		return null;
	}

	private static ArrayList<Pitch> getFirst(ArrayList<Pitch> pitches, int howManyToCopy) {
		ArrayList<Pitch> firstElements = new ArrayList<Pitch>();
		for(int i = 0; i < howManyToCopy; i++){
			firstElements.add(pitches.get(i));
		}
		return firstElements;
		
	}

	public static class PitchQualityComparator implements Comparator<Pitch> {
		@Override
		public int compare(Pitch p1, Pitch p2) {
			double q1 = quality(p1); 
			double q2 = quality(p2); 
			if(q1 < q2) return 1;   // for descending sorting, return the opposite sign
			if(q1 > q2) return -1;
			return 0;
		}
		
		private double quality(Pitch p) {
			return p.volume * (1.0 - Math.abs(p.err));
		}

		
	}

	public static class PitchToneComparator implements Comparator<Pitch> {
		@Override
		public int compare(Pitch p1, Pitch p2) {
			if(p1.tone < p2.tone) return -1;
			if(p1.tone > p2.tone) return 1;
			return 0;
		}
	}

	public static class PitchNoteComparator implements Comparator<Pitch> {
		@Override
		public int compare(Pitch p1, Pitch p2) {
			if(p1.note < p2.note) return -1;
			if(p1.note > p2.note) return 1;
			return 0;
		}
	}

}
