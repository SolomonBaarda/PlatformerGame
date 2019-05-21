package main;

public abstract class GUIButton implements GameObject {

	protected Sprite sprite;
	protected Rectangle rectangle;
	protected boolean fixed;
	
	public GUIButton(Sprite sprite, Rectangle rectangle, boolean fixed) {
		this.sprite = sprite;
		this.rectangle = rectangle;
		this.fixed = fixed;
	}
	
	@Override
	public void render(RenderHandler renderer, int xZoom, int yZoom) { }
	
	
	
	public void render(RenderHandler renderer, int xZoom, int yZoom, Rectangle buttonRectangle) {
		if(sprite != null) {
			renderer.renderSprite(sprite, rectangle.getX() + buttonRectangle.getX(), 
					rectangle.getY() + buttonRectangle.getY(), xZoom, yZoom, fixed);
		}
	}

	@Override
	public void update(Game game) { }

	@Override
	public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) {
				
		if(mouseRectangle.intersects(rectangle)) {
			activate();
			return true;
		}
		return false;
	}
	
	
	public abstract void activate();

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	
	
	
	
}
