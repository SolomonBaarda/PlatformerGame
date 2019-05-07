package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MapOld {

	private Tiles tileSet;
	private int fillTileID = -1;

	private File mapFile, Test;

	private ArrayList<MappedTile> mappedTiles; 
	private HashMap<Integer, String> comments;

	public MapOld(File mapFile, Tiles tileSet) {

		this.tileSet = tileSet;
		this.mapFile = mapFile;
		
		mappedTiles = new ArrayList<>();
		comments = new HashMap<>();

		try {
			Scanner s = new Scanner(mapFile);
			int currentLine = 0;

			while(s.hasNextLine()) {

				// read each line and create instances of tile
				String line = s.nextLine();

				// not comments 
				if(!line.startsWith("//")) {

					if(line.contains(":")) {
						String[] splitLine = line.split(":");
						if(splitLine[0].equalsIgnoreCase("Fill")) {
							fillTileID = Integer.parseInt(splitLine[1]);

							// not sure is needed 
							//currentLine++;
							continue;
						}

					}

					else {
						String[] splitLine = line.split(",");
						if(splitLine.length >= 3) {
							MappedTile mappedTile = new MappedTile(Integer.parseInt(splitLine[0]), 
									Integer.parseInt(splitLine[1]), 
									Integer.parseInt(splitLine[2]));
							mappedTiles.add(mappedTile);
						}
					}

				}
				// do comments 
				else {
					comments.put(currentLine, line);
				}

				currentLine++;
			}
			s.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}




	public void saveMap() {
		int currentLine = 0;

		try {
			if(mapFile.exists()) {
				mapFile.delete();
			}
			mapFile.createNewFile();

			PrintWriter pr = new PrintWriter(mapFile);

			if(fillTileID >= 0) {
				if(comments.containsKey(currentLine)) {
					pr.println(comments.get(currentLine));
					currentLine++;
				}
				pr.println("Fill:" +fillTileID);
			}

			for(int i = 0; i < mappedTiles.size(); i++)	{
				if(comments.containsKey(currentLine)) {
					pr.println(comments.get(currentLine));
				}

				MappedTile tile = mappedTiles.get(i);
				pr.println(tile.getId()+ "," +tile.getX()+ "," +tile.getY());

				currentLine++;
			}


			pr.close();

			// Print list of comments
//			for(int i = 0; i < comments.size(); i++) {
//				System.out.println(i+ " , " +comments.get(i));
//			}


		}
		catch(IOException e) {
			e.printStackTrace();
		}


	}


	public void render(RenderHandler renderer, int xZoom, int yZoom) {

		int tileWidth = 16 * xZoom;
		int tileHeight = 16 * yZoom;


		if(fillTileID >= 0) {

			Rectangle camera = renderer.getCamera();

			for(int y = camera.getY() -tileHeight - (camera.getY() % tileHeight); y < camera.getY() + camera.getHeight(); y += tileHeight) {
				for(int x = camera.getX() - tileWidth - (camera.getX() % tileWidth); x < camera.getX() + camera.getWidth(); x += tileWidth) {

					tileSet.renderTiles(fillTileID, renderer, x, y, xZoom, yZoom);

				}
			}
		}

		for(int i = 0; i < mappedTiles.size(); i++) {
			MappedTile mappedTile = mappedTiles.get(i);
			tileSet.renderTiles(mappedTile.getId(), renderer, 
					mappedTile.getX() * tileWidth, mappedTile.getY() * tileHeight, xZoom, yZoom);
		}


	}

	public void setTile(int tileX, int tileY, int tileID) {
		boolean foundTile = false;

		for(int i = 0; i < mappedTiles.size(); i++) {
			MappedTile mappedTile = mappedTiles.get(i);

			if(mappedTile.getX() == tileX && mappedTile.getY() == tileY) {
				mappedTile.setId(tileID);
				foundTile = true;
				break;
			}
		}
		if(!foundTile) {
			mappedTiles.add(new MappedTile(tileID, tileX, tileY));
		}


	}
	
	public void removeTile(int tileX, int tileY) {
		
		for(int i = 0; i < mappedTiles.size(); i++) {
			MappedTile mappedTile = mappedTiles.get(i);

			if(mappedTile.getX() == tileX && mappedTile.getY() == tileY) {
				mappedTiles.remove(i);
				break;
			}
		}
	}




	// TileID in the tileSet and position of the tile on the map
	class MappedTile {

		private int id, x, y;

		public MappedTile(int id, int x, int y) {
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



	}







}
