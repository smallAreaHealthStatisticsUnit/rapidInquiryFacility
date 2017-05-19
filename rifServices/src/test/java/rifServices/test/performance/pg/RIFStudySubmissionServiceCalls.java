package rifServices.test.performance.pg;

import rifGenericLibrary.businessConceptLayer.User;
import rifGenericLibrary.dataStorageLayer.DisplayableItemSorter;
import rifGenericLibrary.system.RIFServiceException;
import rifServices.test.services.pg.AbstractRIFServiceTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;

import rifServices.businessConceptLayer.BoundaryRectangle;
import rifServices.businessConceptLayer.GeoLevelSelect;
import rifServices.businessConceptLayer.Geography;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class RIFStudySubmissionServiceCalls 
	extends AbstractRIFServiceTestCase {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final long NUMBER_REPETITIONS = 1000;
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public RIFStudySubmissionServiceCalls() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	@Test
	public void getGeoLevelSelectValues() {
		try {

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
		catch(RIFServiceException rifServiceException) {
			fail();
		}	
	}

	@Test
	public void getGeoLevelAreaValues() {
		try {
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
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
			fail();
		}
	}
	
	@Test
	public void getGeographies() {
		try {
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
		catch(RIFServiceException rifServiceException) {
			fail();
		}		
	}

	@Test
	public void getTiles() {
		try {
			User validUser = cloneValidUser();
			Geography validGeography = cloneValidGeography();
			GeoLevelSelect validGeoLevelSelect = cloneValidGeoLevelSelect();
			validGeoLevelSelect.setName("LEVEL3");
			BoundaryRectangle validBoundaryRectangle = cloneValidBoundaryRectangle();
			validBoundaryRectangle.setYMax("55.5268");
			validBoundaryRectangle.setXMax("-4.88654");
			validBoundaryRectangle.setYMin("52.6875");
			validBoundaryRectangle.setXMin("-7.58829");
			//y_max  |  x_max   |  y_min  |  x_min
			 //---------+----------+---------+----------
			 // 55.5268 | -4.88654 | 52.6875 | -7.58829
			 //(1 row)			
			
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < NUMBER_REPETITIONS; i++) {
				rifStudyRetrievalService.getTiles(
					validUser, 
					validGeography, 
					validGeoLevelSelect,
					"123",
					5,
					validBoundaryRectangle);
			}

			long finishTime = System.currentTimeMillis();	
			double duration = (finishTime - startTime) / 1000; //in milliseconds
			double averageRoundTripTime = (duration/(double) NUMBER_REPETITIONS );
		}
		catch(RIFServiceException rifServiceException) {
			
			fail();
		}
	}
	
	
	private void printPerformanceResult(
		final String methodName,
		long startTime,
		long finishTime) {
		
		double duration = finishTime - startTime;
		double averageRoundTripTime = (duration/(double) NUMBER_REPETITIONS);
		
		StringBuilder message = new StringBuilder();
		message.append("Method \"");
		message.append(methodName);
		message.append("\"");
		message.append(" average time over ");
		message.append(NUMBER_REPETITIONS);
		message.append(" repetitions is ");
		message.append(averageRoundTripTime);
		message.append(" milliseconds.");
		
		System.out.println(message.toString());
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
