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
	
	private ArrayList<DataSource> dataSources;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public RIFWorkflowConfiguration() {
		cleanWorkflowConfiguration = CleanWorkflowConfiguration.newInstance();
		convertWorkflowConfiguration = ConvertWorkflowConfiguration.newInstance();
		
		dataSources = new ArrayList<DataSource>();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void addDataSource(
		final DataSource dataSource) {
		
		dataSources.add(dataSource);
	}
	
	public ArrayList<DataSource> getDataSources() {
		return dataSources;		
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
	
	public void setCleaningWorkflowConfiguration(
		final CleanWorkflowConfiguration cleanWorkflowConfiguration) {
		
		this.cleanWorkflowConfiguration = cleanWorkflowConfiguration;
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


