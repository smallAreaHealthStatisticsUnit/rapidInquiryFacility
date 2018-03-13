package rifServices.graphics;

import org.geotools.feature.DefaultFeatureCollection;
import org.json.JSONException;
import org.json.JSONObject;
import rifGenericLibrary.util.RIFLogger;
import rifServices.system.files.TomcatBase;
import rifServices.system.files.TomcatFile;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
	 * RIF map parameter
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
	
		/**
		 * Constructor.
		 * 
		 */	
		public RIFMapsParameter(
			final String resultsColumn,
			final String classifierFunctionName,
			final String colorbrewerPalette,	
			final int numberOfBreaks,
			final Boolean invert) 
				throws Exception {
			this.mapTitle = getMapTitle(resultsColumn);
			this.resultsColumn = resultsColumn;
			this.classifierFunctionName = classifierFunctionName;
			this.columnName = getColumnName(resultsColumn);
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
		
		/** Log RIF parameter
		 *
		 * @param: String mapName 
		 */		
		protected void parameterLog(final String mapName) {
			rifLogger.info(this.getClass(), mapName + ": " +
				"; mapTitle: " + mapTitle + 
				"; resultsColumn: " + resultsColumn + 
				"; classifierFunctionName: " + classifierFunctionName +
				"; columnName: " + columnName +
				"; colorbrewerPalette: " + colorbrewerPalette +
				"; numberOfBreaks: " + numberOfBreaks +
				"; invert: " + invert);
		}
		
		/** get map title
		 *
		 * @param: String feature 
		 *
		 * @returns: String 
		 */		
		private String getMapTitle(final String feature)
				throws Exception {
			if (feature.toLowerCase().equals("relative_risk")) {
				return "Relative Risk";
			}
			else if (feature.toLowerCase().equals("smoothed_smr")) {
				return "Smoothed SMR";
			}
			else if (feature.toLowerCase().equals("posterior_probability")) {
				return "Posterior Probability";
			}
			else {
				throw new Exception("getMapTitle() unsupported fature: " + feature);
			}
		}	

		/** get column name
		 *
		 * @param: String feature 
		 *
		 * @returns: String 
		 */		
		private String getColumnName(final String feature) 
				throws Exception {
			if (feature.toLowerCase().equals("relative_risk")) {
				return "rr";
			}
			else if (feature.toLowerCase().equals("smoothed_smr")) {
				return "sm_smr";
			}
			else if (feature.toLowerCase().equals("posterior_probability")) {
				return "post_prob";
			}
			else {
				throw new Exception("getColumnName() unsupported fature: " + feature);
			}
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

	/** Accessor: get RIFMapsParameter for key
	 *
	 * @param: String key
	 *
	 * @returns: RIFMapsParameter
	 */
	public RIFMapsParameter getRIFMapsParameter(String key) {
		return rifMapsParameters.get(key);
	}

	/** Accessor: get key set
	 *
	 * @returns: Set<String> 
	 */	
	public Set<String> getKeySet() {
      Set<String> keySet = rifMapsParameters.keySet();
      return keySet;
	}
	
	/**
	 * Setup default map parameters
	 */
	private void setupDefaultMapParameters() 
		throws Exception {
		
		RIFMapsParameter rifMapsParameter1 = new RIFMapsParameter(	
			"relative_risk"		/* resultsColumn */,
			"quantile"		/* Classifier function name */, 
			"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			9				/* numberOfBreaks */, 
			true			/* invert */);
		rifMapsParameters.put("viewermap", rifMapsParameter1);
				
		RIFMapsParameter rifMapsParameter2 = new RIFMapsParameter(
			"smoothed_smr"		/* resultsColumn */,
			"quantile"		/* Classifier function name */, 
			"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			9				/* numberOfBreaks */, 
			true			/* invert */);		
		rifMapsParameters.put("diseasemap1", rifMapsParameter2);
		
		RIFMapsParameter rifMapsParameter3 = new RIFMapsParameter(	
			"posterior_probability"		/* resultsColumn */,	
			"AtlasProbability"	/* Classifier function name */, 
			null				/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			0					/* numberOfBreaks */, 
			false				/* invert */);		
		rifMapsParameters.put("diseasemap2", rifMapsParameter3);
	}

	/**
	 * Retrieve front end parameters
	 */	
	private void retrieveFrontEndParameters() 
			throws Exception {
				
		BufferedReader reader = new TomcatFile(
				new TomcatBase(), TomcatFile.FRONT_END_PARAMETERS_FILE).reader();

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line.replaceAll("//.*", "")).append(lineSeparator); // Remove comments
		}

		String jsonText=sb.toString();

		// This regex can cause stack overflows!!!!
		try {
			jsonText=jsonText.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)","");	 /* Comments */
			// Could try:
			// (\/\*.*?\*\/)
			// /\*([^*]|[\r\n]|(\*+([^*/]|[\r\n])))*\*+/
		}
		catch(StackOverflowError t) {
			throw new Exception("Comment remover caused StackOverflowError");
		}

		rifLogger.info(getClass(), "Retrieve FrontEnd Parameters: " + jsonText);
		jsonText = jsonText.replace(lineSeparator, "");
				// Remove line separators
		JSONObject json = new JSONObject(jsonText);

		parseJson(json);
	}

	/**
	 * Parse JSON from frontEndParameters JSON5 file
	 *
	 * @param: JSONObject json
	 */		
	private void parseJson(final JSONObject json) {
		try {
			JSONObject parameters=json.getJSONObject("parameters");	
			try {
				JSONObject mappingDefaults=parameters.getJSONObject("mappingDefaults");
				
				Iterator<String> keys = mappingDefaults.keys();
				while (keys.hasNext()) {
					String mapName = keys.next();
					JSONObject mapOptions = mappingDefaults.optJSONObject(mapName);
					
					/* 
					'diseasemap1': {
						method: 	'quantile', 
						feature:	'smoothed_smr',
						intervals: 	9,
						invert:		true,
						brewerName:	"PuOr"
					} */
					
					if (mapOptions != null) {
						String method=mapOptions.optString("method");
						String feature=mapOptions.optString("feature");
						int intervals=(int)mapOptions.optLong("intervals");
						String invertString=mapOptions.optString("invert");
						boolean invert=false;
						if (invertString.toUpperCase().equals("TRUE")) {
							invert=true;
						}
						String brewerName=mapOptions.optString("brewerName");
							
						try {					
							RIFMapsParameter rifMapsParameter = new RIFMapsParameter(	
								feature						/* resultsColumn */,	
								method						/* Classifier function name */, 
								brewerName					/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
								intervals					/* numberOfBreaks */, 
								invert						/* invert */);	
							rifMapsParameters.put(mapName, rifMapsParameter);
							rifMapsParameter.parameterLog(mapName);
						}
						catch (Exception exception) {
							rifLogger.warning(this.getClass(), "Unable to parse mapOptions from: " + mapOptions.toString(2));
						}
					}	
				}
			}
			catch (JSONException exception) {
				rifLogger.warning(this.getClass(), "Unable to parse mappingDefaults from: " + parameters.toString(2));
			}
		}
		catch (JSONException exception) {
			rifLogger.warning(this.getClass(), "Unable to parse parameters from: " + json.toString(2));
		}
	}
}		
