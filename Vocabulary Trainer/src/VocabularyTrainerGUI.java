import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class VocabularyTrainerGUI extends JFrame {
	private static VocabularyTrainerGUI gui;
	private CheckboxTree fileTree;
	private JPanel cardPanel;
	private JTextField streak;
	private JTextField time;
	private JComboBox displayTypeBox;
	private JButton start;
	private FlashCards deck;
	private Stopwatch stopwatch;
	private Stopwatch nextCardStopwatch;
	private boolean isLocked;
	
	public VocabularyTrainerGUI() {
		super("Japanese Vocabulary Trainer");
		
		//////////Builds Tree of Files //////////		
		fileTree = new CheckboxTree(createFileTree(new File("../Decks")));
		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fileTree.setCellRenderer(new DefaultCheckboxTreeCellRenderer());
		fileTree.setPreferredSize(new Dimension(250, 300));
		fileTree.expandRow(0);
		
		//////////Creates display panel for card on the right//////////
		cardPanel = new JPanel();
		cardPanel.setLayout(new GridBagLayout());
		cardPanel.setPreferredSize(new Dimension(400, 300));
		
		// Creates the control panel on the bottom and adds the buttons
		JPanel controls = new JPanel();
		
		start = new JButton("Start");
		start.addActionListener(new StartListener());
		controls.add(start);
		
		JLabel streakLabel = new JLabel("Streak Amount");
		streak = new JTextField("5");
		streak.setPreferredSize(new Dimension(50, 30));
		//streak.addActionListener(new StreakListener());
		controls.add(streakLabel);
		controls.add(streak);
		
		JLabel timeLabel = new JLabel("Time Threshold (in secs)");
		time = new JTextField("3");
		time.setPreferredSize(new Dimension(50, 30));
		//time.addActionListener(new TimeListener());
		controls.add(timeLabel);
		controls.add(time);
		
		JLabel displayLabel = new JLabel("Display Language");
		String[] displayTypes = {"Kanji + Furigana", "Kanji", "Kana", "English"};
		displayTypeBox = new JComboBox(displayTypes);
		displayTypeBox.setSelectedIndex(0);
		controls.add(displayLabel);
		controls.add(displayTypeBox);
		
		// Sets actions for moving between cards
		PassCardAction passCard = new PassCardAction();
		DefinitionAction definition = new DefinitionAction();
		FailCardAction failCard = new FailCardAction();
		
		
		cardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("RIGHT"), "passCard");
		cardPanel.getActionMap().put("passCard", passCard);
		cardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("DOWN"), "getDefinition");
		cardPanel.getActionMap().put("getDefinition", definition);
		cardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke("LEFT"), "failCard");
		cardPanel.getActionMap().put("failCard", failCard);
		
		// Add components to frame
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(fileTree), cardPanel);
		
		// Initializes the Vocabulary Trainer
		deck = new FlashCards(getCurrentDeckType());
		stopwatch = new Stopwatch();
		nextCardStopwatch = new Stopwatch();
		isLocked = false;
		
		add(splitPane, BorderLayout.CENTER);
		add(controls, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}
	
	public static void main(String[] args) {
		gui = new VocabularyTrainerGUI();
	}
	//////////////////////////////////////// LISTENERS ////////////////////////////////////////
	// Acts as a listener for the load button to load the checked definition lists into the deck.
		class StartListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				deck = new FlashCards(getCurrentDeckType());
				loadFiles((TreeNode) fileTree.getModel().getRoot());
				fileTree.clearChecking();
				
				deck.shuffle();
				startCard();
				nextCardStopwatch.start();
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
							deck.addTerm(definitionList.getFile());
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
	
	
	//////////////////////////////////////// ACTIONS ////////////////////////////////////////
	// An action for when a card is passed.
	class PassCardAction extends AbstractAction {
		// Stop the stopwatch if it needs to be stopped, save the time, and go to the next card.
		public void actionPerformed(ActionEvent e) {
			if(nextCardStopwatch.time() > 200) { // .2 seconds has passed since last card
				nextCardStopwatch.start();
				if(stopwatch.isRunning())
					stopwatch.stop();
				
				if(!deck.isEmpty())
					deck.refresh(stopwatch.time());
				
				startCard();
			}
		}
	}
	
	// Action for when a card's definition is requested.
	class DefinitionAction extends AbstractAction {
		// Stop the stopwatch and go to the definition.
		public void actionPerformed(ActionEvent e) {
			if(stopwatch.isRunning()) {
				stopwatch.stop();
				replaceCard(deck.getTopCardDefinitionPanel());
			}
		}
	}
	
	// Action for when a card is failed.
	class FailCardAction extends AbstractAction {
		// Stop the stopwatch if it needs to be stopped, save a fail time, and go to the next card.
		public void actionPerformed(ActionEvent e) {
			if(nextCardStopwatch.time() > 200) { // .2 seconds has passed since last card
				nextCardStopwatch.start();
				isLocked = true;
				if(stopwatch.isRunning())
					stopwatch.stop();
				
				if(!deck.isEmpty())
					deck.refresh(-1);
				
				startCard();
				isLocked = false;
			}
		}
	}
	
	// Displays the card on top of the deck and starts the stopwatch if there are cards left,
	// or displays a message saying there are no cards left.
	public void startCard() {
		if(!deck.isEmpty()) {
			replaceCard(deck.getTopCardPanel());
			stopwatch.start();
		} else {
			JPanel noCardPanel = new JPanel();
			noCardPanel.add(new JLabel("No cards left in deck."));
			replaceCard(noCardPanel);
		}
			
		gui.requestFocusInWindow();
	}

	// Replaces the information in the card panel with the given panel.
	public void replaceCard(JPanel panel) {
		cardPanel.removeAll();
		cardPanel.add(panel);
		cardPanel.revalidate();
		cardPanel.repaint();
	}

	// Creates the deck file tree, returning the root.
	public DefaultMutableTreeNode createFileTree(File directoryPath) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(directoryPath.getName());
		
		for(File deckFile: directoryPath.listFiles())
			createFileTree(root, deckFile);
		
		return root;
	}
	
	// Recursively builds the file tree through node-file pairs, creating folders and decks when
	// appropriate.
	private void createFileTree(DefaultMutableTreeNode currentNode, File currentFile) {
		if(currentFile.isFile()) { // Is a deck
			FileTreeNode deck = new FileTreeNode(currentFile.getName(), currentFile);
			currentNode.add(deck);
		} else { // Is a folder
			DefaultMutableTreeNode header = new DefaultMutableTreeNode(currentFile.getName());
			currentNode.add(header);
			for(File deckFile: currentFile.listFiles()) // Recursively add all children to file tree
				createFileTree(header, deckFile);
		}
	}
	
	// Gets the currently selected deck type.
	public DeckType getCurrentDeckType() {
		switch(displayTypeBox.getSelectedIndex()) {
		case 1:
			return DeckType.KANJI;
		case 2:
			return DeckType.KANA;
		case 3:
			return DeckType.ENGLISH;
		default:
			return DeckType.KANJI_AND_KANA;
		}
	}
}
