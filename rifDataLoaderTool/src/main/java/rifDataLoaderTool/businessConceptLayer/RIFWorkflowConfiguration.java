package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;

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

public class RIFWorkflowConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private CleanWorkflowConfiguration cleanWorkflowConfiguration;
	private ConvertWorkflowConfiguration convertWorkflowConfiguration;
	private OptimiseWorkflowConfiguration optimiseWorkflowConfiguration;
	private CheckWorkflowConfiguration checkWorkflowConfiguration;
	private PublishWorkflowConfiguration publishWorkflowConfiguration;
	
	private DataSet dataSet;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFWorkflowConfiguration() {
		dataSet = DataSet.newInstance();
		cleanWorkflowConfiguration = CleanWorkflowConfiguration.newInstance(dataSet);
		convertWorkflowConfiguration = ConvertWorkflowConfiguration.newInstance(dataSet);	
		optimiseWorkflowConfiguration = OptimiseWorkflowConfiguration.newInstance(dataSet);
		checkWorkflowConfiguration = CheckWorkflowConfiguration.newInstance(dataSet);
		publishWorkflowConfiguration = PublishWorkflowConfiguration.newInstance(dataSet);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public static RIFWorkflowConfiguration newInstance() {
		RIFWorkflowConfiguration workflowConfiguration
			= new RIFWorkflowConfiguration();		
		return workflowConfiguration;
	}
	
	public static RIFWorkflowConfiguration newInstance(
		final DataSet dataSet) {

		RIFWorkflowConfiguration workflowConfiguration
			= RIFWorkflowConfiguration.newInstance(dataSet);		
		return workflowConfiguration;
	}
	
	public static RIFWorkflowConfiguration createCopy(
		final RIFWorkflowConfiguration originalWorkflowConfiguration) {
		
		RIFWorkflowConfiguration cloneWorkflowConfiguration
			= RIFWorkflowConfiguration.newInstance();
				
		CleanWorkflowConfiguration originalCleanWorkflowConfiguration
			= originalWorkflowConfiguration.getCleanWorkflowConfiguration();
		CleanWorkflowConfiguration cloneCleanWorkflowConfiguration
			= CleanWorkflowConfiguration.createCopy(originalCleanWorkflowConfiguration);
		cloneWorkflowConfiguration.setCleaningWorkflowConfiguration(cloneCleanWorkflowConfiguration);
		
		ConvertWorkflowConfiguration originalConvertWorkflowConfiguration
			= originalWorkflowConfiguration.getConvertWorkflowConfiguration();
		ConvertWorkflowConfiguration cloneConvertWorkflowConfiguration
			= ConvertWorkflowConfiguration.createCopy(originalConvertWorkflowConfiguration);
		cloneWorkflowConfiguration.setConvertWorkflowConfiguration(cloneConvertWorkflowConfiguration);
		
		OptimiseWorkflowConfiguration originalOptimiseWorkflowConfiguration
			= originalWorkflowConfiguration.getOptimiseWorkflowConfiguration();
		OptimiseWorkflowConfiguration cloneOptimiseWorkflowConfiguration
			= OptimiseWorkflowConfiguration.createCopy(originalOptimiseWorkflowConfiguration);
		cloneWorkflowConfiguration.setOptimiseWorkflowConfiguration(cloneOptimiseWorkflowConfiguration);
		
		CheckWorkflowConfiguration originalCheckWorkflowConfiguration
			= originalWorkflowConfiguration.getCheckWorkflowConfiguration();
		CheckWorkflowConfiguration cloneCheckWorkflowConfiguration
			= CheckWorkflowConfiguration.createCopy(originalCheckWorkflowConfiguration);
		cloneWorkflowConfiguration.setCheckWorkflowConfiguration(cloneCheckWorkflowConfiguration);
		
		
		PublishWorkflowConfiguration publishWorkflowConfiguration
			= originalWorkflowConfiguration.getPublishWorkflowConfiguration();
		
		
		return cloneWorkflowConfiguration;
	}

	public CheckWorkflowConfiguration getCheckWorkflowConfiguration() {
		return checkWorkflowConfiguration;
	}
	
	public void setCheckWorkflowConfiguration(
		final CheckWorkflowConfiguration checkWorkflowConfiguration) {
		
		this.checkWorkflowConfiguration = checkWorkflowConfiguration;		
	}
	
	public ConvertWorkflowConfiguration getConvertWorkflowConfiguration() {
		return convertWorkflowConfiguration;
	}
	
	public void setConvertWorkflowConfiguration(
		final ConvertWorkflowConfiguration convertWorkflowConfiguration) {
		
		this.convertWorkflowConfiguration = convertWorkflowConfiguration;
	}
	
	public CleanWorkflowConfiguration getCleanWorkflowConfiguration() {
		return cleanWorkflowConfiguration;		
	}
	
	public OptimiseWorkflowConfiguration getOptimiseWorkflowConfiguration() {
		return optimiseWorkflowConfiguration;
	}
	
	public void setOptimiseWorkflowConfiguration(
		final OptimiseWorkflowConfiguration optimiseWorkflowConfiguration) {
		
		this.optimiseWorkflowConfiguration = optimiseWorkflowConfiguration;
	}
	
	public void setCleaningWorkflowConfiguration(
		final CleanWorkflowConfiguration cleanWorkflowConfiguration) {
		
		this.cleanWorkflowConfiguration = cleanWorkflowConfiguration;
	}
	
	public void setPublishWorkflowConfiguration(
		final PublishWorkflowConfiguration publishWorkflowConfiguration) {
		
		this.publishWorkflowConfiguration = publishWorkflowConfiguration;
	}
	
	public PublishWorkflowConfiguration getPublishWorkflowConfiguration() {
		return publishWorkflowConfiguration;		
	}
	
	public void setDataSet(final DataSet dataSet) {
		this.dataSet = dataSet;
		
		cleanWorkflowConfiguration.setDataSet(dataSet);
		convertWorkflowConfiguration.setDataSet(dataSet);	
		optimiseWorkflowConfiguration.setDataSet(dataSet);
		checkWorkflowConfiguration.setDataSet(dataSet);
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


