package org.sahsu.rif.services.datastorage.common;

import java.net.URL;
import java.util.HashMap;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.util.RIFLogger;

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
 
public class RifCoordinateReferenceSystem {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	private static HashMap<Integer, CoordinateReferenceSystem> sridHashMap = new HashMap<>();
    private static HashMap<String, MathTransform> transformHashMap = new HashMap<>();
	  
	/**
     * Constructor.
	 *
	 * Setup Coordinate Reference System lookup. Use predictable RIF locations for 
	 * epsg.properties.
	 *
	 * Hash srid to CRS, CRS to maths transforms for efficency.
     */
	public RifCoordinateReferenceSystem() {
		// Setup CoordinateReferenceSystem lookup
		try {
			setupReferencingFactory();
		}
		catch(Exception exception) { // If it fails, will use default (WGS84)
			rifLogger.warning(this.getClass(), 
				"Error in RifCoordinateReferenceSystem() constructor: setupReferencingFactory(): ", 
				exception);
		}		
	}
	
	/** 
	 * Get Maths transform to transfrom Coordinate Reference System of WGS64 to rif40_geographies srid
	 *
	 * @param	CoordinateReferenceSystem crs
	 * @returns MathTransform
	 */
	public MathTransform getMathTransform(
		final CoordinateReferenceSystem crs)
			throws Exception {
		MathTransform transform = null; 	// For re-projection
		String crsText = CRS.toSRS(crs);	//  Spatial Reference System identifier
		
		if (!CRS.toSRS(crs).equals(CRS.toSRS(DefaultGeographicCRS.WGS84))) { // Not WGS84
			if (transformHashMap.containsKey(crsText)) {
				transform=transformHashMap.get(crsText);
			}
			else {
				transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, crs,
								true /* Be lenient with errors between datums */);
				if (transform != null) {
					transformHashMap.put(crsText, transform);
				}	
				else {
					rifLogger.warning(this.getClass(), "No MathTransform found for CRS: " + crsText);
				}
			}
		}
		else {
			throw new Exception("Cannot transform co-ordinates from WGS84 to " + crsText);
		}

		return transform;
	}
	
	/** 
	 * Get Coordinate Reference System from srid
	 *
	 * @param	int srid
	 * @returns CoordinateReferenceSystem
	 */
	public CoordinateReferenceSystem getCRS(
		final int srid) 
			throws Exception {
		
		CoordinateReferenceSystem crs=null;
		
		if (sridHashMap.containsKey(srid)) {
			crs=sridHashMap.get(srid);
		}
		else {
			crs=CRS.decode("EPSG:" + srid);
			if (crs != null) {
				sridHashMap.put(srid, crs);
			}
			else {
				rifLogger.warning(this.getClass(), "No CoordinateReferenceSystem found for SRID: " + srid);
			}
		}
		
		return crs;
	}
	
	/** 
	 * Setup CoordinateReferenceSystem referencing factory. Use predictable RIF locations for 
	 * epsg.properties.
	 */
	private void setupReferencingFactory() throws Exception {

		String file="epsg.properties";

		AppFile input = AppFile.getServicesInstance(file);
		URL epsg = input.asUrl();

		if (epsg != null) {
			Hints hints = new Hints(Hints.CRS_AUTHORITY_FACTORY, PropertyAuthorityFactory.class);
			ReferencingFactoryContainer referencingFactoryContainer =
					   ReferencingFactoryContainer.instance(hints);

			PropertyAuthorityFactory propertyAuthorityFactory = new PropertyAuthorityFactory(
				referencingFactoryContainer, 	// ReferencingFactoryContainer
				Citations.fromName("EPSG"), 	// Citation[]
				epsg);							// URL

			ReferencingFactoryFinder.addAuthorityFactory(propertyAuthorityFactory);
			ReferencingFactoryFinder.scanForPlugins(); // hook everything up
			rifLogger.info(this.getClass(),
				"setupReferencingFactory(): Setup CoordinateReferenceSystem lookup OK");
		}
	}
	
	/** 
	 * Get default referenced envelope for map, using the defined extent of data Coordinate Reference System
	 * (i.e. the projection bounding box as a ReferencedEnvelope)
	 *
	 * @param CoordinateReferenceSystem rif40GeographiesCRS [Usually derived from the rif40_geographies SRID]
	 *
	 * @returns ReferencedEnvelope in databaseCRS CRS for the geographical extent of rif40GeographiesCRS
	 */
	public ReferencedEnvelope getDefaultReferencedEnvelope(
		final CoordinateReferenceSystem rif40GeographiesCRS) 
			throws Exception {
		
		CoordinateReferenceSystem databaseCRS=DefaultGeographicCRS.WGS84; // 4326
		if (rif40GeographiesCRS == null) {
			throw new Exception("Null source Coordinate Reference System");
		}
		ReferencedEnvelope envelope = null;
	    Extent rif40GeographiesExtent = rif40GeographiesCRS.getDomainOfValidity();
		if (rif40GeographiesExtent != null) {
			for (GeographicExtent element : rif40GeographiesExtent.getGeographicElements()) {
				if (element instanceof GeographicBoundingBox) {
					GeographicBoundingBox bounds = (GeographicBoundingBox) element;
					ReferencedEnvelope bbox = new ReferencedEnvelope(
						bounds.getSouthBoundLatitude(),
						bounds.getNorthBoundLatitude(),
						bounds.getWestBoundLongitude(),
						bounds.getEastBoundLongitude(),
						rif40GeographiesCRS
					);
					
					if (!CRS.toSRS(rif40GeographiesCRS).equals(CRS.toSRS(databaseCRS))) {
						envelope = bbox.transform(databaseCRS, true /* Be lenient with errors between datums */);
					}
					else {
						envelope=bbox;
					}
				}
			}
		}
		else {	
			envelope=ReferencedEnvelope.EVERYTHING;
			rifLogger.info(this.getClass(), "Unable to get Domain Of Validity for: " + CRS.toSRS(rif40GeographiesCRS) +
				"; using ReferencedEnvelope.EVERYTHING: " + envelope.toString());
		}
		
		if (envelope.getMaxX() == envelope.getMinX() &&
			envelope.getMaxY() == envelope.getMinY()) {
			throw new Exception("BBOX is zero sized: " + envelope.toString());
		}
		
		return envelope;
	}

	/** 
	 * Expand map bounds. xMin: 30% to allow for legend at right
	 *
	 * @param ReferencedEnvelope initialEnvelope [in WGS84]
	 * @param double otherExpansion: other border expansion
	 *
	 * @returns ReferencedEnvelope in WGS84
	 */	
	public ReferencedEnvelope expandMapBounds(
		final ReferencedEnvelope initialEnvelope, 
		final double otherExpansion)	
			throws Exception {
					
		if (initialEnvelope == null) {
			throw new Exception("initialEnvelope is null");
		}		
		else if (!CRS.toSRS(initialEnvelope.getCoordinateReferenceSystem()).
				equals(CRS.toSRS(DefaultGeographicCRS.WGS84))) {
			throw new Exception("Unable to expand bounds: initialEnvelope is not in WGS84: " + 
				CRS.toSRS(initialEnvelope.getCoordinateReferenceSystem()) +
				"; expecting: " + CRS.toSRS(DefaultGeographicCRS.WGS84));
		} 
				
		// WGS84 Bounds: -180.0000, -90.0000, 180.0000, 90.0000	
		double xMin=initialEnvelope.getMinimum(0);
		double xMax=initialEnvelope.getMaximum(0); 		
		double yMin=initialEnvelope.getMinimum(1);
		double yMax=initialEnvelope.getMaximum(1);
	
		double aspectRatio = initialEnvelope.getSpan(0) / initialEnvelope.getSpan(1);  
			// The ratio of the width to the height of an image or screen
		double expansionFactor=1.5;
			// Adjust expansion factor on the basis of the aspect ratio
		if (aspectRatio > 2.0) { // Very wide
			expansionFactor=1.1;
		}
		else if (aspectRatio < 0.7) { // Very tall
			expansionFactor=1.8;
		}
		
		double newxMin=(double)(xMax-((xMax-xMin)*expansionFactor)); // Expand map min Xbound by <expansionFactor>% to allow for legend at left
		rifLogger.info(this.getClass(), 
			"xMin aspectRatio: " + aspectRatio + "; expansionFactor: " + expansionFactor + 
			"; xMin: " + xMin + " to: " + newxMin);
		xMin=newxMin;
		if (xMin < -180) {
			rifLogger.warning(this.getClass(), "Expand map bounds: xMin: " + xMin + " set to -180");
			xMin=-180;
		}
		xMax=(double)(xMin+((xMax-xMin)*otherExpansion)); // Expand map max Xbound by <otherExpansion>% for border
		if (xMax > 180) {
			rifLogger.warning(this.getClass(), "Expand map bounds: xMin: " + xMax + " set to 180");
			xMax=180;
		}			
		yMin=(double)(yMax-((yMax-yMin)*otherExpansion)); // Expand map min Ybound by <otherExpansion>% for border
		if (yMin < -90) {
			rifLogger.warning(this.getClass(), "Expand map bounds: xMin: " + yMin + " set to -90");
			yMin=-90;
		}	
		yMax=(double)(yMin+((yMax-yMin)*otherExpansion)); // Expand map max Xbound by <otherExpansion>% for border
		if (yMax > 90) {
			rifLogger.warning(this.getClass(), "Expand map bounds: xMin: " + yMax + " set to 90");
			yMax=90;
		}		
		ReferencedEnvelope expandedEnvelope = new ReferencedEnvelope(
					xMin /* bounds.getWestBoundLongitude() */,
					xMax /* bounds.getEastBoundLongitude() */,
					yMin /* bounds.getSouthBoundLatitude() */,
					yMax /* bounds.getNorthBoundLatitude() */,
					initialEnvelope.getCoordinateReferenceSystem());

		return expandedEnvelope;
	}
}
