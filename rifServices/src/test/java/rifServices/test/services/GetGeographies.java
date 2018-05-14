package rifServices.test.services;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.DisplayableItemSorter;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.system.RIFGenericLibraryError;
import rifServices.businessConceptLayer.Geography;
import rifServices.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

public final class GetGeographies
		extends CommonRIFServiceTestCase {

	public GetGeographies() {

	}

	@Test
	@Ignore
	public void getGeographies_COMMON1() throws RIFServiceException {

		//test check
		User validUser = cloneValidUser();
		ArrayList<Geography> geographies = rifStudySubmissionService.getGeographies(validUser);
		assertEquals(3, geographies.size());

		DisplayableItemSorter sorter = new DisplayableItemSorter();
		for (Geography geography : geographies) {
			sorter.addDisplayableListItem(geography);
		}

		Geography sahsuGeography = (Geography) sorter.sortList().get(1);
		assertEquals("SAHSU", sahsuGeography.getName());
	}
	
	@Test
	public void getGeographies_NULL1() {

		try {
			rifStudySubmissionService.getGeographies(null);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.EMPTY_API_METHOD_PARAMETER, 1);
		}
	}
		
	@Test
	public void getGeographies_EMPTY1() {
		try {
			User emptyUser = cloneEmptyUser();
			rifStudySubmissionService.getGeographies(emptyUser);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.INVALID_USER, 
				1);
		}	
	}

	/**
	 * check malicious parameters
	 */
	@Test
	public void getGeographies_NONEXISTENT1() {
		try {
			//test check
			User nonExistentUser = cloneNonExistentUser();
			rifStudySubmissionService.getGeographies(nonExistentUser);
			fail();
		}
		catch(RIFServiceException rifServiceException) {
			checkErrorType(
				rifServiceException,
				RIFGenericLibraryError.SECURITY_VIOLATION, 
				1);
		}
	}
			
	/**
	 * check malicious parameters
	 */
	@Test
	public void getGeographies_MALICIOUS1() {
		try {
			//test check
			User maliciousUser = cloneMaliciousUser();
			rifStudySubmissionService.getGeographies(maliciousUser);
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
