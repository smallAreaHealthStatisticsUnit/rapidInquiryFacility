package org.sahsu.taxonomyservices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.fileformats.AppFile;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;
import org.sahsu.rif.generic.taxonomyservices.TermList;
import org.sahsu.rif.generic.util.TaxonomyLogger;

@Path("/")
public class RIFTaxonomyWebServiceResource {

	private static final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();

	private WebServiceResponseUtility webServiceResponseUtility;

	public RIFTaxonomyWebServiceResource() {
		super();
		
		webServiceResponseUtility = new WebServiceResponseUtility();		
	}

	/**
	 * Starts the Taxonomy Service if it is not already running. Normally it should be started
	 * when the application starts.
	 * @param servletRequest a servlet request
	 * @return a Response indicating the status of the start request
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/initialiseService")
	public Response initialiseService(@Context HttpServletRequest servletRequest) {

		return StartService.instance().start();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getTaxonomyServiceProviders")
	public Response getTaxonomyServiceProviders(@Context HttpServletRequest servletRequest) {

		String result;

		try {
				
			checkFederatedServiceWorkingProperly();
			FederatedTaxonomyService federatedTaxonomyService =
					FederatedTaxonomyService.getFederatedTaxonomyService();
			
			List<TaxonomyServiceProvider> taxonomyServiceProviders =
					federatedTaxonomyService.getTaxonomyServiceProviders();
			List<TaxonomyServiceProviderProxy> serviceProviderProxies = new ArrayList<>();
			for (TaxonomyServiceProvider taxonomyServiceProvider : taxonomyServiceProviders) {
				TaxonomyServiceProviderProxy providerProxy =
						TaxonomyServiceProviderProxy.newInstance();
				providerProxy.setIdentifier(
					taxonomyServiceProvider.getIdentifier());
				providerProxy.setName(taxonomyServiceProvider.getName());
				providerProxy.setDescription(taxonomyServiceProvider.getDescription());
				serviceProviderProxies.add(providerProxy);
			}
			
			result = webServiceResponseUtility.serialiseArrayResult(
					servletRequest, serviceProviderProxies);
		} catch(Exception exception) {
			rifLogger.error(getClass(),
			                "GET /getTaxonomyServiceProviders method failed: ",
			                exception);
			result = webServiceResponseUtility.serialiseException(servletRequest, exception);
		}

		return webServiceResponseUtility.generateWebServiceResponse(servletRequest, result);
	}

	/**
	 * Finds the first occurrence of the received value in any taxonomy that we are currently
	 * providing. The search is done from the newest taxonomy (alphabetically) to the oldest,
	 * so if a term occurs in both ICD11 and ICD10, the version from ICD11 will be returned.
	 * @param servletRequest the request
	 * @return the taxonomy term, if found
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/findTermInAnyTaxonomy")
	public TermList findTermInAnyTaxonomy(@Context HttpServletRequest servletRequest,
			@QueryParam("search_text") String searchText,
			@QueryParam("is_case_sensitive") Boolean isCaseSensitive) throws RIFServiceException {

		checkFederatedServiceWorkingProperly();
		FederatedTaxonomyService fed = FederatedTaxonomyService.getFederatedTaxonomyService();

		// We sort the list in reverse alphabetical order so as to search the newer taxonomies
		// before the older.
		List<TaxonomyServiceProvider> providers = fed.getTaxonomyServiceProviders();
		Collections.sort(providers);
		Collections.reverse(providers);
		for (TaxonomyServiceProvider service : providers) {

			TermList found = getMatchingTerms(servletRequest, service.getIdentifier(),
			                                            searchText, isCaseSensitive);
			if (found.listIsUsable()) {
				rifLogger.info(getClass(), found.toString());
				return found;
			}
		}

		// Nothing found, so return empty list.
		return new TermList(Collections.emptyList());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getRootTerms")
	public TermList getRootTerms(
		@Context HttpServletRequest servletRequest,
		@QueryParam("taxonomy_id") String taxonomyServiceID) {

		List<TaxonomyTerm> rootTerms = new ArrayList<>();

		try {
			checkFederatedServiceWorkingProperly();
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();

			rootTerms = federatedTaxonomyService.getRootTerms(taxonomyServiceID);
		}
		catch(Exception exception) {
			rifLogger.error(
				this.getClass(), 
				"GET /getRootTerms method failed: ", 
				exception);
		}
		
		return new TermList(rootTerms);
	}
		
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getMatchingTerms")
	public TermList getMatchingTerms(@Context HttpServletRequest servletRequest,
			@QueryParam("taxonomy_id") String taxonomyServiceID, @QueryParam("search_text") String searchText,
			@QueryParam("is_case_sensitive") Boolean isCaseSensitive) {

		List<TaxonomyTerm> matchingTerms = new ArrayList<>();
		try {
			checkFederatedServiceWorkingProperly();
			FederatedTaxonomyService federatedTaxonomyService =
					FederatedTaxonomyService.getFederatedTaxonomyService();

			matchingTerms = federatedTaxonomyService.getMatchingTerms(
					taxonomyServiceID, searchText, isCaseSensitive);
		} catch(Exception exception) {
			rifLogger.error(getClass(), "GET /getMatchingTerms method failed: ",
			                exception);
		}
		TermList result = new TermList(matchingTerms);
		rifLogger.debug(getClass(), "Matching terms: " + result.toString());
		return result;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getImmediateChildTerms")
	public TermList getImmediateChildTerms(
		@Context HttpServletRequest servletRequest,
		@QueryParam("taxonomy_id") String taxonomyServiceID,
		@QueryParam("parent_term_id") String parentTermID) {

		List<TaxonomyTerm> childTerms = new ArrayList<>();

		try {
			checkFederatedServiceWorkingProperly();
			FederatedTaxonomyService federatedTaxonomyService
				= FederatedTaxonomyService.getFederatedTaxonomyService();

			childTerms = federatedTaxonomyService.getImmediateChildTerms(
					taxonomyServiceID, parentTermID);

		} catch(Exception exception) {
			rifLogger.error(getClass(), "GET /getImmediateChildTerms method failed: ",
			                exception);
		}
		return new TermList(childTerms);
	}

	/**
	 * If the federated taxonomy service did not initialise properly, immediately throw 
	 * an exception to throw back to the client. 
	 * @throws RIFServiceException on failures
	 */
	private void checkFederatedServiceWorkingProperly() throws RIFServiceException {
		
		FederatedTaxonomyService federatedTaxonomyService =
				FederatedTaxonomyService.getFederatedTaxonomyService();
		if (!federatedTaxonomyService.isInitialised()) {

			//none of the taxonomy services will be ready because the
			//federated service has not been initialised yet.
			java.nio.file.Path fullPath = AppFile.getTaxonomyInstance(".").path();
			federatedTaxonomyService.initialise();
		}
	}

}
