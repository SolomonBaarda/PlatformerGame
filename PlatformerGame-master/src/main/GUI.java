package main;

public class GUI implements GameObject {

	private Sprite backgroundSprite;
	private GUIButton[] buttons;
	private Rectangle rectangle;
	private boolean fixed;

	public GUI(Sprite backgroundSprite, GUIButton[] buttons, int x, int y, boolean fixed) {
		this.backgroundSprite = backgroundSprite;
		this.buttons = buttons;
		this.fixed = fixed;

		if(backgroundSprite != null) {
			rectangle = new Rectangle(x, y, backgroundSprite.getWidth(), backgroundSprite.getHeight());
		}
		else {
			rectangle = new Rectangle();
			rectangle.setX(x);
			rectangle.setY(y);
		}
	}

	public GUI(GUIButton[] buttons, int x, int y, boolean fixed) {
		this(null, buttons, x, y, fixed);
	}


	@Override
	public void render(RenderHandler renderer, int xZoom, int yZoom) {

		if(backgroundSprite != null) 
			renderer.renderSprite(backgroundSprite, rectangle.getX(), rectangle.getY(), xZoom, yZoom, fixed) ;

		if(buttons != null) 
			for(int i = 0; i < buttons.length; i++) 
				buttons[i].render(renderer, xZoom, yZoom, rectangle);



	}

	@Override 
	public void update(Game game) {
		if(buttons != null) 
			for(int i = 0; i < buttons.length; i++) 
				buttons[i].update(game);
	}

	@Override
	public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) {

		boolean stopChecking = false;

		if(!fixed) 
			mouseRectangle = new Rectangle(mouseRectangle.getX() + camera.getX(), mouseRectangle.getY() + camera.getY(), 1, 1);
		else
			mouseRectangle = new Rectangle(mouseRectangle.getX(), mouseRectangle.getY(), 1, 1);

		if(rectangle.getWidth() == 0 || rectangle.getHeight() == 0 || mouseRectangle.intersects(rectangle)) {

			mouseRectangle.setX(mouseRectangle.getX() - rectangle.getX());
			mouseRectangle.setY(mouseRectangle.getY() - rectangle.getY());

			for(int i = 0; i < buttons.length; i++) {
				boolean result = buttons[i].handleMouseClick(mouseRectangle, camera, xZoom, yZoom);
				if(stopChecking == false)
					stopChecking = result;
			}
		}

		return stopChecking;
	}

	@Override
	public int getLayer() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Rectangle getRectangle() {
		return rectangle;
	}



}
