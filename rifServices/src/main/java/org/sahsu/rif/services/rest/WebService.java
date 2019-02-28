package org.sahsu.rif.services.rest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.json.XML;
import org.sahsu.rif.generic.concepts.RIFResultTable;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.util.FrontEndLogger;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.generic.util.UrlFromServletRequest;
import org.sahsu.rif.services.concepts.AbstractRIFConcept;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.GeoLevelArea;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.GeoLevelView;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.RIFStudyResultRetrievalAPI;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;
import org.sahsu.rif.services.concepts.YearRange;
import org.sahsu.rif.services.datastorage.common.SampleTestObjectGenerator;
import org.sahsu.rif.services.datastorage.common.ServiceBundle;
import org.sahsu.rif.services.datastorage.common.ServiceResources;
import org.sahsu.rif.services.fileformats.RIFStudySubmissionXMLWriter;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceMessages;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.graphics.RIFTilesException;

public class WebService {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");
	private final ServiceBundle rifStudyServiceBundle;
	protected RIFLogger rifLogger = RIFLogger.getLogger();
	private static String lineSeparator = System.getProperty("line.separator");
	private Date startTime;
	private WebServiceResponseGenerator webServiceResponseGenerator;
	private FrontEndLogger frontEndLogger = FrontEndLogger.getLogger();
	
	WebService() {
		
		startTime = new Date();
		webServiceResponseGenerator = new WebServiceResponseGenerator();
		
		RIFServiceStartupOptions rifServiceStartupOptions =
				RIFServiceStartupOptions.newInstance(true, false);

		rifStudyServiceBundle = ServiceBundle.getInstance(
				ServiceResources.newInstance(rifServiceStartupOptions));
	}
	
	protected Response rifFrontEndLogger(
			final HttpServletRequest servletRequest,
			final String userID,
			final String browserType,
			final String messageType,
			final String message,
			final String errorMessage,
			final String errorStack,
			final String actualTime,
			final String relativeTime) {

		String ipAddress = servletRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = servletRequest.getRemoteAddr();
		}
		String result;
		try {
			frontEndLogger.frontEndMessage(userID,
				browserType,
				ipAddress,
				messageType,
				message,
				errorMessage,
				errorStack,
				actualTime,
				relativeTime);
			result = serialiseStringResult("OK");
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".rifFrontEndLogger error", exception);
			//Convert exceptions to support JSON
			result = serialiseException(servletRequest, exception);
		}
		
		return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}
	
	protected Response isLoggedIn(final HttpServletRequest servletRequest, final String userID) {
		
		String result;
		try {
			String isLoggedInMessage
				= String.valueOf(rifStudyServiceBundle.isLoggedIn(userID));
			result = serialiseStringResult(isLoggedInMessage);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".isLoggedIn error", exception);

			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
	
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response login(
		final HttpServletRequest servletRequest,
		final String userID,
		final String password) {

		String result;
		try {
			rifStudyServiceBundle.login(userID, password);
			String loginMessage
				= RIFServiceMessages.getMessage("general.login.success", userID);
			result = serialiseStringResult(loginMessage);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".login error", exception);

			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
	
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response logout(
		final HttpServletRequest servletRequest,
		final String userID) {
		
		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			rifStudyServiceBundle.logout(user);
			String logoutMessage
				= RIFServiceMessages.getMessage("general.logout.success", userID);
			result = serialiseStringResult(logoutMessage);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".logout error", exception);

			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
	

		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}

	protected RIFStudySubmissionAPI getRIFStudySubmissionService() {
		
		return rifStudyServiceBundle.getRIFStudySubmissionService();
	}
	
	protected RIFStudyResultRetrievalAPI getRIFStudyResultRetrievalService() {
		
		return rifStudyServiceBundle.getRIFStudyRetrievalService();
	}
	
	protected WebServiceResponseGenerator getWebServiceResponseGenerator() {
		
		return webServiceResponseGenerator;
	}
	
	protected Response isInformationGovernancePolicyActive(
		final HttpServletRequest servletRequest,
		final String userID) {
		
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			
			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();
			
			result
				= String.valueOf(studySubmissionService.isInformationGovernancePolicyActive(user));
			
			serialiseStringResult(result);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),
							getClass().getSimpleName() +
							".isInformationGovernancePolicyActive error", exception);
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}

		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getDatabaseType(
			final HttpServletRequest servletRequest,
			final String userID) {
			
			String result = "";
			
			try {
				
				RIFServiceStartupOptions rifServiceStartupOptions
				= RIFServiceStartupOptions.newInstance(
					true,
					false);

				result = serialiseStringResult(rifServiceStartupOptions.getDatabaseDriverPrefix());
			}
			catch(Exception exception) {
				//Convert exceptions to support JSON
				result
					= serialiseException(
						servletRequest,
						exception);
			}

			return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}
	
	protected Response getGeographies(
		final HttpServletRequest servletRequest,
		final String userID) {
		
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			
			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();
			
			ArrayList<Geography> geographies
				= studySubmissionService.getGeographies(user);

			if (geographies != null) {
				ArrayList<String> geographyNames = new ArrayList<>();
				for (Geography geography : geographies) {
					geographyNames.add(geography.getName());
				}
				GeographiesProxy geographiesProxy = new GeographiesProxy();
				geographiesProxy.setNames(geographyNames.toArray(new String[0]));
				result
					= serialiseSingleItemAsArrayResult(
						servletRequest,
						geographiesProxy);
			}
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getGeographies error", exception);
			
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}

		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getGeographicalLevelSelectValues(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName) {
		
		String result;
	
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			Geography geography = Geography.newInstance(geographyName, "xxx");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= rifStudyServiceBundle.getRIFStudySubmissionService();
			ArrayList<GeoLevelSelect> geoLevelSelects
				= studySubmissionService.getGeoLevelSelectValues(
					user,
					geography);

			//Convert results to support JSON
			ArrayList<String> geoLevelSelectNames = new ArrayList<>();
			for (GeoLevelSelect geoLevelSelect : geoLevelSelects) {
				geoLevelSelectNames.add(geoLevelSelect.getName());
			}
			GeoLevelSelectsProxy geoLevelSelectProxy
				= new GeoLevelSelectsProxy();
			geoLevelSelectProxy.setNames(geoLevelSelectNames.toArray(new String[0]));
			result
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					geoLevelSelectProxy);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),
							getClass().getSimpleName() +
							".getGeographicalLevelSelectValues error", exception);

			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
	
		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getDefaultGeoLevelSelectValue(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName) {
		
		String result;
		
		GeoLevelSelectsProxy geoLevelSelectProxy
			= new GeoLevelSelectsProxy();
	
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
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
			result
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					geoLevelSelectProxy);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),
							getClass().getSimpleName() +
							".getDefaultGeoLevelSelectValue error", exception);
			
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
	
		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	//TOUR_WEB_SERVICES-3
	/*
	 * You'll find that most of the important code for advertising a lot of
	 * service methods is in this class.  Each of these methods follows
	 * a similar sequence of steps:
	 * (1) "Inflate" URL parameters so that we end up with Java objects.
	 * (2) Call the corresponding service method in the Java service API.  Note
	 * that the service has no idea that it is being used within a web service.
	 * (3) Convert results into Java objects that can be easily transformed into a
	 * JSON format.  These are the classes that the
	 * com.fasterxml.jackson.databind.ObjectMapper class uses.
	 * (4) Call serialisation methods that will produce a String value that is a
	 * JSON representation of the objects.
	 * (5) Use the web service response generator to determine whether the
	 * response should tell the client that the String value is JSON or plain text.
	 *
	 * Originally, the service API was designed to use parameter values that were
	 * instances of RIF business classes and not just a String value taken from the
	 * URL.  The reason for this was that we didn't want the API to strictly cater to
	 * clients that would pass parameters in a URL.  The "xxx" is a temporary measure
	 * I've taken to ensure we can inflate a multi-field Geography RIF object from a single
	 * geography String value that is passed by a browser.  In future, we may elect to either
	 * (1) require the URL to pass more information or
	 * (2) relax the validation requirements and shorten the parameter list used to
	 * create a new instance of Geography.
	 *
	 * In these methods, the studySubmissionService has no awareness of what is calling it.  We've
	 * done this to help insulate the core part of our data storage and business concepts from
	 * changes that may occur when we are supporting web services.
	 *
	 * Please visit the next step of the tour, where we visit the GeoLevelArea proxy class
	 */
	protected Response getGeoLevelAreaValues(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName,
		final String geoLevelSelectName) {
		
		String result;
		
		try {

			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
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
			ArrayList<String> geoLevelAreaNames = new ArrayList<>();
			for (GeoLevelArea area : areas) {
				geoLevelAreaNames.add(area.getName());
			}

			GeoLevelAreasProxy geoLevelAreasProxy = new GeoLevelAreasProxy();
			geoLevelAreasProxy.setNames(geoLevelAreaNames.toArray(new String[0]));
			result
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					geoLevelAreasProxy);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),getClass().getSimpleName() +
							".getGeoLevelAreaValues error", exception);
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
		
		//TOUR_WEB_SERVICES-6
		/*
		 * By now we have a String value that contains the data of the
		 * GeoLevelArea objects expressed in the JSON format.  The last issue
		 * we have to deal with is whether we indicate in the response that the
		 * data are in plain text or in JSON.  The web browsers use this
		 * information to determine how they will display results on the screen.
		 *
		 * Earlier in development we realised that although Chrome seemed to
		 * render a JSON response correctly, IE kept asking the user if they
		 * wanted to save the response as a document.  This usually indicates that
		 * the browser isn't sure what to do with what the web service has given it.
		 *
		 * After we did a little investigation, we found that we would probably
		 * need a way of deciding whether a client should be given a
		 * "application/json" or "text/plain" response.
		 *
		 * In order to make that decision, we needed additional information that gets
		 * provided in the servletRequest object.
		 *
		 * Before you return a result in a web service method, call the method below.
		 * It hides a lot of complicated decisions that need to be made to make
		 * the decision of returning application/json or text/plain.
		 *
		 */
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getGeoLevelViewValues(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName,
		final String geoLevelSelectName) {
		
		String result;
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
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
			
			result
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					geoLevelViewsProxy);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getGeoLevelViewValues error", exception);
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}

		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	/**
	 * retrieves the numerator associated with a given health theme.
	 * @param userID
	 * @param geographyName
	 * @param healthThemeDescription
	 * @return
	 */
	
	protected Response getNumerator(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName,
		final String healthThemeDescription) {
		
		String result;
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			Geography geography
				= Geography.newInstance(geographyName, "");
			HealthTheme healthTheme
				= HealthTheme.newInstance("xxx", healthThemeDescription.trim());

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
			result
				= serialiseArrayResult(
					servletRequest,
					ndPairProxies);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getNumerator error", exception);
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getDenominator(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName,
		final String healthThemeDescription) {
		
		String result;
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
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
			result
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					firstResult);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),getClass().getSimpleName() +
			                                ".getDenominator error", exception);
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getYearRange(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName,
		final String numeratorTableName) {
		
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
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
			result
				= serialiseSingleItemAsArrayResult(
					servletRequest,
					yearRangeProxy);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(),getClass().getSimpleName() +
			                                ".getYearRange error", exception);
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}
		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getMapBackground(
			final HttpServletRequest servletRequest,
			final String userID,
			final String geographyName) {			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				Geography geography = Geography.newInstance(geographyName, "");
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getMapBackground(
						user, geography);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getMapBackground error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}
	
	protected Response getHomogeneity(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getHomogeneity(
						user, studyID);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getHomogeneity error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}
	
	protected Response getRiskGraph(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getRiskGraph(
						user, studyID);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getRiskGraph error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}
    
	protected Response getCovariateLossReport(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getCovariateLossReport(
						user, studyID);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getCovariateLossReport error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}
		
	protected Response getSelectState(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getSelectState(
						user, studyID);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getSelectState error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}
	
	protected Response getPrintState(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getPrintState(
						user, studyID);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getPrintState error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}

	protected Response setPrintState(
		final HttpServletRequest servletRequest,
		final String userID,
		final String studyID,
		final String format,
		final InputStream inputStream) {
		
		String result = "";

		rifLogger.info(this.getClass(), "ARWS-setPrintState122 userID=="+userID+"==");
		rifLogger.info(this.getClass(), "ARWS-setPrintState122 fileFormat=="+format+"==");
		if (inputStream == null) {
			rifLogger.info(this.getClass(), "ARWS-setPrintState123 nothing submitted for input study print state");
		}
		else {
			rifLogger.info(this.getClass(), "ARWS-setPrintState123 something specified for the input file");
		}
		
		try {
			User user = createUser(servletRequest, userID);
			
			RIFStudySubmission rifStudySubmission = null;
			
			String tmpFormat = "JSON";
			rifLogger.info(this.getClass(), "ARWS-setPrintState122 fileFormat=="+format+"==");
			
			// Convert inputStream to String
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			String printStateText = baos.toString("UTF-8");		

			RIFStudyResultRetrievalAPI studyResultRetrievalService =
					getRIFStudyResultRetrievalService();
			studyResultRetrievalService.setPrintState(
				user,
				studyID,
				printStateText);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".setPrintState error", exception);
			result
				= serialiseException(
					servletRequest,
					exception);
		}	
		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response getPostalCodeCapabilities(
			final HttpServletRequest servletRequest,
			final String userID,
			final String geographyName) {			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				Geography geography = Geography.newInstance(geographyName, "");
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getPostalCodeCapabilities(
						user, geography);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getPostalCodeCapabilities error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
		}
	
	protected Response getPostalCodes(
			final HttpServletRequest servletRequest,
			final String userID,
			final String geographyName,
			final String postcode) {
			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				Geography geography = Geography.newInstance(geographyName, "");
				Locale locale = servletRequest.getLocale();
				if (locale == null) {
					locale=Locale.US;
				}
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				result = studyResultRetrievalService.getPostalCodes(
						user, geography, postcode, locale);
				
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getPostalCodes error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
		}
		
	protected Response getTileMakerCentroids(
			final HttpServletRequest servletRequest,
			final String userID,
			final String geographyName,
			final String geoLevelSelectName) {
			
			String result;
			
			try {
				//Convert URL parameters to RIF service API parameters
				User user = createUser(servletRequest, userID);
				Geography geography = Geography.newInstance(geographyName, "");
				GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance(geoLevelSelectName);
				
				//Call service API
				RIFStudyResultRetrievalAPI studyResultRetrievalService =
						getRIFStudyResultRetrievalService();
				
				RIFResultTable resultTable = studyResultRetrievalService.getTileMakerCentroids(
						user, geography, geoLevelSelect);
				
				RIFResultTableJSONGenerator rifResultTableJSONGenerator =
						new RIFResultTableJSONGenerator();
				result = rifResultTableJSONGenerator.writeResultTable(resultTable);
			} catch(Exception exception) {
				rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getTileMakerCentroids error", exception);
				result = serialiseException(servletRequest, exception);
			}
			
			return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
		}

	protected Response getTileMakerAttributes(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName,
		final String geoLevelSelectName) {
		
		String result;
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance(geoLevelSelectName);
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			result
				= studyResultRetrievalService.getTileMakerAttributes(
					user,
					geography,
					geoLevelSelect);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                ".getTileMakerAttributes error", exception);
			result
				= serialiseException(
					servletRequest,
					exception);
		}
		

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}
	
	protected Response getTileMakerTiles(
		final HttpServletRequest servletRequest,
		final String userID,
		final String geographyName,
		final String geoLevelSelectName,
		final Integer zoomlevel,
		final Integer x,
		final Integer y,
		final String tileType) {
		
		String result;
		boolean isAnError=false;
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect = GeoLevelSelect.newInstance(geoLevelSelectName);
			
			if (tileType != null && !tileType.equals("geojson") && !tileType.equals("topojson") && !tileType.equals("png")) {
				throw new Exception("Invalid tileType: " + tileType);
			}
			
			//Call service API
			RIFStudyResultRetrievalAPI studyResultRetrievalService
				= getRIFStudyResultRetrievalService();
			result
				= studyResultRetrievalService.getTileMakerTiles(
					user,
					geography,
					geoLevelSelect,
					zoomlevel,
					x,
					y,
					tileType);
		}
		catch(RIFTilesException rifTilesException) {
			isAnError=true;
			rifLogger.warning(this.getClass(), rifTilesException.getMessage());
			result
				= serialiseException(
					servletRequest,
					rifTilesException);
		}
		catch(Exception exception) {
			isAnError=true;
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                ".getTileMakerTiles error", exception);
			result
				= serialiseException(
					servletRequest,
					exception);
		}
		
		Response response;
		if (!isAnError && tileType != null && tileType.equals("png")) { // PNG tile create
			response = webServiceResponseGenerator.generateWebServicePngResponse(
					servletRequest,
					result);
		}
		else { // Usual JSON response
			response = webServiceResponseGenerator.generateWebServiceResponse(
					servletRequest,
					result);			
		}
				
		return response;
	}
	
	Response getStudySubmission(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {

		String result;

		try {
			User user = createUser(servletRequest, userID);

			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();

			DiseaseMappingStudy diseaseMappingStudy =
				studySubmissionService.getDiseaseMappingStudy(
					user,
					studyID);

			SampleTestObjectGenerator generator = new SampleTestObjectGenerator();
			RIFStudySubmission sampleStudySubmission
				= generator.createSampleRIFJobSubmission();
			sampleStudySubmission.setStudy(diseaseMappingStudy);

			RIFStudySubmissionXMLWriter writer = new RIFStudySubmissionXMLWriter();
			String xmlResults
				= writer.writeToString(
					user,
					sampleStudySubmission);

			JSONObject jsonObject
				= org.json.XML.toJSONObject(xmlResults);

			//run through XML To JSON converter
			result = jsonObject.toString(4);
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getStudySubmission error", rifServiceException);
			result
				= serialiseException(
					servletRequest,
					rifServiceException);
		}

		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	protected Response createZipFile(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID,
			final String zoomLevel) {

		String result = "{\"status\":\"OK\"}";

		try {
			Locale locale = servletRequest.getLocale();
			User user = createUser(servletRequest, userID);

			String url = new UrlFromServletRequest(servletRequest).get();
				
			RIFStudySubmissionAPI studySubmissionService
			= getRIFStudySubmissionService();

			studySubmissionService.createStudyExtract(
					user,
					studyID,
					zoomLevel,
					locale,
					url);
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".createZipFile error", rifServiceException);
			result
			= serialiseException(
					servletRequest,
					rifServiceException);
		}

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}

	protected Response getExtractStatus(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {
			
		String result = null;

		try {
			User user = createUser(servletRequest, userID);

			RIFStudySubmissionAPI studySubmissionService
			= getRIFStudySubmissionService();

			result=studySubmissionService.getExtractStatus(
					user,
					studyID);
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getExtractStatus error", rifServiceException);
			result
			= serialiseException(
					servletRequest,
					rifServiceException);
		}

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}
	
	protected Response getJsonFile(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID) {
			
		String result = null;

		try {
			Locale locale = servletRequest.getLocale();
			User user = createUser(servletRequest, userID);
/*
getContextPath()        no      /app
getLocalAddr()                  127.0.0.1
getLocalName()                  30thh.loc
getLocalPort()                  8480
getMethod()                     GET
getPathInfo()           yes     /a?+b
getProtocol()                   HTTP/1.1
getQueryString()        no      p+1=c+d&p+2=e+f
getRequestedSessionId() no      S%3F+ID
getRequestURI()         no      /app/test%3F/a%3F+b;jsessionid=S+ID
getRequestURL()         no      http://30thh.loc:8480/app/test%3F/a%3F+b;jsessionid=S+ID
getScheme()                     http
getServerName()                 30thh.loc
getServerPort()                 8480
getServletPath()        yes     /test?
getParameterNames()     yes     [p 2, p 1]
getParameter("p 1")     yes     c d
 */
			rifLogger.debug(this.getClass(),
				"getJsonFile() getLocalName: " + servletRequest.getLocalName() +
				"; getScheme: " + servletRequest.getScheme() +
				"; getLocalAddr: " + servletRequest.getLocalAddr() +
				"; getLocalPort: " + servletRequest.getLocalPort() +
				"; getServerName: " + servletRequest.getServerName() +
				"; getServerPort: " + servletRequest.getServerPort());

			String url = new UrlFromServletRequest(servletRequest).get();

			RIFStudySubmissionAPI studySubmissionService
			= getRIFStudySubmissionService();

			result=studySubmissionService.getJsonFile(
					user,
					studyID,
					locale,
					url);
		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getJsonFile error", rifServiceException);
			result
			= serialiseException(
					servletRequest,
					rifServiceException);
		}

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}
	
	protected Response getFrontEndParameters(
			final HttpServletRequest servletRequest,
			final String userID) {
			
		String result = null;

		User user = createUser(servletRequest, userID);

		RIFStudySubmissionAPI studySubmissionService
		= getRIFStudySubmissionService();

		result=studySubmissionService.getFrontEndParameters(
				user);

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}
	
	protected Response getZipFile(
			final HttpServletRequest servletRequest,
			final String userID,
			final String studyID,
			final String zoomLevel) {

		StringBuilder result = new StringBuilder();
		FileInputStream fileInputStream = null;
		String fileName = null;
		
		try {
			User user = createUser(servletRequest, userID);

			RIFStudySubmissionAPI studySubmissionService
			= getRIFStudySubmissionService();

			fileName=studySubmissionService.getStudyExtractFIleName(
					user,
					studyID);
					
			fileInputStream=studySubmissionService.getStudyExtract(
					user,
					studyID,
					zoomLevel);
				
			if (fileInputStream != null) {
				return webServiceResponseGenerator.generateWebServiceResponse( // streaming version
						servletRequest,
						fileInputStream,
						fileName);
			}
			else {
				return webServiceResponseGenerator.generateWebServiceResponse(
						servletRequest,
						""); // Null response
			}

		}
		catch(RIFServiceException rifServiceException) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getZipFile getStudyExtract error", rifServiceException);
			String message=serialiseException(
						servletRequest,
						rifServiceException);
			return webServiceResponseGenerator.generateWebServiceResponse(
					servletRequest,
					message);
		}
		catch(IOException exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getZipFile IO error", exception);
			String message=serialiseException(
						servletRequest,
						exception);
			return webServiceResponseGenerator.generateWebServiceResponse(
					servletRequest,
					message);
		}
	}
	
	protected Response submitStudy(
		final HttpServletRequest servletRequest,
		final String userID,
		final String format,
		final InputStream inputStream) {
		
		String result = "";

		rifLogger.info(this.getClass(), "ARWS-submitStudy122 userID=="+userID+"==");
		rifLogger.info(this.getClass(), "ARWS-submitStudy122 fileFormat=="+format+"==");
		if (inputStream == null) {
			rifLogger.info(this.getClass(), "ARWS-submitStudy123 nothing submitted for input study");
		}
		else {
			rifLogger.info(this.getClass(), "ARWS-submitStudy123 something specified for the input file");
		}
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			
			RIFStudySubmission rifStudySubmission;
			
			String tmpFormat = "JSON";
			rifLogger.info(this.getClass(), "ARWS-submitStudy122 fileFormat=="+format+"==");
			if (StudySubmissionFormat.JSON.matchesFormat(tmpFormat)) {
				rifLogger.info(this.getClass(), "ARWS-submitStudy122 JSON");
				rifStudySubmission = getRIFSubmissionFromJSONSource(inputStream);
			} else {
				rifLogger.info(this.getClass(), "ARWS-submitStudy122 ");
				rifStudySubmission = RIFStudySubmission.newInstance(inputStream);
			}

			rifLogger.info(getClass(),"RIFStudySubmission created; type is " +
			                          (rifStudySubmission.getStudy().isRiskAnalysis()
					                           ? "Risk Analysis" : "Disease Mapping"));
			
			rifStudySubmission.checkErrors(AbstractRIFConcept.ValidationPolicy.RELAXED);

			RIFStudySubmissionAPI studySubmissionService = getRIFStudySubmissionService();
			studySubmissionService.submitStudy(user, rifStudySubmission, null,
			                                   new UrlFromServletRequest(servletRequest).get());
		} catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".submitStudy error", exception);
			result = serialiseException(servletRequest, exception);
		}
		
		
		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	private RIFStudySubmission getRIFSubmissionFromJSONSource(
		final InputStream inputStream)
		throws RIFServiceException {
		
		String xmlString = null;
		try {
			rifLogger.info(this.getClass(), "ARWS - getRIFSubmissionFromJSONSource start");
			BufferedReader reader
				= new BufferedReader(
					new InputStreamReader(inputStream, "UTF-8"));
			
			StringBuilder buffer = new StringBuilder();
			String currentInputLine
				= reader.readLine();
			while (currentInputLine != null) {
				buffer.append(currentInputLine);
				currentInputLine = reader.readLine();
			}
			reader.close();
			
			JSONObject jsonObject = new JSONObject(buffer.toString());
			rifLogger.debug(this.getClass(), "ARWS - JSON from UI==" + lineSeparator + jsonObject.toString(2) + lineSeparator + "==");
			
			xmlString = XML.toString(jsonObject).replace("\r\n", "").replace("\n", "");
			// Try to pretty print XML to make parsing errors readable
			try {
				StreamSource source = new StreamSource(new StringReader(xmlString));
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				//initialize StreamResult with File object to save to file
				StreamResult result = new StreamResult(new StringWriter());
				transformer.transform(source, result);
				xmlString = result.getWriter().toString(); 
			}
			catch (Exception e) {
				rifLogger.warning(getClass(), "ARWS - pretty print XML SAXParseException: " + e.getMessage() + "==" + lineSeparator +
				xmlString + lineSeparator + "==");
			}

			InputStream xmlInputStream = new ByteArrayInputStream(
					xmlString.getBytes(StandardCharsets.UTF_8));
			rifLogger.debug(getClass(), "ARWS - getRIFSubmissionFromJSONSource JSON TO XML==" + lineSeparator +
				xmlString + lineSeparator + "==");
			RIFStudySubmission rifStudySubmission = RIFStudySubmission.newInstance(xmlInputStream);
			
			// Parse out study_selection to avoid using the XML parser
			JSONObject rifJobSubmission = jsonObject.optJSONObject("rif_job_submission");
			if (rifJobSubmission != null) {
				JSONObject study = rifJobSubmission.optJSONObject("disease_mapping_study");
				if (study == null) {
					study = rifJobSubmission.optJSONObject("risk_analysis_study");
				}

				if (study == null) {

					throw new IllegalStateException("Invalid data received: JSON contains "
					                                + "neither 'disease_mapping_study' nor "
					                                + "'risk_analysis_study'. Complete JSON is: "
					                                + lineSeparator + jsonObject.toString(2));
				}

				String name = study.optString("name");
				String description = study.optString("description");
				String riskAnalysisDescription;
				String studyType;
				JSONObject studySelection = rifJobSubmission.optJSONObject("study_selection");
				if (studySelection != null) {

					// Note that disease mapping studies do not have a corresponding description
					// field.
					riskAnalysisDescription = studySelection.optString("riskAnalysisDescription");
					studyType = studySelection.optString("studyType");

					if (riskAnalysisDescription != null && riskAnalysisDescription.length() > 0) {

						if (rifStudySubmission.getStudy().isRiskAnalysis()) {

							rifLogger.info(this.getClass(),
							               "ARWS - study_selection risk analysis: "
							               + riskAnalysisDescription);
						} else {

							throw new Exception("Parsed risk_analysis_study: \"" + name
							                    + "\"; description: " + description
							                    + " but study_selection.studyType is: " + studyType);
						}
					} else {

						if (rifStudySubmission.getStudy().isDiseaseMapping()) {
							rifLogger.info(this.getClass(),
							               "ARWS - study_selection disease mapping");
						} else {

							throw new Exception("Parsed disease_mapping_study: \"" + name
							                    + "\"; description: " + description
							                    + " but study_selection.studyType is: " + studyType);
						}
					}
					rifStudySubmission.setStudySelection(studySelection);
				}
			}
				
			return rifStudySubmission;

		} catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".getRIFSubmissionFromJSONSource error XML==" + lineSeparator +
				xmlString + lineSeparator + "==", exception);
			String errorMessage
				= RIFServiceMessages.getMessage("webService.submitStudy.error.unableToConvertJSONToXML");

			throw new RIFServiceException(
				RIFServiceError.UNABLE_TO_PARSE_JSON_SUBMISSION,
				errorMessage);
		}
		
	}
	
	User createUser(
			final HttpServletRequest servletRequest,
			final String userID) {
		
		String ipAddress  = servletRequest.getHeader("X-FORWARDED-FOR");
		if(ipAddress == null) {
			ipAddress = servletRequest.getRemoteAddr();
		}
		
		User user = User.newInstance(userID, ipAddress);
		
		return user;
	}
	
	/**
	 * takes advantage of the Jackson project library to serialise objects
	 * for the JSON format.
	 * @param objectToWrite
	 * @return
	 * @throws Exception
	 */
	protected String serialiseArrayResult(
		final HttpServletRequest servletRequest,
		final Object objectToWrite)
		throws Exception {

		printClientInformation("serialiseArrayResult", servletRequest);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, objectToWrite);
		final byte[] data = out.toByteArray();
		return(new String(data));
	}
	
	//TOUR_WEB_SERVICES-5
	/*
	 * So far in our tour, we have created a GeoLevelAreasProxy object that can contain
	 * data from an array of GeoLevelArea objects which considers the needs of JavaScript
	 * clients.  By catering to a particular type of front end client, the proxy classes
	 * are almost between presentatation and business concept layers.
	 *
	 * All the methods beginning with "serialise" are meant to hide references to
	 * Jackson's ObjectMapper class.  We do this so we have a minimum amount of code that is
	 * dependent on referencing Jackson's class libraries.
	 *
	 */
	protected String serialiseSingleItemAsArrayResult(
		final HttpServletRequest servletRequest,
		final Object objectToWrite)
		throws Exception {

		final ArrayList<Object> objectArrayList = new ArrayList<Object>();
		objectArrayList.add(objectToWrite);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, objectArrayList);
		final byte[] data = out.toByteArray();
		
		return new String(data);
	}
	
	protected String serialiseStringResult(
		final String result)
		throws Exception {

		StringBuilder responseText = new StringBuilder();
		responseText.append("[{\"");
		String resultPropertyName
			= RIFServiceMessages.getMessage("webService.json.messagePropertyName");
		responseText.append(resultPropertyName);
		responseText.append("\"");
		responseText.append(":");
		responseText.append("\"");
		responseText.append(result);
		responseText.append("\"}]");

		return responseText.toString();
	}
	
	protected String serialiseNamedArray(
		final String arrayName,
		ArrayList<String> listItems) {
		
		StringBuilder json = new StringBuilder();
		json.append("{\"");
		json.append(arrayName);
		json.append("{\":[");
		for (int i = 0 ; i < listItems.size(); i++) {
			if (i != 0) {
				json.append(",");
			}
			json.append("\"");
			json.append(String.valueOf(listItems.get(i)));
			json.append("\"");
		}
		
		json.append("]}");
		
		return json.toString();
	}
	
	protected String serialiseException(
		final HttpServletRequest servletRequest,
		final Exception exceptionThrownByRIFService) {
	
		printClientInformation("serialiseException", servletRequest);
		
		String result = "";
		try {
			RIFServiceExceptionProxy rifServiceExceptionProxy
				= new RIFServiceExceptionProxy();
			if (exceptionThrownByRIFService instanceof RIFServiceException) {
				RIFServiceException rifServiceException
					= (RIFServiceException) exceptionThrownByRIFService;
				List<String> errorMessages = rifServiceException.getErrorMessages();
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
				String timeStamp = DATE_FORMAT.format(new Date());
				errorMessages[0]
					= RIFServiceMessages.getMessage(
						"webServices.error.unexpectedError",
						timeStamp);
			
				rifServiceExceptionProxy.setErrorMessages(errorMessages);
			}
			result = serialiseSingleItemAsArrayResult(
				servletRequest,
				rifServiceExceptionProxy);
		}
		catch(Exception exception) {
			rifLogger.error(this.getClass(), getClass().getSimpleName() +
			                                 ".serialiseException error", exception);
			//Convert exceptions to support JSON
			serialiseException(
				servletRequest,
				exception);
			String timeStamp = DATE_FORMAT.format(new Date());
			result
				= RIFServiceMessages.getMessage(
					"webServices.error.unableToProvideError",
					timeStamp);
		}
		
		return result;
	}
	
	private void printClientInformation(
		final String messageHeader,
		final HttpServletRequest servletRequest) {
		
		String browserType = servletRequest.getHeader("User-Agent");
		String mimeTypes = servletRequest.getHeader("Accept");
		HttpSession session = servletRequest.getSession();
		String sessionID = session.getId();
		//String ipAddress = servletRequest.get
		
		StringBuilder message = new StringBuilder();
		message.append("==================================================\n");
		message.append(messageHeader);
		message.append(":");
		message.append("browser type:=="+browserType+"==\n");
		message.append("mime types:=="+mimeTypes+"==\n");
		message.append("session id:=="+sessionID+"==\n");
		message.append("==================================================\n");
		//message.append("IP address:=="+ipAdress+"")
		rifLogger.debug(this.getClass(), message.toString());
		
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
		buffer.append(DATE_FORMAT.format(date));
		buffer.append("(");
		long elapsed = date.getTime() - startTime.getTime();
		buffer.append(elapsed);
		buffer.append(" milliseconds since start time");
		rifLogger.info(this.getClass(), buffer.toString());
	}
}
