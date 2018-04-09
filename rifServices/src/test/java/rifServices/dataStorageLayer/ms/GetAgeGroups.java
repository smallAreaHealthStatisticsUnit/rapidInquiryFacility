package rifServices.dataStorageLayer.ms;

import rifGenericLibrary.dataStorageLayer.DatabaseType;
import rifGenericLibrary.dataStorageLayer.RIFDatabaseProperties;
import rifServices.businessConceptLayer.AgeGroup;
import rifServices.businessConceptLayer.AgeGroupSortingOption;
import rifServices.businessConceptLayer.Geography;
import rifServices.businessConceptLayer.NumeratorDenominatorPair;
import rifServices.dataStorageLayer.common.AgeGenderYearManager;
import rifServices.dataStorageLayer.ms.MSSQLAgeGenderYearManager;
import rifServices.system.RIFServiceError;
import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifServices.system.RIFServiceStartupOptions;
import rifServices.test.services.ms.AbstractRIFServiceTestCase;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public final class GetAgeGroups extends AbstractRIFServiceTestCase {

	private AgeGenderYearManager manager;

	@Mock
	private RIFServiceStartupOptions options;

	@Mock
	private RIFDatabaseProperties databaseProps;

	public GetAgeGroups() {
	}

	@Before
	public void setup() {

		super.setup();
		when(resources.getRIFServiceStartupOptions()).thenReturn(options);
		when(options.getRIFDatabaseProperties()).thenReturn(databaseProps);
		when(options.getRifDatabaseType()).thenReturn(DatabaseType.SQL_SERVER);
		when(options.getRIFDatabaseProperties().getDatabaseType())
				.thenReturn(DatabaseType.SQL_SERVER);
		when(databaseProps.getDatabaseType()).thenReturn(DatabaseType.SQL_SERVER);
		manager = new MSSQLAgeGenderYearManager(
				new MSSQLRIFContextManager(resources.getRIFServiceStartupOptions()),
		        resources.getRIFServiceStartupOptions());

		// This is a bit weird: we're telling a mock to return a real object. But I suppose
		// it should work.
		when(resources.getSqlAgeGenderYearManager()).thenReturn(manager);
	}

	@Test
	public void getAgeGroups_COMMON1() throws RIFServiceException {

		User validUser = cloneValidUser();
		Geography validGeography = cloneValidGeography();
		NumeratorDenominatorPair validNDPair = cloneValidNDPair();

		List<AgeGroup> ageGroups = rifStudySubmissionService.getAgeGroups(
				validUser,
				validGeography,
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);

		if (ageGroups.isEmpty()) {

			fail(getClass().getSimpleName() + "; Age Groups cannot be empty");
		}
	}
	
	@Test
	public void getAgeGroups_NULL1() {
		try {
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();
			
			rifStudySubmissionService.getAgeGroups(
				null, 
				validGeography, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}
	
	@Test
	public void getAgeGroups_NULL2() {
		try {
			User validUser = cloneValidUser();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();

			rifStudySubmissionService.getAgeGroups(
				validUser, 
				null, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}
	
	@Test
	public void getAgeGroups_NULL3() {	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
						
			rifStudySubmissionService.getAgeGroups(
				validUser, 
				validGeography, 
				null,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.EMPTY_API_METHOD_PARAMETER,
				1);
		}
	}


	
	@Test
	public void getAgeGroups_EMPTY1() {
		
		try {
			User emptyUser = cloneEmptyUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();

			rifStudySubmissionService.getAgeGroups(
				emptyUser, 
				validGeography, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.INVALID_USER,
				1);
		}
	}
	
	@Test
	public void getAgeGroups_EMPTY2() {
	
		try {
			User validUser = cloneValidUser();
			Geography emptyGeography = cloneEmptyGeography();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();
			
			rifStudySubmissionService.getAgeGroups(
				validUser, 
				emptyGeography, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_GEOGRAPHY,
				1);
		}
	}
	
	@Test
	public void getAgeGroups_EMPTY3() {	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair emptyNDPair = cloneEmptyNDPair();
			
			rifStudySubmissionService.getAgeGroups(
				validUser, 
				validGeography, 
				emptyNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			
			checkErrorType(
				rifServiceException,
				RIFServiceError.INVALID_NUMERATOR_DENOMINATOR_PAIR,
				1);
		}
	}
	
	@Test
	public void getAgeGroups_NONEXISTENT1() {
		try {
			User nonExistentUser = cloneNonExistentUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();
						
			rifStudySubmissionService.getAgeGroups(
				nonExistentUser, 
				validGeography, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}
	
	@Test
	public void getAgeGroups_NONEXISTENT2() {
	
		try {
			User validUser = cloneValidUser();
			Geography nonExistentGeography = cloneNonExistentGeography();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();
			
			rifStudySubmissionService.getAgeGroups(
				validUser, 
				nonExistentGeography, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_GEOGRAPHY,
				1);
		}
	}

	@Test
	public void getAgeGroups_NONEXISTENT3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair nonExistentNDPair = cloneNonExistentNDPair();

			rifStudySubmissionService.getAgeGroups(
				validUser, 
				validGeography, 
				nonExistentNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFServiceError.NON_EXISTENT_ND_PAIR,
				1);
		}
	}
		
	@Test
	public void getAgeGroups_MALICIOUS1() {
	
		try {
			User maliciousUser = cloneMaliciousUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();

			rifStudySubmissionService.getAgeGroups(
				maliciousUser, 
				validGeography, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}


	
	@Test
	public void getAgeGroups_MALICIOUS2() {
	
		try {
			User validUser = cloneValidUser();
			Geography maliciousGeography = cloneMaliciousGeography();
			NumeratorDenominatorPair validNDPair = cloneValidNDPair();

			rifStudySubmissionService.getAgeGroups(
				validUser, 
				maliciousGeography, 
				validNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}
	
	@Test
	public void getAgeGroups_MALICIOUS3() {
	
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			NumeratorDenominatorPair maliciousNDPair = cloneMaliciousNDPair();

			rifStudySubmissionService.getAgeGroups(
				validUser, 
				validGeography, 
				maliciousNDPair,
				AgeGroupSortingOption.ASCENDING_LOWER_LIMIT);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION,
				1);
		}
	}	
}
