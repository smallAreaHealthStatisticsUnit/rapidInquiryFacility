package org.sahsu.rif.dataloader.concepts;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.system.RIFServiceException;

import java.io.Writer;
import java.io.File;
import java.util.ArrayList;

/**
 * Defines the methods in the desktop front end (see most of the classes in the
 * <code> rifDataLoaderTool.presentation</code> package use).  The
 * methods are designed to help make it easier to do automated testing on the
 * business logic without requiring an interactive user interface.  The other
 * benefit is that having the API will make it easier to eventually substitute
 * a Java Swing front end with another kind of front end - such as a web application
 * or some other desktop widget library.
 * 
 * <p>
 * The API also helps to mask calls the data loader tool may have to make to 
 * the Shape File conversion service.  This specialised service will be responsible
 * for converting shape files into a topJSON format.
 * </p>
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
	public void initialiseService(
		final DatabaseConnectionsConfiguration dbParameters)
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

	public void setupConfiguration(
		final User rifManager,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
				
	public String[] getCleaningFunctionNames(final User rifManager) 
		throws RIFServiceException;
	public String getDescriptionForCleaningFunction(
		final User rifManager,
		final String cleaningFunctionName) 
		throws RIFServiceException;
	public String[] getValidationFunctionNames(final User rifManager) 
		throws RIFServiceException;
	public String getDescriptionForValidationFunction(
		final User rifManager, 
		final String validationFunctionName) 
		throws RIFServiceException;
	
	public void generateShapeFileScripts(final ArrayList<ShapeFile> shapeFiles)
		throws RIFServiceException;
	
	public void addFileToDataSetResults(
		final User rifManager,
		final Writer logWriter,
		final File exportDirectory,		
		final File file,
		final DataLoadingResultTheme rifDataLoadingResultTheme,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
	
	public void extractConfiguration(
		final User rifManager,
		final Writer logWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException;

	public void cleanConfiguration(
		final User rifManager,
		final Writer logWriter,		
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration) 
		throws RIFServiceException;	
	
	public void convertConfiguration(
		final User rifManager,
		final Writer logWriter,		
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;

	public void splitConfiguration(
		final User rifManager,
		final Writer logWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
	
	public void combineConfiguration(
		final User rifManager,
		final Writer logWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
	
	public void optimiseConfiguration(
		final User rifManager,
		final Writer logWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;

	public void checkConfiguration(
		final User rifManager,
		final Writer logWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;

	public void publishConfiguration(
		final User rifManager,
		final Writer logWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException;
	
	public String[][] getVarianceInFieldData(
		final User rifManager,
		final DataSetFieldConfiguration dataSetFieldConfiguration)
		throws RIFServiceException;
	
}


