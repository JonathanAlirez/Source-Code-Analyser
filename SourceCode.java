import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class SourceCode {
	// SourceFile = original source code | Left | RevisedFile = new source code
	// | Right
	File sourceFile, revisedFile;
	// Where source code is located
	JTextArea textLeft, textRight;
	// Both booleans show if that text area has been written in
	boolean leftFileSelected = false;
	boolean rightFileSelected = false;
	// To help control CTR-O to know which side to open document on, left or
	// right
	boolean leftFileFocus = false;
	boolean rightFileFocus = false;
	// Highlight code that is different
	Highlighter.HighlightPainter cyanPainter, greenPainter;
	// how many chars have been looked through, help the highlighter know where
	// to highlight
	int rightPos, leftPos;
	// Put here to allow for file to be appended onto end.
	JLabel labelLeft, labelRight;

	public void begin() {
		JFrame frame = new JFrame("Source Code Analyser");
		JMenuBar menuBar = new JMenuBar();
		// Defining our highlighters
		cyanPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);
		greenPainter = new DefaultHighlighter.DefaultHighlightPainter(
				Color.green);

		// Build the first menu. Menu can be open with ALT-S
		JMenu submenu = new JMenu("File");
		submenu.setMnemonic(KeyEvent.VK_S);

		// Open command CTR-O does know which side you have chosen.
		JMenuItem menuItem = new JMenuItem("Open");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					// If left textarea has focus open it there and set file
					// true
					if (leftFileFocus == true) {
						sourceFile = chooser.getSelectedFile();
						CodeAnalyseTools.setTextAreaFile(sourceFile, textLeft);
						leftFileSelected = true;
					} else {
						revisedFile = chooser.getSelectedFile();
						CodeAnalyseTools
								.setTextAreaFile(revisedFile, textRight);
						rightFileSelected = true;
					}
					if (rightFileSelected == true && leftFileSelected == true)
						highlightCode();
				}
			}
		});
		submenu.add(menuItem);

		// Basic save, also re-highlights on a save for editing.
		menuItem = new JMenuItem("Save");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (leftFileFocus == true) {
					CodeAnalyseTools.saveFile(sourceFile, textLeft);
					highlightCode();
				} else {
					CodeAnalyseTools.saveFile(revisedFile, textRight);
					highlightCode();
				}
			}
		});

		submenu.add(menuItem);

		// This re-highlights both files, also saves before.
		menuItem = new JMenuItem("Re-Highlight");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (leftFileSelected == true && rightFileSelected == true)
					highlightCode();
			}
		});

		submenu.add(menuItem);
		menuBar.add(submenu);

		frame.setJMenuBar(menuBar);
		// Two panels to put on top of one another
		JPanel panelLabels = new JPanel();
		JPanel panelText = new JPanel();

		panelLabels.setLayout(new BorderLayout());
		panelText.setLayout(new GridLayout());

		labelLeft = new JLabel("Original Source Code: ");
		labelRight = new JLabel("Revised Source Code: ");

		panelLabels.add(labelLeft, "West");
		panelLabels.add(labelRight, "East");

		// Two textareas for code.
		textLeft = new JTextArea();
		textRight = new JTextArea();

		// allow textareas to scroll with a big file.
		JScrollPane spLeft = new JScrollPane(textLeft);
		JScrollPane spRight = new JScrollPane(textRight);

		textLeft.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Leaves the last known focus to be kept.
				leftFileFocus = true;
				rightFileFocus = false;
			}
		});

		textRight.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// When a command like CTR-O is given, this runs.
				// Can't put rightFileFocus = false, or it will never get to
				// rightFileFocus
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Leaves the last know focus to be kept
				rightFileFocus = true;
				leftFileFocus = false;
			}
		});
		// Adding in all the GUI Objects
		panelText.add(spLeft);
		panelText.add(spRight);
		frame.getContentPane().add(panelLabels, "North");
		frame.getContentPane().add(panelText, "Center");
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	// Highlights code by saving file and then highlighting all code that
	// differs
	protected void highlightCode() {
		// SAVE both files first.
		CodeAnalyseTools.saveFile(sourceFile, textLeft);
		CodeAnalyseTools.saveFile(revisedFile, textRight);
		// Arrays for every line in the file
		ArrayList<String> fileLeft = new ArrayList<String>();
		ArrayList<String> fileRight = new ArrayList<String>();
		// remove any old highlights
		textLeft.getHighlighter().removeAllHighlights();
		textRight.getHighlighter().removeAllHighlights();
		// set these to 0 for re-highlights
		leftPos = 0;
		rightPos = 0;

		try {
			// Putting every line into the two arrays. Length()
			BufferedReader br = new BufferedReader(new FileReader(sourceFile));
			String temp;
			while ((temp = br.readLine()) != null) {
				fileLeft.add(temp);

			}
			br.close();
			BufferedReader br2 = new BufferedReader(new FileReader(revisedFile));
			while ((temp = br2.readLine()) != null) {
				fileRight.add(temp);
			}
			br2.close();
			// If original source code has less lines than new source code.
			if (fileLeft.size() > fileRight.size()) {
				// loop through the lesser code.
				for (int i = 0; i < fileLeft.size(); i++) {
					// Allows for length for highlighting
					rightPos += fileRight.get(i).length() + 1;
					leftPos += fileLeft.get(i).length() + 1;
					// if the two lines aren't equal, run code
					if (!fileLeft.get(i).equals(fileRight.get(i))) {
						// choose between right or left highlighting.
						if (fileLeft.get(i).length() < fileRight.get(i)
								.length())
							// rightPos - fileRight | end location - this line -
							// 1 (\n)
							// allows for begining of line, rightPos = end of
							// line
							textRight.getHighlighter().addHighlight(
									rightPos - fileRight.get(i).length() - 1,
									rightPos, greenPainter);
						else
							textLeft.getHighlighter().addHighlight(
									leftPos - fileLeft.get(i).length() - 1,
									leftPos, greenPainter);
					}
				}
				// source codes have equal lines.
			} else {
				// Exnplained above.
				for (int i = 0; i < fileRight.size(); i++) {
					rightPos += fileRight.get(i).length() + 1;
					leftPos += fileLeft.get(i).length() + 1;
					if (!fileLeft.get(i).equals(fileRight.get(i))) {
						if (fileLeft.get(i).length() < fileRight.get(i)
								.length())
							textRight.getHighlighter().addHighlight(
									rightPos - fileRight.get(i).length() - 1,
									rightPos, cyanPainter);
						else
							textLeft.getHighlighter().addHighlight(
									leftPos - fileLeft.get(i).length() - 1,
									leftPos, greenPainter);
					}
				}
			}
			// One day have error msg open up.
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (BadLocationException e) {
		}
	}
}