package main;

import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


public class RenderHandler {
	private BufferedImage view;
	private int[] pixels;
	
	private int maxScreenWidth, maxScreenHeight;

	private Rectangle camera;

	public RenderHandler(int WIDTH, int HEIGHT) {
		GraphicsDevice[] graphicsDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		maxScreenWidth = 0;
		maxScreenHeight = 0;
		
		for(int i = 0; i < graphicsDevices.length; i++) {
			if(maxScreenWidth < graphicsDevices[i].getDisplayMode().getWidth())
				maxScreenWidth = graphicsDevices[i].getDisplayMode().getWidth();
			
			if(maxScreenHeight < graphicsDevices[i].getDisplayMode().getHeight())
				maxScreenHeight = graphicsDevices[i].getDisplayMode().getHeight();
			
		}
		
		//Create a BufferedImage that will represent our view.
		view = new BufferedImage(maxScreenWidth, maxScreenHeight, BufferedImage.TYPE_INT_RGB);

		camera = new Rectangle(0, 0, WIDTH, HEIGHT);

		//Create an array for pixels
		pixels = ((DataBufferInt) view.getRaster().getDataBuffer()).getData();
	}

	public void render(Graphics graphics) {
		graphics.drawImage(view.getSubimage(0, 0, camera.getWidth(), camera.getHeight()), 0, 0, camera.getWidth(), camera.getHeight(), null);
	}


	/**
	 * Add an image to pixel array, which is then rendered by render().
	 * 
	 * @param image
	 * @param xPos
	 * @param yPos
	 * @param xZoom
	 * @param yZoom
	 */
	public void renderImage(BufferedImage image, int xPos, int yPos, int xZoom, int yZoom, boolean fixed) {
		int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		renderArray(imagePixels, image.getWidth(), image.getHeight(), xPos, yPos, xZoom, yZoom, fixed);
	}



	/**
	 * @param sprite
	 * @param xPos
	 * @param yPos
	 * @param xZoom
	 * @param yZoom
	 * @param fixed
	 */
	public void renderSprite(Sprite sprite, int xPos, int yPos, int xZoom, int yZoom, boolean fixed) {
		renderArray(sprite.getPixels(), sprite.getWidth(), sprite.getHeight(), xPos, yPos, xZoom, yZoom, fixed);
	}

	/**
	 * @param sprite
	 * @param xPos
	 * @param yPos
	 * @param xZoom
	 * @param yZoom
	 * @param fixed
	 * @param xOffset
	 * @param yOffset
	 * @param width
	 * @param height
	 */
	public void renderSprite(Sprite sprite, int xPos, int yPos, int xZoom, int yZoom, 
			boolean fixed, int xOffset, int yOffset, int width, int height) {

		renderArray(sprite.getPixels(), sprite.getWidth(), sprite.getHeight(), xPos, yPos, xZoom, yZoom, 
				xOffset, yOffset, width, height, fixed);
	}




	public void renderRectangle(Rectangle rectangle, int xZoom, int yZoom, boolean fixed) {
		int[] rectanglePixels = rectangle.getPixels();
		if(rectanglePixels != null) {
			renderArray(rectanglePixels, rectangle.getWidth(), rectangle.getHeight(), rectangle.getX(), rectangle.getY(), xZoom, yZoom, fixed);
		}
	}


	public void renderRectangle(Rectangle rectangle, Rectangle offset, int xZoom, int yZoom, boolean fixed) {
		int[] rectanglePixels = rectangle.getPixels();
		if(rectanglePixels != null) {
			renderArray(rectanglePixels, rectangle.getWidth(), rectangle.getHeight(), 
					rectangle.getX() + offset.getX(), rectangle.getY() + offset.getY(), xZoom, yZoom, fixed);
		}
	}



	/**
	 * Method that renders a whole image.
	 * 
	 * @param renderPixels		Entire pixels of image
	 * @param imageWidth		Width of image
	 * @param imageHeight		Height of image
	 * @param xPos				x position of the render on screen
	 * @param yPos				y position of the render on screen
	 * @param xZoom				Horizontal zoom
	 * @param yZoom				Vertical zoom
	 * @param fixed				If the render should offset relative to camera
	 */
	public void renderArray(int[] renderPixels, int renderWidth, int renderHeight, int xPos, int yPos, 
			int xZoom, int yZoom, boolean fixed) {

		renderArray(renderPixels, renderWidth, renderHeight, xPos, yPos, xZoom, yZoom, 0, 0, renderWidth, renderHeight, fixed);
	}


	/**
	 * Method that renders a part of an image.
	 * 
	 * @param renderPixels		Entire pixels of image
	 * @param imageWidth		Width of image
	 * @param imageHeight		Height of image
	 * @param xPos				x position of the render on screen
	 * @param yPos				y position of the render on screen
	 * @param xZoom				Horizontal zoom
	 * @param yZoom				Vertical zoom
	 * @param xOffset			Horizontal offset into the image
	 * @param yOffset			Vertical offset into the image
	 * @param renderWidth		Width of section to render
	 * @param renderHeight		Height of the section to render
	 * @param fixed				If the render should offset relative to camera
	 */
	public void renderArray(int[] renderPixels, int imageWidth, int imageHeight, int xPos, int yPos, 
			int xZoom, int yZoom, int xOffset, int yOffset, int renderWidth, int renderHeight, boolean fixed) {

		for(int y = yOffset; y < yOffset + renderHeight; y++)
			for(int x = xOffset; x < xOffset + renderWidth; x++) 
				for(int yZoomPos = 0; yZoomPos < yZoom; yZoomPos++) 
					for(int xZoomPos = 0; xZoomPos < xZoom; xZoomPos++) {

						setPixel( renderPixels[x + y * imageWidth], 
								((x - xOffset) * xZoom) + xPos + xZoomPos, 
								((y - yOffset) * yZoom) + yPos + yZoomPos, 
								fixed);
					}
	}


	private void setPixel(int pixel, int x, int y, boolean fixed) {

		int pixelIndex = 0;

		if(!fixed) {
			if(x >= camera.getX() && x <= camera.getX() + camera.getWidth() && y >= camera.getY() && y <= camera.getY() + camera.getHeight() ) {

				pixelIndex = (x - camera.getX()) + (y - camera.getY()) * view.getWidth();
			}
		}
		else {
			if(x >= 0 && x <= camera.getWidth() && y >= 0 && y <= camera.getHeight() ) {

				pixelIndex = x + y * view.getWidth();
			}
		}

		if(pixels.length > pixelIndex && pixel != Game.alpha) 
			pixels[pixelIndex] = pixel;
	}



	public void clear() {
		for(int i = 0; i < pixels.length; i++) {
			pixels[i] = 0;
		}
	}



	
	
	public int getMaxWidth() {
		return maxScreenWidth;
	}

	public int getMaxHeight() {
		return maxScreenHeight;
	}

	public Rectangle getCamera() {
		return camera;
	}

	public void setCamera(Rectangle camera) {
		this.camera = camera;
	}





}