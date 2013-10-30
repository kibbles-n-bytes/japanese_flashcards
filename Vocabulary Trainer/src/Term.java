// Acts as an entry for a Japanese term, storing its Japanese form, English form, type, and 
// example sentences.

import java.awt.Component;
import java.awt.Font;
import java.io.*;
import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Term implements Comparable<Term>{
	private Word wordJapanese;
	private String wordEnglish;
	private String type;
	private LinkedList<String> particles; // Only applies to verbs
	private LinkedList<String> sentences;
	private ArrayList<Long> times;
	private Font font;
	private static int unsortedViewingTimes;  // Number of times to see the card before taking its time average into positioning consideration.
	private static int timeSize; // Size of the times list (affects weight of times during average calculations)
	
	// Initializes a term with the given Japanese word, English definition, type, and example 
	// sentences, and an empty set of times.
	public Term(Word wordJapanese, String wordEnglish, String type,
						LinkedList<String> sentences) {
		this.wordJapanese = wordJapanese;
		this.wordEnglish = wordEnglish;
		this.type = type;
		this.sentences = sentences;
		
		particles = new LinkedList<String>();
		
		// initializes times array to empty
		times = new ArrayList<Long>(timeSize);
		for(int i = 0; i < timeSize; i++)
			times.add(null);
		
		font = new Font("SansSerif", Font.BOLD, 50);
		// Loads custom "handwritten" font.
		try {
			font = Font.createFont(JFrame.NORMAL, new File("epkaisho.ttf"));
		} catch(Exception e) { System.out.println("Font could not be loaded."); }
	}
	
	// Initializes a term with the given Japanese word, English definition, type, particles, and 
	// example sentences, and an empty set of times.
	public Term(Word wordJapanese, String wordEnglish, String type, 
						LinkedList<String> particles, LinkedList<String> sentences) {
		this(wordJapanese, wordEnglish, type, sentences);
		this.particles = particles;
	}

	// Words that have a longer average time will appear higher in the deck. Words with the same
	// English definition are defined as the same.
	// NOTE: Fails if searching for a term and the times are the same.
	public int compareTo(Term other) {
		if(wordJapanese.equals(other.wordJapanese)) {
			return 0;
		}
		
		long thisTime = getAverageTime();
		long otherTime = other.getAverageTime();
		
		//System.out.println("This: " + this + " " + thisTime + " / Other: " + other + " " + otherTime);
		
		// Handles cases where one of cards has not been seen
		if(thisTime < 0 && otherTime < 0)
			return 1;
		if(thisTime < 0)
			return -1;
		else if(otherTime < 0)
			return 1;
		
		// Handles normal behavior
		if(thisTime - otherTime > 0)
			return -1;
		else
			return 1;
	}
	
	// Returns the user's average time for this card (or -1 if no average time available).
	public long getAverageTime() {
		long sum = 0;
		int count = 0;
		
		for(int i = 0; i < timeSize; i++) {
			if(times.get(i) != null) {
				sum += times.get(i);
				count++;
			}
		}
		
		if(count < unsortedViewingTimes)
			return -1;
		else
			return sum / timeSize;
	}
	
	// Returns the Kanji + Furigana panel view.
	public JPanel getKanjiAndKanaPanel() {
		return wordJapanese.getKanjiAndKanaPanel(font, 50.0f, 20.0f);
	}
	
	// Returns the Kanji + Okurigana panel view.
	public JPanel getKanjiPanel() {
		return wordJapanese.getKanjiPanel(font, 50.0f);
	}
	
	// Returns the kana-only panel view.
	public JPanel getKanaPanel() {
		return wordJapanese.getKanaPanel(font, 50.0f);
	}
	
	// Returns the English panel view.
	public JPanel getEnglishPanel() {
		JPanel panel = new JPanel();
		panel.add(new JLabel(wordEnglish));
		return panel;
	}
	
	// Adds the given time to the list of times, removing the last entry if it would make the list
	// of times have more than five members. If the given time is negative, then a null time is
	// assumed.
	public void addTime(long time) {
		if(times.size() == timeSize)
			times.remove(0);
		
		if(time > 0)
			times.add(time);
		else
			times.add(null);
	}
	
	// Returns a panel view of all the information stored by the term (Japanese word, English
	// translation, type, and example sentences)
	public JPanel getDefinitionPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JPanel japPanel = getKanjiAndKanaPanel();
		japPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		String engLabelText = wordEnglish;
		if(type.length() != 0)
			engLabelText += " (" + type + ")";
		JLabel engLabel = new JLabel(engLabelText);

		engLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		engLabel.setFont(font.deriveFont(30.0f));
		
		panel.add(japPanel);
		panel.add(engLabel);
		
		for(String particle: particles) {
			JLabel particleLabel = new JLabel(particle);
			particleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			particleLabel.setFont(font.deriveFont(30.0f));
			
			panel.add(particleLabel);
		}
		
		for(String sentence: sentences) {
			JLabel sentenceLabel = new JLabel(sentence);
			sentenceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			sentenceLabel.setFont(font.deriveFont(30.0f));
			
			panel.add(sentenceLabel);
		}
		
		return panel;
	}
	
	// Returns an unmodifiable version of the list of times.
	public List<Long> getTimes() {
		return Collections.unmodifiableList(times);
	}

	public String toString() {
		return wordEnglish;
	}

	// Sets the number of times to see the card before taking its time average into positioning 
	// consideration (Throws IllegalArgumentException if less than 1).
	public static void setUnsortedViewingTimes(int unsortedViewingTimes) {
		if(unsortedViewingTimes < 1)
			throw new IllegalArgumentException("Unsorted viewing times must be positive.");
		
		Term.unsortedViewingTimes = unsortedViewingTimes;
	}

	// Sets the size of the times list (Throws IllegalArgumentException if less than 1).
	public static void setTimeSize(int timeSize) {
		if(timeSize < 1)
			throw new IllegalArgumentException("Time buffer size must be at least 1.");
		
		Term.timeSize = timeSize;
	}
}