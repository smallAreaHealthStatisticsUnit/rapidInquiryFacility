package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.system.RIFDataLoaderActivityStep;
import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.RIFResultTable;


import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public interface RIFDataLoaderAPI {

	/**
	 * Retrieves a list of all configurations for tables loaded into the RIF.  Note that
	 * only tables that user has permission to access will appear.
	 * @param user
	 * @return
	 * @throws RIFDataLoaderException
	 */
	public ArrayList<DBTConfigurationRecord> getRIFTableConfigurations(
		final User user) 
		throws RIFDataLoaderException;

	public ArrayList<DBTConfigurationRecord> getRIFTableConfigurationsByStep(
		final User user,
		final RIFDataLoaderActivityStep rifDataLoaderActivityStep) 
		throws RIFDataLoaderException;	
	
	public ArrayList<DBTConfigurationRecord> getMatchingRIFTableConfigurations(
		final User user,
		final String searchText) 
		throws RIFDataLoaderException;

	public ArrayList<BuiltInRIFDataType> getBuiltInRIFDataTypes(
		final User user)
		throws RIFDataLoaderException;
	
	public ArrayList<CustomRIFDataType> getCustomRIFDataTypes(
		final User user) 
		throws RIFDataLoaderException;
	
	public RIFResultTable getFieldValueVarianceResults(
		final User user,
		final DBFConfigurationRecord fieldConfigurationRecord)
		throws RIFDataLoaderException;
	
	public RIFResultTable getPreviewCleaningResults(
		final User user,
		final DBFConfigurationRecord fieldConfigurationRecord) 
		throws RIFDataLoaderException;
	
	public ArrayList<DerivedPropertyFunction> getDerivedPropertyFunctions(
		final User user) 
		throws RIFDataLoaderException;
	
	/**
	 * Returns a report indicating what table fields are primary keys, unique keys, foreign keys 
	 * and indices. 
	 * @param user
	 * @return an HTML report
	 * @throws RIFDataLoaderException
	 */
	public String getConstraintsReport(
		final User user) 
		throws RIFDataLoaderException;
}


