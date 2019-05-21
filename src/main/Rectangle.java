package main;

public class Rectangle {
	public int x, y, width, height;
	private int[] pixels;

	/**
	 * Create Rectangle 0, 0, 0, 0
	 */
	public Rectangle() {
		this(0, 0, 0, 0);
	}

	/**
	 * Create new Rectangle Object;
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void generateGraphics(int colour) {
		pixels = new int[width * height];

		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x ++) 
				pixels[x + y * width] = colour;

	}


	public void generateGraphics(int borderWidth, int colour) {
		pixels = new int[width * height];

		for(int i = 0; i < pixels.length; i++) {
			pixels[i] = Game.alpha;
		}
		
		for(int y = 0; y < borderWidth; y++)
			for(int x = 0; x < width; x++) 
				pixels[x + y * width] = colour;
		
		for(int y = 0; y < height; y++)
			for(int x = 0; x < borderWidth; x++) 
				pixels[x + y * width] = colour;
		
		for(int y = 0; y < height; y++)
			for(int x = width - borderWidth; x < width; x++) 
				pixels[x + y * width] = colour;
			
		for(int y = height - borderWidth; y < height; y++)
			for(int x = 0; x < width; x++) 
				pixels[x + y * width] = colour;
		
		
	}



	public int[] getPixels() {
		if(pixels != null)
			return pixels;
		else {
			System.out.println("Generate graphics first!");
			return null;
		}
	}
	
	
	public boolean intersects(Rectangle otherRectangle) {
		if(x >= otherRectangle.x + otherRectangle.getWidth() || otherRectangle.getX() >= x + width) 
			return false;
		
		if(y >= otherRectangle.getY() + otherRectangle.getHeight() || otherRectangle.getY() >= y + height)
			return false;
					
		return true;
	}


	public String toString() {
		return "[" +x+ ", " +y+ ", " +width+ ", " +height+ "]";
	}
	


	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}



}
