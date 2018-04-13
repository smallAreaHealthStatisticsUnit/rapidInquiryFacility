package rifServices.dataStorageLayer.common;

import java.sql.Connection;
import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.AgeBand;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.AgeGroupSortingOption;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.businessConceptLayer.Sex;
import rifServices.businessConceptLayer.YearRange;

public interface AgeGenderYearManager extends SQLManager {
	/**
	 * Gets the age groups.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @param sortingOrder the sorting order
	 * @return the age groups
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<AgeGroup> getAgeGroups(User user, Connection connection, Geography geography,
			NumeratorDenominatorPair ndPair, AgeGroupSortingOption sortingOrder)
			throws RIFServiceException;
	
	/**
	 * Gets the genders.
	 *
	 * @return the genders
	 * @throws RIFServiceException the RIF service exception
	 */
	ArrayList<Sex> getGenders()
		throws RIFServiceException;
	
	/**
	 * Gets the year range.
	 *
	 * @param connection the connection
	 * @param geography the geography
	 * @param ndPair the nd pair
	 * @return the year range
	 * @throws RIFServiceException the RIF service exception
	 */
	YearRange getYearRange(User user, Connection connection, Geography geography,
			NumeratorDenominatorPair ndPair) throws RIFServiceException;
	
	void checkNonExistentAgeGroups(
			Connection connection,
			NumeratorDenominatorPair ndPair,
			ArrayList<AgeBand> ageBands)
			throws RIFServiceException;
}