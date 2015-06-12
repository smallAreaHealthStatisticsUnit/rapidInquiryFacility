package rifDataLoaderTool.businessConceptLayer;

import rifServices.system.RIFServiceException;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.User;


import java.util.ArrayList;
import java.util.Date;

/**
 * Describes the API that will be used by the desktop GUI application that will be 
 * part of the {@link rifDataLoaderTool.presentationLayer}.
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

public interface RIFDataLoaderServiceAPI {

	public void initialiseService() throws RIFServiceException;

	public void login(
		final String userID,
		final String password) 
		throws RIFServiceException;
	
	public void logout(
		final User user) 
		throws RIFServiceException;
	
	public void addUser(
		final User user,
		final String password,
		final RIFUserRole rifUserRole,
		final Date expiryDate)
		throws RIFServiceException;

	public void alterUser(
		final User user,
		final String updatedPassword,
		final RIFUserRole updatedRIFUserRole,
		final Date updatedExpirationDate) 
		throws RIFServiceException;
	
	public void deleteUser(
		final User user) 
		throws RIFServiceException;
	
	public void registerDataSource(
		final User user,
		final DataSource dataSource)
		throws RIFServiceException;
	
	public DataSource getDataSourceFromCoreTableName(
		final User user,
		final String coreTableName)
		throws RIFServiceException;

	public ArrayList<DataSetConfiguration> getDataSetConfigurations(
		final User user)
		throws RIFServiceException;
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations(
			final User user,
			final String searchPhrase)
			throws RIFServiceException;
	
	public boolean dataSetConfigurationExists(
		final User user,
		final DataSetConfiguration dataSetConfiguration)
	throws RIFServiceException;
	
	public void deleteDataSetConfigurations(
		final User user,
		final ArrayList<DataSetConfiguration> dataSetConfigurations) 
		throws RIFServiceException;
	
	public void loadConfiguration(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration) 
		throws RIFServiceException;
	
	public void addLoadTableData(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration,
		final String[][] tableData)
		throws RIFServiceException;
	
	public void cleanConfiguration(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration) 
		throws RIFServiceException;
	
	public void convertConfiguration(
		final User user,
		final ConvertWorkflowConfiguration tableConversionConfiguration)
		throws RIFServiceException;
	
	public Integer getCleaningTotalBlankValues(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration)
		throws RIFServiceException;
		
	public Integer getCleaningTotalChangedValues(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration)
		throws RIFServiceException;
		
	public Integer getCleaningTotalErrorValues(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration)
		throws RIFServiceException;
	
	public Boolean cleaningDetectedBlankValue(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException;
	
	public Boolean cleaningDetectedChangedValue(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException;
	
	public Boolean cleaningDetectedErrorValue(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException;
	
	public RIFResultTable getLoadTableData(
		final User user,
		final CleanWorkflowConfiguration tableCleaningConfiguration) 
		throws RIFServiceException;
	
	public RIFResultTable getCleanedTableData(
		final User user,			
		final CleanWorkflowConfiguration tableCleaningConfiguration) 
		throws RIFServiceException;
	
	public void shutdown() 
		throws RIFServiceException;
	
	public String[][] getVarianceInFieldData(
		final User user,
		final CleanWorkflowFieldConfiguration tableFieldCleaningConfiguration)
		throws RIFServiceException;

	
}


