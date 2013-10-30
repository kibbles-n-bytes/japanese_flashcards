import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class CreateDeckGUI extends JFrame {
	private JButton create;
	private JTextField japaneseText;
	private JTextField englishText;
	private JTextField typeText;
	private JTextArea sentencesText;
	private JTextArea particlesText;
	private static final String FOLDER_PATH = "../Decks/Lesson 8/Additional Vocabulary/Adverbs";
	private Font font;
	
	public CreateDeckGUI() {
		super("Create a Deck");
		
		font = new Font("SansSerif", Font.BOLD, 50);
		// Loads custom "handwritten" font.
		try {
			font = Font.createFont(JFrame.NORMAL, new File("epkaisho.ttf"));
		} catch(Exception e) { System.out.println("Font could not be loaded."); }
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		c.gridx = 0;
		c.gridy = 0;
		
		// Create labels
		add(new JLabel("Japanese:"), c);
		c.gridy++;
		add(new JLabel("English:"), c);
		c.gridy++;
		add(new JLabel("Type:"), c);
		c.gridy++;
		add(new JLabel("Particles:"), c);
		c.gridy++;
		add(new JLabel("Sentences:"), c);
		
		c.gridx = 1;
		c.gridy = 0;
		// Create text fields
		japaneseText = new JTextField(20);
		japaneseText.setFont(font.deriveFont(30.0f));
		add(japaneseText, c);
		c.gridy++;
		englishText = new JTextField(20);
		add(englishText, c);
		c.gridy++;
		typeText = new JTextField(20);
		add(typeText, c);
		c.gridy++;
		particlesText = new JTextArea(2,20);
		particlesText.setEditable(true);
		add(particlesText, c);
		c.gridy++;
		sentencesText = new JTextArea(2,20);
		sentencesText.setEditable(true);
		add(sentencesText, c);
		c.gridy++;
		
		create = new JButton("Create");
		create.addActionListener(new CreateListener());
		add(create, c);
		
		File folder = new File(FOLDER_PATH);
		if(!folder.exists())
			folder.mkdirs();
		
		CreateAction createCard = new CreateAction();
		create.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("ctrl ENTER"), "createCard");
		create.getActionMap().put("createCard", createCard);
		
		pack();
		setVisible(true);
	}
	
	// Main method that constructs the gui.
	public static void main(String[] args) {
		CreateDeckGUI gui = new CreateDeckGUI();
	}
	
	// Action listener for the create button.
	class CreateListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
		}
	}

	class CreateAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			String japanese = japaneseText.getText();
			String english = englishText.getText();
			String type = typeText.getText();
			String sentences = sentencesText.getText();
			String particles = particlesText.getText();
			
			if(japanese.length() == 0)
				return;
			
			File word = new File(FOLDER_PATH + "/" + japanese + ".txt");
			
			// Prints to output file.
			try {
				PrintStream output = new PrintStream(word, "UTF-8");
				output.print("wordJapanese:");
				output.println(japanese);
				
				output.print("wordEnglish:");
				output.println(english);
				
				if(type.length() != 0) {
					output.print("type:");
					output.println(type);
				}
				
				Scanner particleScanner = new Scanner(particles);
				while(particleScanner.hasNextLine()) {
					output.print("particle:");
					output.println(particleScanner.nextLine());
				}
				particleScanner.close();
				
				Scanner sentenceScanner = new Scanner(sentences);
				while(sentenceScanner.hasNextLine()) {
					output.print("sentence:");
					output.println(sentenceScanner.nextLine());
				}
				sentenceScanner.close();
				
				output.close();
			} catch(FileNotFoundException | UnsupportedEncodingException exc) {
				System.out.println("Cannot create file.");
				return;
			}
			
			// Clears out fields and sets focus back on japanese text field.
			japaneseText.setText("");
			englishText.setText("");
			typeText.setText("");
			particlesText.setText("");
			sentencesText.setText("");
			
			japaneseText.requestFocusInWindow();
		}
	}
}
