package main;

import java.awt.image.BufferedImage;

public class SpriteSheet {

	private int[] pixels;
	private Sprite[] loadedSprites;
	private BufferedImage image;
	public final int WIDTH;
	public final int HEIGHT;
	private boolean spritesLoaded = false;

	private int spriteSizeX;

	public SpriteSheet(BufferedImage sheetImage) {

		image = sheetImage;
		WIDTH = sheetImage.getWidth();
		HEIGHT = sheetImage.getHeight();

		pixels = new int[WIDTH * HEIGHT];
		pixels = sheetImage.getRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
	}

	
	public void loadSprites(int spriteSizeX, int spriteSizeY) {
		loadSprites(spriteSizeX, spriteSizeY, 0);
	}
	
	
	public void loadSprites(int spriteSizeX, int spriteSizeY, int gap) {
		loadedSprites = new Sprite[ (WIDTH / spriteSizeX) * (HEIGHT / spriteSizeY)];

		this.spriteSizeX = spriteSizeX;
		int spriteNo = 0;
		for(int y = 0 + gap; y < HEIGHT; y += spriteSizeY + gap) {
			for(int x = 0 + gap; x < WIDTH; x += spriteSizeX + gap) {

				loadedSprites[spriteNo] = new Sprite(this, x, y, spriteSizeX, spriteSizeY);
				spriteNo++;
			}
		}
		spritesLoaded = true;
	}


	
	
	public Sprite getSprite(int x, int y) {

		if(spritesLoaded) {
			int spriteID = x + y * (WIDTH / spriteSizeX);

			if(spriteID < loadedSprites.length)
				return loadedSprites[spriteID];
			else 
				System.out.println("Sprite ID out of bounds. (max = " +loadedSprites.length+ ")");
		}
		else 
			System.out.println("Load sprites first!");

		return null;
	}


	public int[] getPixels() {
		return pixels;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Sprite[] getLoadedSprites() {
		return loadedSprites;
	}


}
