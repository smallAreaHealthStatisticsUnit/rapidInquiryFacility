package org.sahsu.rif.services.graphics;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.geotools.feature.DefaultFeatureCollection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.StudyType;
import org.sahsu.rif.services.datastorage.common.SQLManager;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.util.Json5Parse;

import javax.sql.rowset.CachedRowSet;

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

	private StudyType studyType = StudyType.DISEASE_MAPPING;	
	
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
		private double breaks[];
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
		 * Constructor.
		 * 
		 */			
		public RIFMapsParameter(
			final String resultsColumn,
			final String classifierFunctionName,
			final String colorbrewerPalette,	
			final double[] breaks,
			final Boolean invert) 
				throws Exception {
			this.mapTitle = getMapTitle(resultsColumn);
			this.resultsColumn = resultsColumn;
			this.classifierFunctionName = classifierFunctionName;
			this.columnName = getColumnName(resultsColumn);
			this.colorbrewerPalette = colorbrewerPalette;
			this.breaks=breaks;
			this.numberOfBreaks = (breaks.length-1);
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
			RIFStyle rifStyle=null;
			
			if (breaks != null && breaks.length > 0) { // User defined
				rifStyle=new RIFStyle(
					classifierFunctionName,
					columnName,
					colorbrewerPalette,
					breaks,
					invert,
					featureCollection);
			}
			else { // Pre-defined
				rifStyle=new RIFStyle(
					classifierFunctionName,
					columnName,
					colorbrewerPalette,
					numberOfBreaks,
					invert,
					featureCollection);
			}
			return rifStyle;
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
	} // End of RIFMapsParameter()
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private static double atlasProbabilityBreaks[] = {0.0, 0.20, 0.81, 1.0};
	private static double atlasRelativeRiskBreaks[] = {Double.NEGATIVE_INFINITY, 
		0.68, 0.76, 0.86, 0.96, 1.07, 1.2, 1.35, 1.51, Double.POSITIVE_INFINITY};

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
	public RIFMapsParameters(final SQLManager manager, final CachedRowSet rif40Studies) {
		try {
			setupDefaultMapParameters(manager, rif40Studies);
			retrieveFrontEndParameters(manager, rif40Studies);
		}		
		catch(StackOverflowError stackOverflowError) {
			rifLogger.warning(this.getClass(), 
				"Comment remover caused StackOverflowError", stackOverflowError);
			throw new NullPointerException();
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
	private void setupDefaultMapParameters(final SQLManager manager, final CachedRowSet rif40Studies) 
		throws Exception {
		
		String studyID=manager.getColumnFromResultSet(rif40Studies, "study_id");
		String selectStateText=manager.getColumnFromResultSet(rif40Studies, "select_state", true /* allowNulls */, false /*  allowNoRows */);
		
		if (selectStateText != null) {
			JSONObject selectStateJson = new JSONObject(selectStateText); // Check it parses OK
			String studyTypeStr = selectStateJson.optString("studyType");
			if (studyTypeStr != null && studyTypeStr.equals(StudyType.RISK_ANALYSIS.type())) {
				rifLogger.info(getClass(), "rif40_studies.study_id: " + studyID + "; use database select state study type: risk analysis"); 
				this.studyType = StudyType.RISK_ANALYSIS;
			}
			else if (studyType != null && studyType.equals(StudyType.DISEASE_MAPPING.type())) {
				rifLogger.info(getClass(), "rif40_studies.study_id: " + studyID + "; use database select state study type: disease mapping"); 
				this.studyType = StudyType.DISEASE_MAPPING;
			}
			else {
				rifLogger.info(getClass(), "rif40_studies.study_id: " + studyID + "; no database select state study type, assume: disease_mapping;" +
					lineSeparator +
					"; selectStateJson: " + selectStateJson.toString(2)); 
			}	
		}
		else {
			rifLogger.info(getClass(), "rif40_studies.study_id: " + studyID + "; no database select state, assume study type: disease_mapping"); 
		}
			
		RIFMapsParameter rifMapsParameter1 = new RIFMapsParameter(	
			"relative_risk"		/* resultsColumn */,
			"quantile"		/* Classifier function name */, 
			"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
			9				/* numberOfBreaks */, 
			true			/* invert */);
		rifMapsParameters.put("viewermap", rifMapsParameter1);
		
		if (this.studyType == StudyType.DISEASE_MAPPING) { // No Bayesian smoothing in risk analysis studies
			RIFMapsParameter rifMapsParameter2 = new RIFMapsParameter(
				"smoothed_smr"		/* resultsColumn */,
				"quantile"		/* Classifier function name */, 
				"PuOr"			/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				9				/* numberOfBreaks */, 
				true			/* invert */);		
			rifMapsParameters.put("diseasemap1", rifMapsParameter2);
			
			RIFMapsParameter rifMapsParameter3 = new RIFMapsParameter(	
				"posterior_probability"		/* resultsColumn */,	
				"AtlasProbability"			/* Classifier function name */, 
				"RdYlGn"					/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
				atlasProbabilityBreaks		/* breaks */, 
				false						/* invert */);		
			rifMapsParameters.put("diseasemap2", rifMapsParameter3);
		}
	}

	/**
	 * Retrieve front end parameters
	 */	
	private void retrieveFrontEndParameters(final SQLManager manager, final CachedRowSet rif40Studies) 
			throws Exception {

		String studyID=manager.getColumnFromResultSet(rif40Studies, "study_id");
		String printState=manager.getColumnFromResultSet(rif40Studies, "print_state", true /* allowNulls */, false /*  allowNoRows */);
		
		BufferedReader reader = AppFile.getServicesInstance(AppFile.FRONT_END_PARAMETERS_FILE).reader();

		Json5Parse frontEndJson5Parse = new Json5Parse(reader);
		JSONObject frontEndJson = frontEndJson5Parse.toJson(); // Check it parses OK
		JSONObject parametersJson = frontEndJson.optJSONObject("parameters");
		
		JSONObject printStateJson = null;

//		rifLogger.info(getClass(), "Retrieve FrontEnd Parameters: " + frontEndJson.toString(2));
			
		if (parametersJson == null) {
			throw new RIFServiceException(
				RIFServiceError.JSON_PARSE_ERROR,
				"retrieveFrontEndParameters json parse error: missing \"parameters\" key for rif40_studies.study_id: " + studyID + " update");
		}
		printStateJson = parametersJson.optJSONObject("mappingDefaults");
		if (printStateJson == null) {
			throw new RIFServiceException(
				RIFServiceError.JSON_PARSE_ERROR,
				"retrieveFrontEndParameters json parse error: missing \"mappingDefaults\" key for rif40_studies.study_id: " + studyID + " update");
		}
			
		if (printState != null) {
			printStateJson = new JSONObject(printState); // Check it parses OK
			if (printStateJson == null) {
				throw new RIFServiceException(
					RIFServiceError.JSON_PARSE_ERROR,
					"retrieveFrontEndParameters json parse error unable to pare print_state for rif40_studies.study_id: " + studyID + " update" +
					"; print_state: " + printState);
			}			
			rifLogger.info(getClass(), "rif40_studies.study_id: " + studyID + "; use database print state: " + printStateJson.toString(2));
		}
		else {
			rifLogger.info(getClass(), "rif40_studies.study_id: " + studyID + "; use FrontEnd Parameters print state: " + printStateJson.toString(2));
		}
		parseJson(printStateJson, studyID); // Call internal RIF parser
	}


	/**
	 * Parse JSON from frontEndParameters/database print_state JSON5 file
	 *
	 * @param: JSONObject json
	 *
	 * Expecting three keys: viewermap, diseasemap1, diseasemap2
	 */		
	private void parseJson(final JSONObject printStateJson, final String studyID) {
				
		Iterator<String> keys = printStateJson.keys();
		while (keys.hasNext()) {
			String mapName = keys.next();
			JSONObject mapOptions = printStateJson.optJSONObject(mapName);
			
			/* 
			'diseasemap1': {
				method: 	'quantile', 
				feature:	'smoothed_smr',
				intervals: 	9,
				invert:		true,
				brewerName:	"PuOr"
			} 
			*/
			
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
				JSONArray breaksArray = mapOptions.optJSONArray("breaks");
				if ((studyType == StudyType.RISK_ANALYSIS && feature.equals("relative_risk")) || 
				    (studyType == StudyType.DISEASE_MAPPING)) {
					
					if (breaksArray != null) { // User defined
						intervals=(breaksArray.length()-1);
						double[] breaks = new double[intervals+1];
						for (int i = 0; i < breaksArray.length(); i++) {
							breaks[i]=breaksArray.getDouble(i);
						}
						try {					
							RIFMapsParameter rifMapsParameter = new RIFMapsParameter(	
								feature						/* resultsColumn */,	
								method						/* User style name */, 
								brewerName					/* colorbrewer palette: http://colorbrewer2.org/#type=diverging&scheme=PuOr&n=8 */, 
								breaks						/* Breaks */, 
								invert						/* invert */);	
							rifMapsParameters.put(mapName, rifMapsParameter);
							rifMapsParameter.parameterLog(mapName);
						}
						catch (Exception exception) {
							rifLogger.warning(this.getClass(), 
								"Unable to parse user defined mapOptions from: " + mapOptions.toString(2));
						}							
					}
					else { // Predefined 
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
							rifLogger.warning(this.getClass(), 
								"Unable to parse predefined mapOptions from: " + mapOptions.toString(2) +
								"; for rif40_studies.study_id: " + studyID);
						}							
					}
				}
				else {			
					rifLogger.info(this.getClass(), 
						"Risk Analysis feature disabled: " + feature);
				}
			}	
		}
	}
}		
