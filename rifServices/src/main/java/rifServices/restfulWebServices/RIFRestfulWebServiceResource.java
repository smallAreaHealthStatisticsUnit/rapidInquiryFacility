package rifServices.restfulWebServices;


import rifServices.ProductionRIFJobSubmissionService;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceMessages;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.businessConceptLayer.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;











import java.sql.Connection;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.*;

import com.fasterxml.jackson.databind.*;


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

@Path("/")
public class RIFRestfulWebServiceResource {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ProductionRIFJobSubmissionService service;
	
	private SimpleDateFormat sd;
	private Date startTime;
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFRestfulWebServiceResource() {
		startTime = new Date();
		sd = new SimpleDateFormat("HH:mm:ss:SSS");

		RIFServiceStartupOptions rifServiceStartupOptions
			= new RIFServiceStartupOptions();
		StringBuilder webApplicationFolderPath
			= new StringBuilder();
		webApplicationFolderPath.append("C:");
		webApplicationFolderPath.append(File.separator);
		webApplicationFolderPath.append("Program Files");
		webApplicationFolderPath.append(File.separator);
		webApplicationFolderPath.append("Apache Software Foundation");
		webApplicationFolderPath.append(File.separator);
		webApplicationFolderPath.append("Tomcat 8.0");
		webApplicationFolderPath.append(File.separator);
		webApplicationFolderPath.append("webapps");
		webApplicationFolderPath.append(File.separator);
		webApplicationFolderPath.append("rifServices");
		webApplicationFolderPath.append(File.separator);
		webApplicationFolderPath.append("WEB-INF");
		webApplicationFolderPath.append(File.separator);
		webApplicationFolderPath.append("classes");
		rifServiceStartupOptions.setWebApplicationFilePath(webApplicationFolderPath.toString());
		service = new ProductionRIFJobSubmissionService();
		service.initialiseService(rifServiceStartupOptions);

		try {
			service.login("keving", new String("a").toCharArray());
		}
		catch(RIFServiceException exception) {
			exception.printStackTrace(System.out);
		}
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	@GET
	@Produces({"application/json"})	
	@Path("/getProjects")
	public String getProjects(
		@QueryParam("userID") String userID) {
				
		String result = "";
		
		ArrayList<ProjectProxy> projectProxies 
			= new ArrayList<ProjectProxy>();
		
		try {
			User user = User.newInstance(userID, "xxx");

			ArrayList<Project> projects
				= service.getProjects(user);
			
			for (Project project : projects) {
				ProjectProxy projectProxy
					= new ProjectProxy();
				projectProxy.setName(project.getName());
				projectProxies.add(projectProxy);
			}
			
			result = serialiseResult(projectProxies);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/getProjectDescription")
	public String getProjectDescription(
		@QueryParam("userID") String userID,
		@QueryParam("projectName") String projectName) {
				
		String result = "";

		try {
			User user = User.newInstance(userID, "xxx");

			ArrayList<Project> projects
				= service.getProjects(user);			
			Collator collator = RIFServiceMessages.getCollator();
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
				result = serialiseResult(selectedProject.getDescription());
			}
			
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
		
	@GET
	@Produces({"application/json"})	
	@Path("/getGeographies")
	public String getGeographies(
		@QueryParam("userID") String userID) {
				
		String result = "";
		
		ArrayList<GeographyProxy> geographyProxies 
			= new ArrayList<GeographyProxy>();
		
		try {
			User user = User.newInstance(userID, "xxx");

			ArrayList<Geography> geographies
				= service.getGeographies(user);
			for (Geography geography : geographies) {
				GeographyProxy geographyProxy
					= new GeographyProxy();
				geographyProxy.setName(geography.getName());
				geographyProxies.add(geographyProxy);
			}
			
			result = serialiseResult(geographyProxies);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/getGeoLevelSelects")
	public String getGeoLevelSelects(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName) {
				
		String result = "";
		
		
		GeoLevelSelectsProxy geoLevelSelectProxy
			= new GeoLevelSelectsProxy();
		
		try {
			User user = User.newInstance(userID, "xxxxxxxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");
			ArrayList<GeoLevelSelect> geoLevelSelects
				= service.getGeographicalLevelSelectValues(
					user, 
					geography);
			ArrayList<String> geoLevelSelectNames = new ArrayList<String>();			
			for (GeoLevelSelect geoLevelSelect : geoLevelSelects) {
				geoLevelSelectNames.add(geoLevelSelect.getName());
			}
			
			geoLevelSelectProxy.setNames(geoLevelSelectNames.toArray(new String[0]));
			result = serialiseResult(geoLevelSelectProxy);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
		
	@GET
	@Produces({"application/json"})	
	@Path("/getGeoLevelAreas")
	public String getGeoLevelAreas(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName) {
				
		String result = "";
		
		GeoLevelAreasProxy geoLevelAreasProxy = new GeoLevelAreasProxy();
		
		try {
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			ArrayList<GeoLevelArea> areas
				= service.getGeoLevelAreaValues(
					user, 
					geography, 
					geoLevelSelect);
			
			ArrayList<String> geoLevelAreaNames = new ArrayList<String>();
			for (GeoLevelArea area : areas) {
				geoLevelAreaNames.add(area.getName());
			}
			geoLevelAreasProxy.setNames(geoLevelAreaNames.toArray(new String[0]));
			result = serialiseResult(geoLevelAreasProxy);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/getGeoLevelViews")
	public String getGeoLevelViews(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName) {
				
		String result = "";
				
		GeoLevelViewsProxy geoLevelViewsProxy = new GeoLevelViewsProxy();
		
		try {
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			
			ArrayList<GeoLevelView> geoLevelViews
				= service.getGeoLevelViewValues(
					user, 
					geography, 
					geoLevelSelect);
			
			ArrayList<String> geoLevelViewNames = new ArrayList<String>();
			for (GeoLevelView geoLevelView : geoLevelViews) {
				geoLevelViewNames.add(geoLevelView.getName());
			}
			geoLevelViewsProxy.setNames(geoLevelViewNames.toArray(new String[0]));
			
			result = serialiseResult(geoLevelViewsProxy);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}	
	
	@GET
	@Produces({"application/json"})	
	@Path("/getGeoLevelToMaps")
	public String getGeoLevelToMaps(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName) {
				
		String result = "";
		
		GeoLevelToMapsProxy geoLevelToMapsProxy 
			= new GeoLevelToMapsProxy();
		
		try {
			User user = User.newInstance(userID, "xxxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			
			ArrayList<GeoLevelToMap> geoLevelToMaps
				= service.getGeoLevelToMapValues(
					user, 
					geography, 
					geoLevelSelect);
			ArrayList<String> geoLevelToMapsNames = new ArrayList<String>();
			
			for (GeoLevelToMap geoLevelToMap : geoLevelToMaps) {
				geoLevelToMapsNames.add(geoLevelToMap.getName());
			}
			geoLevelToMapsProxy.setNames(geoLevelToMapsNames.toArray(new String[0]));
			
			result = serialiseResult(geoLevelToMapsProxy);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}

	
	@GET
	@Produces({"application/json"})	
	@Path("/getGeoLevelToMapAreas")
	public String getGeoLevelToMapAreas(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,
		@QueryParam("geoLevelSelectName") String geoLevelSelectName,
		@QueryParam("geoLevelAreaName") String geoLevelAreaName,
		@QueryParam("geoLevelToMapName") String geoLevelToMapName) {
				
		String result = "";
		
		ArrayList<MapAreaProxy> mapAreaProxies 
			= new ArrayList<MapAreaProxy>();
		
		try {
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "");
			GeoLevelSelect geoLevelSelect
				= GeoLevelSelect.newInstance(geoLevelSelectName);
			GeoLevelArea geoLevelArea
				= GeoLevelArea.newInstance(geoLevelAreaName);
			GeoLevelToMap geoLevelToMap
				= GeoLevelToMap.newInstance(geoLevelToMapName);
			
			geoLevelToMap.checkErrors();
			
			
			ArrayList<MapArea> mapAreas
				= service.getMapAreas(
					user, 
					geography, 
					geoLevelSelect,
					geoLevelArea,
					geoLevelToMap);
					
			for (MapArea mapArea : mapAreas) {
				MapAreaProxy mapAreaProxy
					= new MapAreaProxy();
				mapAreaProxy.setIdentifier(mapArea.getIdentifier());
				mapAreaProxy.setLabel(mapArea.getLabel());
				mapAreaProxies.add(mapAreaProxy);
			}
			
			result = serialiseResult(mapAreaProxies);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
		
	@GET
	@Produces({"application/json"})	
	@Path("/getHealthThemes")
	public String getHealthThemes(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName) {
				
		String result = "";
		
		ArrayList<HealthThemeProxy> healthThemeProxies 
			= new ArrayList<HealthThemeProxy>();
		
		try {
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "");
			
			ArrayList<HealthTheme> healthThemes
				= service.getHealthThemes(
					user, 
					geography);
			for (HealthTheme healthTheme : healthThemes) {
				HealthThemeProxy healthThemeProxy
					= new HealthThemeProxy();
				healthThemeProxy.setName(healthTheme.getName());
				healthThemeProxies.add(healthThemeProxy);
			}
			
			result = serialiseResult(healthThemeProxies);
		}
		catch(Exception exception) {
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
	@GET
	@Produces({"application/json"})	
	@Path("/getNumerator")
	public String getNumerator(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,		
		@QueryParam("healthThemeDescription") String healthThemeDescription) {
				
		String result = "";
		
		
		ArrayList<NumeratorDenominatorPairProxy> ndPairProxies 
			= new ArrayList<NumeratorDenominatorPairProxy>();
				
		try {
			User user 
				= User.newInstance(userID, "xxx");
			Geography geography 
				= Geography.newInstance(geographyName, "");
			HealthTheme healthTheme 
				= HealthTheme.newInstance("xxx", healthThemeDescription);
			
			ArrayList<NumeratorDenominatorPair> ndPairs
				= service.getNumeratorDenominatorPairs(
					user, 
					geography, 
					healthTheme);
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
			result = serialiseException(exception);			
		}
		
		return result;
		
	}

	@GET
	@Produces({"application/json"})	
	@Path("/denominator")
	public String getDenominator(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,		
		@QueryParam("healthThemeDescription") String healthThemeDescription) {
				
		String result = "";
				
		try {
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "");
			HealthTheme healthTheme = HealthTheme.newInstance("xxx", healthThemeDescription);
			
			ArrayList<NumeratorDenominatorPair> ndPairs
				= service.getNumeratorDenominatorPairs(
					user, 
					geography, 
					healthTheme);
			
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
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/sexes")
	public String getSexes(
		@QueryParam("userID") String userID) {
				
		String result = "";
				
		try {
			User user = User.newInstance(userID, "xxx");
			
			ArrayList<Sex> sexes
				= service.getGenders(
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
			sexesProxy.setNames(sexNames.toArray(new String[0]));
			result = serialiseResult(sexesProxy);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
		
	}
	
	//getCovariates
	//
	/*
	public ArrayList<AbstractCovariate> getCovariates(
			final User user,
			final Geography geography,
			final GeoLevelSelect geoLevelSelect,
			final GeoLevelToMap geoLevelToMap)
			throws RIFServiceException;	
	
	public ArrayList<AgeGroup> getAgeGroups(
			final User user,
			final Geography geography,
			final NumeratorDenominatorPair ndPair,
			final AgeGroupSortingOption sortingOrder) 
			throws RIFServiceException;
	*/
	
	@GET
	@Produces({"application/json"})	
	@Path("/ageGroups")
	public String getAgeGroups(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,	
		@QueryParam("numeratorTableName") String numeratorTableName) {

		String result = "";
		
		try {
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");
			NumeratorDenominatorPair ndPair
				= service.getNumeratorDenominatorPairFromNumeratorTable(
					user, 
					geography, 
					numeratorTableName);
			ArrayList<AgeGroup> ageGroups
				= service.getAgeGroups(
					user, 
					geography, 
					ndPair, 
					RIFJobSubmissionAPI.AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			
			ArrayList<AgeGroupProxy> ageGroupProxies = new ArrayList<AgeGroupProxy>();
			for (AgeGroup ageGroup : ageGroups) {
				AgeGroupProxy ageGroupProxy = new AgeGroupProxy();
				ageGroupProxy.setName(ageGroup.getName());
				ageGroupProxy.setLowerLimit(ageGroup.getLowerLimit());
				ageGroupProxy.setUpperLimit(ageGroup.getUpperLimit());
				ageGroupProxies.add(ageGroupProxy);
			}
			result = serialiseResult(ageGroupProxies);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}

		return result;
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/yearRange")
	public String getYearRange(
		@QueryParam("userID") String userID,
		@QueryParam("geographyName") String geographyName,	
		@QueryParam("numeratorTableName") String numeratorTableName) {
			
		String result = "";
		
		try {
			User user = User.newInstance(userID, "xxx");
			Geography geography = Geography.newInstance(geographyName, "xxx");
			NumeratorDenominatorPair ndPair
				= service.getNumeratorDenominatorPairFromNumeratorTable(
					user, 
					geography, 
					numeratorTableName);
			
			YearRange yearRange
				= service.getYearRange(user, geography, ndPair);
			YearRangeProxy yearRangeProxy = new YearRangeProxy();
			yearRangeProxy.setLowerBound(yearRange.getLowerBound());
			yearRangeProxy.setUpperBound(yearRange.getUpperBound());
			result = serialiseResult(yearRangeProxy);
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}

	@GET
	@Produces({"application/json"})	
	@Path("/healthCodeTaxonomies")
	public String getHealthCodeTaxonomies(
		@QueryParam("userID") String userID) {
	
		String result = "";

		try {
			User user = User.newInstance(userID, "xxx");
			ArrayList<HealthCodeTaxonomy> healthCodeTaxonomies
				= service.getHealthCodeTaxonomies(user);
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
			result = serialiseResult(healthCodeTaxonomyProxies);					
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;		
	}
		
	@GET
	@Produces({"application/json"})	
	@Path("/topLevelCodes")
	public String getTopLevelCodes(
		@QueryParam("userID") String userID,
		@QueryParam("healthCodeTaxonomyNameSpace") String healthCodeTaxonomyNameSpace) {
			
		String result = "";
		
		try {
			User user = User.newInstance(userID, "xxx");
			HealthCodeTaxonomy healthCodeTaxonomy
				= service.getHealthCodeTaxonomyFromNameSpace(
					user, 
					healthCodeTaxonomyNameSpace);
			ArrayList<HealthCode> healthCodes
				= service.getTopLevelCodes(user, healthCodeTaxonomy);
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
			result = serialiseResult(healthCodeProxies);					
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}


	@GET
	@Produces({"application/json"})	
	@Path("/getHealthCodesForSearchText")
	public String getTopLevelCodes(
		@QueryParam("userID") String userID,
		@QueryParam("healthCodeTaxonomyNameSpace") String healthCodeTaxonomyNameSpace,
		@QueryParam("searchText") String searchText) {
	
		String result = "";
		try {			
			User user = User.newInstance(userID, "xxx");
			HealthCodeTaxonomy healthCodeTaxonomy
				= service.getHealthCodeTaxonomyFromNameSpace(
					user, 
					healthCodeTaxonomyNameSpace);
			ArrayList<HealthCode> healthCodes
				= service.getHealthCodes(
					user, 
					healthCodeTaxonomy, 
					searchText);
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
			result = serialiseResult(healthCodeProxies);					
		}
		catch(Exception exception) {
			result = serialiseException(exception);			
		}
		
		return result;
	}	

	@GET
	@Produces({"application/json"})	
	@Path("/immediateSubHealthCodes")
	public String getImmediateSubHealthCodes(
		@QueryParam("userID") String userID,
		@QueryParam("healthCode") String healthCode,
		@QueryParam("healthCodeNameSpace") String healthCodeNameSpace) {
	
		String result = "";
		try {			
			User user = User.newInstance(userID, "xxx");
			
			HealthCode parentHealthCode
				= service.getHealthCode(user, healthCode, healthCodeNameSpace);
			ArrayList<HealthCode> healthCodes
				= service.getImmediateSubterms(user, parentHealthCode);
			
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
			result = serialiseResult(healthCodeProxies);					
		}
		catch(Exception exception) {
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
	private String serialiseResult(Object objectToWrite) 
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

	private String serialiseException(
		Exception exceptionThrownByRIFService) {
		
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
	private void printTime(String header) {
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
