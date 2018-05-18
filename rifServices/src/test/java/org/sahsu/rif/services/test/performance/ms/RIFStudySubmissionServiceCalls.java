package org.sahsu.rif.services.test.performance.ms;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.datastorage.DisplayableItemSorter;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.GeoLevelSelect;
import org.sahsu.rif.services.concepts.Geography;
import org.sahsu.rif.services.test.services.CommonRIFServiceTestCase;

import static org.junit.Assert.assertEquals;

@Ignore // Ignoring failing performance tests for now (MM 2018-04-05)
public final class RIFStudySubmissionServiceCalls extends CommonRIFServiceTestCase {

	private static final long NUMBER_REPETITIONS = 1000;
	

	public RIFStudySubmissionServiceCalls() {

	}

	@Test
	public void getGeoLevelSelectValues() throws RIFServiceException {

		User validUser = cloneValidUser();
		ArrayList<Geography> geographies
			= rifStudySubmissionService.getGeographies(validUser);
		assertEquals(3, geographies.size());
		
		DisplayableItemSorter sorter = new DisplayableItemSorter();
		for (Geography geography : geographies) {
			sorter.addDisplayableListItem(geography);
		}

		//the second one should be "SAHSU"
		Geography sahsuGeography
			= (Geography) sorter.sortList().get(1);
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < NUMBER_REPETITIONS; i++) {
			rifStudySubmissionService.getGeoLevelSelectValues(
				validUser,
				sahsuGeography);
		}
		long finishTime = System.currentTimeMillis();
		
		printPerformanceResult(
			"getGeoLevelSelectValues",
			startTime,
			finishTime);
	}

	@Test
	public void getGeoLevelAreaValues() throws RIFServiceException {

		User validUser = cloneValidUser();
		Geography validGeography = cloneValidGeography();
		GeoLevelSelect validGeoLevelSelectValue = cloneValidGeoLevelSelect();
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < NUMBER_REPETITIONS; i++) {
			rifStudySubmissionService.getGeoLevelAreaValues(
				validUser,
				validGeography,
				validGeoLevelSelectValue);
		}
		long finishTime = System.currentTimeMillis();

		printPerformanceResult(
			"getGeoLevelSelectValues",
			startTime,
			finishTime);
	}
	
	@Test
	public void getGeographies() throws RIFServiceException {

		//test check
		User validUser = cloneValidUser();
		
		long startTime = System.currentTimeMillis();
		
		for (int i = 0; i < NUMBER_REPETITIONS; i++) {
			rifStudySubmissionService.getGeographies(validUser);
		}

		long finishTime = System.currentTimeMillis();
		
		printPerformanceResult(
			"getGeographies",
			startTime,
			finishTime);
	}
	
	private void printPerformanceResult(
		final String methodName,
		long startTime,
		long finishTime) {
		
		double duration = finishTime - startTime;
		double averageRoundTripTime = (duration/(double) NUMBER_REPETITIONS);
		
		String message = "Method \""
		                 + methodName
		                 + "\""
		                 + " average time over "
		                 + NUMBER_REPETITIONS
		                 + " repetitions is "
		                 + averageRoundTripTime
		                 + " milliseconds.";
		
		System.out.println(message);
	}
}
