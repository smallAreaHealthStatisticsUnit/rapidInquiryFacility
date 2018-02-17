package rifServices.dataStorageLayer.common;

import rifGenericLibrary.util.RIFLogger;
import rifServices.dataStorageLayer.common.RifCoordinateReferenceSystem;
	
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;

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
	private CoordinateReferenceSystem crs=null;
	private ReferencedEnvelope expandedEnvelope=null;
	private ReferencedEnvelope wgs84Envelope=null;
	private ReferencedEnvelope initialEnvelope=null;
	
	private double gridSquareWidth=-1;
	private double gridVertexSpacing=-1;
	
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
	 * @param CoordinateReferenceSystem crs
	 */
	public RifFeatureCollection(
			DefaultFeatureCollection featureCollection,
			CoordinateReferenceSystem crs) {
		this.rifCoordinateReferenceSystem = new RifCoordinateReferenceSystem();
		
		this.featureCollection=featureCollection;
		this.crs=crs;		
	}
	
	/**
	 * Setup Rif Feature Collection
	 *
     */
	public void SetupRifFeatureCollection()
			throws Exception {
			
		this.initialEnvelope=featureCollection.getBounds(); // In rif40GeographiesCRS
		ReferencedEnvelope nexpandedEnvelope=rifCoordinateReferenceSystem.expandMapBounds(
				this.initialEnvelope, 
				1.3 /* xMinExpansion */,
				0.0 /* otherExpansion */);
//		ReferencedEnvelope nexpandedEnvelope=rifCoordinateReferenceSystem.expandMapBounds(
//				this.initialEnvelope.transform(DefaultGeographicCRS.WGS84, true /* Be lenient */));
		if (nexpandedEnvelope != null) {
//			this.expandedEnvelope=nexpandedEnvelope.transform(this.crs, true /* Be lenient */); 
			this.expandedEnvelope=nexpandedEnvelope; 
				// In rif40GeographiesCRS
		}
		else {
			throw new Exception("rifCoordinateReferenceSystem.expandMapBounds return NULL ReferencedEnvelope");
		}
//		this.wgs84Envelope=nexpandedEnvelope;
		this.wgs84Envelope=nexpandedEnvelope.transform(DefaultGeographicCRS.WGS84, true /* Be lenient */);
		
		double xLength=initialEnvelope.getMaximum(0) - initialEnvelope.getMinimum(0); 
		double yLength=initialEnvelope.getMaximum(1) - initialEnvelope.getMinimum(1);
		
		if (xLength > 45.0) {
			this.gridVertexSpacing=10.0; 	// In WGS84 degrees
			this.gridSquareWidth=10.0;		// In WGS84 degrees
		}
		else if (xLength > 4.5) {
			this.gridVertexSpacing=1.0; 	// In WGS84 degrees
			this.gridSquareWidth=1.0;		// In WGS84 degrees
		}	
		else if (xLength > 0.45) {
			this.gridVertexSpacing=0.1; 	// In WGS84 degrees
			this.gridSquareWidth=0.1;		// In WGS84 degrees			
		}	
		else {
			this.gridVertexSpacing=0.01; 	// In WGS84 degrees
			this.gridSquareWidth=0.01;		// In WGS84 degrees					
		}
		
/*
rifCoordinateReferenceSystem.expandMapBounds();

Expand map bounds bbox: [-8.862089573194751,52.66337037331786 -4.83726118921235,55.56628681071138]
initialEnvelope: ReferencedEnvelope[-7.933283023044967 : -4.83726118921235, 52.66337037331786 : 55.56628681071138]
expandedEnvelope: ReferencedEnvelope[-8.862089573194751 : -4.83726118921235, 52.66337037331786 : 55.56628681071138]
finalEnvelope: ReferencedEnvelope[-8.9 : -4.800000000000001, 52.6 : 55.6]
gridSize: 0.1 

11:42:34.091 [http-nio-8080-exec-114] ERROR rifGenericLibrary.util.RIFLogger : [rifServices.dataStorageLayer.common.RifFeatureCollection]:
Setup RifFeatureCollection initialEnvelope: ReferencedEnvelope[25138.06641232228 : 208645.63128912938, 321745.13526015036 : 634476.4710041389]
nexpandedEnvelope: ReferencedEnvelope[-8.9 : -4.800000000000001, 52.6 : 55.6]
expandedEnvelope: ReferencedEnvelope[-66948.73732961965 : 223689.2246765948, 303928.40606039204 : 655669.3987442616]
wgs84Envelope: ReferencedEnvelope[-8.9 : -4.800000000000001, 52.6 : 55.6]
gridVertexSpacing: 10.0; gridSquareWidth: 10.0

11:42:57.355 [http-nio-8080-exec-114] INFO  rifGenericLibrary.util.RIFLogger : [rifServices.graphics.RIFMaps]:
Create map 935x1132; areas: 3690; file: c:\rifDemo\scratchSpace\d301-400\s367\maps\smoothed_smr_367.svg
bounding box: ReferencedEnvelope[-66948.73732961965 : 223689.2246765948, 303928.40606039204 : 655669.3987442616]; CRS: EPSG:27700
screenArea: java.awt.Rectangle[x=0,y=0,width=935,height=1132]
Layer[1]: Smoothed SMR; bounds: ReferencedEnvelope[25138.06641232228 : 208645.63128912938, 321745.13526015036 : 634476.4710041389]
Layer[2]: Legend; bounds: NONE
*/			
		rifLogger.error(this.getClass(), 
			"Setup RifFeatureCollection initialEnvelope: " + initialEnvelope.toString() + lineSeparator +
			"nexpandedEnvelope: " + nexpandedEnvelope.toString() + lineSeparator +
			"expandedEnvelope: " + expandedEnvelope.toString() + lineSeparator +
			"wgs84Envelope: " + wgs84Envelope.toString() + lineSeparator +
			"gridVertexSpacing: " + gridVertexSpacing + "; gridSquareWidth: " + gridSquareWidth);

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
//		else if (!CRS.toSRS(expandedEnvelope.getCoordinateReferenceSystem()).equals(CRS.toSRS(crs))) {
//			throw new Exception("SRID expandedEnvelope: " + CRS.toSRS(expandedEnvelope.getCoordinateReferenceSystem()) + 
//				" is not in SRID CRS: " + CRS.toSRS(crs));
//		}
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
//		else if (!CRS.toSRS(initialEnvelope.getCoordinateReferenceSystem()).equals(CRS.toSRS(crs))) {
//			throw new Exception("SRID initialEnvelope: " + CRS.toSRS(initialEnvelope.getCoordinateReferenceSystem()) + 
//				" is not in SRID CRS: " + CRS.toSRS(crs));
//		}
		return initialEnvelope;
	}

	/**
     * Get map expended WGS84 envelope
	 *
	 * @returns ReferencedEnvelope
	 */		
	public ReferencedEnvelope getWgs84Envelope() 
			throws Exception {
				
		if (wgs84Envelope == null) {
			throw new Exception("wgs84Envelope is null");
		}
		else if (!CRS.toSRS(wgs84Envelope.getCoordinateReferenceSystem()).
				equals(CRS.toSRS(DefaultGeographicCRS.WGS84))) {
			throw new Exception("wgs84Envelope is not in WGS84: " + 
				CRS.toSRS(wgs84Envelope.getCoordinateReferenceSystem()) +
				"; expecting: " + CRS.toSRS(DefaultGeographicCRS.WGS84));
		}
		return wgs84Envelope;
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

}