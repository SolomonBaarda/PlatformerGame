/*
 * Platformer game 
 * 
 * @author Solomon Baarda
 * 
 */

/*
 * Created using the following tutorial:
 * 
 * https://marcusman.com
 * https://www.youtube.com/watch?v=lDzKX3djE-M
 */


package main;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Game extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int alpha = 0xFFFF00DC;

	private Canvas canvas = new Canvas();
	private RenderHandler renderer;

	private int WIDTH, HEIGHT;
	public final int xZoom = 3;
	public final int yZoom = 3;
	public final static int tilePixels = 16;

	private SpriteSheet sheet;
	private SpriteSheet playerSheet;
	private SpriteSheet font;
	private AnimatedSprite playerAnimation;

	private Tiles tiles;
	private Map map;

	private Player player;

	private int selectedTileID = 0;
	private int selectedLayer = 1;

	private GameObject[] objects;
	private KeyboardListener keyListener = new KeyboardListener(this);
	private MouseEventListener mouseListener = new MouseEventListener(this);


	public Game() {
		//Make our program shutdown when we exit out.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Set the position and size of our frame.
		WIDTH = 1000;
		HEIGHT = 800;
		//setBounds(0,0, WIDTH, HEIGHT);

		// Set the size of the canvas
		canvas.setBounds(0, 0, WIDTH, HEIGHT);

		//Add our graphics component
		add(canvas);

		pack();

		//Put our frame in the centre of the screen.
		setLocationRelativeTo(null);

		//Make our frame visible.
		setVisible(true);

		//Create our object for buffer strategy.
		canvas.createBufferStrategy(3);

		// Create instance of the RenderHandler
		renderer = new RenderHandler(canvas.getWidth(), canvas.getHeight());

		// LOAD SPRITES
		// Load sprite sheet
		BufferedImage sheetImage = loadImage("/sprites/Tiles.png");
		sheet = new SpriteSheet(sheetImage);
		sheet.loadSprites(tilePixels, tilePixels);
		// Load player sprite sheet
		BufferedImage playerSheetImage = loadImage("/sprites/Player.png");
		playerSheet = new SpriteSheet(playerSheetImage);
		playerSheet.loadSprites(20, 26);
		// Player animated sprite
		playerAnimation = new AnimatedSprite(playerSheet, 8);
		BufferedImage fontImage = loadImage("/sprites/font_5.png");
		font = new SpriteSheet(fontImage);
		//playerSheet.loadSprites(12, 12, 2);


		// print all files in directory 
		//		File file = new File(".");
		//		for(String fileNames : file.list()) System.out.println(fileNames);

		// Load tile list
		tiles = new Tiles( new File("assets/tiles/TileList.txt"), sheet);



		File mapFile = askForMap();


		// Load map
		map = new Map( mapFile, tiles, new Sprite(loadImage("/sprites/background.png")), xZoom, yZoom);

		// Load SDK GUI
		GUIButton[] buttons = new GUIButton[tiles.size() + map.numLayers +1];
		Sprite[] tileSprites = tiles.getSprites();

		// Tile buttons
		for(int i = 0; i < tiles.size(); i++) {
			Rectangle tileRectangle = new Rectangle(0, i*(tilePixels*xZoom + 2), tilePixels * xZoom, tilePixels * yZoom);
			buttons[i] = new SDKButton(this, i , tileSprites[i], tileRectangle);
		}

		// Layer buttons 
		for(int i = 0; i <= map.numLayers; i++) {
			Rectangle tileRectangle = new Rectangle(i*(tilePixels*xZoom + 2) + 50, 0, tilePixels * xZoom, tilePixels * yZoom);
			buttons[tiles.size() + i] = new LayerButton(this, i, tileRectangle);
		}

		// Create GUI
		GUI gui = new GUI(buttons, 5, 5, true);

		// LOAD OBJECTS
		// Create gameObjects array
		objects = new GameObject[2];
		// Create Player
		player = new Player(playerAnimation, new File("./players/Player.txt"), xZoom, yZoom);
		// Add player to gameObjects
		objects[0] = player;
		// Add GUI to gameObjects
		objects[1] = gui;

		// Add listeners 
		canvas.addKeyListener(keyListener);
		canvas.addFocusListener(keyListener);
		canvas.addMouseListener(mouseListener);
		canvas.addMouseMotionListener(mouseListener);

		addComponentListener( new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {	
				int newWidth = canvas.getWidth();
				int newHeight = canvas.getHeight();

				if(newWidth > renderer.getMaxWidth())
					newWidth = renderer.getMaxWidth();

				if(newHeight > renderer.getMaxHeight())
					newHeight = renderer.getMaxHeight();

				renderer.getCamera().setWidth(newWidth);
				renderer.getCamera().setHeight(newHeight);

				canvas.setSize(newWidth, newHeight);
				pack();
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {				
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {				
			}
			@Override
			public void componentShown(ComponentEvent arg0) {				
			}
		} );


		canvas.requestFocus();
		
		System.out.println("Game() loaded.");
	}



	public File askForMap() {
		System.out.println();


		File listFile = new File("./saves");
		int mapCount = 0;
		for(String fileNames : listFile.list())
			mapCount++;

		if(mapCount == 0) {
			System.out.println("There are no currently saved maps. Please create one!");
		}
		else if(mapCount == 1) {
			System.out.print("There is " +mapCount+ " currently saved map: ");
			for(String fileNames : listFile.list())
				System.out.print(fileNames);
			System.out.println(".");
		}
		else if(mapCount > 1) {
			System.out.print("The " +mapCount+ " currently saved maps are: ");

			int count = 0;
			for(String fileNames : listFile.list()) {
				System.out.print(fileNames);
				count++;
				if(count == mapCount - 1)
					System.out.print(" and ");
				else if(count == mapCount)
					System.out.println(".");
				else {
					System.out.print(", ");
				}
			}
		}
		System.out.print("Please enter the name of the map you wish to play or create: ");

		Scanner s = new Scanner(System.in);

		while(true) {
			String mapName = s.next();

			File mapFile = new File("./saves/"+mapName);
			if(!mapFile.exists()) {
				try {
					mapFile.createNewFile();

				} catch (IOException e) {
					System.out.println("Map could not be created!");
					e.printStackTrace();
					s.close();
					return null;
				}
			}
			s.close();
			return mapFile;


		}
	}




	public void update() {
		// Update all GameObjects
		for(int i = 0; i < objects.length; i++)
			objects[i].update(this);


	}


	public BufferedImage loadImage(String path) {
		try {
			BufferedImage loadedImage = ImageIO.read(Game.class.getResource(path));
			BufferedImage formattedImage = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			formattedImage.getGraphics().drawImage(loadedImage, 0, 0, null);

			return formattedImage;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Sprite loadSprite(String path) {
		return new Sprite(loadImage(path));
	}


	/**
	 * Renders the pixel array to the screen.
	 */
	public void render() {
		BufferStrategy bufferStrategy = canvas.getBufferStrategy();
		Graphics graphics = bufferStrategy.getDrawGraphics();
		super.paint(graphics);


		// Render the map
		map.render(renderer, objects, xZoom, yZoom);

		// Update all GameObjects
		//		for(int i = 0; i < objects.length; i++)
		//			objects[i].render(renderer, xZoom, yZoom);

		renderer.render(graphics);

		graphics.dispose();
		bufferStrategy.show();
		renderer.clear();
	}


	public void run() {
		//		BufferStrategy bufferStrategy = canvas.getBufferStrategy();
		//		int i = 0;
		//		int x = 0;

		long lastTime = System.nanoTime(); //long 2^63
		double nanoSecondConversion = 1000000000.0 / 60; // 60 ticks per second
		double changeInSeconds = 0;

		while(true) {
			long now = System.nanoTime();

			changeInSeconds += (now - lastTime) / nanoSecondConversion;
			while(changeInSeconds >= 1) {
				update();
				changeInSeconds--;
			}

			render();
			lastTime = now;
		}

	}



	public void leftClick(int x, int y) {

		Rectangle mouseRectangle = new Rectangle(x, y, 1, 1);
		boolean stoppedChecking = false;

		for(int i = 0; i < objects.length; i++) 
			if(!stoppedChecking)
				stoppedChecking = objects[i].handleMouseClick(mouseRectangle, renderer.getCamera(), xZoom, yZoom);

		if(!stoppedChecking) {
			int xTile = (int) Math.floor( (x + renderer.getCamera().getX()) / (16.0 * xZoom) );
			int yTile = (int) Math.floor( (y + renderer.getCamera().getY()) / (16.0 * yZoom) ); 

			map.setTile(selectedLayer, 0, xTile, yTile, selectedTileID);
		}
	}

	public void rightClick(int x, int y) {

		int xTile = (int) Math.floor( (x + renderer.getCamera().getX()) / (16.0 * xZoom) );
		int yTile = (int) Math.floor( (y + renderer.getCamera().getY()) / (16.0 * yZoom) ); 

		map.removeTile(selectedLayer, xTile, yTile);
	}

	public void handleCTRL(boolean[] keys) {

		if(keys[KeyEvent.VK_S]) {
			map.saveMap();
			System.out.println("Map saved succesfully!");
		}
	}





	public Map getMap() {
		return map;
	}


	public int getSelectedLayer() {
		return selectedLayer;
	}


	public void setSelectedLayer(int selectedLayer) {
		this.selectedLayer = selectedLayer;
	}


	public int getSelectedTile() {
		return selectedTileID;
	}


	public void changeSelectedTile(int tileID) {
		selectedTileID = tileID;
	}


	public MouseEventListener getMouseListener() {
		return mouseListener;
	}


	public RenderHandler getRenderer() {
		return renderer;
	}


	public KeyboardListener getKeyListener() {
		return keyListener;
	}


	public static void main(String[] args) {
		Game game = new Game();
		Thread gameThread = new Thread(game);
		gameThread.start();
	}

}