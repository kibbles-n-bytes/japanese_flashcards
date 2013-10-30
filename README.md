Japanese Flash Cards
===================

Allows for Japanese learners in training (though any language is technically fine) to practice with a unique, time-based flashcard program.

Version: 1.0b
Language: Java

This program simulates a deck of Japanese flash cards, allowing for the user to load custom-created decks from a file tree.

For each card, a timer is run on how long it took you to either pass or fail the card. If the card's timer runs past the allocated time threshold, the card is automatically considered a failure. The program uses an average of the times for that card during your current session to figure out where to put the card in the deck so you see harder cards more often. Cards are taken out of the current session after reaching a certain number of views in a row that are considered passes. The time thresholds and amount of views to pass and all similar variables can be changed within the GUI itself (I think. Otherwise, it's pretty easy to find within the source).

Features:

    English to Japanese or Japanese to English
    Kanji and Hiragana or Hiragana only
    Shuffling
    A timer to turn to the next card

The main function for the actual program is in "Vocabulary Trainer/src/VocabularyTrainerGUI.java" ! Decks can be built using the "Deck Builder" application. Decks are stored in the "decks" folder. I should probably make sure the program still functions, since this is now public...

Note: Currently crashes on OS X during loading of cards. 
