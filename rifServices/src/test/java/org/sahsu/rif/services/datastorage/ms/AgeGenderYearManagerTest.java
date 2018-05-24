package org.sahsu.rif.services.datastorage.ms;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DatabaseType;
import org.sahsu.rif.generic.datastorage.RIFDatabaseProperties;
import org.sahsu.rif.generic.system.RIFGenericLibraryError;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AgeGroup;
import org.sahsu.rif.services.concepts.AgeGroupSortingOption;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.concepts.NumeratorDenominatorPair;
import org.sahsu.rif.services.datastorage.common.AgeGenderYearManager;
import org.sahsu.rif.services.datastorage.common.RIFContextManager;
import org.sahsu.rif.services.system.RIFServiceError;
import org.sahsu.rif.services.system.RIFServiceStartupOptions;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public final class AgeGenderYearManagerTest extends CommonRIFServiceTestCase {

	private AgeGenderYearManager manager;

	@Mock
	private RIFServiceStartupOptions options;

	@Mock
	private RIFDatabaseProperties databaseProps;

	@Mock
	private RIFContextManager contextManager;

	public AgeGenderYearManagerTest() {
	}

	@Before
	public void setup() {

		super.setup();
		when(resources.getRIFServiceStartupOptions()).thenReturn(options);
		when(options.getRIFDatabaseProperties()).thenReturn(databaseProps);
		when(options.getRifDatabaseType()).thenReturn(DatabaseType.SQL_SERVER);
		when(options.getRIFDatabaseProperties().getDatabaseType())
				.thenReturn(DatabaseType.SQL_SERVER);
		when(options.getDatabaseDriverPrefix()).thenReturn("??");
		when(databaseProps.getDatabaseType()).thenReturn(DatabaseType.SQL_SERVER);
		manager = new AgeGenderYearManager(
				contextManager, resources.getRIFServiceStartupOptions());

		// This is a bit weird: we're telling a mock to return a real object. But I suppose
		// it should work.
		when(resources.getSqlAgeGenderYearManager()).thenReturn(manager);
	}

	@Test
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
	@Ignore
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
