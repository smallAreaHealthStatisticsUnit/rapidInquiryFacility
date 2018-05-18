package org.sahsu.rif.dataloader.presentation.interactive;

import java.util.ArrayList;
import java.util.Observable;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.GeographyMetaData;
import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.dataloader.system.DataLoaderToolSession;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 * This class manages changes that are made to an underlying 
 * {@dataLoaderTool.businessConceptLayer.DataLoaderToolConfiguration} that
 * holds all the configuration details needed to populate the RIF production
 * database.  
 * 
 * <p>
 * The class supports two main activities:
 * <ul>
 * <li>
 * it can indicate whether the application should save changes to the existing
 * data loader tool configuration
 * </li>
 * <li>
 * it can indicate whether enough data appear in one section of the data loader 
 * tool configuration to warrant the UI allowing users to edit the next section
 * of the forms.
 * </li>
 * </ul>
 * </p>
 *
 *<p>
 *As well as noting that save changes should be done when items are added or deleted,
 *it also notes whether in an update - if the original and revised copies are different.
 *As part of the second activity, the class maintains a list of observers which happen
 *to be parts of the electronic form that is part of the 
 *{@rifDataLoaderTool.presentationLayer.interactive.RIFDataLoaderToolApplication}.
 *It notifies observers when parts of the configuration have been specified.  The
 *broadcast allows observing GUI parts to determine whether they should be sensitised
 *or not.  For example, once the data loader tool configuration contains at least
 *one health theme, the change manager will broadcast that health themes have been
 *notified.  In the main GUI of the application, the denominator list panel will 
 *receive the notification and decide that it is now appropriate to sensitise the 
 *denominator list editing buttons it manages.  Note that the notifications pass
 *a state that comes from {@rifDataLoaderTool.presentationLayer.DataLoadingOrder}.
 *</p>
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

public class DataLoaderToolChangeManager 
	extends Observable {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataLoaderToolSession session;
	private boolean saveChanges;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderToolChangeManager(final DataLoaderToolSession session) {
		this.session = session;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void resetDataLoadingSteps() {
		setChanged();
		notifyObservers(DataLoadingOrder.NO_CONFIGURATION_DATA_SPECIFIED);
		
	}
	
	public void setGeographyMetaData(
		final GeographyMetaData geographyMetaData)
		throws RIFServiceException {
		
		//if (originalGeographyMetaData.hasIdenticalContents(revisedGeographyMetaData)) {
		//	return;			
		//}
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		dataLoaderToolConfiguration.setGeographyMetaData(geographyMetaData);
				
		if (dataLoaderToolConfiguration.getNumberOfDenominators() > 0) {
			//we cannot change the geography meta data because
			//other data sets depend on it
			/*
			String errorMessage
				= RIFDataLoaderToolMessages.getMessage("");
			RIFServiceException rifServiceException
				= new RIFServiceException(
					RIFDataLoaderToolError, 
					errorMessage);
			throw rifServiceException;
			*/
		}

		setChanged();
		notifyObservers(DataLoadingOrder.GEOGRAPHY_META_DATA_SPECIFIED);	
	}
	
	public void addHealthTheme(final HealthTheme healthTheme) {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();

		int originalNumberHealthThemes
			= dataLoaderToolConfiguration.getNumberOfHealthThemes();
		
		dataLoaderToolConfiguration.addHealthTheme(healthTheme);

		int revisedNumberHealthThemes
			= dataLoaderToolConfiguration.getNumberOfHealthThemes();
		
		if (originalNumberHealthThemes != revisedNumberHealthThemes) {
			//successfully added an item
			indicateSaveChanges();

			if ((originalNumberHealthThemes == 0) && 
				(revisedNumberHealthThemes > 0)) {

				notifyDataLoadingObservers(DataLoadingOrder.HEALTH_THEMES_SPECIFIED);			
			}
		}
	}

	
	public void updateHealthTheme(
		final HealthTheme originalHealthTheme, 
		final HealthTheme revisedHealthTheme) {
		
		if (originalHealthTheme.hasIdenticalContents(revisedHealthTheme)) {
			return;
		}
		
		indicateSaveChanges();
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		dataLoaderToolConfiguration.updateHealthTheme(
			originalHealthTheme, 
			revisedHealthTheme);
	}
	
	public void deleteHealthTheme(final HealthTheme healthTheme) 
		throws RIFServiceException {
		
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		int originalNumberHealthThemes
			= dataLoaderToolConfiguration.getNumberOfHealthThemes();		
		dataLoaderToolConfiguration.deleteHealthTheme(healthTheme);

		int revisedNumberHealthThemes
			= dataLoaderToolConfiguration.getNumberOfHealthThemes();			

		if (originalNumberHealthThemes != revisedNumberHealthThemes) {
			indicateSaveChanges();

			if ((originalNumberHealthThemes > 0) && 
				(revisedNumberHealthThemes == 0)) {
				
				notifyDataLoadingObservers(DataLoadingOrder.NO_CONFIGURATION_DATA_SPECIFIED);				
			}
		}		
	}
	
	public void addDenominator(final DataSetConfiguration denominator) {

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		
		//Assess whether list is empty before we add the new item
		int originalNumberDenominators
			= dataLoaderToolConfiguration.getNumberOfDenominators();
		
		dataLoaderToolConfiguration.addDenominatorDataSetConfiguration(denominator);

		int revisedNumberDenominators
			= dataLoaderToolConfiguration.getNumberOfDenominators();
		
		if (originalNumberDenominators != revisedNumberDenominators) {
			indicateSaveChanges();
			
			if ((originalNumberDenominators == 0) && 
				(revisedNumberDenominators > 0)) {

				notifyDataLoadingObservers(DataLoadingOrder.DENOMINATORS_SPECIFIED);				
			}			
		}		
	}
	
	public void updateDenominator(
		final DataSetConfiguration originalDenominator, 
		final DataSetConfiguration revisedDenominator) {
		
		if (originalDenominator.hasIdenticalContents(revisedDenominator)) {
			return;
		}
		
		indicateSaveChanges();		

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		dataLoaderToolConfiguration.updateDenominator(
			originalDenominator, 
			revisedDenominator);		
	}


	public void deleteDenominators(
		final ArrayList<DataSetConfiguration> denominatorsToDelete) {
				

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		int originalDenominatorCount
			= dataLoaderToolConfiguration.getNumberOfDenominators();
		//Remove any dependencies
		for (DataSetConfiguration denominatorToDelete : denominatorsToDelete) {
			dataLoaderToolConfiguration.deleteDenominatorDataSetConfiguration(denominatorToDelete);			
		}
		int revisedDenominatorCount
			= dataLoaderToolConfiguration.getNumberOfDenominators();
		if ((originalDenominatorCount > 0) && 
			(revisedDenominatorCount == 0)) {
			indicateSaveChanges();
			notifyDataLoadingObservers(DataLoadingOrder.HEALTH_THEMES_SPECIFIED);			
		}
	}
	
	public void deleteDenominator(final DataSetConfiguration denominator) {
				

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		int originalDenominatorCount
			= dataLoaderToolConfiguration.getNumberOfDenominators();
		//Remove any dependencies

		dataLoaderToolConfiguration.deleteDenominatorDataSetConfiguration(denominator);
		int revisedDenominatorCount
			= dataLoaderToolConfiguration.getNumberOfDenominators();
		if ((originalDenominatorCount > 0) && 
			(revisedDenominatorCount == 0)) {
			indicateSaveChanges();		

			notifyDataLoadingObservers(DataLoadingOrder.HEALTH_THEMES_SPECIFIED);			
		}
		
	}

	
	
	public void addNumerator(final DataSetConfiguration numerator) {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();
		
		//Assess whether list is empty before we add the new item
		int originalNumberNumerators
			= dataLoaderToolConfiguration.getNumberOfNumerators();
		dataLoaderToolConfiguration.addNumeratorDataSetConfiguration(numerator);

		int revisedNumberNumerators
			= dataLoaderToolConfiguration.getNumberOfNumerators();		
		
		if (originalNumberNumerators != revisedNumberNumerators) {
			indicateSaveChanges();

			if ((originalNumberNumerators == 0) && 
				(revisedNumberNumerators > 0)) {
				notifyDataLoadingObservers(DataLoadingOrder.NUMERATORS_SPECIFIED);
			}
		}	
	}

	public void updateNumerator(
		final DataSetConfiguration originalNumerator,
		final DataSetConfiguration revisedNumerator) {
			
		if (originalNumerator.hasIdenticalContents(revisedNumerator)) {
			return;
		}

		indicateSaveChanges();		
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();	
		dataLoaderToolConfiguration.updateDenominator(
			originalNumerator, 
			revisedNumerator);
	}

	public void deleteNumerators(
		final ArrayList<DataSetConfiguration> numeratorsToDelete) {
				
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();	
		int originalNumeratorCount
			= dataLoaderToolConfiguration.getNumberOfNumerators();
		//Remove any dependencies
		for (DataSetConfiguration numeratorToDelete : numeratorsToDelete) {
			dataLoaderToolConfiguration.deleteNumeratorDataSetConfiguration(numeratorToDelete);			
		}
		int revisedNumeratorCount
			= dataLoaderToolConfiguration.getNumberOfNumerators();
		if ((originalNumeratorCount > 0) && 
			(revisedNumeratorCount == 0)) {			

			indicateSaveChanges();
			notifyDataLoadingObservers(DataLoadingOrder.DENOMINATORS_SPECIFIED);			
		}		
	}
	
	
	public void deleteNumerator(final DataSetConfiguration numerator) 
		throws RIFServiceException {

		
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();	
		int originalNumberNumerators
			= dataLoaderToolConfiguration.getNumberOfNumerators();
		dataLoaderToolConfiguration.deleteNumeratorDataSetConfiguration(numerator);
		int revisedNumberNumerators
			= dataLoaderToolConfiguration.getNumberOfNumerators();
		
		if (originalNumberNumerators != revisedNumberNumerators) {
			
			indicateSaveChanges();		
			
			if ((originalNumberNumerators > 0) && 
				(revisedNumberNumerators == 0)) {
				
				notifyDataLoadingObservers(DataLoadingOrder.DENOMINATORS_SPECIFIED);				
			}		
		}		
	}
	
	public void addCovariate(final DataSetConfiguration covariate) {

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();	
		
		//Assess whether list is empty before we add the new item
		int originalCovariateCount	
			= dataLoaderToolConfiguration.getNumberOfCovariates();
		dataLoaderToolConfiguration.addCovariateDataSetConfiguration(covariate);
		int revisedCovariateCount
			= dataLoaderToolConfiguration.getNumberOfCovariates();
		
		if (originalCovariateCount != revisedCovariateCount) {
			
			indicateSaveChanges();		
			
			if ((originalCovariateCount == 0) && 
				(revisedCovariateCount > 0)) {
				
				notifyDataLoadingObservers(DataLoadingOrder.SUFFICIENT_CONFIGURATION_DATA_SPECIFIED);				
			}		
		}
	}

	public void updateCovariate(
		final DataSetConfiguration originalCovariate,
		final DataSetConfiguration revisedCovariate) {
				
		if (originalCovariate.hasIdenticalContents(revisedCovariate)) {
			return;
		}
	
		indicateSaveChanges();		

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();	
		dataLoaderToolConfiguration.updateCovariate(
			originalCovariate, 
			revisedCovariate);
	}


	public void deleteCovariates(
		final ArrayList<DataSetConfiguration> covariatesToDelete) {

		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();	
		int originalCovariateCount
			= dataLoaderToolConfiguration.getNumberOfCovariates();
		//Remove any dependencies
		for (DataSetConfiguration covariateToDelete : covariatesToDelete) {
			dataLoaderToolConfiguration.deleteCovariateDataSetConfiguration(covariateToDelete);			
		}
		
		int revisedCovariateCount
			= dataLoaderToolConfiguration.getNumberOfCovariates();
		if ((originalCovariateCount > 0) && 
			(revisedCovariateCount == 0)) {	
			indicateSaveChanges();
			notifyDataLoadingObservers(DataLoadingOrder.NUMERATORS_SPECIFIED);
		}
	}
	
	public void deleteCovariate(final DataSetConfiguration covariate) 
		throws RIFServiceException {
					
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= getCurrentDataLoaderToolConfiguration();	
		int originalCovariateCount
			= dataLoaderToolConfiguration.getNumberOfCovariates();
		dataLoaderToolConfiguration.deleteCovariateDataSetConfiguration(covariate);
		int revisedCovariateCount
			= dataLoaderToolConfiguration.getNumberOfCovariates();
		if ((originalCovariateCount > 0) && 
			(revisedCovariateCount == 0)) {
			indicateSaveChanges();
			notifyDataLoadingObservers(DataLoadingOrder.NUMERATORS_SPECIFIED);			
		}
	}
	
	public void notifyDataLoadingObservers(final DataLoadingOrder dataLoadingOrder) {
		setChanged();
		notifyObservers(dataLoadingOrder);
	}
	
	public boolean saveChanges() {
		return saveChanges;
	}
	
	public void indicateSaveChanges() {
		saveChanges = true;
	}
	
	private DataLoaderToolConfiguration getCurrentDataLoaderToolConfiguration() {
		return session.getDataLoaderToolConfiguration();
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


