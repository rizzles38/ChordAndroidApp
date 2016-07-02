package edu.calpoly.csc.chord;

import java.util.ArrayList;

import edu.calpoly.csc.chord.PeakDetector.Peak;
import edu.calpoly.csc.chord.PitchDetector.Pitch;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class AudioIn extends Activity {

	private static final int SAMPLE_RATE = 44100;
	private static final int FFT_SIZE = 1024 * 8;
	short[] audioData = new short[FFT_SIZE];
	double[] re = new double[FFT_SIZE];
	double[] im = new double[FFT_SIZE];
	double[] fft = new double[FFT_SIZE / 2];
	WindowFunction windowFunction = new WindowFunction(re.length);
	FFT FFT = new FFT(re.length);
	AudioRecord audioRecord;
	boolean isListening = false;
	ListeningTask listeningTask = null;
	String chord = "";
	String lastChord1 = chord;
	TextView textView;
	SignalPlotView signalPlotView;
	FftPlotView fftPlotView;
	double plotScale = 0.0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_in);
		textView = (TextView) this.findViewById(R.id.textView);
		signalPlotView = (SignalPlotView) this.findViewById(R.id.signalView); 
		signalPlotView.sampleRate = (double)SAMPLE_RATE;
		signalPlotView.fftSize = (double)FFT_SIZE;
		signalPlotView.setKeepScreenOn(true);
		
		fftPlotView = (FftPlotView) this.findViewById(R.id.fftView); 
		fftPlotView.sampleRate = (double)SAMPLE_RATE;
		fftPlotView.fftSize = (double)FFT_SIZE;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.audio_in, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isListening) {
			isListening = true;
			audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.MIC,
					SAMPLE_RATE, 
					AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, 
					audioData.length * Short.SIZE / Byte.SIZE * 2);
			Log.d("Chord", "size of audioData = " + audioData.length);

			listeningTask = new ListeningTask();
			listeningTask.execute();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isListening = false;
	}

	private class ListeningTask extends AsyncTask<Void, String, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.d("Chord", "Start Listening");
				audioRecord.startRecording();
				while (isListening) {
					for (int i = 0; i < audioData.length / 2; i++) {
						audioData[i] = audioData[i + audioData.length / 2];
					}
					audioRecord.read(audioData, audioData.length / 2, audioData.length / 2);
					Utils.convertShortArrayToReIm(audioData, re, im);

					int syncPos = Utils.findSynchShift(re, signalPlotView.signalPlot);
					
					for (int i = 0; i < signalPlotView.signalPlot.length; i++) {
						signalPlotView.signalPlot[i] = re[i + syncPos] * plotScale;
					}

					windowFunction.smooth(re);

					FFT.fft(re, im);
					for (int i = 0; i < fft.length; i++) {
						fft[i] = Math.hypot(re[i], im[i]);
					}
					Utils.normalizePositiveMax(fft);
					double ave = Utils.getAve(fft);

					plotScale = 0.007 / ave; 
					if(plotScale > 1.0) plotScale = 1.0;
					
					for (int i = 0; i < fftPlotView.fftPlot.length; i++) {
						fftPlotView.fftPlot[i] = fft[i] * plotScale;
					}

					Utils.normalizePositiveMax(signalPlotView.signalPlot);
					for (int i = 0; i < signalPlotView.signalPlot.length; i++) {
						signalPlotView.signalPlot[i] *= plotScale;
					}

					String outString;
					if (Math.sqrt(ave) < 0.1) {
						ArrayList<Peak> peaks = PeakDetector.getPeaks(fft, 0.1);
						double fundamental = peaks.get(0).position * (double) SAMPLE_RATE / (double) FFT_SIZE;
						ArrayList<Pitch> pitches = PitchDetector.getPitches(peaks, (double) SAMPLE_RATE, (double) FFT_SIZE,	fundamental);
						chord = ChordDetector.detectChord(pitches);

						for(int i = 0; i < FftPlotView.PITCH_PLOT_SIZE; i++) {
							if(i < pitches.size()){
								fftPlotView.pitchPlot[i].frequency = pitches.get(i).frequency;
								fftPlotView.pitchPlot[i].volume = pitches.get(i).volume;
							} else {
								fftPlotView.pitchPlot[i].frequency = 0.0;
								fftPlotView.pitchPlot[i].volume = 0.0;
							}
						}

						StringBuilder outStringBuilder = new StringBuilder(100);
						for (Pitch pitch : pitches) {
							outStringBuilder.append(String.format(
									"vol=%4.2f fr=%8.2f tone=%6.2f note=%2s oct=%d err=%3d%%\n",
									pitch.volume,
									pitch.frequency,
									pitch.tone,
									ChordDetector.notes[pitch.note],
									pitch.octaveNumber,
									(int)(pitch.chordErr*100.0)));
						}
						outString = chord + "\n" + outStringBuilder.toString();

					} else {
						outString = null;
					}
					publishProgress(outString);

					lastChord1 = chord;
				}
				audioRecord.stop();
				audioRecord.release();
				audioRecord = null;
				Log.d("Chord", "Stopped Listening");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... outString) {

			if (chord.equals(lastChord1) 
					&& outString[0] != null) {
				textView.setText(outString[0]);
			} else {
				textView.setText("");
				for(int i = 0; i < FftPlotView.PITCH_PLOT_SIZE; i++) {
					fftPlotView.pitchPlot[i].frequency = 0.0;
					fftPlotView.pitchPlot[i].volume = 0.0;
				}
				
			}
			
			signalPlotView.invalidate();
			fftPlotView.invalidate();
		}
	}
	
}
