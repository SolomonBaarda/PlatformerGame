package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Map {

	private File mapFile;
	private Tiles tileSet;
	private Sprite background;

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

	private HashMap<Integer, String> comments;

	public Map(File mapFile, Tiles tileSet, Sprite background, int xZoom, int yZoom) {
		this.mapFile = mapFile;
		this.tileSet = tileSet;
		this.background = background;
		this.xZoom = xZoom;
		this.yZoom = yZoom;

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		if(!mapFile.exists()) {
			try {
				mapFile.createNewFile();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

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

			System.out.println("Map loaded successfully!");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	public void render(RenderHandler renderer, GameObject[] objects, int xZoom, int yZoom) {
		// Render background
		Rectangle camera = renderer.getCamera();
		renderer.renderSprite(background, 0,0, 0, 1, 1, true, camera.getX(), camera.getY(), camera.getWidth(), camera.getHeight() );

		int tileWidth = Game.tilePixels * xZoom;
		int tileHeight = Game.tilePixels * yZoom;

		for(int layer = 0; layer < numLayers; layer++) {

			int topLeftX = renderer.getCamera().getX();
			int topLeftY = renderer.getCamera().getY();
			int bottomRightX = renderer.getCamera().getX() + renderer.getCamera().getWidth();
			int bottomRightY = renderer.getCamera().getY() + renderer.getCamera().getHeight();

			int leftBlockX = (topLeftX/tileWidth - blockStartX) / blockWidth;

			int blockX = leftBlockX;
			int blockY = (topLeftY/tileHeight - blockStartY) / blockHeight;

			int pixelX = topLeftX;
			int pixelY = topLeftY;

			while(pixelX < bottomRightX && pixelY < bottomRightY) {

				if(blockX >= 0 && blockY >= 0 && blockX < blocks.length && blockY < blocks.length) {
					if(blocks[blockX][blockY] != null)
						blocks[blockX][blockY].render(renderer, layer, tileWidth, tileHeight, xZoom, yZoom);
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
			
			for(int i = 0; i < mappedTiles.size(); i++)	{
				if(comments.containsKey(currentLine)) {
					pr.println(comments.get(currentLine));
				}

				MappedTile tile = mappedTiles.get(i);
				pr.println(tile.getLayer()+ "," +tile.getRotation()+ "," +tile.getId()+ "," +tile.getX()+ "," +tile.getY());

				currentLine++;
			}
			pr.close();

		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}



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

			System.out.println(newLengthX);
			System.out.println(newLengthY);
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


	//
	//	public boolean checkCollision(Rectangle playerHitbox, int playerLayer, int xZoom, int yZoom) {
	//		int tileWidth = Game.tilePixels * xZoom;
	//		int tileHeight = Game.tilePixels * yZoom;
	//
	//		// coordinates to check in a 4x4 area around the player
	//		int topLeftX = (playerHitbox.getX() - tileWidth) / tileWidth;
	//		int topLeftY = (playerHitbox.getY() - tileHeight) / tileHeight;
	//		int bottomRightX = (playerHitbox.getX() + playerHitbox.getWidth() + tileWidth) / tileWidth;
	//		int bottomRightY = (playerHitbox.getY() + playerHitbox.getHeight() + tileHeight) / tileHeight;
	//
	//		//  start top left, go to bottom right
	//		for(int x = topLeftX; x <= bottomRightX; x++)
	//			for(int y = topLeftY; y <= bottomRightY; y++) {
	//				MappedTile tile = getTile(playerLayer, x, y);
	//
	//				if(tile != null) {					
	//					int collisionType = tileSet.collisionType(tile.id);
	//
	//					//Full tile collision
	//					if(collisionType == 1) {
	//						System.out.println("Type 1");
	//						Rectangle tileRectangle = new Rectangle(tile.x*tileWidth, tile.y*tileHeight, tileWidth, tileHeight);
	//							if(tileRectangle.intersects(playerHitbox)) {
	//								System.out.println("colliding");
	//							return true;
	//							}
	//
	//					} 
	//					//Top of tile collision
	//					else if(collisionType == 2) {
	//						System.out.println("Type 2");
	//						Rectangle tileRectangle = new Rectangle(tile.x*tileWidth, tile.y*tileHeight, tileWidth, tileHeight/2);
	//						if(tileRectangle.intersects(playerHitbox))
	//							return true;
	//
	//					} 
	//					//Left of tile collision
	//					else if(collisionType == 3) {
	//						System.out.println("Type 3");
	//						Rectangle tileRectangle = new Rectangle(tile.x*tileWidth, tile.y*tileHeight, tileWidth/2, tileHeight);
	//						if(tileRectangle.intersects(playerHitbox))
	//							return true;
	//
	//					} 
	//					//Bottom of tile collision
	//					else if (collisionType == 4) {
	//						Rectangle tileRectangle = new Rectangle(tile.x*tileWidth, tile.y*tileHeight + tileHeight - 16, tileWidth, 16);
	//						Rectangle adjustedRect = new Rectangle(playerHitbox.getX(), playerHitbox.getY() + playerHitbox.getHeight(), playerHitbox.getWidth(), 1);
	//						if(tileRectangle.intersects(adjustedRect))
	//							return true;
	//
	//					} 
	//					//Right of tile collision
	//					else if (collisionType == 5) {
	//						Rectangle tileRectangle = new Rectangle(tile.x*tileWidth + tileWidth - 16, tile.y*tileHeight, 16, tileHeight);
	//						if(tileRectangle.intersects(playerHitbox))
	//							return true;
	//					}
	//
	//				}
	//			}
	//		return false;
	//	}
	//



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
		return background.getWidth();
	}

	public int getHeight() {
		return background.getHeight();
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
