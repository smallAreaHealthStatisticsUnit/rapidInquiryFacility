package rifServices.restfulWebServices;

import java.io.ByteArrayOutputStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import com.fasterxml.jackson.databind.ObjectMapper;

import rifServices.dataStorageLayer.ProductionRIFStudyServiceBundle;
import rifServices.dataStorageLayer.RIFStudyResultRetrievalAPI;
import rifServices.dataStorageLayer.RIFStudySubmissionAPI;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.GeoLevelArea;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.GeoLevelView;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.HealthTheme;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.YearRange;

/**
 * This is a web service class that is analoguous to  
 * to {@link rifServices.dataStorageLayer.AbstractRIFService}. Its purpose is
 * to wrap API methods that are common to both {@link rifServices.dataStorageLayer.RIFStudySubmissionAPI}
 * and {@link rifServices.dataStorageLayer.RIFStudyResultRetrievalAPI}.
 * 
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
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
 * Kevin Garwood
 * @author kgarwood
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

abstract class AbstractRIFWebServiceResource {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private static final ProductionRIFStudyServiceBundle rifStudyServiceBundle 
		= ProductionRIFStudyServiceBundle.getRIFServiceBundle();
	private SimpleDateFormat sd;
	private Date startTime;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRIFWebServiceResource() {
		System.out.println("AbstractRIFWebServiceResource constructor 1 for class=="+getClass().getName()+"==");

		startTime = new Date();
		sd = new SimpleDateFormat("HH:mm:ss:SSS");

		RIFServiceStartupOptions rifServiceStartupOptions
			= new RIFServiceStartupOptions(true);
		
		try {
			rifStudyServiceBundle.initialise(rifServiceStartupOptions);
			//rifStudyServiceBundle.login("ffabbri", new String("ffabbri").toCharArray());
			//rifStudyServiceBundle.login("kgarwood", new String("kgarwood").toCharArray());
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}
	}

	protected String login(
		final String userID,
		final String password) {

		String result = "";
		try {			
			rifStudyServiceBundle.login(userID, password);
			result
				= RIFServiceMessages.getMessage("general.login.success", userID);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
	
		return result;
	}
	
	protected String logout(
		final String userID) {
		
		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxx");
			rifStudyServiceBundle.logout(user);
			result = RIFServiceMessages.getMessage("general.logout.success", userID);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
	
		return result;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	protected RIFStudySubmissionAPI getRIFStudySubmissionService() {
		return rifStudyServiceBundle.getRIFStudySubmissionService();
	}
	
	protected RIFStudyResultRetrievalAPI getRIFStudyResultRetrievalService() {
		return rifStudyServiceBundle.getRIFStudyRetrievalService();
	}
	
	
	protected String getGeographies(
		final String userID) {
			
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxx");
			
			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();
			
			ArrayList<Geography> geographies
				= studySubmissionService.getGeographies(user);

			if (geographies == null) {
				return "";
			}
			else {
				ArrayList<String> geographyNames = new ArrayList<String>();			
				for (Geography geography : geographies) {
					geographyNames.add(geography.getName());
				}
				GeographiesProxy geographiesProxy = new GeographiesProxy();		
				geographiesProxy.setNames(geographyNames.toArray(new String[0]));
				result = serialiseResult(geographiesProxy);
			}			
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
	
		return result;
	
	}

	protected String getGeographicalLevelSelectValues(
		final String userID,
		final String geographyName) {
			
		String result = "";
	
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxxxxxxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();			
			ArrayList<GeoLevelSelect> geoLevelSelects
				= studySubmissionService.getGeoLevelSelectValues(
					user, 
					geography);

			//Convert results to support JSON			
			ArrayList<String> geoLevelSelectNames = new ArrayList<String>();			
			for (GeoLevelSelect geoLevelSelect : geoLevelSelects) {
				geoLevelSelectNames.add(geoLevelSelect.getName());
			}
			GeoLevelSelectsProxy geoLevelSelectProxy
				= new GeoLevelSelectsProxy();		
			geoLevelSelectProxy.setNames(geoLevelSelectNames.toArray(new String[0]));
			result = serialiseResult(geoLevelSelectProxy);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
	
		return result;
	
	}
		
	protected String getDefaultGeoLevelSelectValue(
		final String userID,
		final String geographyName) {
			
		String result = "";
		
		GeoLevelSelectsProxy geoLevelSelectProxy
			= new GeoLevelSelectsProxy();
	
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxxxxxxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();
			GeoLevelSelect defaultGeoLevelSelect
				= studySubmissionService.getDefaultGeoLevelSelectValue(
					user, 
					geography);

			//Convert results to support JSON			
			String[] geoLevelSelectValues = new String[1];
			geoLevelSelectValues[0] = defaultGeoLevelSelect.getName();
			geoLevelSelectProxy.setNames(geoLevelSelectValues);
			result = serialiseResult(geoLevelSelectProxy);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
	
		return result;
	
	}

	protected String getGeoLevelAreaValues(
		final String userID,
		final String geographyName,
		final String geoLevelSelectName) {
						
		String result = "";
		
		GeoLevelAreasProxy geoLevelAreasProxy = new GeoLevelAreasProxy();
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();			
			ArrayList<GeoLevelArea> areas
				= studySubmissionService.getGeoLevelAreaValues(
					user, 
					geography, 
					geoLevelSelect);
			
			//Convert results to support JSON
			ArrayList<String> geoLevelAreaNames = new ArrayList<String>();
			for (GeoLevelArea area : areas) {
				geoLevelAreaNames.add(area.getName());
			}
			geoLevelAreasProxy.setNames(geoLevelAreaNames.toArray(new String[0]));
			result = serialiseResult(geoLevelAreasProxy);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
	
	protected String getGeoLevelViewValues(
		final String userID,
		final String geographyName,
		final String geoLevelSelectName) {
				
		String result = "";
				
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();	
			ArrayList<GeoLevelView> geoLevelViews
				= studySubmissionService.getGeoLevelViewValues(
					user, 
					geography, 
					geoLevelSelect);
			
			//Convert results to support JSON
			GeoLevelViewsProxy geoLevelViewsProxy = new GeoLevelViewsProxy();			
			ArrayList<String> geoLevelViewNames = new ArrayList<String>();
			for (GeoLevelView geoLevelView : geoLevelViews) {
				geoLevelViewNames.add(geoLevelView.getName());
			}
			geoLevelViewsProxy.setNames(geoLevelViewNames.toArray(new String[0]));
			
			result = serialiseResult(geoLevelViewsProxy);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
		
		return result;
		
	}	
	
	/**
	 * retrieves the numerator associated with a given health theme.
	 * @param userID
	 * @param geographyName
	 * @param healthThemeDescription
	 * @return
	 */
	
	protected String getNumerator(
		final String userID,
		final String geographyName,
		final String healthThemeDescription) {
		
		String result = "";
						
		try {
			//Convert URL parameters to RIF service API parameters
			User user 
				= User.newInstance(userID, "xxx");
			Geography geography 
				= Geography.newInstance(geographyName, "");
			HealthTheme healthTheme 
				= HealthTheme.newInstance("xxx", healthThemeDescription.trim());
			System.out.println("getNumerator healthTheme1 name=="+healthTheme.getName()+"==description=="+healthTheme.getDescription()+"==");
			System.out.println("getNumerator healthTheme2 name=="+healthTheme.getName()+"==description=="+healthTheme.getDescription().trim()+"==");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();				
			ArrayList<NumeratorDenominatorPair> ndPairs
				= studySubmissionService.getNumeratorDenominatorPairs(
					user, 
					geography, 
					healthTheme);

			//Convert results to support JSON
			ArrayList<NumeratorDenominatorPairProxy> ndPairProxies 
				= new ArrayList<NumeratorDenominatorPairProxy>();
			for (NumeratorDenominatorPair ndPair : ndPairs) {
				NumeratorDenominatorPairProxy ndPairProxy
					= new NumeratorDenominatorPairProxy();
				ndPairProxy.setNumeratorTableName(ndPair.getNumeratorTableName());
				ndPairProxy.setNumeratorTableDescription(ndPair.getNumeratorTableDescription());
				ndPairProxy.setDenominatorTableName(ndPair.getDenominatorTableName());
				ndPairProxy.setDenominatorTableDescription(ndPair.getDenominatorTableDescription());
				ndPairProxies.add(ndPairProxy);
			}			
			result = serialiseResult(ndPairProxies);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
		
		return result;		
	}

	protected String getDenominator(
		final String userID,
		final String geographyName,
		final String healthThemeDescription) {
		
		String result = "";
				
		try {
			//Convert URL parameters to RIF service API parameters
			User user 	
				= User.newInstance(userID, "xxx");
			Geography geography 
				= Geography.newInstance(geographyName, "");
			HealthTheme healthTheme 
				= HealthTheme.newInstance("xxx", healthThemeDescription);

			//Call service API			
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();				
			ArrayList<NumeratorDenominatorPair> ndPairs
				= studySubmissionService.getNumeratorDenominatorPairs(
					user, 
					geography, 
					healthTheme);
			
			//Convert results to support JSON

			//We should be guaranteed that at least one pair will be returned.
			//All the numerators returned should have the same denominator
			//Therefore, we should be able to pick the first ndPair and extract
			//the denominator.
			NumeratorDenominatorPair firstResult
				= ndPairs.get(0);
			NumeratorDenominatorPairProxy ndPairProxy
				= new NumeratorDenominatorPairProxy();
			ndPairProxy.setNumeratorTableName(firstResult.getNumeratorTableName());
			ndPairProxy.setNumeratorTableDescription(firstResult.getNumeratorTableDescription());
			ndPairProxy.setDenominatorTableName(firstResult.getDenominatorTableName());
			ndPairProxy.setDenominatorTableDescription(firstResult.getDenominatorTableDescription());							
			result = serialiseResult(firstResult);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
	
	
	protected String getYearRange(
		final String userID,
		final String geographyName,
		final String numeratorTableName) {
			
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();			
			NumeratorDenominatorPair ndPair
				= studySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
					user, 
					geography, 
					numeratorTableName);			
			YearRange yearRange
				= studySubmissionService.getYearRange(user, geography, ndPair);
			
			//Convert results to support JSON
			YearRangeProxy yearRangeProxy = new YearRangeProxy();
			yearRangeProxy.setLowerBound(yearRange.getLowerBound());
			yearRangeProxy.setUpperBound(yearRange.getUpperBound());
			result = serialiseResult(yearRangeProxy);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON			
			result = serialiseException(exception);			
		}
		
		return result;
	}
	


	/**
	 * takes advantage of the Jackson project library to serialise objects
	 * for the JSON format.
	 * @param objectToWrite
	 * @return
	 * @throws Exception
	 */
	protected String serialiseResult(
		final Object objectToWrite) 
		throws Exception {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, objectToWrite);
		final byte[] data = out.toByteArray();
		return(new String(data));
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	protected String serialiseException(
		final Exception exceptionThrownByRIFService) {
		
		String result = "";
		try {			
			RIFServiceExceptionProxy rifServiceExceptionProxy
				= new RIFServiceExceptionProxy();
			if (exceptionThrownByRIFService instanceof RIFServiceException) {
				RIFServiceException rifServiceException
					= (RIFServiceException) exceptionThrownByRIFService;
				ArrayList<String> errorMessages
					= rifServiceException.getErrorMessages();
				rifServiceExceptionProxy.setErrorMessages(errorMessages.toArray(new String[0]));
			}
			else {
				/*
				 * We should never encounter this.  However, if we do, 
				 * then we should just indicate that an unexpected error has occurred.
				 * We may assume that the root cause of the error has been logged within
				 * the implementation of the service.
				 */
				String[] errorMessages = new String[1];
				String timeStamp = sd.format(new Date());
				errorMessages[0]
					= RIFServiceMessages.getMessage(
						"webServices.error.unexpectedError",
						timeStamp);
			
				rifServiceExceptionProxy.setErrorMessages(errorMessages);
			}
			result = serialiseResult(rifServiceExceptionProxy);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			serialiseException(exception);
			String timeStamp = sd.format(new Date());
			result 
				= RIFServiceMessages.getMessage(
					"webServices.error.unableToProvideError",
					timeStamp);
		}
		
		return result;
	}
	
	/**
	 * Used as a crude way to find how long individual service operations are taking to 
	 * complete.
	 * @param header
	 */
	protected void printTime(final String header) {
		Date date = new Date();
		StringBuilder buffer = new StringBuilder();
		buffer.append(header);
		buffer.append(":");
		buffer.append(sd.format(date));
		buffer.append("(");
		long elapsed = date.getTime() - startTime.getTime();
		buffer.append(elapsed);
		buffer.append(" milliseconds since start time");
		System.out.println(buffer.toString());		
	}
	

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
