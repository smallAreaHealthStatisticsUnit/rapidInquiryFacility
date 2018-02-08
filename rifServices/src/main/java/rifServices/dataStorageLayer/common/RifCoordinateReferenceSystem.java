package rifServices.dataStorageLayer.common;

import rifGenericLibrary.util.RIFLogger;

import java.io.*;
import java.lang.*;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.PropertyAuthorityFactory;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.JTS;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.CRS;
import org.geotools.data.shapefile.ShapefileDataStore; 
import org.geotools.data.FeatureWriter; 
import org.geotools.data.Transaction; 


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
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
	private static Map<String, String> environmentalVariables = System.getenv();
	private static String catalinaHome = environmentalVariables.get("CATALINA_HOME");

    private static HashMap<Integer, CoordinateReferenceSystem> sridHashMap = 
		new HashMap<Integer, CoordinateReferenceSystem>();
    private static HashMap<String, MathTransform> transformHashMap = 
		new HashMap<String, MathTransform>();
	  
	/**
     * Constructor.
	 *
	 * Setup Coordinate Reference System lookup. Use predictable RIF locations for 
	 * epsg.properties: %CATALINA_HOME%\conf and %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes
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
	public MathTransform getMathTransform(CoordinateReferenceSystem crs)
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
	public CoordinateReferenceSystem getCRS(int srid) 
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
	 * epsg.properties: %CATALINA_HOME%\conf and %CATALINA_HOME%\webapps\rifServices\WEB-INF\classes
	 */
	private void setupReferencingFactory() 
		throws Exception {

		String file="epsg.properties";
		String file1 = null;
		String file2 = null;
		File input = null;
		URL epsg = null;
		
		try {
			
			if (catalinaHome != null) {
				file1=catalinaHome + "\\conf\\" + file;
				file2=catalinaHome + "\\webapps\\rifServices\\WEB-INF\\classes\\" + file;
			}
			else {
				rifLogger.warning(this.getClass(), 
					"setupReferencingFactory: CATALINA_HOME not set in environment"); 
				file1="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\conf\\" + file;
				file2="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\" + file;
			}
			
			input=new File(file1);
			if (input.exists()) {
				rifLogger.info(this.getClass(), 
						"setupReferencingFactory: using: " + file1);
			} 
			else {
				input=new File(file2);	
				if (input.exists()) {
					rifLogger.info(this.getClass(), 
						"setupReferencingFactory: using: " + file2);
				}
				else {				
					rifLogger.warning(this.getClass(), 
						"setupReferencingFactory error: unable to find files: " + 
							file1 + " and " + file2);
					input=null;
				}
			}			
				
			if (input != null) {
				epsg = input.toURI().toURL();

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
			else {
				throw new Exception("Null URL");
			}
		}
		catch(Exception exception) { // If it fails, will use default (WGS84)
		
			if (epsg != null) {
				rifLogger.warning(this.getClass(), 
					"URL: " + epsg.getFile() + " had error: ", exception);
			}
			else {
				rifLogger.warning(this.getClass(), 
					"URL: <NULL> had error: ", exception);
			}
		}
	}		
}
