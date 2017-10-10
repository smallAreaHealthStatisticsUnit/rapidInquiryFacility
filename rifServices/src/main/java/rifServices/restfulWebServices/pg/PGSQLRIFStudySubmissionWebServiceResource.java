package rifServices.restfulWebServices.pg;

import rifServices.system.RIFServiceMessages;
import rifServices.businessConceptLayer.*;
import rifGenericLibrary.system.RIFGenericLibraryMessages;
import rifGenericLibrary.businessConceptLayer.Parameter;
import rifGenericLibrary.businessConceptLayer.User;

import com.sun.jersey.multipart.*;

import javax.ws.rs.*;

import rifServices.restfulWebServices.*;

import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;

import java.text.Collator;
import java.util.ArrayList;
import java.io.*;



/**
 * This class advertises API methods found in 
 * {@link rifServices.businessConceptLayer.RIFJobSubmissionAPI}
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
 * 
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

@Path("/")
public class PGSQLRIFStudySubmissionWebServiceResource 
	extends PGSQLAbstractRIFWebServiceResource {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLRIFStudySubmissionWebServiceResource() {
		super();

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
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
				= new ArrayList<CalculationMethodProxy>();
			for (CalculationMethod calculationMethod : calculationMethods) {
				CalculationMethodProxy calculationMethodProxy
					= new CalculationMethodProxy();
				calculationMethodProxy.setCodeRoutineName(calculationMethod.getCodeRoutineName());
				calculationMethodProxy.setDescription(calculationMethod.getDescription());
				calculationMethodProxy.setPrior(calculationMethod.getPrior().getName());
				ArrayList<Parameter> parameters = calculationMethod.getParameters();
				ArrayList<ParameterProxy> parameterProxies 
					= new ArrayList<ParameterProxy>();
				for (Parameter parameter : parameters) {
					ParameterProxy parameterProxy 
						= new ParameterProxy();
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
			Collator collator = RIFGenericLibraryMessages.getCollator();
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
			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();			
			studySubmissionService.test(user);
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
			ArrayList<AgeGroup> ageGroups
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

	@GET
	@Produces({"application/json"})	
	@Path("/getHealthCodeTaxonomies")
	public Response getHealthCodeTaxonomies(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID) {
	
		String result = "";

		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();			
			ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
				= studySubmissionService.getHealthCodeTaxonomies(user);

			//Convert results to support JSON						
			ArrayList<HealthCodeTaxonomyProxy> healthCodeTaxonomyProxies
				 = new ArrayList<HealthCodeTaxonomyProxy>();
			for (HealthCodeTaxonomy healthCodeTaxonomy : healthCodeTaxonomies) {
				HealthCodeTaxonomyProxy healthCodeTaxonomyProxy
					= new HealthCodeTaxonomyProxy();
				healthCodeTaxonomyProxy.setName(healthCodeTaxonomy.getName());
				healthCodeTaxonomyProxy.setDescription(healthCodeTaxonomy.getDescription());
				healthCodeTaxonomyProxy.setNameSpace(healthCodeTaxonomy.getNameSpace());
				healthCodeTaxonomyProxy.setVersion(healthCodeTaxonomy.getVersion());
				healthCodeTaxonomyProxies.add(healthCodeTaxonomyProxy);
			}
			result 
				= serialiseArrayResult(
					servletRequest,
					healthCodeTaxonomyProxies);
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
	@Path("/getTopLevelCodes")
	public Response getTopLevelCodes(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID,
		@QueryParam("healthCodeTaxonomyNameSpace") String healthCodeTaxonomyNameSpace) {
			
		String result = "";
		
		try {
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();			
			HealthCodeTaxonomy healthCodeTaxonomy
				= studySubmissionService.getHealthCodeTaxonomyFromNameSpace(
					user, 
					healthCodeTaxonomyNameSpace);
			ArrayList<HealthCode> healthCodes
				= studySubmissionService.getTopLevelHealthCodes(user, healthCodeTaxonomy);

			//Convert results to support JSON						
			ArrayList<HealthCodeProxy> healthCodeProxies
				= new ArrayList<HealthCodeProxy>();
			for (HealthCode healthCode : healthCodes) {
				HealthCodeProxy healthCodeProxy = new HealthCodeProxy();
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
	
	
	@GET
	@Produces({"application/json"})	
	@Path("/getParentHealthCode")
	public Response getParentHealthCode(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID,
		@QueryParam("childHealthCode") String healthCode,
		@QueryParam("childHealthCodeNameSpace") String healthCodeNameSpace) {
	
		String result = "";
		try {			
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();			
			HealthCode childHealthCode
				= studySubmissionService.getHealthCode(
					user, 
					healthCode, 
					healthCodeNameSpace);
			HealthCode parentHealthCode
				= studySubmissionService.getParentHealthCode(user, childHealthCode);			

			//Convert results to support JSON						
			HealthCodeProxy healthCodeProxy
				= new HealthCodeProxy();
			if (parentHealthCode != null) {
				healthCodeProxy.setCode(parentHealthCode.getCode());
				healthCodeProxy.setDescription(parentHealthCode.getDescription());
				healthCodeProxy.setNameSpace(parentHealthCode.getNameSpace());
				healthCodeProxy.setIsTopLevelTerm(String.valueOf(parentHealthCode.isTopLevelTerm()));
				healthCodeProxy.setNumberOfSubTerms(String.valueOf(parentHealthCode.getNumberOfSubTerms()));
				result 
					= serialiseSingleItemAsArrayResult(
						servletRequest,
						healthCodeProxy);
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
	

	@GET
	@Produces({"application/json"})	
	@Path("/getImmediateChildHealthCodes")
	public Response getImmediateChildHealthCodes(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID,
		@QueryParam("healthCode") String healthCode,
		@QueryParam("healthCodeNameSpace") String healthCodeNameSpace) {
	
		String result = "";
		try {			
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();			
			HealthCode parentHealthCode
				= studySubmissionService.getHealthCode(
					user, 
					healthCode, 
					healthCodeNameSpace);
			ArrayList<HealthCode> healthCodes
				= studySubmissionService.getImmediateChildHealthCodes(user, parentHealthCode);
			
			//Convert results to support JSON						
			ArrayList<HealthCodeProxy> healthCodeProxies
				= new ArrayList<HealthCodeProxy>();
			for (HealthCode currentHealthCode : healthCodes) {
				HealthCodeProxy healthCodeProxy
					= new HealthCodeProxy();
				healthCodeProxy.setCode(currentHealthCode.getCode());
				healthCodeProxy.setDescription(currentHealthCode.getDescription());
				healthCodeProxy.setNameSpace(currentHealthCode.getNameSpace());
				healthCodeProxy.setIsTopLevelTerm(String.valueOf(currentHealthCode.isTopLevelTerm()));
				healthCodeProxy.setNumberOfSubTerms(String.valueOf(currentHealthCode.getNumberOfSubTerms()));
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
		
		WebServiceResponseGenerator webServiceResponseGenerator
			= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);		
	}

	@GET
	@Produces({"application/json"})	
	@Path("/getHealthCodesMatchingSearchText")
	public Response getHealthCodesMatchingSearchText(
		@Context HttpServletRequest servletRequest,
		@QueryParam("userID") String userID,
		@QueryParam("nameSpace") String nameSpace,		
		@QueryParam("searchText") String searchText,
		@QueryParam("isCaseSensitive") boolean isCaseSensitive) {
		
		String result = "";
		try {			
			//Convert URL parameters to RIF service API parameters
			User user = createUser(servletRequest, userID);

			//Call service API
			RIFStudySubmissionAPI studySubmissionService
				= getRIFStudySubmissionService();
			
			HealthCodeTaxonomy healthCodeTaxonomy
				= studySubmissionService.getHealthCodeTaxonomyFromNameSpace(
					user, 
					nameSpace);	
			ArrayList<HealthCode> matchingHealthCodes
				= studySubmissionService.getHealthCodesMatchingSearchText(
					user, 
					healthCodeTaxonomy, 
					searchText,
					isCaseSensitive);
			
			//Convert results to support JSON						
			ArrayList<HealthCodeProxy> healthCodeProxies
				= new ArrayList<HealthCodeProxy>();
			for (HealthCode matchingHealthCode : matchingHealthCodes) {
				HealthCodeProxy healthCodeProxy
					= new HealthCodeProxy();
				healthCodeProxy.setCode(matchingHealthCode.getCode());
				healthCodeProxy.setDescription(matchingHealthCode.getDescription());
				healthCodeProxy.setNameSpace(matchingHealthCode.getNameSpace());
				healthCodeProxy.setIsTopLevelTerm(String.valueOf(matchingHealthCode.isTopLevelTerm()));
				healthCodeProxy.setNumberOfSubTerms(String.valueOf(matchingHealthCode.getNumberOfSubTerms()));
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

		
		WebServiceResponseGenerator webServiceResponseGenerator
			= getWebServiceResponseGenerator();

		return webServiceResponseGenerator.generateWebServiceResponse(
			servletRequest,
			result);		
	}
	
						
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

	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
}
