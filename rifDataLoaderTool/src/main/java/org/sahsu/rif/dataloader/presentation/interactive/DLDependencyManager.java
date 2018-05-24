package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.ArrayList;
import java.util.HashMap;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.Geography;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class DLDependencyManager {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private GeographyMetaData geographyMetaData;
	private HashMap<DataSetConfiguration, ArrayList<DataSetConfiguration>> dependenciesOnDenominator;
	private HashMap<Geography, ArrayList<DataSetConfiguration>> dependenciesOnGeography;
	private HashMap<HealthTheme, ArrayList<DataSetConfiguration>> dependenciesOnHealthTheme;
	// ==========================================
	// Section Construction
	// ==========================================

	public DLDependencyManager() {
		geographyMetaData = new GeographyMetaData();
		dependenciesOnDenominator 
			= new HashMap<DataSetConfiguration, ArrayList<DataSetConfiguration>>();
		dependenciesOnGeography
			= new HashMap<Geography, ArrayList<DataSetConfiguration>>();
		dependenciesOnHealthTheme
			= new HashMap<HealthTheme, ArrayList<DataSetConfiguration>>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void resetDependencies(
		final DataLoaderToolConfiguration dataLoaderToolConfiguration) {
	
		geographyMetaData = dataLoaderToolConfiguration.getGeographyMetaData();
		dependenciesOnDenominator.clear();
		dependenciesOnGeography.clear();
		dependenciesOnHealthTheme.clear();
		
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= dataLoaderToolConfiguration.getAllDataSetConfigurations();
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			Geography geography
				= dataSetConfiguration.getGeography();
			registerDependencyOnGeography(dataSetConfiguration, geography);
			HealthTheme healthTheme
				= dataSetConfiguration.getHealthTheme();
			registerDependencyOnHealthTheme(dataSetConfiguration, healthTheme);
			
			if (dataSetConfiguration.getRIFSchemaArea() == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
				DataSetConfiguration denominator
					= dataSetConfiguration.getDependencyDataSetConfiguration();
				registerDependencyOnDenominator(dataSetConfiguration, denominator);
			}	
		}
	}
	
	public void registerGeographyMetaData(final GeographyMetaData geographyMetaData) {
		this.geographyMetaData = geographyMetaData;
	}
		
	public void registerDependencyOnDenominator(
		final DataSetConfiguration numeratorDataSetConfiguration,
		final DataSetConfiguration denominatorDataSetConfiguration) {
		
		ArrayList<DataSetConfiguration> dependentNumerators
			= dependenciesOnDenominator.get(denominatorDataSetConfiguration);
		if (dependentNumerators == null) {
			dependentNumerators = new ArrayList<DataSetConfiguration>();
			
		}
		dependenciesOnDenominator.put(
				denominatorDataSetConfiguration, 
				dependentNumerators);
		
		dependentNumerators.add(numeratorDataSetConfiguration);
	}
	
	public void registerDependencyOnHealthTheme(
		final DataSetConfiguration dataSetConfiguration,
		final HealthTheme healthTheme) {
		
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= dependenciesOnHealthTheme.get(healthTheme);
		if (dataSetConfigurations == null) {
			dataSetConfigurations = new ArrayList<DataSetConfiguration>();
		}
		dataSetConfigurations.add(dataSetConfiguration);
		
		dependenciesOnHealthTheme.put(
			healthTheme, 
			dataSetConfigurations);		
	}
	
	public void deregisterDependenciesOfDataSet(
		final DataSetConfiguration dataSet) {

		if (dataSet.getRIFSchemaArea() == RIFSchemaArea.HEALTH_NUMERATOR_DATA) {
			//remove the dependency of the numerator on the denominator		
			DataSetConfiguration denominator
				= dataSet.getDependencyDataSetConfiguration();
			ArrayList<DataSetConfiguration> dependentNumerators		
				= dependenciesOnDenominator.get(denominator);
			if (dependentNumerators != null) {				
				dependentNumerators.remove(dataSet);
			}
		}
		
		deregisterGeographyDependencyOfDataSet(dataSet);
		deregisterHealthThemeDependencyOfDataSet(dataSet);	
	}
	
	private void deregisterGeographyDependencyOfDataSet(
		final DataSetConfiguration dataSetConfiguration) {
		
		Geography geography = dataSetConfiguration.getGeography();
		
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= dependenciesOnGeography.get(geography);
		if (dataSetConfigurations != null) {
			dataSetConfigurations.remove(dataSetConfiguration);
			if (dataSetConfigurations.size() == 0) {
				//removed all the dependencies
				dependenciesOnGeography.remove(geography);
			}
		}		
	}

	private void deregisterHealthThemeDependencyOfDataSet(
		final DataSetConfiguration dataSetConfiguration) {
			
		HealthTheme healthTheme 
			= dataSetConfiguration.getHealthTheme();		
		ArrayList<DataSetConfiguration> dataSetConfigurations
			= dependenciesOnHealthTheme.get(healthTheme);
		if (dataSetConfigurations != null) {
			dataSetConfigurations.remove(dataSetConfiguration);
			if (dataSetConfigurations.size() == 0) {
				//removed all the dependencies
				dependenciesOnHealthTheme.remove(dataSetConfiguration);
			}
		}		
	}
			
	
	
	public void registerDependencyOnGeography(
		final DataSetConfiguration dataSetConfiguration,
		final Geography geography) {

		
		ArrayList<DataSetConfiguration> dependentDataSetConfigurations
			= dependenciesOnGeography.get(geography);
		if (dependentDataSetConfigurations == null) {
			dependentDataSetConfigurations = new ArrayList<DataSetConfiguration>();
		}
		dependenciesOnGeography.put(
			geography, 
			dependentDataSetConfigurations);			
		
		dependentDataSetConfigurations.add(dataSetConfiguration);
	
	}

	/**
	 * Determines if a denominator is being referenced by other
	 * numerators
	 * 
	 * @param denominatorDataSetConfiguration
	 * @throws RIFServiceException
	 */
	public void checkDenominatorDependencies(
		final DataSetConfiguration denominatorDataSetConfiguration) 
		throws RIFServiceException {

		ArrayList<DataSetConfiguration> dependentNumerators
			= dependenciesOnDenominator.get(
				denominatorDataSetConfiguration);
		if ((dependentNumerators == null) ||
			(dependentNumerators.size() == 0)) {
			//no dependencies
			return;
		}
		
		StringBuilder dependencyListPhrase = new StringBuilder();
		for (int i = 0; i < dependentNumerators.size(); i++) {
			if (i != 0) {
				dependencyListPhrase.append(",");
			}
			dependencyListPhrase.append(dependentNumerators.get(i).getDisplayName());
		}
		
		String errorMessage
			= RIFDataLoaderToolMessages.getMessage(
				"dlDependencyManager.error.denominatorHasDependencies",
				denominatorDataSetConfiguration.getDisplayName(),
				dependencyListPhrase.toString());
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFDataLoaderToolError.DATA_LOADER_DEPENDENCY_PROBLEM,
				errorMessage);
		throw rifServiceException;

	}
	

	/**
	 * Determines if a geography is being referenced by any data
	 * sets
	 * 
	 * @param denominatorDataSetConfiguration
	 * @throws RIFServiceException
	 */
	public void checkGeographyDependencies(
		final Geography geography) 
		throws RIFServiceException {
		
		ArrayList<DataSetConfiguration> dependentDataSets
			= dependenciesOnDenominator.get(
				geography);
		if ((dependentDataSets == null) ||
			(dependentDataSets.size() == 0)) {
			//no dependencies
			return;
		}
		
		StringBuilder dependencyListPhrase = new StringBuilder();
		for (int i = 0; i < dependentDataSets.size(); i++) {
			if (i != 0) {
				dependencyListPhrase.append(",");
			}
			dependencyListPhrase.append(dependentDataSets.get(i).getDisplayName());
		}
		
		String errorMessage
			= RIFDataLoaderToolMessages.getMessage(
				"dlDependencyManager.error.geographyHasDependencies",
				geography.getDisplayName(),
				dependencyListPhrase.toString());
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFDataLoaderToolError.DATA_LOADER_DEPENDENCY_PROBLEM, 
				errorMessage);
		throw rifServiceException;

	}


	/**
	 * Determines if a health theme is being referenced by any data
	 * sets
	 * 
	 * @param denominatorDataSetConfiguration
	 * @throws RIFServiceException
	 */
	public void checkHealthThemeDependencies(
		final HealthTheme healthTheme) 
		throws RIFServiceException {
		
		ArrayList<DataSetConfiguration> dependentDataSets
			= dependenciesOnHealthTheme.get(
					healthTheme);
		if ((dependentDataSets == null) ||
			(dependentDataSets.size() == 0)) {
			//no dependencies
			return;
		}
		
		StringBuilder dependencyListPhrase = new StringBuilder();
		for (int i = 0; i < dependentDataSets.size(); i++) {
			if (i != 0) {
				dependencyListPhrase.append(",");
			}
			dependencyListPhrase.append(dependentDataSets.get(i).getDisplayName());
		}
		
		String errorMessage
			= RIFDataLoaderToolMessages.getMessage(
				"dlDependencyManager.error.healthThemeHasDependencies",
				healthTheme.getDisplayName(),
				dependencyListPhrase.toString());
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFDataLoaderToolError.DATA_LOADER_DEPENDENCY_PROBLEM, 
				errorMessage);
		throw rifServiceException;
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


