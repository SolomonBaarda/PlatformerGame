package main;

public class LayerButton extends GUIButton {
	
	private Game game;
	private int layer;
	private boolean isSelected = false;;
	
	public LayerButton(Game game, int layer, Rectangle rectangle) {
		super(null ,rectangle, true);
		
		this.game = game;
		this.layer = layer;
		
		rectangle.generateGraphics(0xFFFFDB3D);
	}

	@Override
	public void render(RenderHandler renderer, int xZoom, int yZoom, Rectangle buttonRectangle) {
		renderer.renderRectangle(rectangle, buttonRectangle, 1, 1, fixed);

		
	}


	@Override
	public void update(Game game) {
		if(layer == game.getSelectedLayer()) {
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
	public int getLayer() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void activate() {
		game.setSelectedLayer(layer);
	}

}
