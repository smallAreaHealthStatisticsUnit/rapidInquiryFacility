package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.graphics.SlippyTile;

import org.json.JSONObject;

import java.util.Base64;
import java.util.Base64.Encoder;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Caching for RIF Generated Tiles. Tiles are either in GeoJSON or PNG form
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class RIFTilesCache {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static String EXTRACT_DIRECTORY = null;

	/**
	 * Constructor
	 *
	 * @param RIFServiceStartupOptions for the extract directory
	 *
	 * @throws NullPointerException
	 */
	public RIFTilesCache(final RIFServiceStartupOptions options) {
		try {
			EXTRACT_DIRECTORY = options.getExtractDirectory();
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFTiles() constructor");
			throw new NullPointerException();
		}
	}
	
	/**
	 * Cache GeoJSON/PNG tile. Write to .<fileExtension>.tmp and then rename to ensure file creation is atomic
	 * <p>
	 * Write GeoJSON ByteArrayOutputStream to tile: 
	 * EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>/<y>.<fileExtension>
	 * </p>
	 *
	 * @param tileGeoJson 		Tile GeoJSON as JSONObject (is null if pngTileStream is not null)
	 * @param pngTileStream 	PNG tile as ByteArrayOutputStream (is null if tileGeoJson is not null)
	 * @param geography			Uppercase String
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
	 * @param geoLevel 			Uppercase String
	 * @param fileExtension		File extension: .json or .png
	 *
	 * @throws RIFServiceException
	 */	
	public void cacheTile(
		final JSONObject tileGeoJson,
		final ByteArrayOutputStream pngTileStream,
		final String geography,
		final SlippyTile slippyTile, 
		final String geoLevel, 
		final String fileExtension) 
			throws RIFServiceException {
			
		File tmpFile=getCachedTileFile(geography, slippyTile, geoLevel, fileExtension + ".tmp");
		File file=getCachedTileFile(geography, slippyTile, geoLevel, fileExtension);
		if (!file.exists() && !tmpFile.exists()) {
//			rifLogger.debug(getClass(), "Cache temporary tile: " + tmpFile.getAbsolutePath());
			FileOutputStream stream = null;
			try {
				stream = new FileOutputStream(tmpFile);
				if (tileGeoJson == null && pngTileStream == null) {
					throw new RIFServiceException(
						RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR, "Both tileGeoJson and pngTileStream are null");
				}
				else if (tileGeoJson != null && pngTileStream != null) {
					throw new RIFServiceException(
						RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR, "Both tileGeoJson and pngTileStream are not null");
				}
				else if (tileGeoJson == null && pngTileStream != null) {
					stream.write(pngTileStream.toByteArray());
				}
				else if (tileGeoJson != null && pngTileStream == null) {
					stream.write(tileGeoJson.toString().getBytes(StandardCharsets.UTF_8));
				}
			} 
			catch (IOException ioException) {
				try {
					if (tmpFile.exists()) {
						tmpFile.delete();
					}
				}
				catch (SecurityException securityException) {
					throw new RIFServiceException(
						RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
						"Recursive error writing " + tmpFile.toString() + ": " + ioException.getMessage() + 
						"; 2nd error: " + securityException.getMessage(), securityException);
				}
				throw new RIFServiceException(
					RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
					"Error writing " + tmpFile.toString() + ": " + ioException.getMessage(), ioException);
			}
			finally {
				try {
					stream.close();
				} 
				catch (IOException ioException) {
					try {
						if (tmpFile.exists()) {
							tmpFile.delete();
						}
					}
					catch (SecurityException securityException) {
						throw new RIFServiceException(
							RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
							"Recursive error writing " + tmpFile.toString() + ": " + ioException.getMessage() + 
							"; 2nd error: " + securityException.getMessage(), securityException);
					}
					throw new RIFServiceException(
						RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
						"Error writing " + tmpFile.toString() + ": " + ioException.getMessage(), ioException);
				}
			}
			
			if (!tmpFile.exists()) {
				throw new RIFServiceException(
					RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
						"Temporary file: " + tmpFile.toString() + " does not exist");
			}
			else if (tmpFile.length() == 0) {
				throw new RIFServiceException(
					RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
						"Temporary file: " + tmpFile.toString() + " is zero sized");
			}
			
			if (!file.exists()) {
				try {
					rifLogger.info(getClass(), "Cache tile(" + tmpFile.length() + " bytes): " + file.getAbsolutePath());
					tmpFile.renameTo(file);
				}
				catch (Exception exception) {
					try {
						if (!tmpFile.exists()) {
							tmpFile.delete();
						}
					}
					catch (SecurityException securityException) {
						throw new RIFServiceException(
							RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
							"Recursive error renaming: " + " to: " + file.toString() + tmpFile.toString() + ": " + exception.getMessage() + 
							"; 2nd error: " + securityException.getMessage(), securityException);
					}
					throw new RIFServiceException(
						RIFServiceError.TILE_CACHE_FILE_WRITE_ERROR,
						"Error renaming: " + tmpFile.toString() + " to: " + file.toString() + ": " + exception.getMessage());
				}
			}
		}
		else if (tmpFile.exists()) {
			rifLogger.info(getClass(), "Temporary tile(" + tmpFile.length() + " bytes) exists, cannot cache: " + file.getAbsolutePath());
		}
		else if (file.exists()) {
			rifLogger.info(getClass(), "Tile(" + file.length() + " bytes) already cached: " + file.getAbsolutePath());
		}
	}
	
	/**
	 * Get GeoJSON tile.
	 * <p>
	 * Fetch GeoJSON ByteArrayOutputStream tile from: 
	 * EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>/<y>.json
	 * </p>
	 *
	 * @param geography			Uppercase String
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
	 * @param geoLevel 			Uppercase String
	 *
	 * @return String GeoJSON tile
	 *
	 * @throws RIFServiceException
	 */			
	public String getCachedGeoJsonTile(
		final String geography,
		final SlippyTile slippyTile, 
		final String geoLevel) 
			throws RIFServiceException {
			
		JSONObject tileGeoJson = new JSONObject();
		File file=getCachedTileFile(geography, slippyTile, geoLevel, "json");
		if (file.exists()) {
			rifLogger.info(getClass(), "DeoJSON tile (" + file.length() + " bytes) cache hit: " + file.getAbsolutePath());
			byte[] bytes = null;
			try {
				bytes = Files.readAllBytes(file.toPath());
			}
			catch (IOException ioException) {
				throw new RIFServiceException(
					RIFServiceError.TILE_CACHE_FILE_READ_ERROR,
					"Error reading " + file.toString() + ": " + ioException.getMessage(), ioException);
			}
			String result = new String(bytes, StandardCharsets.UTF_8);
			return result;
		}
		
		return null;
	}

	/**
	 * Get PNG tile.
	 * <p>
	 * Fetch PNG ByteArrayOutputStream tile from: 
	 * EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>/<y>.png
	 * </p>
	 *
	 * @param geography			Uppercase String
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
	 * @param geoLevel 			Uppercase String
	 *
	 * @return String GeoJSON tile base64 encoded (to use same code infrastructure as GeoJSON and TopoJSON tiles)
	 *
	 * @throws RIFServiceException
	 */		
	public String getCachedPngTile(
		final String geography,
		final SlippyTile slippyTile, 
		final String geoLevel) 
			throws RIFServiceException {
			
		File file=getCachedTileFile(geography, slippyTile, geoLevel, "png");	
		if (file.exists()) {
			rifLogger.info(getClass(), "PNG tile (" + file.length() + " bytes) cache hit: " + file.getAbsolutePath());
			byte[] bytes = null;
			try {
				bytes = Files.readAllBytes(file.toPath());
			}
			catch (IOException ioException) {
				throw new RIFServiceException(
					RIFServiceError.TILE_CACHE_FILE_READ_ERROR,
					"Error reading " + file.toString() + ": " + ioException.getMessage(), ioException);
			}
			String result=Base64.getEncoder().encodeToString(bytes);
			return result;
		}
		return null;
	}

	/**
	 * Create File object for file in tile cache directory. The file does NOT need to exist.
	 * <p>
	 * The cache directory is: EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>
	 * The file is y.<fileExtension>
	 * </p>
	 *
	 * @param geography			Uppercase String
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
	 * @param geoLevel 			Uppercase String
	 * @param fileExtension		File extension: .json or .png
	 *
	 * @return File object
	 *
	 * @throws RIFServiceException
	 */			
	public File getCachedTileFile(
		final String geography,
		final SlippyTile slippyTile, 
		final String geoLevel,
		String fileExtension) 
			throws RIFServiceException {
			
		Path path = createTileDirectoryPath(geography, slippyTile, geoLevel);
		return new File(path.toString() + File.separator + new Integer(slippyTile.getY()).toString() + "." + fileExtension);
	}

	/**
	 * Create path to tile cache directory. Will create directory if required.
	 * <p>
	 * The cache directory is: EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>
	 * </p>
	 *
	 * @param geography			Uppercase String
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
	 * @param geoLevel 			Uppercase String
	 *
	 * @return Path to tile cache directory: EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x> 
	 *
	 * @throws RIFServiceException
	 */
	private Path createTileDirectoryPath(
		final String geography,
		final SlippyTile slippyTile, 
		final String geoLevel) 
			throws RIFServiceException {

		if (geography == null) { 
			throw new RIFServiceException(
					RIFServiceError.INVALID_PARAMETER,
					"NULL geography specified, unable to create tile cache path");
		}
		if (geoLevel == null) { 
			throw new RIFServiceException(
					RIFServiceError.INVALID_PARAMETER,
					"NULL geoLevel specified, unable to create tile cache path");
		}
		if (slippyTile == null) { 
			throw new RIFServiceException(
					RIFServiceError.INVALID_PARAMETER,
					"NULL slippyTile specified, unable to create tile cache path");
		}
		Path path = FileSystems.getDefault().getPath(EXTRACT_DIRECTORY, "scratchspace").
						resolve("tiles").
						resolve(geography).resolve(geoLevel).
						resolve(new Integer(slippyTile.getZoomlevel()).toString()).
						resolve(new Integer(slippyTile.getX()).toString());
	
		if (!path.toFile().exists()) {
			rifLogger.info(getClass(), "Creating tile cache directory: " + path.toString());
			path.toFile().mkdirs();
		}
				
		return path;
	}		
	
}