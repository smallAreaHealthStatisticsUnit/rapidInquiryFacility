package org.sahsu.rif.services.datastorage.common;

import java.sql.Connection;
import java.util.ArrayList;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.DiseaseMappingStudy;
import org.sahsu.rif.services.concepts.HealthCode;
import org.sahsu.rif.services.concepts.HealthCodeTaxonomy;
import org.sahsu.rif.services.concepts.Investigation;

public interface HealthOutcomeManager {

	/**
	 * Gets the health code taxonomies.
	 *
	 * @return the health code taxonomies
	 * @throws RIFServiceException the RIF service exception
	 */
	HealthCodeTaxonomy getHealthCodeTaxonomyFromNameSpace(
			String healthCodeTaxonomyNameSpace)
		throws RIFServiceException;
	
	/**
	 * Gets the health code taxonomies.
	 *
	 * @return the health code taxonomies
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<HealthCodeTaxonomy> getHealthCodeTaxonomies()
		throws RIFServiceException;
	
	/**
	 * Gets the health codes for investigation.
	 *
	 * @param connection the connection
	 * @param user the user
	 * @param diseaseMappingStudy the disease mapping study
	 * @param investigation the investigation
	 * @return the health codes for investigation
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<HealthCode> getHealthCodesForInvestigation(
			Connection connection,
			User user,
			DiseaseMappingStudy diseaseMappingStudy,
			Investigation investigation)
		throws RIFServiceException;
	
	/**
	 * Gets the top level codes.
	 *
	 * @param connection the connection
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @return the top level codes
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<HealthCode> getTopLevelCodes(
			HealthCodeTaxonomy healthCodeTaxonomy)
		throws RIFServiceException;
	
	/**
	 * Gets the immediate subterms.
	 *
	 * @param connection the connection
	 * @param parentHealthCode the parent health code
	 * @return the immediate subterms
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<HealthCode> getImmediateSubterms(
			HealthCode parentHealthCode)
		throws RIFServiceException;
	
	HealthCode getHealthCode(
			String code,
			String nameSpace)
			throws RIFServiceException;
	
	/**
	 * Gets the parent health code.
	 *
	 * @param connection the connection
	 * @param childHealthCode the child health code
	 * @return the parent health code
	 * @throws RIFServiceException the RIF service exception
	 */
	HealthCode getParentHealthCode(
			HealthCode childHealthCode)
		throws RIFServiceException;
	
	/**
	 * Gets the health codes.
	 *
	 * @param connection the connection
	 * @param healthCodeTaxonomy the health code taxonomy
	 * @param searchText the search text
	 * @return the health codes
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<HealthCode> getHealthCodes(
			HealthCodeTaxonomy healthCodeTaxonomy,
			String searchText,
			boolean isCaseSensitive)
		throws RIFServiceException;
}
