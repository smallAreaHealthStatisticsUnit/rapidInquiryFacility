package org.sahsu.taxonomyservices;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.FederatedTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyServiceProvider;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.generic.util.TaxonomyLogger;

@Path("/")
public class RIFTaxonomyWebServiceResource {

	private static final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();

	private WebServiceResponseUtility webServiceResponseUtility;

	public RIFTaxonomyWebServiceResource() {
		super();
		
		webServiceResponseUtility = new WebServiceResponseUtility();		
	}

	@GET
	@Produces({"application/json"})	
	@Path("/initialiseService")
	public Response initialiseService(
		@Context HttpServletRequest servletRequest) {
	
		String result;
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
			rifLogger.error(
				this.getClass(), 
				"GET /initialiseService method failed: ", 
				exception);
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
			rifLogger.error(
				this.getClass(), 
				"GET /getTaxonomyServiceProviders method failed: ", 
				exception);
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

			List<TaxonomyTerm> rootTerms = federatedTaxonomyService.getRootTerms(taxonomyServiceID);
			result = serialiseTaxonomyTerms(servletRequest, rootTerms);
		}
		catch(Exception exception) {
			rifLogger.error(
				this.getClass(), 
				"GET /getRootTerms method failed: ", 
				exception);
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

			List<TaxonomyTerm> matchingTerms = federatedTaxonomyService.getMatchingTerms(
					taxonomyServiceID, searchText, isCaseSensitive);

			result = serialiseTaxonomyTerms(servletRequest, matchingTerms);

		} catch(Exception exception) {
			rifLogger.error(getClass(), "GET /getMatchingTerms method failed: ",
			                exception);
			result = webServiceResponseUtility.serialiseException(servletRequest, exception);
		}
		return webServiceResponseUtility.generateWebServiceResponse(servletRequest, result);
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

			List<TaxonomyTerm> childTerms = federatedTaxonomyService.getImmediateChildTerms(
					taxonomyServiceID, parentTermID);

			result = serialiseTaxonomyTerms(servletRequest, childTerms);
		} catch(Exception exception) {
			rifLogger.error(getClass(), "GET /getImmediateChildTerms method failed: ",
			                exception);
			result = webServiceResponseUtility.serialiseException(servletRequest, exception);
		}
		return webServiceResponseUtility.generateWebServiceResponse(servletRequest, result);
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
			rifLogger.error(
				this.getClass(), 
				"GET /getParentTerm method failed: ", 
				exception);
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
	
	private String serialiseTaxonomyTerms(final HttpServletRequest servletRequest,
			final List<TaxonomyTerm> taxonomyTerms) throws Exception {
		
		List<TaxonomyTermProxy> termProxies = new ArrayList<>();
		for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
			TaxonomyTermProxy termProxy = TaxonomyTermProxy.newInstance();
			termProxy.setIdentifier(taxonomyTerm.getIdentifier());
			termProxy.setLabel(taxonomyTerm.getLabel());
			termProxy.setDescription(taxonomyTerm.getDescription());
			termProxies.add(termProxy);
		}
	
		return webServiceResponseUtility.serialiseArrayResult(servletRequest, termProxies);
	}
	
	/**
	 * If the federated taxonomy service did not initialise properly, immediately throw 
	 * an exception to throw back to the client. 
	 * @throws RIFServiceException
	 */
	private void checkFederatedServiceWorkingProperly(final HttpServletRequest servletRequest)
			throws RIFServiceException {
		
		FederatedTaxonomyService federatedTaxonomyService
			= FederatedTaxonomyService.getFederatedTaxonomyService();
		if (!federatedTaxonomyService.isInitialised()) {
			//none of the taxonomy services will be ready because the
			//federated service has not been initialised yet.
			ServletContext servletContext = servletRequest.getServletContext();
			String fullPath = servletContext.getRealPath("/WEB-INF/classes");
			federatedTaxonomyService.initialise(fullPath);								
		}
	}
}
