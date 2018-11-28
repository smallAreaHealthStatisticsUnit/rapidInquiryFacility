package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;

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

public class RIFTilesCache {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static String EXTRACT_DIRECTORY;

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
	
	/*
	 * Function: 	cacheTile()
	 * Description: Cache GeoJSON/PNG tile. Write to .json.tmp and then rename to ensure atomic
	 * Returns:     Write GeoJSON ByteArrayOutputStream to tile: 
	 *					EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>/<y>.json
	 */	
	public void cacheTile(
		final JSONObject tileGeoJson,
		ByteArrayOutputStream pngTileStream,
		final String geography,
		final Integer zoomlevel, 
		final String geoLevel, 
		final Integer x, 
		final Integer y,
		final String fileExtension) 
			throws RIFServiceException {
			
		File tmpFile=getCachedTileFile(geography, zoomlevel, geoLevel, x, y, fileExtension + ".tmp");
		File file=getCachedTileFile(geography, zoomlevel, geoLevel, x, y, fileExtension);
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
	
	
	/*
	 * Function: 	getCachedGeoJsonTile()
	 * Description:	Get GeoJSON tile
	 * Returns:     NULL or GeoJSON file as string read from the tile cache directory: 
	 *					EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>/<y>.json
	 */		
	public String getCachedGeoJsonTile(
		final String geography,
		final Integer zoomlevel, 
		final String geoLevel, 
		final Integer x, 
		final Integer y) 
			throws RIFServiceException {
			
		JSONObject tileGeoJson = new JSONObject();
		File file=getCachedTileFile(geography, zoomlevel, geoLevel, x, y, "json");
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

	/*
	 * Function: 	getCachedPngTile()
	 * Description:	Get PNG tile
	 * Returns:     NULL or PNG file as Base64 encoded string read from the tile cache directory: 
	 *					EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>/<y>.png
	 */		
	public String getCachedPngTile(
		final String geography,
		final Integer zoomlevel, 
		final String geoLevel, 
		final Integer x, 
		final Integer y) 
			throws RIFServiceException {
			
		File file=getCachedTileFile(geography, zoomlevel, geoLevel, x, y, "png");	
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
		
	/*
	 * Function: 	getCachedTileFile()
	 * Description:	Create File object for file in tile cache directory. File does NOT need to exist
	 * Returns:     File object for file in tile cache directory: 
	 *					EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x>/<y>.<fileExtension>
	 */		
	public File getCachedTileFile(
		final String geography,
		final Integer zoomlevel, 
		final String geoLevel, 
		final Integer x, 
		final Integer y,
		String fileExtension) 
			throws RIFServiceException {
			
		Path path = createTileDirectoryPath(geography, zoomlevel, geoLevel, x);
		return new File(path.toString() + File.separator + y.toString() + "." + fileExtension);
	}
	
	/*
	 * Function: 	createTileDirectoryPath()
	 * Description:	Create path to tile cache directory. Will create directory if required
	 * Returns:     Path to tile cache directory: EXTRACT_DIRECTORY/scratchspace/tiles/<geography>/<geolevel>/<zoomlevel>/<x> 
	 */
	private Path createTileDirectoryPath(
		final String geography,
		final Integer zoomlevel, 
		final String geoLevel, 
		final Integer x) 
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
		if (zoomlevel == null) { 
			throw new RIFServiceException(
					RIFServiceError.INVALID_PARAMETER,
					"NULL zoomlevel specified, unable to create tile cache path");
		}	
		if (x == null) { 
			throw new RIFServiceException(
					RIFServiceError.INVALID_PARAMETER,
					"NULL x specified, unable to create tile cache path");
		}		

		Path path = FileSystems.getDefault().getPath(EXTRACT_DIRECTORY, "scratchspace").
						resolve("tiles").resolve(geography).resolve(geoLevel).resolve(zoomlevel.toString()).resolve(x.toString());
	
		if (!path.toFile().exists()) {
			rifLogger.info(getClass(), "Creating tile cache directory: " + path.toString());
			path.toFile().mkdirs();
		}
				
		return path;
	}		
	
}