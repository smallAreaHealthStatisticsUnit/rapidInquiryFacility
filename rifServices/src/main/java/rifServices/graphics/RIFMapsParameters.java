package rifServices.graphics;

import rifGenericLibrary.util.RIFLogger;

import java.io.*;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import rifServices.graphics.RIFStyle;

import org.geotools.feature.DefaultFeatureCollection;

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
	
public class RIFMapsParameters {
	
	/* 
	 * Information about a RIF Style range band
	 */
	public static class RIFMapsParameter
	{
		private String mapTitle;
		private String resultsColumn;	
		private String classifierFunctionName;	
		private String columnName;	
		private String colorbrewerPalette;	
		private int numberOfBreaks;	
		private Boolean invert;	
		
		public RIFMapsParameter(
			final String mapTitle, 
			final String resultsColumn,
			final String classifierFunctionName,	
			final String columnName,	
			final String colorbrewerPalette,	
			final int numberOfBreaks,
			final Boolean invert) {
			this.mapTitle = mapTitle;
			this.resultsColumn = resultsColumn;
			this.classifierFunctionName = classifierFunctionName;
			this.columnName = columnName;
			this.colorbrewerPalette = colorbrewerPalette;
			this.numberOfBreaks = numberOfBreaks;
			this.invert = invert;
		}
		/**
		 * Accessors 
		 */		
		public String getMapTitle() {
			return mapTitle;
		}
		public String getResultsColumn() {
			return resultsColumn;
		}
		public RIFStyle getRIFStyle(DefaultFeatureCollection featureCollection) {
			return new RIFStyle(
				classifierFunctionName,
				columnName,
				colorbrewerPalette,
				numberOfBreaks,
				invert,
				featureCollection);
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	
    private HashMap<String, RIFMapsParameter> rifMapsParameters = new HashMap<String, RIFMapsParameter>();
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================
	/**
     * Constructor.
     * 
     */
	public RIFMapsParameters() {
		try {
			setupDefaultMapParameters();
			retrieveFrontEndParameters();
		}
		catch(Exception exception) {
			rifLogger.warning(this.getClass(), 
				"Error in RIFMapsParameters() constructor", exception);
			throw new NullPointerException();
		}		
	}	

	public RIFMapsParameter getRIFMapsParameter(String key) {
		return rifMapsParameters.get(key);
	}
	
	public Set<String> getKeySet() {
      Set<String> keySet = rifMapsParameters.keySet();
      return keySet;
	}
	
	private void setupDefaultMapParameters() {
		
		RIFMapsParameter rifMapsParameter1 = new RIFMapsParameter(		
			"Relative Risk"		/* mapTitle */, 
			"relative_risk"		/* resultsColumn */,
			"quantile"		/* Classifier function name */, 
			"rr"			/* Column */, 
			"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			9				/* numberOfBreaks */, 
			true			/* invert */);
		rifMapsParameters.put("viewermap", rifMapsParameter1);
				
		RIFMapsParameter rifMapsParameter2 = new RIFMapsParameter(
			"Smoothed SMR"		/* mapTitle */, 
			"smoothed_smr"		/* resultsColumn */,
			"quantile"		/* Classifier function name */, 
			"sm_smr"		/* Column */, 
			"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			9				/* numberOfBreaks */, 
			true			/* invert */);		
		rifMapsParameters.put("diseasemap1", rifMapsParameter2);
		
		RIFMapsParameter rifMapsParameter3 = new RIFMapsParameter(	
			"Posterior Probability"		/* mapTitle */, 
			"posterior_probability"		/* resultsColumn */,	
			"AtlasProbability"	/* Classifier function name */, 
			"post_prob"			/* Column */, 
			null				/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			0					/* numberOfBreaks */, 
			false				/* invert */);		
		rifMapsParameters.put("diseasemap2", rifMapsParameter3);
	}
	
	private void retrieveFrontEndParameters() 
			throws Exception {
				
		Map<String, String> environmentalVariables = System.getenv();
		InputStream input = null;
		String fileName1;
		String fileName2;
		String catalinaHome = environmentalVariables.get("CATALINA_HOME");
		BufferedReader reader = null;
		
		if (catalinaHome != null) {
			fileName1=catalinaHome + "\\conf\\frontEndParameters.json5";
			fileName2=catalinaHome + "\\webapps\\rifServices\\WEB-INF\\classes\\frontEndParameters.json5";
		}
		else {
			rifLogger.warning(this.getClass(), 
				"MSSQLAbstractRIFStudySubmissionService.getFrontEndParameters: CATALINA_HOME not set in environment"); 
			fileName1="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\conf\\frontEndParameters.json5";
			fileName2="C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\rifServices\\WEB-INF\\classes\\frontEndParameters.json5";
		}
			
		try {
			input = new FileInputStream(fileName1);
				rifLogger.info(this.getClass(), "Using: " + fileName1);
				// Read JSON
				reader = new BufferedReader(new InputStreamReader(input));
		} 
		catch (IOException ioException) {
			try {
				input = new FileInputStream(fileName2);
					rifLogger.info(this.getClass(), "Using: " + fileName2);
				// Read JSON
				reader = new BufferedReader(new InputStreamReader(input));
			} 
			catch (IOException ioException2) {				
				rifLogger.warning(this.getClass(), 
					"retrieveFrontEndParameters IO error for files: " + 
						fileName1 + " and " + fileName2, 
					ioException2);
				return;
			}
		} 	
		
		try {
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
			  sb.append(line + lineSeparator);
			}
				
			String jsonText=sb.toString();
			jsonText=jsonText.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)","");	
					// Remove comments
			rifLogger.info(getClass(), "Retrieve FrontEnd Parameters: " + jsonText);
			jsonText=jsonText.replace(lineSeparator, "");							
					// Remove line separators
			JSONObject json = new JSONObject(jsonText);	
		}
		catch (Exception exception) {
			rifLogger.warning(this.getClass(), 
				"retrieveFrontEndParameters parse error for files: " + 
					fileName1 + " and " + fileName2, 
				exception);	
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} 
				catch (IOException ioException) {
					rifLogger.warning(this.getClass(), 
						"retrieveFrontEndParameters IO error for files: " + 
							fileName1 + " and " + fileName2, 
						ioException);
					return;
				}
			}	
		}
	}
}		