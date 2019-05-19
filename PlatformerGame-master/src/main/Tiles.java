package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Tiles {

	private SpriteSheet spriteSheet;
	private ArrayList<Tile> tilesList;

	/**
	 * Method that loads all tiles from file and spriteSheet.
	 * Assumes spriteSheet has already been loaded.
	 * 
	 * @param tilesFile
	 * @param spriteSheet
	 */
	public Tiles(File tilesFile, SpriteSheet spriteSheet) {

		this.spriteSheet = spriteSheet;
		tilesList = new ArrayList<>();

		try {
			Scanner s = new Scanner(tilesFile);
			while(s.hasNextLine()) {

				// read each line and create a tile
				String line = s.nextLine();

				if(!line.startsWith("//")) {
					String[] splitLine = line.split("-");
					String tileName = splitLine[0];
					int spriteX = Integer.parseInt(splitLine[1].trim());
					int spriteY = Integer.parseInt(splitLine[2].trim());

					Tile tile;
					if(splitLine.length >= 4) {
						tile = new Tile(tileName, spriteSheet.getSprite(spriteX, spriteY), Integer.parseInt(splitLine[3].trim()));
						//System.out.println(tileName + ", " + Integer.parseInt(splitLine[3].trim()));
					}
					else {
						tile = new Tile(tileName, spriteSheet.getSprite(spriteX, spriteY));
					}

					tilesList.add(tile);
				}
			}
			s.close();

			System.out.println("Tile list loaded succesfully!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}



	public void renderTiles(int tileID, RenderHandler renderer, int rotation, int xPos, int yPos, int xZoom, int yZoom) {

		if(tileID >= 0 && tilesList.size() > tileID) {

			renderer.renderSprite(tilesList.get(tileID).getSprite(), xPos, yPos, xZoom, yZoom, false);
		}
		else {
			System.out.println("Tile ID " +tileID+ " is out of bounds. (max = " +tilesList.size()+ ")");
		}

	}



	

	public int size() {
		return tilesList.size();
	}


	public Sprite[] getSprites() {
		Sprite[] sprites = new Sprite[size()];

		for(int i = 0; i < sprites.length; i++) 
			sprites[i] = tilesList.get(i).getSprite();

		return sprites;
	}

	
	public int collisionType(int tileID) {
		if(tileID >= 0 && tilesList.size() > tileID) {

			return tilesList.get(tileID).collisionType;
		}
		else {
			return -1;
		}
	}

	public class Tile {

		private String tileName;
		private Sprite sprite;
		private int collisionType;
		private boolean collision;

		public Tile(String tileName, Sprite sprite) {
			this(tileName, sprite, 0);
		}

		public Tile(String tileName, Sprite sprite, int collisionType) {
			this.tileName = tileName;
			this.sprite = sprite;
			this.collisionType = collisionType;
			if(collisionType == 0)
				collision = false;
			else
				collision = true;
		}

		
		
		public String getTileName() {
			return tileName;
		}

		public Sprite getSprite() {
			return sprite;
		}



	}


}
