package rifDataLoaderTool.targetDBScriptGenerator;

import rifDataLoaderTool.businessConceptLayer.DataSetConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFSchemaArea;

import java.io.File;

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

public abstract class AbstractDataLoadingScriptGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private File scriptDirectory;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractDataLoadingScriptGenerator() {

	}

	protected void setScriptDirectory(final File scriptDirectory) {
		this.scriptDirectory = scriptDirectory;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	protected String getPublishedFilePath(
		final DataSetConfiguration dataSetConfiguration) {
		
		StringBuilder filePath = new StringBuilder();
		filePath.append(scriptDirectory.getAbsolutePath());
		filePath.append(File.separator);		
		String coreDataSetName
			= dataSetConfiguration.getName();
		RIFSchemaArea rifSchemaArea 
			= dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);		
		filePath.append(publishTableName);
		return filePath.toString();
	}
	

	protected String getPublishedDenominatorFilePathForNumerator(
		final DataSetConfiguration numerator) {
		
		DataSetConfiguration denominator
			= numerator.getDependencyDataSetConfiguration();

		StringBuilder filePath = new StringBuilder();
		filePath.append(scriptDirectory.getAbsolutePath());
		filePath.append(File.separator);		
		String coreDataSetName
			= denominator.getName();
		RIFSchemaArea rifSchemaArea 
			= denominator.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);		
		filePath.append(publishTableName);
		return filePath.toString();
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


