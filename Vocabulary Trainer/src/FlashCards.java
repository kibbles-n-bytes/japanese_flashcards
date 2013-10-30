// This class simulates a deck of flash cards, allowing the client to create cards from a .txt 
// file, see the current card and definition, and go to the next card. Deck must have at least 3
// members.

import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;

public class FlashCards {

	private SortedSet<Term> deck;
	private Queue<Term> buffer;
	private DeckType deckType;

	///// STATIC FIELDS /////
	private static int unsortedViewingTimes = 1;  // Number of times to see the card before 
																			  // taking its time average into positioning 
																			  // consideration.
	private static int timeSize = 4; // Size of the times list (affects weight of times during 
													   // average calculations)
	private static long timeThreshold = 3000; // Maximum time allowed to retire a card
	private static int bufferSize = 2; // Minimum turns to wait before adding card back in deck
	
	// Initializes an empty deck.
	public FlashCards(DeckType deckType) { 
		deck = new TreeSet<Term>();
		buffer = new LinkedList<Term>();
		this.deckType = deckType;
		Term.setUnsortedViewingTimes(unsortedViewingTimes);
		Term.setTimeSize(timeSize);
	}
	
	// Returns the top card's panel view, in the format specified by the deck's display type.
	public JPanel getTopCardPanel() {
		if(isEmpty())
			throw new IllegalStateException("Not enough cards in deck.");
		
		if(deckType == DeckType.KANJI_AND_KANA)
			return deck.first().getKanjiAndKanaPanel();
		else if(deckType == DeckType.KANJI)
			return deck.first().getKanjiPanel();
		else if(deckType == DeckType.KANA)
			return deck.first().getKanaPanel();
		else // deckType == DeckType.ENGLISH
			return deck.first().getEnglishPanel();
	}
	
	// Returns the top card's definition panel view.
	public JPanel getTopCardDefinitionPanel() {
		return deck.first().getDefinitionPanel();
	}
	
	// Creates a new entry in the deck from the given definition file.
	public void addTerm(File file) throws FileNotFoundException {
		Scanner input = new Scanner(file, "UTF-8");
		
		Word wordJapanese = new Word();
		String wordEnglish = "";
		String type = "";
		LinkedList<String> sentences = new LinkedList<String>();
		LinkedList<String> particles = new LinkedList<String>();
		
		while(input.hasNextLine()) {
			// gets rid of BOM from UTF-8 encoding
			String[] cardInfo = input.nextLine().replace((char) 65279, ' ').split(":"); 
			
			switch(cardInfo[0].trim()) {
			case "wordJapanese":
				wordJapanese.addJapanese(cardInfo[1].trim());
				break;
			case "wordEnglish":
				wordEnglish = cardInfo[1].trim();
				break;
			case "type":
				type = cardInfo[1].trim();
				break;
			case "sentence":
				sentences.add(cardInfo[1].trim());
				break;
			case "particle":
				particles.add(cardInfo[1].trim());
				break;
			}
		}

		Term term = new Term(wordJapanese, wordEnglish, type, particles, sentences);
		deck.add(term);

		input.close();
	}
	
	// Returns a list of cards in the deck.
	public String toString() { return deck.toString() + " / " + buffer.toString(); }
	
	// Returns whether the deck is empty.
	public boolean isEmpty() { return deck.size() == 0; }
	
	// Takes the current top term, updates its time and puts it in the buffer. Starts adding
	// cards from the buffer back into the deck after three turns.
	public void refresh(long time) {
		// TROUBLESHOOTING STUFF
		// System.out.println(deck + "/" + buffer);
		
		Term top = deck.first();
		
		// Removes top card from deck (cannot call deck.remove() since it gets confused if the
		// average times are the same.
		Iterator<Term> iter = deck.iterator();
		iter.next();
		iter.remove();
		
		//TROUBLESHOOTING STUFF
		/*
		System.out.println("Time: " + time);
		System.out.println("Time list before: " + top.getTimes());
		*/
		
		top.addTime(time);
		
		// TROUBLESHOOTING STUFF
		/*
		System.out.println("Time list after: " + top.getTimes());
		System.out.println("Average time: " + top.getAverageTime());
		System.out.println();
		*/
		
		

		
		// Only adds the card to the buffer (and thus back to the deck) if it shouldn't be retired
		if(!shouldRetire(top))
			buffer.add(top);
		
		// If buffer has reached capacity, or the deck is empty while the buffer still has some,
		// move cards from buffer to deck
		if(buffer.size() > bufferSize || (deck.size() == 0 && buffer.size() != 0))
			deck.add(buffer.remove());
	}
	
	// Returns true if all the term's times are below the time threshold (false otherwise).
	private boolean shouldRetire(Term term) {
		// RETIRES IF ALL TIMES ABOVE THRESHOLD
		/*
		List<Long> times = term.getTimes();
		
		for(int i = 0; i < times.size(); i++) {
			if(times.get(i) == null || times.get(i) > timeThreshold)
				return false;
		}
		
		return true;
		*/
		
		// RETIRES IF AVERAGE TIME ABOVE THRESHOLD OR ALL TIMES
		// NOT YET FILLED IN
		List<Long> times = term.getTimes();
		
		for(int i = 0; i < times.size(); i++) {
			if(times.get(i) == null)
				return false;
		}
		
		long averageTime = term.getAverageTime();
		if(averageTime != -1 && averageTime < timeThreshold)
			return true;
		else
			return false;
	}

	// Shuffles the cards in the deck.
	public void shuffle() {
		List<Term> deckList = new LinkedList<Term>(deck);
		Collections.shuffle(deckList);
		deck = new TreeSet<Term>(deckList);
	}
}