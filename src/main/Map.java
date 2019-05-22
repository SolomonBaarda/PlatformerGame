package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Map {

	private File mapFile;
	private Tiles tileSet;
	private Sprite blockBackgroundSky;
	private Sprite blockBackgroundGround;
	private Sprite blockBackgroundStone;

	private ArrayList<MappedTile> mappedTiles; 
	private Block[][] blocks;

	private int blockStartX, blockStartY;

	// zoom 3 by default, gets updated when constructor called
	private int xZoom = 3;
	private int yZoom = 3;
	private final int tileWidth = Game.tilePixels * xZoom;
	private final int tileHeight = Game.tilePixels * yZoom;

	private final int blockWidth = 8; 
	private final int blockHeight = 8; 
	private final int blockPixelWidth = blockWidth * tileWidth;
	private final int blockPixelHeight = blockHeight * tileHeight;

	public int numLayers = 3;

	private int mapWidth, mapHeight;
	private int mapWidthBlocks, mapHeightBlocks;

	private int seed;
	private int grassBlockY = 2;
	private boolean generated;

	private HashMap<Integer, String> comments;

	/**
	 * @param mapFile
	 * @param tileSet
	 * @param blockBackgroundSky
	 * @param blockBackgroundGround
	 * @param mapWidthBlocks
	 * @param mapHeightBlocks
	 * @param xZoom
	 * @param yZoom
	 */
	public Map(File mapFile, Tiles tileSet, Sprite blockBackgroundSky, Sprite blockBackgroundGround, Sprite blockBackgroundStone, int mapWidthBlocks, int mapHeightBlocks, int xZoom, int yZoom) {
		this.mapFile = mapFile;
		this.tileSet = tileSet;
		this.blockBackgroundSky = blockBackgroundSky;
		this.blockBackgroundGround = blockBackgroundGround;
		this.blockBackgroundStone = blockBackgroundStone;
		this.xZoom = xZoom;
		this.yZoom = yZoom;

		this.mapWidthBlocks = mapWidthBlocks;
		this.mapHeightBlocks = mapHeightBlocks;
		this.mapWidth = mapWidthBlocks * blockPixelWidth;
		this.mapHeight = mapHeightBlocks * blockPixelHeight;

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;


		try {
			Scanner s = new Scanner(mapFile);
			int currentLine = 0;

			mappedTiles = new ArrayList<>();
			comments = new HashMap<>();

			while(s.hasNextLine()) {

				// read each line and create instances of tile
				String line = s.nextLine();

				// not comments 
				if(!line.startsWith("//")) {

					if(!line.startsWith("SEED")) {
						String[] splitLine = line.split(",");
						if(splitLine.length == 5) {
							MappedTile mappedTile = new MappedTile(
									// Layer
									Integer.parseInt(splitLine[0].trim()), 
									// Rotation
									Integer.parseInt(splitLine[1].trim()), 
									// TileID
									Integer.parseInt(splitLine[2].trim()),
									// x position
									Integer.parseInt(splitLine[3].trim()),
									// y position
									Integer.parseInt(splitLine[4].trim()) );

							if(mappedTile.getX() < minX)
								minX = mappedTile.getX();
							if(mappedTile.getY() < minY)
								minY = mappedTile.getY();
							if(mappedTile.getX() > maxX)
								maxX = mappedTile.getX();
							if(mappedTile.getY() > maxY)
								maxY = mappedTile.getY();

							if(numLayers <= mappedTile.layer)
								numLayers = mappedTile.layer + 1;

							mappedTiles.add(mappedTile);
						}
					}
					else if(line.startsWith("SEED")){
						String[] seed = line.split(":");
						this.seed = Integer.parseInt(seed[1]);
						comments.put(0, "SEED:" +seed);
					}

				}
				// do comments 
				else {
					comments.put(currentLine, line);
				}

				currentLine++;

			}
			s.close();

			if(mappedTiles.size() == 0) {
				minX = -blockWidth;
				minY = -blockWidth;
				maxX = blockWidth;
				maxY = blockHeight;
			}

			blockStartX = minX;
			blockStartY = minY;
			int blockSizeX = (maxX + blockWidth) - minX;
			int blockSizeY = (maxY + blockHeight) - minY;

			blocks = new Block[blockSizeX][blockSizeY];

			// Loop through all mappedTiles in level and add them to blocks
			for(int i = 0; i < mappedTiles.size(); i++) {
				MappedTile mappedTile = mappedTiles.get(i);

				int blockX = (mappedTile.getX() - minX) / blockWidth;
				int blockY = (mappedTile.getY() - minY) / blockHeight;

				assert(blockX >= 0 && blockX < blocks.length && blockY >= 0 && blockY <= blocks[0].length);

				if(blocks[blockX][blockY] == null)
					blocks[blockX][blockY] = new Block();

				blocks[blockX][blockY].addTile(mappedTile);
			}

			if(mappedTiles.isEmpty() && comments.isEmpty()) {
				System.out.println("World is empty. Generating world!");
				
				if(seed == 0) {
					seed = (new Random()).nextInt();
				}
				
				generateWorld(mapWidthBlocks, mapHeightBlocks, seed);
				saveMap();
			}
			
			System.out.println(mapFile+ " loaded successfully!");


		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Map failed to load!");
		}


	}


	public void render(RenderHandler renderer, GameObject[] objects, int xZoom, int yZoom) {
		// Render background
		Rectangle camera = renderer.getCamera();
		Sprite block;

		int topLeftX = camera.x/blockPixelWidth;
		int topLeftY = camera.y/blockPixelHeight;
		int bottomRightX = (camera.x + camera.width)/blockPixelWidth + 1;
		int bottomRightY = (camera.y + camera.height)/blockPixelHeight + 1;

		for(int blockY = topLeftY; blockY < bottomRightY; blockY++) {
			for(int blockX = topLeftX; blockX < bottomRightX; blockX++) {
				// if dirt level
				if(blockY == grassBlockY)
					block = blockBackgroundGround;
				else if(blockY >= grassBlockY + 1)
					block = blockBackgroundStone;
				else {
					block = blockBackgroundSky;
				}
				renderer.renderSprite(block, blockX*blockPixelWidth, blockY*blockPixelHeight, xZoom, yZoom, false);
			}
		}


		for(int layer = 0; layer <= numLayers; layer++) {
			topLeftX = camera.x;
			topLeftY = camera.y;
			bottomRightX = camera.x + camera.width;
			bottomRightY = camera.y + camera.height;

			int leftBlockX = (topLeftX/tileWidth - blockStartX) / blockWidth;

			int blockX = leftBlockX;
			int blockY = (topLeftY/tileHeight - blockStartY) / blockHeight;

			int pixelX = topLeftX;
			int pixelY = topLeftY;

			while(pixelX < bottomRightX && pixelY < bottomRightY) {
				if(blockX >= 0 && blockY >= 0 && blockX < blocks.length && blockY < blocks.length) {
					if(blocks[blockX][blockY] != null) {
						blocks[blockX][blockY].render(renderer, layer, tileWidth, tileHeight, xZoom, yZoom);
					}
				}

				blockX++;
				pixelX += blockPixelWidth;

				if(pixelX > bottomRightX) {
					pixelX = topLeftX;
					blockX = leftBlockX;
					blockY++;
					pixelY += blockPixelHeight;
					if(pixelY > bottomRightY)
						break;
				}
			}

//			topLeftX = camera.x/blockPixelWidth;
//			topLeftY = camera.y/blockPixelHeight;
//			bottomRightX = (camera.x + camera.width)/blockPixelWidth + 1;
//			bottomRightY = (camera.y + camera.height)/blockPixelHeight + 1;
//
//			for(int blockY = topLeftY; blockY < bottomRightY; blockY++) {
//				for(int blockX = topLeftX; blockX < bottomRightX; blockX++) {
//					if(blockX >= 0 && blockY >= 0 && blockX < blocks.length && blockY < blocks.length) {
//						if(blocks[blockX][blockY] != null) {
//
//							blocks[blockX][blockY].render(renderer, layer, tileWidth, tileHeight, xZoom, yZoom);
//						}
//					}
//				}
//			}


			for(int i = 0; i < objects.length; i++) {
				if(objects[i].getLayer() == layer)
					objects[i].render(renderer, xZoom, yZoom);

			}

		}
		for(int i = 0; i < objects.length; i++) 
			if(objects[i].getLayer() == Integer.MAX_VALUE)
				objects[i].render(renderer, xZoom, yZoom);


	}




	public void saveMap() {
		int currentLine = 0;

		try {
			if(mapFile.exists()) {
				mapFile.delete();
			}
			mapFile.createNewFile();
			
			PrintWriter pr = new PrintWriter(mapFile);
			
			pr.println("SEED:" +seed);

			for(int i = 0; i < mappedTiles.size(); i++)	{
				if(comments.containsKey(currentLine)) {
					pr.println(comments.get(currentLine));
				}

				MappedTile tile = mappedTiles.get(i);
				pr.println(tile.getLayer()+ "," +tile.getRotation()+ "," +tile.getId()+ "," +tile.getX()+ "," +tile.getY());

				currentLine++;
			}
			pr.close();
			System.out.println(mapFile+ " saved succesfully!");

		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}



	/**
	 * Method that places blocks for a map, only called on a new, empty map.
	 * 
	 * @param mapWidthBlocks
	 * @param mapHeightBlocks
	 */
	public void generateWorld(int mapWidthBlocks, int mapHeightBlocks, int seed) {

		// use seed idea
		this.seed = seed;
		this.mapWidthBlocks = mapWidthBlocks;
		this.mapHeightBlocks = mapHeightBlocks;
		
		// Create random number generator with seed for world generation 
		Random r = new Random(seed);

		comments.put(0, "// layer, rotation, tileID, x, y");

		// Create stone
		for(int y=mapHeightBlocks/2; y<mapHeightBlocks; y++)
			for(int x=0; x<mapWidthBlocks; x++)
				setBlock(x, y, tileSet.findTile("Stone"), 1, 0);
		
		// Create dirt
		for(int y=grassBlockY; y<mapHeightBlocks/2; y++)
			for(int x=0; x<mapWidthBlocks; x++)
				setBlock(x, y, tileSet.findTile("Dirt"), 1, 0);
		
		// Create grass
		for(int y = grassBlockY*blockHeight; y < grassBlockY*blockHeight + 2; y++)
			for(int x=0; x<mapWidthBlocks*blockWidth; x++)
				setTile(1, 0, x, y, tileSet.findTile("Grass"));
		
		// Generate trees
		for(int x=0; x<mapWidth; x++) {
			if(r.nextInt(3) == 0) {
				setTile(0, 0, x, grassBlockY*blockWidth-1, tileSet.findTile("TreeBottom"));
				setTile(0, 0, x, grassBlockY*blockWidth-2, tileSet.findTile("TreeTop"));
			}
		}
		
		
		
		System.out.println("World generated!");
		
	}
	
	
	/**
	 * @param tileTopLeftX
	 * @param tileTopLeftY
	 * @param tileBottomRightX
	 * @param tileBottomRightY
	 * @param tileID
	 * @param layer
	 * @param rotation
	 */
	public void setTiles(int tileTopLeftX, int tileTopLeftY, int tileBottomRightX, int tileBottomRightY, int tileID, int layer, int rotation) {
		for(int y = tileTopLeftY; y <= tileBottomRightY; y++)
			for(int x = tileTopLeftX; x <= tileBottomRightX; x++) {
				setTile(layer, rotation, x, y, tileID);
			}
	}




	/**
	 * @param layer
	 * @param tileX
	 * @param tileY
	 * @return
	 */
	public MappedTile getTile(int layer, int tileX, int tileY) {
		int blockX = (tileX - blockStartX) / blockWidth;
		int blockY = (tileY - blockStartY) / blockHeight;

		if(blockX < 0 || blockX >= blocks.length || blockY < 0 || blockY >= blocks[0].length)
			return null;

		Block block = blocks[blockX][blockY];

		if(block == null)
			return null;
		else
			return block.getTile(layer, tileX, tileY);
	}

	
	
	

	/**
	 * @param blockX
	 * @param blockY
	 * @param tileID
	 * @param layer
	 * @param rotation
	 */
	public void setBlock(int blockX, int blockY, int tileID, int layer, int rotation) {
		setTiles(blockX * blockWidth, blockY * blockHeight, blockX * blockWidth + blockWidth - 1, blockY * blockHeight + blockHeight - 1, tileID, layer, rotation);
	}
	
	
	
	
	/**
	 * @param layer
	 * @param rotation
	 * @param tileX
	 * @param tileY
	 * @param tileID
	 */
	public void setTile(int layer, int rotation, int tileX, int tileY, int tileID) {

		if(layer >= numLayers)
			numLayers = layer + 1;

		// Search through all mapped tiles for a tile in the current place
		for(int i = 0; i < mappedTiles.size(); i++) {
			MappedTile mappedTile = mappedTiles.get(i);
			// if found, replace it and return out
			if(mappedTile.getLayer() == layer && mappedTile.getX() == tileX && mappedTile.getY() == tileY) {
				mappedTile.setId(tileID);
				return;
			}
		}

		// if we get here, the space is empty
		MappedTile mappedTile = new MappedTile(layer, rotation, tileID, tileX, tileY);
		// add to mapped tiles
		mappedTiles.add(mappedTile);

		// add to blocks
		int blockX = (tileX - blockStartX) / blockWidth;
		int blockY = (tileY - blockStartY) / blockHeight;

		if(blockX >= 0 && blockY >= 0 && blockX < blocks.length && blockY < blocks[0].length) {
			if(blocks[blockX][blockY] == null)
				blocks[blockX][blockY] = new Block();

			blocks[blockX][blockY].addTile(mappedTile);
		}
		else {
			int newMinX = blockStartX;
			int newMinY = blockStartY;
			int newLengthX = blocks.length;
			int newLengthY = blocks[0].length;

			if(blockX < 0) {
				int increaseAmount = blockX * -1;
				newMinX = blockStartX - blockWidth * increaseAmount;
				newLengthX = newLengthX + increaseAmount;
			}
			else if(blockX >= blocks.length)
				newLengthX = blocks.length + blockX;

			if(blockY < 0) {
				int increaseAmount = blockY * -1;
				newMinY = blockStartY - blockHeight * increaseAmount;
				newLengthY = newLengthY + increaseAmount;
			}
			else if(blockY >= blocks[0].length)
				newLengthY = blocks[0].length + blockY;

			System.out.println("New blockLength "+ newLengthX +" "+ newLengthY);
			Block[][] newBlocks = new Block[newLengthX][newLengthY];

			for(int x = 0; x < blocks.length; x++) 
				for(int y = 0; y < blocks[0].length; y++) 
					if(blocks[x][y] != null) {
						newBlocks[x + (blockStartX - newMinX)/blockWidth][y + (blockStartY - newMinY)/blockHeight] = blocks[x][y]; 

					}

			blocks = newBlocks;
			blockStartX = newMinX;
			blockStartY = newMinY;
			blockX = (tileX - blockStartX) / blockWidth;
			blockY = (tileY - blockStartY) / blockHeight;

			if(blocks[blockX][blockY] == null)
				blocks[blockX][blockY] = new Block();
			blocks[blockX][blockY].addTile(mappedTile);

		}
	}



	public void removeTile(int layer, int tileX, int tileY) {

		// loop through all mapped tiles
		for(int i = 0; i < mappedTiles.size(); i++) {
			MappedTile mappedTile = mappedTiles.get(i);

			// if found
			if(mappedTile.getLayer() == layer && mappedTile.getX() == tileX && mappedTile.getY() == tileY)  {
				// remove from mapped tiles
				mappedTiles.remove(mappedTile);

				//remove from block
				int blockX = (tileX - blockStartX) / blockWidth;
				int blockY = (tileY - blockStartY) / blockHeight;
				assert(blockX >= 0 && blockY >= 0 && blockX < blocks.length && blockY < blocks[0].length);
				blocks[blockX][blockY].removeTile(mappedTile);

				break;
			}
		}
	}



	public ArrayList<Rectangle> getCollision(Rectangle objectHitbox, int objectLayer) {

		// Area around object to check for collision 
		int topLeftX = (objectHitbox.x - tileWidth) / tileWidth;
		int topLeftY = (objectHitbox.y - tileHeight) / tileHeight;
		int bottomRightX = (objectHitbox.x + objectHitbox.width*xZoom + tileWidth) / tileWidth;
		int bottomRightY = (objectHitbox.y + objectHitbox.height*yZoom + tileHeight) / tileHeight;

		ArrayList<Rectangle> collidedTileHitbox = new ArrayList<>();

		for(int x = topLeftX; x <= bottomRightX; x++)
			for(int y = topLeftY; y <= bottomRightY + tileHeight; y++) {
				MappedTile tile = getTile(objectLayer, x, y);

				if(tile != null) {
					// find type of collision for block
					int collisionType = tileSet.collisionType(tile.id);

					if(collisionType == 0) {
						// No collision fore these blocks
					}

					// Full block collision
					if(collisionType == 1) {
						Rectangle tileHitbox = new Rectangle(tile.x * tileWidth, tile.y * tileHeight, tileWidth, tileHeight);

						if(objectHitbox.intersects(tileHitbox)) {
							collidedTileHitbox.add(tileHitbox);

						}


					}
					// Half block collision
					else if(collisionType == 2) {
						Rectangle tileHitbox = new Rectangle(tile.x * tileWidth, tile.y * tileHeight, tileWidth, tileHeight);

						if(objectHitbox.intersects(tileHitbox)) {
							collidedTileHitbox.add(tileHitbox);

						}


					}


				}


			}

		if(collidedTileHitbox.size() > 0)
			return collidedTileHitbox;

		return null;
	}


	/**
	 * Method that searches for the closest tile below the player, and returns its y position.
	 * 
	 * @param layer
	 * @param playerX
	 * @param playerY
	 * @param playerWidth
	 * @param playerHeight
	 * @return				bottom of the map otherwise
	 */
	public int findYMin(int layer, int playerX, int playerY, int playerWidth, int playerHeight) {
		int playerCentreX = playerX + ((playerWidth * xZoom) / 2);

		for(int y = playerY + playerHeight * yZoom; y < getHeight(); y += tileHeight) {
			MappedTile tile = getTile(layer, (playerCentreX/tileWidth), (y/tileHeight));
			if(tile != null) {
				return tile.getY()*tileWidth;
			}
		}
		return getHeight() - playerHeight * yZoom;
	}


	public int getWidth() {
		return mapWidth;
	}

	public int getHeight() {
		return mapHeight;
	}

	// represents 8x8 block of tiles
	@SuppressWarnings("unchecked")
	private class Block {

		public ArrayList<MappedTile>[] mappedTilesByLayer;


		public Block() {
			mappedTilesByLayer = new ArrayList[numLayers];
			for(int i = 0; i < mappedTilesByLayer.length; i++)
				mappedTilesByLayer[i] = new ArrayList<MappedTile>();
		}

		public void render(RenderHandler renderer, int layer, int tileWidth, int tileHeight, int xZoom, int yZoom) {

			if(mappedTilesByLayer.length > layer) {
				ArrayList<MappedTile> mappedTiles = mappedTilesByLayer[layer];

				for(int i = 0; i < mappedTiles.size(); i++) {
					MappedTile mappedTile = mappedTiles.get(i);
					tileSet.renderTiles(mappedTile.getId(), renderer, mappedTile.getRotation(), mappedTile.getX() * tileWidth, 
							mappedTile.getY() * tileHeight, xZoom, yZoom);
				}
			}
		}

		public void addTile(MappedTile tile) {
			if(mappedTilesByLayer.length <= tile.layer) {
				ArrayList<MappedTile>[] newTilesByLayer = new ArrayList[tile.layer + 1];

				int i = 0;
				for(i = 0; i < mappedTilesByLayer.length; i++)
					newTilesByLayer[i] = mappedTilesByLayer[i];
				for(; i < newTilesByLayer.length; i++)
					newTilesByLayer[i] = new ArrayList<MappedTile>();

				mappedTilesByLayer = newTilesByLayer;
			}
			mappedTilesByLayer[tile.layer].add(tile);
		}


		public void removeTile(MappedTile tile) {
			mappedTilesByLayer[tile.layer].remove(tile);
		}


		public MappedTile getTile(int layer, int tileX, int tileY) {

			for(MappedTile tile : mappedTilesByLayer[layer]) {
				if(tile.getX() == tileX && tile.getY() == tileY)
					return tile;
			}
			return null;

		}



	}


	// TileID in the tileSet and position of the tile on the map
	private class MappedTile {

		private int layer, rotation, id, x, y;

		public MappedTile(int layer, int rotation, int id, int x, int y) {
			this.layer = layer;
			this.rotation = rotation;
			this.id = id;
			this.x = x;
			this.y = y;
		}



		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getLayer() {
			return layer;
		}

		public void setLayer(int layer) {
			this.layer = layer;
		}

		public int getRotation() {
			return rotation;
		}

		public void setRotation(int rotation) {
			this.rotation = rotation;
		}



	}

}
