package org.sahsu.rif.services.graphics;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.datastorage.common.RIFTilesCache;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.Geometries;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.label.LabelCacheImpl;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Create PNG tiles from geoJSON
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class RIFPdfTiles {
	
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static final String lineSeparator = System.getProperty("line.separator");

	private static RIFTilesCache rifTilesCache = null;
	
	/**
	 * Constructor
	 *
	 * @param options RIFServiceStartupOptions for the extract directory
	 *
	 * @throws NullPointerException on constructor failure
	 */
	public RIFPdfTiles(final RIFServiceStartupOptions options) {

		try {
			rifTilesCache = new RIFTilesCache(options);
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFTiles() constructor");
			throw new NullPointerException();
		}
	}

	/** 
	 * Create NULL PNG tile
	 *
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
	 *
	 * @return PNG coded in base64, or null if not relevant (non 0/0/0 null tile)
	 *
	 * @throws RIFServiceException RIF error
     */		
	private String createNullTile(
		final SlippyTile slippyTile,
		final String geography,
		final String geoLevel) throws RIFServiceException {
		String result;
		try {
			int w = 256;
			int h = 256;
			BufferedImage bufferedImage = new BufferedImage(w, h, 
				BufferedImage.TYPE_INT_ARGB); // Allow transparency [will work for PNG as well!]
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			Graphics2D g2d = bufferedImage.createGraphics();
			
			g2d.setColor(new Color(0, 0, 0, 0)); // Transparent
			g2d.fillRect(0, 0, w, h);
			ImageIO.write(bufferedImage, "png", os);

			rifTilesCache.cacheTile(null /* tileGeoJson */, os, "NULL", slippyTile, "NULL", "png");
			result=Base64.getEncoder().encodeToString(os.toByteArray());
		
			rifTilesCache.cacheTile(null /* tileGeoJson */, os, geography, slippyTile, geoLevel, "png");	
			g2d.dispose();
			
			return result;		
		}
		catch (Exception exception) {
			try { // Write failing JSON to cache
				File file=rifTilesCache.getCachedTileFile("NULL", slippyTile, "NULL", "json");	
				if (file.exists()) {
					file.delete();
				}
			}
			catch (Exception cacheException) {
				rifLogger.error(getClass(), "Failed to cache failing GeoJSON for NULL tile" +
					"; slippyTile: " + slippyTile.getPathFileName() + " [Ignored]", cacheException);
			}
			throw new RIFServiceException(
				RIFServiceError.TILE_GENERATE_GEOTOOLS_ERROR,
				"NULL tile generation error: " + exception.getMessage(), exception);
		}
	}
	
	/** 
	 * Convert geoJSON to transparent PNG tile, cropped to BBOX
	 * <p>
	 * https://gis.stackexchange.com/questions/245875/convert-geojson-to-png
	 * </p>
	 *
	 * @param tileGeoJson		JSONObject of tile GeoJSON
	 * @param geography			geography as uppercase String
	 * @param slippyTile 		SlippyTile (zoomlevel, x, y)
	 * @param geoLevel 			geolevel as uppercase String
	 *
	 * @return PNG coded in base64, or null if not relevant (non 0/0/0 null tile)
	 *
	 * @throws RIFServiceException RIF error
	 * @throws JSONException Error manipulating JSON
     */	
	public String geoJson2png(
		final JSONObject tileGeoJson,
		final String geography,
		final SlippyTile slippyTile, 
		final String geoLevel) throws JSONException, RIFServiceException {

		String result;
		try {
			JSONArray bboxJson = slippyTile.tile2boundingBox();
			FeatureJSON featureJSON = new FeatureJSON();
			InputStream is = new ByteArrayInputStream(tileGeoJson.toString().getBytes());
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			// Convert GeoJSON toFeatureCollection 	
			FeatureCollection features = featureJSON.readFeatureCollection(is);
			if (features.size() == 0) {
				if (!(geography.equals("NULL") || geoLevel.equals("NULL"))) {
					String tileGeoJsonStr = tileGeoJson.toString();
					if (tileGeoJsonStr.length() > 300 ) {
						tileGeoJsonStr = tileGeoJsonStr.substring(0, 300);
					}
					throw new JSONException("No features for tileGeoJson: " + tileGeoJsonStr +
						"; geography: " + geography +
						"; geoLevel: " + geoLevel);
				}
				else {
					return createNullTile(slippyTile, "NULL", "NULL");
				}
			}
			// Style 
			MapContent mapContent = new MapContent();
			mapContent.setTitle(geoLevel + slippyTile.getPathFileName() + ".png");
		
			int w = 256;
			int h = 256;
			// bboxJson is: [minX, minY, maxX, maxY] 
			ReferencedEnvelope bounds = new ReferencedEnvelope(
					bboxJson.getDouble(0) /* xMin: West */,
					bboxJson.getDouble(2) /* xMax: East */,
					bboxJson.getDouble(1) /* yMin: South */,
					bboxJson.getDouble(3) /* yMax: North */,
					DefaultGeographicCRS.WGS84
				);	
				
			// Intersect featureCollection with bounding box
			Polygon mask = JTS.toGeometry(bounds);
			FeatureIterator iterator=features.features();
			boolean intersects=false;
			try {
				while (iterator.hasNext()) {
					Feature feature = iterator.next();
					Geometry geometry = (Geometry)feature.getDefaultGeometryProperty().getValue();
					Geometries geomType = Geometries.get(geometry);
					switch (geomType) {
						case POLYGON: 
							Polygon polygon = (Polygon)geometry;
							if (polygon != null &&
								polygon.isValid() && 
								polygon.intersects(mask)) {
								intersects=true;
							}
							break;
						case MULTIPOLYGON:
							MultiPolygon multipolygon=(MultiPolygon)geometry; 
							if (multipolygon != null &&
								multipolygon.isValid() && 
								multipolygon.intersects(mask)) {
								intersects=true;
							}
							break;
					default: // Anything else should already have been removed by RifWellKnownText.createGeometryFromWkt()
						rifLogger.info(getClass(), "Unsupported Geometry: \"" + geomType.toString() + 
							"\" for geoLevel: " + geoLevel +
							"; slippyTile: " + slippyTile.getPathFileName() +
							"; tileGeoJson " + tileGeoJson.toString());
						throw new RIFServiceException(RIFServiceError.TILE_GENERATE_GEOTOOLS_ERROR, 
							"Unsupported Geometry: \"" + geomType.toString() + 
							"\" for geoLevel: " + geoLevel +
							"; slippyTile: " + slippyTile.getPathFileName());
					}
					if (intersects) {
						break;
					}
				}
			}
			finally {
				iterator.close();
			}
			
			if (!intersects) {
				return createNullTile(slippyTile, geography.toLowerCase(), geoLevel.toLowerCase()); // And cache
			}

			MapViewport mapViewport = mapContent.getViewport();
	//		mapViewport.setMatchingAspectRatio(true);
			mapViewport.setScreenArea(new Rectangle(Math.round(w), Math.round(h)));
			mapViewport.setBounds(bounds);
			mapContent.setViewport(mapViewport);
			
			if (!features.isEmpty()) {
				Style style = SLD.createSimpleStyle(features.getSchema());
				Layer layer = new FeatureLayer(features, style);
				mapContent.addLayer(layer);
			}
			
			BufferedImage bufferedImage = new BufferedImage(w, h, 
				BufferedImage.TYPE_INT_ARGB); // Allow transparency [will work for PNG as well!]
			Graphics2D g2d = bufferedImage.createGraphics();
			
			Rectangle outputArea = new Rectangle(0, 0, w, h);
			GTRenderer renderer = new StreamingRenderer();
			LabelCacheImpl labelCache = new LabelCacheImpl();
			Map<Object, Object> hints = renderer.getRendererHints();
			if (hints == null) {
				hints = new HashMap<>();
			}
			hints.put(StreamingRenderer.LABEL_CACHE_KEY, labelCache);
			renderer.setRendererHints(hints);
			renderer.setMapContent(mapContent);

			renderer.paint(g2d, outputArea, bounds); 
			ImageIO.write(bufferedImage, "png", os);

			rifTilesCache.cacheTile(null /* tileGeoJson */, os, geography.toLowerCase(), slippyTile, geoLevel.toLowerCase(), "png");
			result=Base64.getEncoder().encodeToString(os.toByteArray());
			
			mapContent.dispose();
			g2d.dispose();
		}
		catch (Exception exception) {
			try { // Write failing JSON to cache
				File file=rifTilesCache.getCachedTileFile(geography.toLowerCase(), slippyTile, geoLevel.toLowerCase(), "json");	
				if (file.exists()) {
					if (file.delete()) {
						rifTilesCache.cacheTile(tileGeoJson, null /* pngTileStream */, geography.toLowerCase(), slippyTile, geoLevel.toLowerCase(), 	"json");
					}
				}
			}
			catch (Exception cacheException) {
				rifLogger.error(getClass(), "Failed to cache failing GeoJSON for geography: " + geography +
					"; geoLevel: " + geoLevel +
					"; slippyTile: " + slippyTile.getPathFileName() + " [Ignored]", cacheException);
			}
			String tileGeoJsonStr=tileGeoJson.toString(2);
			if (tileGeoJsonStr.length() > 600) {
				tileGeoJsonStr=tileGeoJsonStr.substring(1, 600);
			}
			throw new RIFServiceException(
				RIFServiceError.TILE_GENERATE_GEOTOOLS_ERROR,
				"Tile generation error: " + exception.getMessage() + lineSeparator +
				"GeoJSON: " + tileGeoJsonStr, exception);
		}
		
		return result;
	}

}