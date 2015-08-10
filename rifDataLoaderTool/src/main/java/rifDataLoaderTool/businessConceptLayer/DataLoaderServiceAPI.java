package rifDataLoaderTool.businessConceptLayer;

import rifGenericLibrary.system.RIFServiceException;
import rifServices.businessConceptLayer.User;
import rifDataLoaderTool.system.RIFDataLoaderToolSession;

import java.io.Writer;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
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

public interface DataLoaderServiceAPI {
	public void initialiseService()
		throws RIFServiceException;
	
	public void login(
		final String userID,
		final String password)
		throws RIFServiceException;

	public void logout(
		final User rifManager) 
		throws RIFServiceException;
	
	public void shutdownService() 
		throws RIFServiceException;

	public void loadConfiguration(
		final User rifManager,
		final Writer logWriter,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException;

	public void cleanConfiguration(
		final User rifManager,
		final Writer logWriter,		
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException;	
	
	public void convertConfiguration(
		final User rifManager,
		final Writer logWriter,		
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;

	public void splitConfiguration(
		final User rifManager,
		final Writer logWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
	
	public void combineConfiguration(
		final User rifManager,
		final Writer logWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
	
	public void optimiseConfiguration(
		final User rifManager,
		final Writer logWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;

	public void checkConfiguration(
		final User rifManager,
		final Writer logWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;

	public void publishConfiguration(
		final User rifManager,
		final Writer logWriter,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
	
	public String[][] getVarianceInFieldData(
		final User rifManager,
		final DataSetFieldConfiguration dataSetFieldConfiguration)
		throws RIFServiceException;
	
}


