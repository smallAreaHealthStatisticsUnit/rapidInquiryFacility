package taxonomyServices;

import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.taxonomyServices.FederatedTaxonomyService;
import rifGenericLibrary.taxonomyServices.TaxonomyServiceProvider;
import rifGenericLibrary.taxonomyServices.TaxonomyTerm;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

import java.util.ArrayList;


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
public class RIFTaxonomyWebServiceResource {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private WebServiceResponseUtility webServiceResponseUtility;

	//private RIFServiceException serviceInitialisationException;
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFTaxonomyWebServiceResource() {
		super();
		
		webServiceResponseUtility = new WebServiceResponseUtility();		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	@GET
	@Produces({"application/json"})	
	@Path("/initialiseService")
	public Response initialiseService(
		@Context HttpServletRequest servletRequest) {
	
		String result = String.valueOf(false);
		try {			
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();
			ServletContext servletContext
				= servletRequest.getServletContext();
			String fullPath
				= servletContext.getRealPath("/WEB-INF/classes");
			federatedTaxonomyService.initialise(fullPath);								
			webServiceResponseUtility.serialiseStringResult(String.valueOf(true));
			result = String.valueOf(true);
		}
		catch(Exception exception) {
			result 
				= webServiceResponseUtility.serialiseException(
					servletRequest, 
					exception);
		}

		return webServiceResponseUtility.generateWebServiceResponse(
			servletRequest,
			result);		
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/getTaxonomyServiceProviders")
	public Response getTaxonomyServiceProviders(
		@Context HttpServletRequest servletRequest) {

		String result = "";

		try {
				
			checkFederatedServiceWorkingProperly(servletRequest);
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();
			
			ArrayList<TaxonomyServiceProvider> taxonomyServiceProviders
				= federatedTaxonomyService.getTaxonomyServiceProviders();
			ArrayList<TaxonomyServiceProviderProxy> serviceProviderProxies
				= new ArrayList<TaxonomyServiceProviderProxy>();
			for (TaxonomyServiceProvider taxonomyServiceProvider : taxonomyServiceProviders) {
				TaxonomyServiceProviderProxy providerProxy
					= TaxonomyServiceProviderProxy.newInstance();
				providerProxy.setIdentifier(
					taxonomyServiceProvider.getIdentifier());
				providerProxy.setName(taxonomyServiceProvider.getName());
				providerProxy.setDescription(taxonomyServiceProvider.getDescription());
				serviceProviderProxies.add(providerProxy);
			}
			
			result 
				= webServiceResponseUtility.serialiseArrayResult(
					servletRequest, 
					serviceProviderProxies);
		}
		catch(Exception exception) {
			result 
				= webServiceResponseUtility.serialiseException(
					servletRequest, 
					exception);
		}

		return webServiceResponseUtility.generateWebServiceResponse(
			servletRequest,
			result);
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/getRootTerms")
	public Response getRootTerms(
		@Context HttpServletRequest servletRequest,
		@QueryParam("taxonomy_id") String taxonomyServiceID) {

		String result = "";

		try {
			checkFederatedServiceWorkingProperly(servletRequest);
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();

			ArrayList<TaxonomyTerm> rootTerms
				= federatedTaxonomyService.getRootTerms(taxonomyServiceID);
			result 
				= serialiseTaxonomyTerms(
					servletRequest,
					rootTerms);
		}
		catch(Exception exception) {
			result
				= webServiceResponseUtility.serialiseException(
					servletRequest, 
					exception);
		}
		
		return webServiceResponseUtility.generateWebServiceResponse(
			servletRequest,
			result);		
	}
		
	@GET
	@Produces({"application/json"})	
	@Path("/getMatchingTerms")
	public Response getMatchingTerms(
		@Context HttpServletRequest servletRequest,
		@QueryParam("taxonomy_id") String taxonomyServiceID,
		@QueryParam("search_text") String searchText,
		@QueryParam("is_case_sensitive") Boolean isCaseSensitive) {

		String result = "";
		try {
			checkFederatedServiceWorkingProperly(servletRequest);
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();

			ArrayList<TaxonomyTerm> matchingTerms
				= federatedTaxonomyService.getMatchingTerms(
					taxonomyServiceID, 
					searchText, 
					isCaseSensitive);

			result 
				= serialiseTaxonomyTerms(
					servletRequest,
					matchingTerms);

		}
		catch(Exception exception) {
			result
				= webServiceResponseUtility.serialiseException(
					servletRequest, 
					exception);
		}
		return webServiceResponseUtility.generateWebServiceResponse(
			servletRequest,
			result);		
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/getImmediateChildTerms")
	public Response getImmediateChildTerms(
		@Context HttpServletRequest servletRequest,
		@QueryParam("taxonomy_id") String taxonomyServiceID,
		@QueryParam("parent_term_id") String parentTermID) {

		String result = "";

		try {
			checkFederatedServiceWorkingProperly(servletRequest);
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();

			ArrayList<TaxonomyTerm> childTerms
				= federatedTaxonomyService.getImmediateChildTerms(
					taxonomyServiceID, 
					parentTermID);
			
			result 
				= serialiseTaxonomyTerms(
					servletRequest,
					childTerms);
		}
		catch(Exception exception) {
			result = 
				webServiceResponseUtility.serialiseException(
					servletRequest, 
					exception);
		}
		return webServiceResponseUtility.generateWebServiceResponse(
			servletRequest,
			result);		
	}
	
	@GET
	@Produces({"application/json"})	
	@Path("/getParentTerm")
	public Response getParentTerm(
		@Context HttpServletRequest servletRequest,
		@QueryParam("taxonomy_id") String taxonomyServiceID,
		@QueryParam("child_term_id") String childTermID) {
	
		String result = "";

		try {
			checkFederatedServiceWorkingProperly(servletRequest);
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();

			TaxonomyTerm parentTerm
				= federatedTaxonomyService.getParentTerm(
					taxonomyServiceID, 
					childTermID);
			result = serialiseTaxonomyTerm(
						servletRequest, 
						parentTerm);
		}
		catch(Exception exception) {
			result
				= webServiceResponseUtility.serialiseException(
					servletRequest, 
					exception);
		}
		
		return webServiceResponseUtility.generateWebServiceResponse(
			servletRequest,
			result);		
	}
	
	private String serialiseTaxonomyTerm(
		final HttpServletRequest servletRequest,
		final TaxonomyTerm taxonomyTerm) 
		throws Exception {

		TaxonomyTermProxy termProxy
			= TaxonomyTermProxy.newInstance();
		termProxy.setIdentifier(taxonomyTerm.getIdentifier());
		termProxy.setLabel(taxonomyTerm.getLabel());
		termProxy.setDescription(taxonomyTerm.getDescription());
		return webServiceResponseUtility.serialiseSingleItemAsArrayResult(
			servletRequest, 
			termProxy);
	}
	
	private String serialiseTaxonomyTerms(
		final HttpServletRequest servletRequest,
		final ArrayList<TaxonomyTerm> taxonomyTerms) 
		throws Exception {
		
		ArrayList<TaxonomyTermProxy> termProxies
			= new ArrayList<TaxonomyTermProxy>();
		for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
			TaxonomyTermProxy termProxy
				= TaxonomyTermProxy.newInstance();
			termProxy.setIdentifier(taxonomyTerm.getIdentifier());
			termProxy.setLabel(taxonomyTerm.getLabel());
			termProxy.setDescription(taxonomyTerm.getDescription());
			termProxies.add(termProxy);
		}
	
		return webServiceResponseUtility.serialiseArrayResult(
			servletRequest, 
			termProxies);			
	}
	
	/**
	 * If the federated taxonomy service did not initialise properly, immediately throw 
	 * an exception to throw back to the client. 
	 * @throws RIFServiceException
	 */
	private void checkFederatedServiceWorkingProperly(
		final HttpServletRequest servletRequest) 
		throws RIFServiceException {
		
		FederatedTaxonomyService federatedTaxonomyService
			= FederatedTaxonomyService.getFederatedTaxonomyService();
		if (federatedTaxonomyService.isInitialised() == false) {
			//none of the taxonomy services will be ready because the
			//federated service has not been initialised yet.
			ServletContext servletContext
				= servletRequest.getServletContext();
			String fullPath
				= servletContext.getRealPath("/WEB-INF/classes");
			federatedTaxonomyService.initialise(fullPath);								
		}
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
