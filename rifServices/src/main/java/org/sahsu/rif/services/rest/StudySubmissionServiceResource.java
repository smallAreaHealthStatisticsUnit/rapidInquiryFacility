package org.sahsu.rif.services.rest;

import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.util.UrlFromServletRequest;
import org.sahsu.rif.services.concepts.AbstractCovariate;
import org.sahsu.rif.services.concepts.AdjustableCovariate;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.AgeGroupSortingOption;
import org.sahsu.rif.services.concepts.CalculationMethod;
import org.sahsu.rif.services.concepts.GeoLevelToMap;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.HealthTheme;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.concepts.Project;
import org.sahsu.rif.services.concepts.RIFStudySubmissionAPI;
import org.sahsu.rif.services.concepts.Sex;
import org.sahsu.rif.services.system.RIFServiceMessages;

import com.sun.jersey.multipart.FormDataParam;

/**
 * This class advertises API methods found in 
 * rifServices.businessConceptLayer.RIFJobSubmissionAPI
 * as a web service.  
 * 
 * Two issues have dominated the design of this class:
 * <ul>
 * <li>
 * the slight mismatch between URL parameter values and corresponding instances of Java
 * objects
 * </li>
 * <li>
 * the level of granularity in the conversations we would expect the web service to have
 * with the client
 * </li>
 * <li>
 * The efficiency with which 
 * </ul>
 * 
 * <p>
 */
@Path("/")
public class StudySubmissionServiceResource extends WebService {

private Messages GENERIC_MESSAGES = Messages.genericMessages();

	public StudySubmissionServiceResource() {
		super();
	}

	@GET
	@Produces({"application/json"})
	@Path("/login")
	public Response login(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("password") String password) {

		return super.login(
				servletRequest,
				userID,
				password);
	}

	@GET
	@Produces({"application/json"})
	@Path("/isLoggedIn")
	public Response login(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		return super.isLoggedIn(
				servletRequest,
				userID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/logout")
	public Response logout(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		return super.logout(
				servletRequest,
				userID);
	}


	@GET
	@Produces({"application/json"})
	@Path("/isInformationGovernancePolicyActive")
	public Response isInformationGovernancePolicyActive(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		return super.isInformationGovernancePolicyActive(
				servletRequest,
				userID);
	}

	//This method does not have a PG version, used before login only
	@GET
	@Produces({"application/json"})
	@Path("/getDatabaseType")
	public Response getDatabaseType(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		return super.getDatabaseType(
				servletRequest,
				userID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getFrontEndParameters")
	public Response getFrontEndParameters(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		return super.getFrontEndParameters(
				servletRequest,
				userID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeographies")
	public Response getGeographies(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		return super.getGeographies(
				servletRequest,
				userID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeoLevelSelectValues")
	public Response getGeographicalLevelSelectValues(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName) {

		return super.getGeographicalLevelSelectValues(
				servletRequest,
				userID,
				geographyName);
	}


	@GET
	@Produces({"application/json"})
	@Path("/getDefaultGeoLevelSelectValue")
	public Response getDefaultGeoLevelSelectValue(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName) {

		return super.getDefaultGeoLevelSelectValue(
				servletRequest,
				userID,
				geographyName);
	}

	//TOUR_WEB_SERVICES-2
	/*
	 * This advertises the web service for returning a collection of geographic
	 * areas.  For example, if userID=kgarwood, geographyName=UK and
	 * geoLevelSelectName="region", it might return something like:
	 * "Northwest England", "Southwest England", etc.
	 *
	 * The annotations help Jersey advertise this method to clients and
	 * they tell it how to advertise the results.
	 *
	 * Here "Path" helps determine how the web service is going to appear
	 * in the URL that is used by the client.  Here it is used in a call
	 * like:
	 *
	 * http://localhost:8080/rifServices/studySubmission/getGeoLevelAreaValues?
	 * userID=kgarwood&geographyName=SAHSULAND&geoLevelSelectName=LEVEL2
	 *
	 * The QueryParam annotations help map parameters in the URL to parameters
	 * in Java.  In this example, the formal String parameter userID would be
	 * assigned kgarwood, geographyName would be assigned 'SAHSULAND' and
	 * geoLevelSelectName would be assigned LEVEL2.
	 *
	 * Wherever possible we try to ensure that the names of parameters in the URL
	 * are exactly the same as the parameter names in the method that is used in
	 * a web service.
	 *
	 * The Produces annotation path is used to tell the client that the return
	 * value is in the format application/json.  We have this annotation set for
	 * all of the web service methods.  However, we recently discovered that
	 * IE 11+ and Google Chrome do not support JSON format equally well.
	 *
	 * We now end up overriding the assumption that we return "application/json"
	 * and at some point we may try to remove the Produces tag.
	 *
	 * Because so many web service methods will be shared between study submission
	 * and study result retrieval, we have moved a lot of the method implementations
	 * into a superclass.
	 *
	 * Originally I tried to move all of these annotations into the superclass as well.
	 * However, I encountered an error and (consulting poor memory!) it either indicated that
	 * indicated there was confusion about this method being in an abstract class or that
	 * there was some kind of ambiguous interpretation about whether the method was part
	 * of the study submission or study result retrieval service.
	 *
	 * The problems disappeared when I made copies of the annotations in the subclassess,
	 * as the code below shows.  This problem should be revisited later on.
	 */
	@GET
	@Produces({"application/json"})
	@Path("/getGeoLevelAreaValues")
	public Response getGeoLevelAreaValues(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("geoLevelSelectName") String geoLevelSelectName) {

		return super.getGeoLevelAreaValues(
				servletRequest,
				userID,
				geographyName,
				geoLevelSelectName);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getGeoLevelViews")
	public Response getGeoLevelViewValues(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("geoLevelSelectName") String geoLevelSelectName) {

		return super.getGeoLevelViewValues(
				servletRequest,
				userID,
				geographyName,
				geoLevelSelectName);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getNumerator")
	public Response getNumerator(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("healthThemeDescription") String healthThemeDescription) {


		return super.getNumerator(
				servletRequest,
				userID,
				geographyName,
				healthThemeDescription);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getDenominator")
	public Response getDenominator(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("healthThemeDescription") String healthThemeDescription) {

		return super.getDenominator(
				servletRequest,
				userID,
				geographyName,
				healthThemeDescription);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getYearRange")
	public Response getYearRange(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("numeratorTableName") String numeratorTableName) {

		return super.getYearRange(
				servletRequest,
				userID,
				geographyName,
				numeratorTableName);
	}

	/**
	 * @param userID
	 * @return
	 */
	@GET
	@Produces({"application/json"})
	@Path("/getAvailableCalculationMethods")
	public Response getAvailableCalculationMethods(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		String result = "";


		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
					= getRIFStudySubmissionService();
			ArrayList<CalculationMethod> calculationMethods
					= studySubmissionService.getAvailableCalculationMethods(user);

			//Convert results to support JSON
			ArrayList<CalculationMethodProxy> calculationMethodProxies
					= new ArrayList<>();
			for (CalculationMethod calculationMethod : calculationMethods) {
				CalculationMethodProxy calculationMethodProxy
						= new CalculationMethodProxy();
				calculationMethodProxy.setCodeRoutineName(calculationMethod.getCodeRoutineName());
				calculationMethodProxy.setDescription(calculationMethod.getDescription());
				calculationMethodProxy.setPrior(calculationMethod.getPrior().getName());
				List<Parameter> parameters = calculationMethod.getParameters();
				List<ParameterProxy> parameterProxies = new ArrayList<>();
				for (Parameter parameter : parameters) {
					ParameterProxy parameterProxy = new ParameterProxy();
					parameterProxy.setName(parameter.getName());
					parameterProxy.setValue(parameter.getValue());
					parameterProxies.add(parameterProxy);
				}
				calculationMethodProxy.setParameterProxies(parameterProxies);
				calculationMethodProxies.add(calculationMethodProxy);
			}
			result
					= serialiseArrayResult(
					servletRequest,
					calculationMethodProxies);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			exception.printStackTrace(System.out);
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getProjects")
	public Response getProjects(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		String result = "";


		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
					= getRIFStudySubmissionService();
			ArrayList<Project> projects
					= studySubmissionService.getProjects(user);

			//Convert results to support JSON
			ArrayList<ProjectProxy> projectProxies
					= new ArrayList<ProjectProxy>();
			for (Project project : projects) {
				ProjectProxy projectProxy
						= new ProjectProxy();
				projectProxy.setName(project.getName());
				projectProxies.add(projectProxy);
			}
			result
					= serialiseArrayResult(
					servletRequest,
					projectProxies);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getProjectDescription")
	public Response getProjectDescription(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("projectName") String projectName) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);


			//Call service API
			RIFStudySubmissionAPI studySubmissionService
					= getRIFStudySubmissionService();
			ArrayList<Project> projects
					= studySubmissionService.getProjects(user);

			//Convert results to support JSON
			Collator collator = GENERIC_MESSAGES.getCollator();
			Project selectedProject = null;
			for (Project project : projects) {
				if (collator.equals(projectName, project.getName())) {
					selectedProject = project;
					break;
				}
			}
			if (selectedProject == null) {
				result
						= RIFServiceMessages.getMessage(
						"webService.getProjectDescription.error.projectNotFound",
						projectName);
			}
			else {
				result
						= serialiseStringResult(
						selectedProject.getDescription());
			}

		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}

	@POST
	@Produces({"application/json"})
	@Path("/test")
	public Response test(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService = getRIFStudySubmissionService();
			studySubmissionService.test(user, new UrlFromServletRequest(servletRequest).get());
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			//Convert exceptions to support JSON
			result = serialiseException(servletRequest, exception);
		}
		WebServiceResponseGenerator webServiceResponseGenerator = getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(servletRequest, result);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getHealthThemes")
	public Response getHealthThemes(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName) {

		String result = "";


		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			Geography geography = Geography.newInstance(geographyName, "");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
					= getRIFStudySubmissionService();

			ArrayList<HealthTheme> healthThemes
					= studySubmissionService.getHealthThemes(
					user,
					geography);

			//Convert results to support JSON
			ArrayList<HealthThemeProxy> healthThemeProxies
					= new ArrayList<HealthThemeProxy>();
			for (HealthTheme healthTheme : healthThemes) {
				HealthThemeProxy healthThemeProxy
						= new HealthThemeProxy();
				healthThemeProxy.setName(healthTheme.getName());
				healthThemeProxy.setDescription(healthTheme.getDescription());
				healthThemeProxies.add(healthThemeProxy);
			}
			result
					= serialiseArrayResult(
					servletRequest,
					healthThemeProxies);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}


		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getSexes")
	public Response getSexes(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
					= getRIFStudySubmissionService();
			ArrayList<Sex> sexes
					= studySubmissionService.getSexes(
					user);

			//We should be guaranteed that at least one pair will be returned.
			//All the numerators returned should have the same denominator
			//Therefore, we should be able to pick the first ndPair and extract
			//the denominator.
			SexesProxy sexesProxy = new SexesProxy();
			ArrayList<String> sexNames = new ArrayList<String>();

			for (Sex sex : sexes) {
				sexNames.add(sex.getName());
			}

			//Convert results to support JSON
			sexesProxy.setNames(sexNames.toArray(new String[0]));
			result
					= serialiseSingleItemAsArrayResult(
					servletRequest,
					sexesProxy);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}

		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}


	@GET
	@Produces({"application/json"})
	@Path("/getCovariates")
	public Response getCovariates(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("geoLevelToMapName") String geoLevelToMapName) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			Geography geography
					= Geography.newInstance(geographyName, "");
			GeoLevelToMap geoLevelToMap
					= GeoLevelToMap.newInstance(geoLevelToMapName);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
					= getRIFStudySubmissionService();
			ArrayList<AbstractCovariate> covariates
					= studySubmissionService.getCovariates(
					user,
					geography,
					geoLevelToMap);

			//Convert results to support JSON
			ArrayList<CovariateProxy> covariateProxies
					= new ArrayList<CovariateProxy>();
			for (AbstractCovariate covariate : covariates) {
				CovariateProxy covariateProxy
						= new CovariateProxy();
				if (covariate instanceof AdjustableCovariate) {
					covariateProxy.setCovariateType("adjustable");
				}
				else {
					covariateProxy.setCovariateType("exposure");
				}
				covariateProxy.setName(covariate.getName());
				covariateProxy.setMinimumValue(covariate.getMinimumValue());
				covariateProxy.setMaximumValue(covariate.getMaximumValue());
				covariateProxies.add(covariateProxy);
			}

			result
					= serialiseArrayResult(
					servletRequest,
					covariateProxies);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}


		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);

	}

	@GET
	@Produces({"application/json"})
	@Path("/getAgeGroups")
	public Response getAgeGroups(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("geographyName") String geographyName,
			@QueryParam("numeratorTableName") String numeratorTableName) {

		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);
			Geography geography = Geography.newInstance(geographyName, "xxx");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
					= getRIFStudySubmissionService();
			NumeratorDenominatorPair ndPair
					= studySubmissionService.getNumeratorDenominatorPairFromNumeratorTable(
					user,
					geography,
					numeratorTableName);
			List<AgeGroup> ageGroups
					= studySubmissionService.getAgeGroups(
					user,
					geography,
					ndPair,
					AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);

			//Convert results to support JSON
			AgeGroupJSONGenerator ageGroupJSONGenerator
					= new AgeGroupJSONGenerator();
			result
					= ageGroupJSONGenerator.writeJSONMapAreas(ageGroups);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result
					= serialiseException(
					servletRequest,
					exception);
		}


		WebServiceResponseGenerator webServiceResponseGenerator
				= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
				servletRequest,
				result);
	}


	/*
	@GET
	@Produces({"application/json"})
	@Path("/getHealthCodesForSearchText")
	public Response getHealthCodesForSearchText(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID,
		@QueryParam("healthCodeTaxonomyNameSpace") String healthCodeTaxonomyNameSpace,
		@QueryParam("searchText") String searchText,
		@DefaultValue("false") @QueryParam("isContextSensitive") boolean isContextSensitive) {

		String result = "";
		try {
			//Convert URL parameters to RIF service API parameters
			User user = User.newInstance(userID, "xxx");

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();
			HealthCodeTaxonomy healthCodeTaxonomy
				= studySubmissionService.getHealthCodeTaxonomyFromNameSpace(
					user,
					healthCodeTaxonomyNameSpace);

			ArrayList<HealthCode> healthCodes
				= studySubmissionService.getHealthCodesMatchingSearchText(
					user,
					healthCodeTaxonomy,
					searchText,
					isContextSensitive);

			//Convert results to support JSON
			ArrayList<HealthCodeProxy> healthCodeProxies
				= new ArrayList<HealthCodeProxy>();
			for (HealthCode healthCode : healthCodes) {
				HealthCodeProxy healthCodeProxy
					= new HealthCodeProxy();
				healthCodeProxy.setCode(healthCode.getCode());
				healthCodeProxy.setDescription(healthCode.getDescription());
				healthCodeProxy.setNameSpace(healthCode.getNameSpace());
				healthCodeProxy.setIsTopLevelTerm(String.valueOf(healthCode.isTopLevelTerm()));
				healthCodeProxy.setNumberOfSubTerms(String.valueOf(healthCode.getNumberOfSubTerms()));
				healthCodeProxies.add(healthCodeProxy);
			}
			result
				= serialiseArrayResult(
					servletRequest,
					healthCodeProxies);
		}
		catch(Exception exception) {
			//Convert exceptions to support JSON
			result
				= serialiseException(
					servletRequest,
					exception);
		}

		return generateAppropriateContentTypeResponse(
			servletRequest,
			result);
	}
*/

	@POST
	@Produces({"application/json"})
	@Path("/submitStudy")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response submitStudy(
			@Context HttpServletRequest servletRequest,
			@FormDataParam("userID") String userID,
			@FormDataParam("fileFormat") String fileFormat,
			@FormDataParam("fileField") InputStream inputStream) {

		return super.submitStudy(
				servletRequest,
				userID,
				fileFormat,
				inputStream);
	}


	@GET
	@Produces({"application/json"})
	@Path("/getStudySubmission")
	public Response getstudy(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID) {


		return super.getStudySubmission(
				servletRequest,
				userID,
				studyID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getExtractStatus")
	public Response getExtractStatus(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID) {

		return super.getExtractStatus(
				servletRequest,
				userID,
				studyID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/createZipFile")
	public Response createZipFile(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID,
			@QueryParam("zoomLevel") String zoomLevel) {

		return super.createZipFile(
				servletRequest,
				userID,
				studyID,
				zoomLevel);
	}

	@GET
	@Produces({"application/zip"})
	@Path("/getZipFile")
	public Response getZipFile(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID,
			@QueryParam("zoomLevel") String zoomLevel) {

		return super.getZipFile(
				servletRequest,
				userID,
				studyID,
				zoomLevel);
	}

	@GET
	@Produces({"application/json"})
	@Path("/getJsonFile")
	public Response getJsonFile(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("studyID") String studyID) {

		return super.getJsonFile(
				servletRequest,
				userID,
				studyID);
	}

	@GET
	@Produces({"application/json"})
	@Path("/rifFrontEndLogger")
	public Response rifFrontEndLogger(
			@Context HttpServletRequest servletRequest,
			@QueryParam("userID") String userID,
			@QueryParam("browserType") String browserType,
			@QueryParam("messageType") String messageType,
			@QueryParam("message") String message,
			@QueryParam("errorMessage") String errorMessage,
			@QueryParam("errorStack") String errorStack,
			@QueryParam("actualTime") String actualTime,
			@QueryParam("relativeTime") String relativeTime) {

		return super.rifFrontEndLogger(
				servletRequest,
				userID,
				browserType,
				messageType,
				message,
				errorMessage,
				errorStack,
				actualTime,
				relativeTime);
	}
}
