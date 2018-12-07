package org.sahsu.rif.services.graphics;

import org.json.JSONArray;

/**
 * Slippy tiles support: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class SlippyTile {
	
	static Integer zoomlevel;
	static Integer x; 
	static Integer y;
		
	/**
	 * Constructor
	 *
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 * @param x 				X tile number		
	 * @param y					Y tile number
	 *
	 * @throws NullPointerException on Constructor failure
	 */
	public SlippyTile(
		final Integer zoomlevel, 
		final Integer x, 
		final Integer y) {
			this.zoomlevel = zoomlevel;
			this.x = x;
			this.y = y;
	}

	/** 
	 * Get slippy tile path and name (without the extension)
	 *
	 * @return String slippy tile path and name
     */	
	public String getPathFileName() {
		return "/" + zoomlevel + "/" + x + "/" + y;	
	}

	/** 
	 * Get slippy tile zoomlevel
	 *
	 * @return int zoomlevel
     */	
	public int getZoomlevel() {
		return zoomlevel;	
	}	
		
	/** 
	 * Get slippy tile x
	 *
	 * @return int x
     */	
	public int getX() {
		return x;	
	}	
	
	/** 
	 * Get slippy tile y
	 *
	 * @return int y
     */	
	public int getY() {
		return y;	
	}	
	
	/** 
	 * Get bounding box for tile
	 *
	 * <p>
	 * Java OSM BBOX functions from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 * </p>
	 *
	 * @return JSONArray bboxJson is: [minX, minY, maxX, maxY]
     */	
	public JSONArray tile2boundingBox() {
		JSONArray bboxJson = new JSONArray();
		bboxJson.put(tile2lon(x, zoomlevel));		// Wast: minX
		bboxJson.put(tile2lat(y + 1, zoomlevel));	// South: minY
		bboxJson.put(tile2lon(x + 1, zoomlevel));	// East: maxX
		bboxJson.put(tile2lat(y, zoomlevel));		// North: maxY
		return bboxJson;
	}

	/** 
	 * Get Longitude for tile
	 *
	 * <p>
	 * Java OSM BBOX functions from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 * </p>
	 *
	 * @param x 				X tile number	
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 *
	 * @return Longitude (X) in 4326
     */		
	private double tile2lon(int x, int zoomlevel) {
		return x / Math.pow(2.0, zoomlevel) * 360.0 - 180;
	}

	/** 
	 * Get Latitude for tile
	 *
	 * <p>
	 * Java OSM BBOX functions from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 * </p>
	 *
	 * @param y 				Y tile number	
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 *
	 * @return Latitude (Y) in 4326 
     */			
	private double tile2lat(int y, int zoomlevel) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, zoomlevel);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}

}