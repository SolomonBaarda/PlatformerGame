package main;

public class Bomb implements GameObject {

	private Sprite sprite;
	private Rectangle hitbox;

	private final int bombSize = 8;
	private final int DEFAULT_BOMB_TIMER = 180;
	private int bombTimerTicks;

	private Vector2 velocity;

	public Bomb(Sprite sprite, int xZoom, int yZoom, int xSpawnPos, int ySpawnPos, int bombTimerTicks, float xVel, float yVel) {
		this.sprite = sprite;
		this.bombTimerTicks = bombTimerTicks;

		if(bombTimerTicks <= 0)
			bombTimerTicks = DEFAULT_BOMB_TIMER;

		hitbox = new Rectangle(xSpawnPos, ySpawnPos, bombSize * xZoom, bombSize * yZoom);
		hitbox.generateGraphics(1, 0xFF00FF90);

		velocity = new Vector2(xVel, yVel);
	}


	@Override
	public void render(RenderHandler renderer, int xZoom, int yZoom) {
		renderer.renderSprite(sprite, hitbox.x, hitbox.y, xZoom, yZoom, false);
	}

	@Override
	public void update(Game game) {
		// If timer has not finished 
		if(bombTimerTicks > 0) {
			if(velocity.y < TERMINAL_VELOCITY)
				velocity.y += GRAVITY;


			hitbox.x += velocity.x;
			hitbox.y += velocity.y;

			bombTimerTicks--;
		}
		// If timer ends
		else {
			// Explode
			Map map = game.getMap();
			
			for(int x = 0; x < 3; x++)
				for(int y = 0; y < 3; y++) 
					for(int layer = 0; layer <= map.getNumLayers(); layer++)
						map.removeTile(layer, hitbox.x / map.tileWidth, hitbox.y / map.tileHeight);
				
			// Remove from gameObjects when exploded 
			game.getObjects().remove(this);
		}

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
		return hitbox;
	}

}
