package main;

public class HotbarButton extends GUIButton {

	private Game game;
	private Sprite tileSprite;

	public HotbarButton(Game game, Sprite tileSprite, Rectangle rectangle) {
		super(tileSprite, rectangle, true);

		this.game = game;
		this.tileSprite = tileSprite;
		rectangle.generateGraphics(game.xZoom, 0xFFFFFF);
	}



	@Override
	public void render(RenderHandler renderer, int xZoom, int yZoom, Rectangle buttonRectangle) {
		renderer.renderRectangle(rectangle, buttonRectangle, 1, 1, fixed);

		if(sprite != null) {
			renderer.renderSprite(sprite, 
					rectangle.getX() + buttonRectangle.getX() + (xZoom - (xZoom - 1)) * rectangle.getWidth() / 2 / xZoom, 
					rectangle.getY() + buttonRectangle.getY() + (yZoom - (yZoom - 1)) * rectangle.getHeight() / 2 / yZoom, 
					xZoom - 1, 
					yZoom - 1, 
					fixed);	
		}
	}


	@Override
	public void update(Game game) {
		game.setSelectedGUI(2);
	}







	@Override
	public int getLayer() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void activate() {
		game.changeSelectedTile(1);
		System.out.println("Not implemented yet");
	}

	public void setSprite(Sprite sprite) {
		this.tileSprite = sprite;
	}



}
