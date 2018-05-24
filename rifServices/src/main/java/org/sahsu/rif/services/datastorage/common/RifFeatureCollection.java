package org.sahsu.rif.services.datastorage.common;

import org.sahsu.rif.generic.util.RIFLogger;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CoordinateSystem;	 

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import java.lang.*;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Peter Hambly
 * @author phambly
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public class RifFeatureCollection {
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private static RifCoordinateReferenceSystem rifCoordinateReferenceSystem = null;
		
	private DefaultFeatureCollection featureCollection=null;
	private DefaultFeatureCollection backgroundAreasFeatureCollection=null;
	private CoordinateReferenceSystem crs=null;
	private ReferencedEnvelope expandedEnvelope=null;
	private ReferencedEnvelope initialEnvelope=null;
	
	private double gridSquareWidth=-1;
	private double gridVertexSpacing=-1;
	private String gridScale=null;
	private String gridUnits=null;
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================
	/**
     * Constructor.
	 *
	 * @param DefaultFeatureCollection featureCollection, 
	 * @param DefaultFeatureCollection backgroundAreasFeatureCollection, 
	 * @param CoordinateReferenceSystem crs
	 */
	public RifFeatureCollection(
			DefaultFeatureCollection featureCollection,
			DefaultFeatureCollection backgroundAreasFeatureCollection,
			CoordinateReferenceSystem crs) {
		this.rifCoordinateReferenceSystem = new RifCoordinateReferenceSystem();
		
		this.featureCollection=featureCollection;
		this.backgroundAreasFeatureCollection=backgroundAreasFeatureCollection;
		this.crs=crs;		
	}
	
	/**
	 * Setup Rif Feature Collection
	 *
	 * Set: gridUnits, gridScale, initialEnvelope [from featureCollection], gridVertexSpacing, gridSquareWidth, 
	 *      expanded (map + legend extent) envelope to enlarged, rounded up gridSquareWidth grid
     */
	 public void SetupRifFeatureCollection()
			throws Exception {
			
		CoordinateSystem coordinateSystem=crs.getCoordinateSystem();
		if (coordinateSystem == null) {
			throw new Exception("Unable to get coordinateSystem for CRS: " + CRS.toSRS(crs));
		}
		CoordinateSystemAxis coordinateSystemAxis=coordinateSystem.getAxis(1);
		if (coordinateSystemAxis == null) {
			throw new Exception("Unable to get coordinateSystemAxis for CRS: " + CRS.toSRS(crs) +
				"; WKT: " + coordinateSystem.toWKT());
		}
		this.gridUnits=coordinateSystemAxis.getUnit().toString();
		
		// Set initialEnvelope from featureCollection
		this.initialEnvelope=featureCollection.getBounds(); // In rif40GeographiesCRS
		ReferencedEnvelope nexpandedEnvelope=rifCoordinateReferenceSystem.expandMapBounds(
				this.initialEnvelope.transform(DefaultGeographicCRS.WGS84, true /* Be lenient */),
				1.03 	/* otherExpansion */);
		if (nexpandedEnvelope != null) {
			this.expandedEnvelope=nexpandedEnvelope.transform(this.crs, true /* Be lenient */); 
				// Transform back to rif40GeographiesCRS
		}
		else {
			throw new Exception("rifCoordinateReferenceSystem.expandMapBounds return NULL ReferencedEnvelope");
		}
		
		if (this.gridUnits == null) {
			this.gridUnits="km";
		}
		else if (this.gridUnits.equals("m")) { // Shift Metres to KM if possible
			this.gridUnits="km";
		}
		
		// Set grid X/Y spacing from intial (map extent) envelope
		double xLength=initialEnvelope.getMaximum(0) - initialEnvelope.getMinimum(0); 
		double yLength=initialEnvelope.getMaximum(1) - initialEnvelope.getMinimum(1);
		// Scale appropriately
		if (xLength > 2000000.0 || yLength > 2000000.0) {
			this.gridVertexSpacing=1000000.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=1000000.0;		
			if (this.gridUnits.equals("km")) {
				this.gridScale="1000" + this.gridUnits;		
			}
			else {
				this.gridScale="1000000" + this.gridUnits;
			}
		}
		else if (xLength > 1000000.0 || yLength > 1000000.0) {
			this.gridVertexSpacing=500000.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=50000.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="500" + this.gridUnits;
			}
			else {
				this.gridScale="500000" + this.gridUnits;
			}				
		}
		else if (xLength > 200000.0 || yLength > 200000.0) {
			this.gridVertexSpacing=100000.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=100000.0;		
			if (this.gridUnits.equals("km")) {
				this.gridScale="100" + this.gridUnits;		
			}
			else {
				this.gridScale="100000" + this.gridUnits;
			}
		}
		else if (xLength > 100000.0 || yLength > 100000.0) {
			this.gridVertexSpacing=50000.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=50000.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="50" + this.gridUnits;
			}
			else {
				this.gridScale="50000" + this.gridUnits;
			}				
		}	
		else if (xLength > 20000.0 || yLength > 20000.0) {
			this.gridVertexSpacing=10000.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=10000.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="10" + this.gridUnits;		
			}
			else {
				this.gridScale="10000" + this.gridUnits;
			}	
		}	
		else if (xLength > 10000.0 || yLength > 10000.0) {
			this.gridVertexSpacing=5000.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=5000.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="5" + this.gridUnits;
			}
			else {
				this.gridScale="5000" + this.gridUnits;
			}				
		}	
		else if (xLength > 2000.0 || yLength > 2000.0) {
			this.gridVertexSpacing=1000.0; 		// In rif40GeographiesCRS units
			this.gridSquareWidth=1000.0;		
			if (this.gridUnits.equals("km")) {
				this.gridScale="1" + this.gridUnits;		
			}
			else {
				this.gridScale="1000" + this.gridUnits;
			}
		}	
		else if (xLength > 1000.0 || yLength > 1000.0) {
			this.gridVertexSpacing=500.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=500.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="500m";			
				this.gridUnits=	"m";
			}
			else {
				this.gridScale="500" + this.gridUnits;
			}				
		}	
		else if (xLength > 200.0 || yLength > 200.0) {
			this.gridVertexSpacing=100.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=100.0;		
			if (this.gridUnits.equals("km")) {
				this.gridScale="0.1" + this.gridUnits;		
			}
			else {
				this.gridScale="100" + this.gridUnits;
			}	
		}
		else if (xLength > 100.0 || yLength > 100.0) {
			this.gridVertexSpacing=50.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=50.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="50m";			
				this.gridUnits=	"m";
			}
			else {
				this.gridScale="50" + this.gridUnits;
			}				
		}		
		else if (xLength > 20.0 || yLength > 20.0) {
			this.gridVertexSpacing=10.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=10.0;		
			if (this.gridUnits.equals("km")) {
				this.gridScale="10m";	
				this.gridUnits=	"m";			
			}
			else {
				this.gridScale="10" + this.gridUnits;
			}		
		}	
		else if (xLength > 10.0 || yLength > 10.0) {
			this.gridVertexSpacing=5.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=5.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="5m";			
				this.gridUnits=	"m";
			}
			else {
				this.gridScale="5" + this.gridUnits;
			}				
		}		
		else if (xLength > 2.0 || yLength > 2.0) {
			this.gridVertexSpacing=1.0; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=1.0;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="1m";			
				this.gridUnits=	"m";
			}
			else {
				this.gridScale="1" + this.gridUnits;
			}				
		}	
		else if (xLength > 1.0 || yLength > 1.0) {
			this.gridVertexSpacing=0.5; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=0.5;	
			if (this.gridUnits.equals("km")) {
				this.gridScale="0.5m";			
				this.gridUnits=	"m";
			}
			else {
				this.gridScale="0.5" + this.gridUnits;
			}				
		}		
		else if (xLength > 0.20 || yLength > 0.20) {
			this.gridVertexSpacing=0.1; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=0.1;		
			if (this.gridUnits.equals("km")) {
				this.gridScale="0.1m";		
				this.gridUnits=	"m";	
			}
			else {
				this.gridScale="0.1" + this.gridUnits;
			}					
		}	
		else {
			this.gridVertexSpacing=0.01; 	// In rif40GeographiesCRS units
			this.gridSquareWidth=0.01;		
			if (this.gridUnits.equals("km")) {
				this.gridScale="0.01m";		
				this.gridUnits=	"m";	
			}
			else {
				this.gridScale="0.01" + this.gridUnits;
			}							
		}
		
		// Set grid squares size to integer grid squares from expanded (map + legend extent) envelope
		// In rif40GeographiesCRS units
		double xMin=(double)((int)(this.expandedEnvelope.getMinimum(0)/gridSquareWidth))*gridSquareWidth;
		double xMax=(double)((int)(this.expandedEnvelope.getMaximum(0)/gridSquareWidth))*gridSquareWidth; 
		double yMin=(double)((int)(this.expandedEnvelope.getMinimum(1)/gridVertexSpacing))*gridVertexSpacing;
		double yMax=(double)((int)(this.expandedEnvelope.getMaximum(1)/gridVertexSpacing))*gridVertexSpacing; 		
		ReferencedEnvelope gridEnvelope = new ReferencedEnvelope(
					xMin /* bounds.getWestBoundLongitude() */,
					xMax /* bounds.getEastBoundLongitude() */,
					yMin /* bounds.getSouthBoundLatitude() */,
					yMax /* bounds.getNorthBoundLatitude() */,
					this.expandedEnvelope.getCoordinateReferenceSystem());
					
		double xMinRemainder=this.expandedEnvelope.getMinimum(0)%gridSquareWidth;	
		double xMaxRemainder=this.expandedEnvelope.getMaximum(0)%gridSquareWidth;	
		double yMinRemainder=this.expandedEnvelope.getMinimum(1)%gridVertexSpacing;	
		double yMaxRemainder=this.expandedEnvelope.getMaximum(1)%gridVertexSpacing;		
		if (xMinRemainder < 0.0) { // Has been rounded down
			xMin-=gridSquareWidth;
		}	
		if (xMaxRemainder > 0.0) { // Has been rounded down
			xMax+=gridSquareWidth;
		}	
		if (yMinRemainder < 0.0) { // Has been rounded down
			yMin-=gridVertexSpacing;
		}	
		if (yMaxRemainder > 0.0) { // Has been rounded down
			yMax+=gridVertexSpacing;
		}	
		ReferencedEnvelope finalGridEnvelope = new ReferencedEnvelope( // After rounding
					xMin /* bounds.getWestBoundLongitude() */,
					xMax /* bounds.getEastBoundLongitude() */,
					yMin /* bounds.getSouthBoundLatitude() */,
					yMax /* bounds.getNorthBoundLatitude() */,
					this.expandedEnvelope.getCoordinateReferenceSystem());

		rifLogger.info(this.getClass(), 
			"Setup RifFeatureCollection units: " + gridUnits + "; scale: " + gridScale + lineSeparator +
			"initialEnvelope(SRID): " + initialEnvelope.toString() + lineSeparator +
			"nexpandedEnvelope(WGS84): " + nexpandedEnvelope.toString() + lineSeparator +
			"expandedEnvelope(SRID): " + expandedEnvelope.toString() + lineSeparator +
			"gridEnvelope(SRID): " + gridEnvelope.toString() + lineSeparator +
			"xMin: " + xMin +
			"; xMax: " + xMax +
			"; yMin: " + yMin +
			"; yMax: " + yMax + lineSeparator +
			"xMinRemainder: " + xMinRemainder +
			"; xMaxRemainder: " + xMaxRemainder +
			"; yMinRemainder: " + yMinRemainder +
			"; yMaxRemainder: " + yMaxRemainder + lineSeparator +
			"finalGridEnvelope(SRID): " + finalGridEnvelope.toString() + lineSeparator +
			"gridVertexSpacing(SRID): " + gridVertexSpacing + "; gridSquareWidth: " + gridSquareWidth);
			
		// Set expanded (map + legend extent) envelope to enlarged, rounded up gridSquareWidth grid
		if (finalGridEnvelope != null) {
			this.expandedEnvelope=finalGridEnvelope.transform(this.crs, true /* Be lenient */); 
				// Transform back to rif40GeographiesCRS
		}
		else {
			throw new Exception("gridEnvelope create return NULL ReferencedEnvelope");
		}
				
		if (backgroundAreasFeatureCollection != null) {				
			rifLogger.info(this.getClass(), "Crop backgroundAreas feature set to expandedEnvelope, size: " + 
					backgroundAreasFeatureCollection.size());
			Geometry expandedEnvelopeGeometry=(Geometry)createPolygonFromBBox(xMin, yMin, xMax-xMin, yMax-yMin);
			FeatureIterator<SimpleFeature> iterator = backgroundAreasFeatureCollection.features();
			DefaultFeatureCollection newBackgroundAreasFeatureCollection = new DefaultFeatureCollection(
				backgroundAreasFeatureCollection.getID(), 
				backgroundAreasFeatureCollection.getSchema());
			int removes=0;
			try {
				int k=0;
				while (iterator.hasNext()) {
					k++;
					SimpleFeature feature = iterator.next();
					Geometry geometry = (Geometry) feature.getAttribute(0);
					if (geometry == null) {
						rifLogger.warning(this.getClass(), "No geometry for backgroundAreasFeature: " + k);
					}
					else if (geometry.within(expandedEnvelopeGeometry)) { // geometry is completely within expandedEnvelopeGeometry
																		 // (no touching edges)
						newBackgroundAreasFeatureCollection.add(feature);
					}
					else { // Remove
						String areaId=(String)feature.getAttribute("AREAID");
						String areaName=(String)feature.getAttribute("AREANAME");
						rifLogger.info(this.getClass(), "Remove backgroundAreasFeature: " + k + "; areaId:" + areaId + "; areaName: " + areaName);
						removes++;
					}
				}
			}
			finally {
				 iterator.close();
				 if (removes > 0) {
					rifLogger.info(this.getClass(), "Removed " + removes + " features from backgroundAreasFeatureCollection");
					backgroundAreasFeatureCollection=newBackgroundAreasFeatureCollection;
				 }
			}
		}
		else {
			rifLogger.info(this.getClass(), "No backgroundAreas feature collection to Crop");
		}
	}

	/**
     * create Polygon from bounding box
	 *
	 * @param double xMin,
	 * @param double yMin,
	 * @param double width,
	 * @param double height,
	 *
	 * @returns Polygon
	 */		
	private Polygon createPolygonFromBBox(double xMin, double yMin, double width, double height) {
		GeometryFactory factory=JTSFactoryFinder.getGeometryFactory(null);
		
		Coordinate[] coordinates=new Coordinate[5];
		coordinates[0]=new Coordinate(xMin, yMin);
		coordinates[1]=new Coordinate(xMin + width, yMin);
		coordinates[2]=new Coordinate(xMin + width, yMin + height);
		coordinates[3]=new Coordinate(xMin, yMin + height);
		coordinates[4]=new Coordinate(xMin, yMin);
		LinearRing lr=factory.createLinearRing(coordinates);
		Polygon polygon=factory.createPolygon(lr,new LinearRing[]{});
		
		return polygon;
	}

	/**
     * Get feature collection
	 *
	 * @returns DefaultFeatureCollection
	 */	
	public DefaultFeatureCollection getFeatureCollection() {
		return featureCollection;
	}
	
	/**
     * Get background Areas Feature collection
	 *
	 * @returns DefaultFeatureCollection
	 */	
	public DefaultFeatureCollection getBackgroundAreasFeatureCollection() {
		return backgroundAreasFeatureCollection;
	}
	
	/**
     * Get Coordinate Reference System of SRID
	 *
	 * @returns CoordinateReferenceSystem
	 */		
	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return crs;
	}
	
	/**
     * Get map expended SRID envelope
	 *
	 * @returns ReferencedEnvelope
	 */		
	public ReferencedEnvelope getExpandedEnvelope() 
			throws Exception {
								
		if (expandedEnvelope == null) {
			throw new Exception("expandedEnvelope is null");
		}
		else if (!CRS.toSRS(expandedEnvelope.getCoordinateReferenceSystem()).equals(CRS.toSRS(crs))) {
			throw new Exception("SRID expandedEnvelope: " + CRS.toSRS(expandedEnvelope.getCoordinateReferenceSystem()) + 
				" is not in SRID CRS: " + CRS.toSRS(crs));
		}
		return expandedEnvelope;
	}	

	/**
     * Get map initial SRID envelope
	 *
	 * @returns ReferencedEnvelope
	 */		
	public ReferencedEnvelope getInitialEnvelope() 
			throws Exception {
				
		if (initialEnvelope == null) {
			throw new Exception("initialEnvelope is null");
		}				
		else if (!CRS.toSRS(initialEnvelope.getCoordinateReferenceSystem()).equals(CRS.toSRS(crs))) {
			throw new Exception("SRID initialEnvelope: " + CRS.toSRS(initialEnvelope.getCoordinateReferenceSystem()) + 
				" is not in SRID CRS: " + CRS.toSRS(crs));
		}
		return initialEnvelope;
	}
	
	/**
     * Get map grid square width
	 *
	 * @returns double
	 */		
	public double getGridSquareWidth() {
		return gridSquareWidth;
	}	

	/**
     * Get map grid vertex spacing
	 *
	 * @returns double
	 */		
	public double getGridVertexSpacing() {
		return gridVertexSpacing;
	}
	
	/**
     * Get grid units: e.g. km
	 *
	 * @returns String
	 */
	public String getGridUnits() {
		return gridUnits;
	}
	
	/**
     * Get grid scale: e.g. 10km
	 *
	 * @returns String
	 */
	public String getGridScale() {
		return gridScale;
	}
}
