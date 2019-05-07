package main;

import java.awt.image.BufferedImage;

public class AnimatedSprite extends Sprite implements GameObject {

	private Sprite[] sprites;
	private int currentSprite, speed, counter;

	private int startSprite, endSprite;

	/**
	 * @param images
	 * @param speed		speed = number of frames sprite is drawn
	 */
	public AnimatedSprite(BufferedImage[] images, int speed) {
		// Call empty constructor
		super();

		this.speed = speed;
		currentSprite = 0;
		counter = 0;
		this.startSprite = 0;
		this.endSprite = images.length - 1;

		sprites = new Sprite[images.length];

		// Aray of sprites
		for(int i = 0; i < sprites.length; i++)
			sprites[i] = new Sprite(images[i]);

	}


	public AnimatedSprite(SpriteSheet sheet, int speed) {

		sprites = sheet.getLoadedSprites();
		this.speed = speed;
		this.startSprite = 0;
		this.endSprite = sprites.length - 1;
	}


	public AnimatedSprite(SpriteSheet sheet, Rectangle[] positions, int speed) {

		sprites = new Sprite[positions.length];
		this.speed = speed;
		this.startSprite = 0;
		this.endSprite = positions.length - 1;

		for(int i = 0; i < sprites.length; i++)
			sprites[i] = new Sprite(sheet, positions[i].getX(), positions[i].getY(), positions[i].getWidth(), positions[i].getHeight());
	}


	@Override
	// Render is dealt with the layer class
	public void render(RenderHandler renderer, int xZoom, int yZoom) { }


	public void reset() {
		counter = 0;
		currentSprite = startSprite;
	} 


	@Override
	public void update(Game game) {
		counter++;
		if(counter >= speed) {
			counter = 0;
			incrementSprite();
		}
	}



	public void setAnimationRange(int startSprite, int endSprite) {
		this.startSprite = startSprite;
		this.endSprite = endSprite;
		reset();
	}


	public void incrementSprite() {
		currentSprite++;
		if(currentSprite >= endSprite) {
			currentSprite = startSprite;
		}
	}

	@Override
	public int getWidth() {
		return sprites[currentSprite].getWidth();
	}

	@Override
	public int getHeight() {
		return sprites[currentSprite].getHeight();
	}

	@Override
	public int[] getPixels() {
		return sprites[currentSprite].getPixels();
	}


	@Override
	public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) {
		return false;
		
	}


	@Override
	public int getLayer() {
		return -1;
	}


	@Override
	public Rectangle getRectangle() {
		return null;
	}

}
