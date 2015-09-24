import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTextArea;

public class CodeAnalyseTools {
	
	// Simple save a file function.
	public static void saveFile(File file, JTextArea text) {
		try {
			// BufferedWriter for faster writing powers.
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(text.getText().toString());
			bw.close();
		} catch (IOException e) {
		}
	}

	// This populates the textarea with the source code read in from selected file
	public static void setTextAreaFile(File revisedFile, JTextArea textField) {
		String fileString = "";
		try {
			// BufferedReader for faster reading
			BufferedReader br = new BufferedReader(new FileReader(revisedFile));
			String temp;
			while ((temp = br.readLine()) != null) {
				// \n is why we have to -1 in SourceCode.
				fileString += temp + "\n";
			}
			br.close();
			textField.setText(fileString);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
}
