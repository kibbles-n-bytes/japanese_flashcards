// Michael Kibbe
// 10/12/12
// This class acts as the user interface for the client to be able to interact with the flash 
// card deck.

import it.cnr.imaa.essi.lablib.gui.checkboxtree.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.*;

public class FlashCardGUI extends JFrame {

	////// Constants for determining what is currently showing /////
	public static final int FACE = 0;
	public static final int KANA = 1;
	public static final int DEFINITION = 2;
	
	/////////// FIELDS ///////////
	private static FlashCardGUI gui;
	private CheckboxTree fileTree;
	private JLabel card;
	private JButton shuffle;
	private JButton load;
	private JButton reset;
	private JCheckBox autoCheckBox;
	private JLabel speedLabel;
	private JTextField speed;
	private JCheckBox kanaCheckBox;
	private JComboBox<String> languageComboBox;
	private FlashCards deck;
	private int position;
	private boolean kanaOnly;
	private boolean englishToJapanese;
	private boolean isShuffled;
	private Timer timer;
	
	// Constructs a default FlashCardGUI frame with a file tree on the left, flash card on the
	// right, and a bar of controls on the bottom.
	public FlashCardGUI() {
		// Creates frame
		super("Japanese Flash Card");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		////////// Builds Tree of Files //////////
		DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode("Decks");
		
		fileTree = new CheckboxTree(treeRoot);
		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fileTree.setCellRenderer(new DefaultCheckboxTreeCellRenderer());
		fileTree.setPreferredSize(new Dimension(250, 300));
		
		File deckDir = new File("decks");
		
		for(File dir: deckDir.listFiles()) {
			// Create the header lesson
			DefaultMutableTreeNode header = new DefaultMutableTreeNode(dir.getName());
			treeRoot.add(header);
			// Create the decks underneath it
			for(File deckFile: dir.listFiles()) {
				FileTreeNode deck = new FileTreeNode(deckFile.getName(), deckFile);
				header.add(deck);
			}
		}
		
		fileTree.expandRow(0);
		
		////////// Creates card on right and implements advancing to next card. //////////
		card = new JLabel();
		card.setHorizontalAlignment(SwingConstants.CENTER);
		card.setPreferredSize(new Dimension(400, 300));
		
		Font font = new Font("SansSerif", Font.BOLD, 50);
		
		// Loads custom "handwritten" font.
		try {
			font = Font.createFont(NORMAL, new File("epkaisho.ttf"));
			font = font.deriveFont(50.0f);
		} catch(Exception e) { System.out.println("Didn't work."); }
		
		card.setFont(font);
		
		NextCardAction nextCard = new NextCardAction();
		PreviousCardAction previousCard = new PreviousCardAction();
		DefinitionAction definition = new DefinitionAction();
		KanaAction kana = new KanaAction();
		ReturnAction returnToFace = new ReturnAction();
		
		// Sets actions for moving between cards
		card.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("RIGHT"), "getNextCard");
		card.getActionMap().put("getNextCard", nextCard);
		card.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("LEFT"), "getPreviousCard");
		card.getActionMap().put("getPreviousCard", previousCard);
		
		// Sets actions for getting definitions/kana
		card.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("DOWN"), "getDefinition");
		card.getActionMap().put("getDefinition", definition);
		card.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("UP"), "getKana");
		card.getActionMap().put("getKana", kana);
		
		// Sets key release actions for getting definitions/kana
		card.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("released DOWN"), "returnToFace");
		card.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("released UP"), "returnToFace");
		card.getActionMap().put("returnToFace", returnToFace);
		
		// Creates the control panel on the bottom and adds the buttons
		JPanel controls = new JPanel();
		
		load = new JButton("Load");
		load.addActionListener(new LoadListener());
		controls.add(load);
		
		shuffle = new JButton("Shuffle");
		shuffle.addActionListener(new ShuffleListener());
		controls.add(shuffle);
		
		reset = new JButton("Reset");
		reset.addActionListener(new ResetListener());
		controls.add(reset);
		
		kanaCheckBox = new JCheckBox("Kana only");
		kanaCheckBox.addItemListener(new KanaListener());
		controls.add(kanaCheckBox);
		
		String[] languageModes = {"日本語 --> English", "English --> 日本語" };
		languageComboBox = new JComboBox<String>(languageModes);
		languageComboBox.setSelectedIndex(0);
		languageComboBox.addActionListener(new LanguageListener());
		controls.add(languageComboBox);
		
		autoCheckBox = new JCheckBox("Auto");
		autoCheckBox.addItemListener(new AutoListener());
		speedLabel = new JLabel("Speed (in secs)");
		speed = new JTextField("1");
		speed.setPreferredSize(new Dimension(50, 30));
		speed.addActionListener(new SpeedListener());
		controls.add(autoCheckBox);
		controls.add(speedLabel);
		controls.add(speed);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(fileTree), card);
		
		// Creates the deck of cards
		deck = new FlashCards();
		card.setText(deck.getCurrent());
		position = FACE;
		kanaOnly = false;
		englishToJapanese = false;
		isShuffled = false;
		timer = new Timer(1000, new TimerListener());
		
		add(splitPane, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}
	
//////////////////////////////////////// METHODS ////////////////////////////////////////
	// Makes the face of the current card display.
	private void turnToFace() {
		position = FACE;
		
		if(englishToJapanese)
			card.setText(deck.getDefinition());
		else if(kanaOnly)
			card.setText(deck.getKana());
		else 
			card.setText(deck.getCurrent());
	}
	
	// Makes the definition of the current card display.
	private void turnToDefinition() {
		position = DEFINITION;
		
		if(englishToJapanese && kanaOnly)
			card.setText(deck.getKana());
		else if(englishToJapanese && !kanaOnly)
			card.setText(deck.getCurrent());
		else
			card.setText(deck.getDefinition());
	}
	
	// Makes the kana spelling of the current card display.
	private void turnToKana() {
		position = KANA;
		card.setText(deck.getKana());
	}
	
	// Updates the amount of time to wait between flash cards when on Auto mode.
	private void updateTimerDelay() {
		try {
		int speedNum = (int) Math.round(Double.parseDouble(speed.getText()) * 1000);
		timer.setDelay(speedNum);
		} catch(Exception e) {
			System.out.println("Could not understand given speed.");
		}
	}
	
	public static void main(String[] args) { gui = new FlashCardGUI(); }
	
	//////////////////////////////////////// LISTENERS ////////////////////////////////////////
	
	// Acts as a listener for the shuffle button to shuffle the deck.
	class ShuffleListener implements ActionListener {
		public void actionPerformed(ActionEvent e) { 
				deck.shuffle();
				deck.reset();
				turnToFace();
				isShuffled = true;
				gui.requestFocusInWindow();
		}
	}
	
	// Acts as a listener for the load button to load the checked definition lists into the deck.
	class LoadListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			deck = new FlashCards();
			
			loadFiles((TreeNode) fileTree.getModel().getRoot());
			
			turnToFace();
			fileTree.clearChecking();
			isShuffled = false;
			gui.requestFocusInWindow();
		}
	
		// Recursively loads the checked definition lists into the deck in order.
		private void loadFiles(TreeNode node) {
			if(node.getChildCount() > 0) { // Keep going if not a leaf
				for(Enumeration<TreeNode> e = node.children(); e.hasMoreElements(); )
					loadFiles(e.nextElement());
			} else { // We know it's a definition list and, if checked, add it to the deck
				FileTreeNode definitionList = (FileTreeNode) node;
				
				if(isChecked(definitionList)) {
					try {
						deck.addFile(definitionList.getFile());
					} catch(FileNotFoundException ex) {
						System.out.println("Could not load: " + definitionList.getFile().getName());
					}
				}
			}
		}
		
		// Returns whether the given FileTreeNode is checked.
		private boolean isChecked(FileTreeNode definitionList) {
			TreePath[] checkedPaths = fileTree.getCheckingPaths();
			
			for(TreePath checkedPath: checkedPaths) {
				if(definitionList.equals(checkedPath.getLastPathComponent()))
						return true;
			}
			
			return false;
		}
	}
	
	// Acts as a listener for the reset button to set the deck back to the beginning.
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			deck.reset();
			turnToFace();
			gui.requestFocusInWindow();
		}
	}
	
	// Acts as a listener for the "Kana Only" setting.
	class KanaListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) { 
			kanaOnly = e.getStateChange() == ItemEvent.SELECTED; 
			
			if(position == FACE)
				turnToFace();
			else if(position == DEFINITION)
				turnToDefinition();
			else {//position == KANA
				if(kanaOnly)
					turnToFace();
				else
					turnToKana();
			}
			
			gui.requestFocusInWindow();
		}
	}
	
	// Acts as a listener for the language setting.
	class LanguageListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String selected = (String) languageComboBox.getSelectedItem();
			englishToJapanese = selected.equals("English --> 日本語");
			
			if(position == FACE)
				turnToFace();
			else if(position == DEFINITION)
				turnToDefinition();
			else {//position == KANA
				if(kanaOnly)
					turnToFace();
				else
					turnToKana();
			}
			
			gui.requestFocusInWindow();
		}
	}
	
	// Acts as a listener for the checkbox of the automatic rotation feature.
	class AutoListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				//updateTimerDelay();
				timer.start();
			} else // e.getStateChange() == ItemEvent.DESELECTED
				timer.stop();
			
			gui.requestFocusInWindow();
		}
	}
	
	// Acts as a listener for the automatic rotation feature, going to the next card after the
	// delay period.
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(deck.hasNext())
				deck.advance();
			else {
				if(isShuffled)
					deck.shuffle();
				
				deck.reset();
			}
			
			turnToFace();
		}
	}
	
	// Acts as a listener for the automatic rotation speed text field.
	class SpeedListener implements ActionListener {
		public void actionPerformed(ActionEvent e) { 
			updateTimerDelay(); 
			gui.requestFocusInWindow();
		}
	}
	
	//////////////////////////////////////// ACTIONS ////////////////////////////////////////
	// An action that advances to the next card in the deck.
	class NextCardAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if(deck.hasNext()) {
				deck.advance();
				turnToFace();
			}
		}
	}
	
	// An action that goes back to the previous card in the deck.
	class PreviousCardAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if(deck.hasPrevious()) {
				deck.goBack();
				turnToFace();
			}
		}
	}
	
	// An action that displays the definition of the current card.
	class DefinitionAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if(!deck.isEmpty() && position != DEFINITION)
				turnToDefinition();
		}
	}
	
	// An action that displays the kana spelling of the current card.
	class KanaAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if(!deck.isEmpty() && position != KANA && !kanaOnly) // Pretends top kana card 
				turnToKana();															   // doesn't exist if kana only
		}
	}
	
	// An action that returns the current card back to its face.
	class ReturnAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) { 
			turnToFace(); 
		}
	}
}
