package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Font {
	
	private File fontFile;
	private SpriteSheet fontSheet;
	private HashMap<String, Sprite> letters;
	
	public Font(File fontFile, SpriteSheet fontSheet) {
		this.fontFile = fontFile;
		this.fontSheet = fontSheet;
		letters = new HashMap<>();
		
		try {
			Scanner s = new Scanner(fontFile);
			while(s.hasNextLine()) {
				
				// read each line and create a tile
				String line = s.nextLine();
				
				if(!line.startsWith("//")) {
					String[] splitLine = line.split(",");
					String letter = splitLine[0];
					int spriteX = Integer.parseInt(splitLine[1]);
					int spriteY = Integer.parseInt(splitLine[2]);
					
					Sprite newLetter = fontSheet.getSprite(spriteX, spriteY);
					letters.put(letter, newLetter);
				}
			}
			s.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void renderWord() {
		
	}
	
	public void renderLetter() {
		
	}

}
