package main;

public class SDKButton extends GUIButton {

	private Game game;
	private int tileID;
	boolean isSelected = false;

	public SDKButton(Game game, int tileID, Sprite tileSprite, Rectangle rectangle) {
		super(tileSprite, rectangle, true);
		this.game = game;
		this.tileID = tileID;
		rectangle.generateGraphics(0xFFFFDB3D);
	}


	@Override
	public void render(RenderHandler renderer, int xZoom, int yZoom, Rectangle buttonRectangle) {
		renderer.renderRectangle(rectangle, buttonRectangle, 1, 1, fixed);

		renderer.renderSprite(sprite, 
				rectangle.getX() + buttonRectangle.getX() + (xZoom - (xZoom - 1)) * rectangle.getWidth() / 2 / xZoom, 
				rectangle.getY() + buttonRectangle.getY() + (yZoom - (yZoom - 1)) * rectangle.getHeight() / 2 / yZoom, 
				xZoom - 1, 
				yZoom - 1, 
				fixed);	
	}


	@Override
	public void update(Game game) {
		if(tileID == game.getSelectedTile()) {
			if(!isSelected) {
				rectangle.generateGraphics(0);
				isSelected = true;
			}

		}
		else {
			if(isSelected) {
				rectangle.generateGraphics(0xFFFFDB3D);
				isSelected = false;
			}
		}

	}


	@Override
	public void activate() {
		game.changeSelectedTile(tileID);
	}


	@Override
	public int getLayer() {
		return Integer.MAX_VALUE;
	}

}
