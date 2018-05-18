package org.sahsu.rif.dataloader.system;

import java.io.File;

import org.sahsu.rif.dataloader.concepts.DataLoaderServiceAPI;
import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.RIFDataTypeFactory;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.presentation.UserInterfaceFactory;
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

public class DataLoaderToolSession {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	//Data
	private DataLoaderServiceAPI dataLoaderService;
	//private DataLoaderToolSettings dataLoaderToolSettings;	
	private DataLoaderToolConfiguration dataLoaderToolConfiguration;
	private User rifManager;
	private boolean saveChanges;
	
	//GUI Components
	private UserInterfaceFactory userInterfaceFactory;	
	
	private File currentBrowsingDirectory;
	
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderToolSession() {
		saveChanges = false;
		userInterfaceFactory = new UserInterfaceFactory();		
		this.dataLoaderService = dataLoaderService;
		
		dataLoaderToolConfiguration = DataLoaderToolConfiguration.newInstance();
		rifManager = User.newInstance("kgarwood", "xxx");
		currentBrowsingDirectory = new File(".");
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	
	public DataLoaderServiceAPI getDataLoaderService() {
		return dataLoaderService;
	}
	
	public void setDataLoaderService(final DataLoaderServiceAPI dataLoaderService) {
		
		this.dataLoaderService = dataLoaderService;
	}
	
	
	public File getCurrentBrowsingDirectory() {
		return currentBrowsingDirectory;
	}
	
	public void setCurrentBrowsingDirectory(final File currentBrowsingDirectory) {
		this.currentBrowsingDirectory = currentBrowsingDirectory;
	}
	
	
	public void initialiseService() 
		throws RIFServiceException {
		System.out.println("Data Loader Service 1");
		dataLoaderService.initialiseService(dataLoaderToolConfiguration.getDatabaseConnectionConfiguration());		
	}
	
	public UserInterfaceFactory getUserInterfaceFactory() {
		return userInterfaceFactory;
	}
	
	public DataLoaderToolConfiguration getDataLoaderToolConfiguration() {
		return dataLoaderToolConfiguration;
	}
	
	public void setDataLoaderToolConfiguration(final DataLoaderToolConfiguration dataLoaderToolConfiguration) {
		this.dataLoaderToolConfiguration = dataLoaderToolConfiguration;
	}
	
	
	public RIFDataTypeFactory getRIFDataTypeFactory() {
		return dataLoaderToolConfiguration.getRIFDataTypeFactory();
	}
	
	public void setSaveChanges(final boolean saveChanges) {
		this.saveChanges = saveChanges;
	}
	
	public boolean saveChanges() {
		return saveChanges;
	}
	
	public User getRIFManager() {
		return rifManager;
	}
	
	
	public void setWorkingDirectoryFromSelectedFile(final File selectedFile) {
		if (selectedFile == null) {
			return;
		}
		
		//find the last occurrence of the File separator
		String filePath = selectedFile.getAbsolutePath();
		int lastIndex = filePath.lastIndexOf(File.separator);
		
		if (lastIndex != -1) {			
			String workingDirectoryPath
				= filePath.substring(0, lastIndex);
			System.out.println("Directory path==" + workingDirectoryPath + "==");
			this.currentBrowsingDirectory = new File(workingDirectoryPath);
		}		
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


