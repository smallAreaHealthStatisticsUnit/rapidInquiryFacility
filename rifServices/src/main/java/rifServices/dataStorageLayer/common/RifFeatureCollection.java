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
				this.initialEnvelope.transform(DefaultGeographicCRS.WGS84, true /* Be lenient */),
				1.3 /* xMinExpansion */,
				0.0 /* otherExpansion */);
		if (nexpandedEnvelope != null) {
			this.expandedEnvelope=nexpandedEnvelope.transform(this.crs, true /* Be lenient */); 
		}
		else {
			throw new Exception("rifCoordinateReferenceSystem.expandMapBounds return NULL ReferencedEnvelope");
		}
		
		double xLength=initialEnvelope.getMaximum(0) - initialEnvelope.getMinimum(0); 
		double yLength=initialEnvelope.getMaximum(1) - initialEnvelope.getMinimum(1);
		
		if (xLength > 500000.0) {
			this.gridVertexSpacing=100000.0; 	// In WGS84 degrees
			this.gridSquareWidth=100000.0;		// In WGS84 degrees
		}
		else if (xLength > 50000.0) {
			this.gridVertexSpacing=10000.0; 	// In WGS84 degrees
			this.gridSquareWidth=10000.0;		// In WGS84 degrees
		}
		else if (xLength > 5000.0) {
			this.gridVertexSpacing=1000.0; 		// In WGS84 degrees
			this.gridSquareWidth=1000.0;		// In WGS84 degrees
		}
		else if (xLength > 500.0) {
			this.gridVertexSpacing=100.0; 	// In WGS84 degrees
			this.gridSquareWidth=100.0;		// In WGS84 degrees
		}
		else if (xLength > 50.0) {
			this.gridVertexSpacing=10.0; 	// In WGS84 degrees
			this.gridSquareWidth=10.0;		// In WGS84 degrees
		}	
		else if (xLength > 5.0) {
			this.gridVertexSpacing=1.0; 	// In WGS84 degrees
			this.gridSquareWidth=1.0;		// In WGS84 degrees
		}	
		else if (xLength > 0.50) {
			this.gridVertexSpacing=0.1; 	// In WGS84 degrees
			this.gridSquareWidth=0.1;		// In WGS84 degrees			
		}	
		else {
			this.gridVertexSpacing=0.01; 	// In WGS84 degrees
			this.gridSquareWidth=0.01;		// In WGS84 degrees					
		}
				
		rifLogger.error(this.getClass(), 
			"Setup RifFeatureCollection initialEnvelope: " + initialEnvelope.toString() + lineSeparator +
			"nexpandedEnvelope: " + nexpandedEnvelope.toString() + lineSeparator +
			"expandedEnvelope: " + expandedEnvelope.toString() + lineSeparator +
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

}