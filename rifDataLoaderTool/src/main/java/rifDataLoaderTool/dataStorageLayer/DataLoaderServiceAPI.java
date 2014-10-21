package rifDataLoaderTool.dataStorageLayer;

import rifDataLoaderTool.businessConceptLayer.DataSource;

import rifDataLoaderTool.businessConceptLayer.TableCleaningConfiguration;
import rifDataLoaderTool.businessConceptLayer.TableConversionConfiguration;
import rifDataLoaderTool.system.RIFDataLoaderToolException;
import rifServices.businessConceptLayer.RIFResultTable;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;

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

public interface DataLoaderServiceAPI {

	public void initialiseService();

	public void registerDataSource(
		final User user,
		final DataSource dataSource)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public DataSource getDataSourceFromCoreTableName(
		final User user,
		final String coreTableName)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public void loadConfiguration(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public void addLoadTableData(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration,
		final String[][] tableData)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public void cleanConfiguration(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public void convertConfiguration(
		final User user,
		final TableConversionConfiguration tableConversionConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public Integer getCleaningTotalBlankValues(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException;
		
	public Integer getCleaningTotalChangedValues(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException;
		
	public Integer getCleaningTotalErrorValues(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public Boolean cleaningDetectedBlankValue(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public Boolean cleaningDetectedChangedValue(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public Boolean cleaningDetectedErrorValue(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration,
		final int rowNumber,
		final String targetBaseFieldName)
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public RIFResultTable getLoadTableData(
		final User user,
		final TableCleaningConfiguration tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException;
	
	public RIFResultTable getCleanedTableData(
		final User user,			
		final TableCleaningConfiguration tableCleaningConfiguration) 
		throws RIFServiceException,
		RIFDataLoaderToolException;	
}


