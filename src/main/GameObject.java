package main;

public interface GameObject {
	
	public final float GRAVITY = 0.5f;
	public final float TERMINAL_VELOCITY = 10;

	// Call as much as possible
	void render(RenderHandler renderer, int xZoom, int yZoom);
	
	// Call 60x per second
	void update(Game game);
	
	// Called whenever mouse is clicked on canvas
	// Return true to stop checking clicks
	public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom);
	
	public int getLayer();
	
	public Rectangle getRectangle();
	
}
