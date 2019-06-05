package main;

public class Hotbar extends GUI {

	Game game;
	GUIButton[] hotbarButtons;
	
	public Hotbar(Game game, GUIButton[] hotbarButtons, int x, int y) {
		super(hotbarButtons, x, y, true);
		this.game = game;
		this.hotbarButtons = hotbarButtons;
		
	}
	
	
	public void render(RenderHandler renderer, int xZoom, int yZoom) {
		
		for(int i = 0; i < hotbarButtons.length; i++)
			hotbarButtons[i].render(renderer, xZoom, yZoom);
	}
	
	
	
	public void setSlot(int index) {
		
	}
	
	
	
}
