// This class simulates a deck of flash cards, allowing the client to create cards from a .txt 
// file, see the current card and definition, and go to the next card.

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.LinkedHashMap;

public class FlashCards {

	private LinkedHashMap<String, String[]> deck;
	private String current; 
	private ListIterator<String> iterator;
	private int direction;
	
	// Initializes a new deck from the given file of definitions.
	public FlashCards(File file) {
		this();
		
		try {
			addFile(file);
		} catch (FileNotFoundException e) {
			System.out.println("Could not create deck from file.");
		}
	}
	
	// Initializes an empty deck.
	public FlashCards() { 
		deck = new LinkedHashMap<String, String[]>();
		current = null;
		iterator = null;
		direction = 0;
	}
	
	// Creates the flash cards from the definitions file, splitting each term/definition pair
	// by the ":".
	public void addFile(File file) throws FileNotFoundException {
		Scanner input = new Scanner(file);
		
		if(input.hasNextLine())
			System.out.println("Yes");
		else
			System.out.println("No");
		
		// builds the deck
		while(input.hasNextLine()) {
			// gets rid of BOM from UTF-8 encoding
			String[] cardInfo = input.nextLine().replace((char) 65279, ' ').split(":"); 
			
			// Takes care of Japanese colons
			if(cardInfo.length == 1)
				cardInfo = cardInfo[0].split("：");
			
			if(cardInfo.length > 1){
				String kanji = cardInfo[0].trim();
				String kana = "";
				String english = "";
				
				if(cardInfo.length == 3) {
					kana = cardInfo[1].trim();
					english = cardInfo[2].trim();
				} else { // cardInfo.length == 2 (for words all in kana)
					kana = cardInfo[0].trim();
					english = cardInfo[1].trim();
				}
				
				String[] kanaAndEnglish = {kana, english};
			
				deck.put(kanji,  kanaAndEnglish);
			}
		}

		input.close();
		reset();
	}

	// Get the current card name.
	public String getCurrent() { return current; }
	
	// Get the kana representation of the current card.
	public String getKana() { 
		if(isEmpty())
			return null;
		return deck.get(current)[0]; 
	}
	
	// Get the definition of the current card.
	public String getDefinition() { 
		if(isEmpty())
			return null;
		return deck.get(current)[1]; 
	}
	
	// Advances the deck to the next card (or returns null if no more cards available).
	// throws NoSuchElementException.
	public void advance() { 
		if(!hasNext())
			throw new NoSuchElementException("No card in deck to advance to.");
		
		if(direction < 0) 
			current = iterator.next();
		
		current = iterator.next();
		direction = 1;
		}
	
	// Goes back in the deck to the previous card (or returns null if no cards behind it).
	public void goBack() {
		if(!hasPrevious())
			throw new NoSuchElementException("No card in deck to go back to.");
		
		if(direction > 0)
			current = iterator.previous();

		current = iterator.previous();
		direction = -1;
	}
	
	// Shuffles the deck so the cards are randomly ordered.
	public void shuffle() {
		if(isEmpty())
			return;
		
		ArrayList<String> cards = new ArrayList<String>(deck.keySet());
		Collections.shuffle(cards);
		LinkedHashMap<String, String[]> deck = new LinkedHashMap<String, String[]>();
		
		for(String card: cards) {
			deck.put(card, this.deck.get(card));
		}

		this.deck = deck;
		reset();
	}
	
	// Returns a list of cards in the deck.
	public String toString() { return deck.toString(); }
	
	// Resets the current position in the deck to the very top.
	public void reset() {
		if(isEmpty())
			return;
		
		iterator = new LinkedList<String>(deck.keySet()).listIterator();
		current = iterator.hasNext() ? iterator.next() : null;
		direction = 0;
	}
	
	// Checks if the there are any more cards remaining in the deck.
	public boolean hasNext() { return iterator != null && iterator.hasNext(); }
	
	// Checks if there are any cards in the deck behind the current one.
	public boolean hasPrevious() { return iterator != null && iterator.hasPrevious() && direction != 0; }
	
	// Returns whether the deck is empty.
	public boolean isEmpty() { return deck.size() == 0; }
}
