// Acts as a Japanese word, storing kanji (with its furigana) and kana. Is able to return a
// panel with the word and its furigana displayed.

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Word {
	private LinkedList<String[]> parts;
	
	public Word() {
		parts = new LinkedList<String[]>();
	}
	
	// Adds the given hiragana/katakana part to the word.
	public void add(String part) {
		parts.add(new String[]{part});
	}
	
	// Adds the given kanji (and stores associated kana) to the word.
	public void add(String kanji, String kana) {
		parts.add(new String[]{kanji, kana});
	}
	
	// Takes in a font, and floating point values for the size of the kanji (and regular kana) and 
	// furigana, and returns a panel with the Japanese reading of the word with furigana added.
	public JPanel getKanjiAndKanaPanel(Font font, float kanjiSize, float furiSize) {
		JPanel panel = new JPanel(new GridBagLayout());
		
		Iterator<String[]> iter = parts.iterator();
		int i = 0;
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		
		// Adds kanji and kana in row 1, furigana in row 0
		while(iter.hasNext()) {
			String[] part = iter.next();
			JLabel main = new JLabel(part[0]);
			main.setFont(font.deriveFont(kanjiSize));
			
			c.gridx = i;
			panel.add(main, c);
			
			// Adds furigana to kanji
			if(part.length == 2) {
				c.gridy = 0;
				JLabel furigana = new JLabel(part[1]);
				furigana.setFont(font.deriveFont(furiSize));
				panel.add(furigana, c);
				c.gridy = 1;
			}
			
			i++;
		}
		
		return panel;
	}

	// Returns a panel view of the Japanese word spelled with just kanji and okurigana.
	public JPanel getKanjiPanel(Font font, float fontSize) {
		JPanel panel = new JPanel();
		JLabel japaneseLabel = new JLabel();
		japaneseLabel.setFont(font.deriveFont(fontSize));
		
		String japaneseText = "";
		for(String[] part : parts)
			japaneseText += part[0];
		
		japaneseLabel.setText(japaneseText);
		panel.add(japaneseLabel);
		
		return panel;
	}
	
	// Returns a panel view of the Japanese word spelled with just hiragana.
	public JPanel getKanaPanel(Font font, float fontSize) {
		JPanel panel = new JPanel();
		JLabel japaneseLabel = new JLabel();
		japaneseLabel.setFont(font.deriveFont(fontSize));
		
		String japaneseText = "";
		for(String[] part : parts) {
			if(part.length == 2)
				japaneseText += part[1];
			else
				japaneseText += part[0];
		}
		
		japaneseLabel.setText(japaneseText);
		panel.add(japaneseLabel);
		
		return panel;
	}
	
	// Takes a String of structured Japanese, parses it, and creates a word from it.
	// Kanji should be surrounded by curly braces with its furigana to the right also 
	// surrounded by curly braces.
	public void addJapanese(String jap) {
		for(int i = 0; i < jap.length(); i++) {
			
			if(jap.charAt(i) == '｛') { // We're at a kanji
				int openIndex = i;
				int closeIndex = jap.indexOf('｝', openIndex + 1);
				String kanji = jap.substring(openIndex + 1, closeIndex);
				
				openIndex = closeIndex + 1; // Get open curly position for furigana
				closeIndex = jap.indexOf('｝', openIndex + 1);
				String furigana = jap.substring(openIndex + 1, closeIndex);
				
				add(kanji, furigana);
				i = closeIndex;
			} else // Every character normally found is added as just kana.
				add("" + jap.charAt(i));
			
		}
	}

	// Returns true if the other word has the exact same kanji, furigana, and kana.
	public boolean equals(Word other) {
		if(parts.size() != other.parts.size())
			return false;
		
		Iterator<String[]> thisIter = parts.iterator();
		Iterator<String[]> otherIter = other.parts.iterator();
		
		while(thisIter.hasNext()) {
			String[] thisPart = thisIter.next();
			String[] otherPart = otherIter.next();
			
			if(thisPart.length != otherPart.length)
				return false;
			
			for(int i = 0; i < thisPart.length; i++) {
				if(thisPart[i] != otherPart[i])
					return false;
			}
		}
		
		return true;
	}
}
