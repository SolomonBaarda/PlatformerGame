package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Player implements GameObject {

	private Rectangle hitbox;
	private Sprite sprite;
	private AnimatedSprite animatedSprite = null;

	private int layer = 1;

	private File playerFile;

	private String name;

	private int maxHealth = 100;
	private int health;

	// 0 = right, 1 = left, 2 = up, 3 = down
	private int direction;

	// Platformer movement
	private int speed = 7;
	private int jumpSpeed = 20;
	private Vector2 velocity = new Vector2();
	private final float GRAVITY = 0.5f;
	private final float TERMINAL_VELOCITY = 10;

	public Player(Sprite sprite, File playerFile, int xZoom, int yZoom) {

		this.sprite = sprite;
		this.playerFile = playerFile;
		if(sprite instanceof AnimatedSprite) {
			animatedSprite = (AnimatedSprite) sprite;
		}

		updateDirection();

		int x = 0;
		int y = 0;

		// If player file does'nt exist - create new player
		if(!playerFile.exists()) {
			try {
				// Create file
				playerFile.createNewFile();
				Scanner s = new Scanner(System.in);
				// Save player name
				System.out.print("Enter player name: ");
				name = s.next();

				// Print player name to file
				PrintWriter pr = new PrintWriter(playerFile);
				pr.println(name);
				s.close();

				health = maxHealth;
				pr.println(health);

				pr.close();
				System.out.println("Player name saved!");

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to create player name.");
			}
		}
		// Player does exist - load player
		else {
			try {
				// Load player name from file
				Scanner s = new Scanner(playerFile);
				name = s.next();

				health = s.nextInt();
				
				s.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("Failed to load player name.");
			}
			
			hitbox = new Rectangle(x, y - yZoom*sprite.getHeight(), sprite.getWidth(), sprite.getHeight());
			hitbox.generateGraphics(1, 0xFF00FF90);

		}

		// Print out player welcome message
		String welcomeMessage = " Welcome " +name+ "! ";
		System.out.println();
		for(int i = 0; i < welcomeMessage.length(); i++)
			System.out.print("*");
		System.out.println();
		System.out.println(welcomeMessage);
		for(int i = 0; i < welcomeMessage.length(); i++)
			System.out.print("*");
		System.out.println();
	}




	/**
	 * Method that updates the sprite's direction
	 */
	private void updateDirection() {
		if(animatedSprite != null) {
			animatedSprite.setAnimationRange(direction * 8, (direction * 8) + 7 );
		}
	}


	@Override
	public void render(RenderHandler renderer, int xZoom, int yZoom) {
		if(animatedSprite != null) {
			renderer.renderSprite(animatedSprite, hitbox.getX(), hitbox.getY(), xZoom, yZoom, false);
		}
		else if(sprite != null) {
			renderer.renderSprite(sprite, hitbox.getX(), hitbox.getY(), xZoom, yZoom, false);
		}
		else {
			renderer.renderRectangle(hitbox, xZoom, yZoom, false);
		}


		// render hitbox for testing 
		// renderer.renderRectangle(hitbox, xZoom, yZoom, false);
	}




	@Override
	public void update(Game game) {
		KeyboardListener keyListener = game.getKeyListener();

		boolean didMove = false;
		boolean onGround = false;
		int newDirection = direction;

		Map map = game.getMap();

		// yMin = y value of surface below player
		int yMin = map.findYMin(layer, hitbox.x, hitbox.y, hitbox.width, hitbox.height);

		ArrayList<Rectangle> collidedTiles = map.getCollision(new Rectangle(hitbox.x, hitbox.y, hitbox.width * game.xZoom, hitbox.height * game.yZoom), layer);

		if(collidedTiles != null) {
			for(int i = 0; i < collidedTiles.size(); i++) {
				// Each tile hitbox rectangle 
				Rectangle tile = collidedTiles.get(i);

				// Block is below
				if(hitbox.y + game.yZoom * hitbox.height > tile.y && hitbox.y + game.yZoom * hitbox.height < tile.y + tile.height/2) {
					yMin = tile.y - (game.yZoom * hitbox.height);
					hitbox.y = yMin;
					
					onGround = true;
				}

				// Block is above
				else if(hitbox.y < tile.y + tile.height && hitbox.y > tile.y + tile.height/2) {
					hitbox.y = tile.y + tile.height;

					// If jumping and collide with ceiling 
					if(velocity.y < 0) {
						velocity.y = 0;
						onGround = false;
					}
				}

				// Block is left
				else if(hitbox.x < tile.x + tile.width && hitbox.x > tile.x + 3*tile.width/4) {
					hitbox.x = tile.x + tile.width;
				}

				// Block is right
				else if(hitbox.x + (hitbox.width * game.xZoom) > tile.x && hitbox.x + (hitbox.width * game.xZoom) < tile.x + tile.width/4) {
					hitbox.x = tile.x - (hitbox.width * game.xZoom);
				}

				
			}
		}

		// If on the ground, or on a block
		if(hitbox.y == yMin) {
			onGround = true;
		}
		// If in the air
		else {
			onGround = false;
			
			if(velocity.y < TERMINAL_VELOCITY)
				velocity.y += GRAVITY;
		}

		if(onGround) {
			velocity.y = 0;
			velocity.x = 0;
		}
		

		// Read key inputs
		// Face backwards
		if(keyListener.up()) {
			newDirection = 2;
		}
		// Face forward
		if(keyListener.down()) {
			newDirection = 3;
		}

		// Jump
		if(keyListener.space()) {
			didMove = true;

			// Only allow jump if on the ground
			if(onGround)
				velocity.y = -jumpSpeed;
		}

		// Move left
		if(keyListener.left()) {
			didMove = true;
			newDirection = 1;

			velocity.x = -speed;
		}

		// Move right
		if(keyListener.right()) {
			didMove = true;
			newDirection = 0;

			velocity.x = speed;
		}

		// Stand still
		if(keyListener.leftAndRight()) {
			newDirection = 3;
			didMove = false;
			velocity.x = 0;
		}


		// set possible new values
		int newX = (int) (hitbox.x + velocity.x);
		int newY = (int) (hitbox.y + velocity.y);

		// set map bounds
		if(newX < 0)
			newX = 0;
		else if(newX > map.getWidth() - hitbox.getWidth() * game.xZoom)
			newX = map.getWidth() - hitbox.getWidth() * game.xZoom;

		if(newY < 0)
			newY = 0;
		else if(newY > map.getHeight() - hitbox.getHeight() * game.yZoom)
			newY = map.getHeight() - hitbox.getHeight() * game.yZoom;



		// Set new position
		hitbox.x = newX;
		hitbox.y = newY;



		// update sprites
		if(didMove) 
			animatedSprite.update(game);
		else 
			animatedSprite.reset();

		// Update direction sprite is facing
		if(newDirection != direction) {
			direction = newDirection;
			updateDirection();
		}

		// Always update the camera
		updateCamera(map, game.getRenderer().getCamera());
	}



	/**
	 * Method that updates the cameras current position to 
	 * keep the player in the centre of the screen.
	 * 
	 * @param map
	 * @param camera
	 */
	public void updateCamera(Map map, Rectangle camera) {
		int x = hitbox.x - (camera.width / 2);
		if(x < 0)
			x = 0;
		if(x + camera.width > map.getWidth())
			x = map.getWidth() - camera.width;

		int y = hitbox.y - (camera.height / 2);
		if(y < 0)
			y = 0;
		if(y + camera.height > map.getHeight())
			y = map.getHeight() - camera.height;

		camera.x = x;
		camera.y = y;
	}


	/**
	 * Method that saves the current players details to text file.
	 */
	public void savePlayer() {
		if(playerFile.exists()) {
			playerFile.delete();
		}
		try {
			playerFile.createNewFile();

			PrintWriter pr = new PrintWriter(playerFile);

			pr.println(name);
			pr.println(health);
			pr.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Player save failed.");
		}

	}

	@Override
	public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) {
		return false;		
	}

	@Override
	public int getLayer() {
		return layer;
	}


	@Override
	public Rectangle getRectangle() {
		return hitbox;
	}



}
