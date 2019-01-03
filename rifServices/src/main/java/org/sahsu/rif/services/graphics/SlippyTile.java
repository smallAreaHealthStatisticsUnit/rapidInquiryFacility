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

	private Integer zoomlevel;
	private Integer x;
	private Integer y;
		
	/**
	 * Constructor from x/y and zoomlevel
	 *
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 * @param x 				X tile number		
	 * @param y					Y tile number
	 */
	public SlippyTile(
		final Integer zoomlevel, 
		final Integer x, 
		final Integer y) throws RIFTilesException {
		
		if (zoomlevel < 0 || zoomlevel > 18) {
			throw new RIFTilesException(new Exception("Invalid zoomlevel (0-18): " + zoomlevel), null);
		}
		int maxTiles=(int)Math.pow(2, zoomlevel); // For zoomlevel
		if (x < 0 || x >= maxTiles) {
			throw new RIFTilesException(new Exception("Invalid x (0-" + (maxTiles-1) + "): " + x), null);
		}
		if (y < 0 || y >= maxTiles) {
			throw new RIFTilesException(new Exception("Invalid y (0-" + (maxTiles-1) + "): " + y), null);
		}
		this.zoomlevel = zoomlevel;
		this.x = x;
		this.y = y;
	}

	/** 
	 * Constructor from Lat/Long and zoomlevel
	 *
	 * <p>
	 * Java OSM functions from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 * </p>
	 *
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 * @param lat				Latitude
	 * @param lon				Longitude
	 *
     */	
	private SlippyTile(
			final double lat, 
			final double lon, 
			final int newZoomlevel) {
		int newX = (int)Math.floor( (lon + 180) / 360 * (1<<newZoomlevel) ) ;
		int newY = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 
				2 * (1<<newZoomlevel) ) ;
		if (newX < 0) {
			newX=0;
		}
		if (newX >= (1<<newZoomlevel)) {
			newX=((1<<newZoomlevel)-1);
		}
		if (newY < 0) {
			newY=0;
		}
		if (newY >= (1<<newZoomlevel)) {
			newY=((1<<newZoomlevel)-1);
		}

		this.zoomlevel = newZoomlevel;
		this.x = newX;
		this.y = newY;
	}

	/** 
	 * Get slippy tile parent (next lower resolution zoomlevel)
     *
	 * @return SlippyTile
	 *
	 * @throws RIFTilesException: invalid zoomlevel in slippyTile
     */	
	public SlippyTile getParentTile() throws RIFTilesException {
		double lon=toLongitude(); // minX
		double lat=toLatitude(); // minY
		int newZoomlevel=zoomlevel-1;
		
		if (zoomlevel < 1) {
			throw new RIFTilesException(new Exception("Invalid zoomlevel (1-18): " + zoomlevel), null);
		}
		SlippyTile parentTile = new SlippyTile(lat, lon, newZoomlevel);
		
		if (parentTile.getX() == x && parentTile.getY() == y && parentTile.getZoomlevel() == zoomlevel) {
			throw new RIFTilesException(new Exception("Parent == child: " + parentTile.toString()), null);
		}
		
		return parentTile;
	}

	/** 
	 * Get slippy tile info
	 *
	 * @return String slippy tile info
     */	
	public String toString() {
		return "zoomlevel: " + zoomlevel + "; x: " + x + "; y: " + y;
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

		bboxJson.put(toLongitude(x, zoomlevel));		// Wast: minX
		bboxJson.put(toLatitude(y, zoomlevel));			// South: minY
		bboxJson.put(toLongitude(x + 1, zoomlevel));	// East: maxX
		bboxJson.put(toLatitude(y + 1, zoomlevel));		// North: maxY
		return bboxJson;
	}

	/** 
	 * Get Longitude for tile
	 *
	 * <p>
	 * Java OSM BBOX functions from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 * </p>
	 *
	 * @return Longitude (X) in 4326
     */		
	private double toLongitude() {
		return x / Math.pow(2.0, zoomlevel) * 360.0 - 180;
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
	private double toLongitude(int x, int zoomlevel) {
		return x / Math.pow(2.0, zoomlevel) * 360.0 - 180;
	}

	/** 
	 * Java OSM BBOX functions from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 * </p>
	 *
	 * @param y 				Y tile number	
	 * @param zoomlevel 		0-9 (depends in TileMaker maximum zoomlevel)
	 *
	 * @return Latitude (Y) in 4326 
     */			
	private double toLatitude(int y, int zoomlevel) {		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, zoomlevel);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}

	/** 
	 * Get Latitude for tile
	 *
	 * <p>
	 * Java OSM BBOX functions from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
	 * </p>
	 *
	 * @return Latitude (Y) in 4326 
     */			
	private double toLatitude() {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, zoomlevel);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}

}