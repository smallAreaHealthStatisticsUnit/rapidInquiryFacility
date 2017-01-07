package rifDataLoaderTool.targetDBScriptGenerator;

import rifDataLoaderTool.businessConceptLayer.*;
import rifDataLoaderTool.fileFormats.DataLoaderToolConfigurationReader;

import java.util.ArrayList;
import java.io.*;

/**
 * This class generates a single PostgreSQL script that will load all of the 
 * data sets that have been processed by the data loader tool
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

public class PGDataLoadingScriptGenerator {

	public static void main(String[] args) {
		try {
			File sampleConfigFile
				= new File("C:\\rifDataLoaderTool\\SAHSULAND_ConfigurationFile.xml");
			DataLoaderToolConfigurationReader reader	
				 = new DataLoaderToolConfigurationReader();
			reader.readFile(sampleConfigFile);
			DataLoaderToolConfiguration dataLoaderToolConfiguration
				= reader.getDataLoaderToolConfiguration();
			PGDataLoadingScriptGenerator scriptGenerator
				= new PGDataLoadingScriptGenerator();
			File scriptFile
				= new File("C:\\rifDataLoaderTool\\LoadItScript.sql");
			scriptGenerator.writeScript(scriptFile, dataLoaderToolConfiguration);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private PGHealthThemeScriptGenerator healthThemeScriptGenerator;
	private PGDenominatorScriptGenerator denominatorScriptGenerator;
	private PGNumeratorScriptGenerator numeratorScriptGenerator;
	private PGCovariateScriptGenerator covariateScriptGenerator;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public PGDataLoadingScriptGenerator() {
		healthThemeScriptGenerator = new PGHealthThemeScriptGenerator();
		denominatorScriptGenerator = new PGDenominatorScriptGenerator();
		numeratorScriptGenerator = new PGNumeratorScriptGenerator();
		covariateScriptGenerator = new PGCovariateScriptGenerator();
		
		File scriptDirectory = new File("C:\\rifDataLoaderTool");		
		denominatorScriptGenerator.setScriptDirectory(scriptDirectory);
		numeratorScriptGenerator.setScriptDirectory(scriptDirectory);
		covariateScriptGenerator.setScriptDirectory(scriptDirectory);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void writeScript(
		final File scriptFile,
		final DataLoaderToolConfiguration dataLoaderToolConfiguration) {
		
		try {
			
			FileWriter fileWriter = new FileWriter(scriptFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		
			//Part I: Pre-pend script for loading geospatial data
		
			//Part II: Load health themes
			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			bufferedWriter.write("-- Adding Health Themes");
			bufferedWriter.newLine();
			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			ArrayList<DLHealthTheme> healthThemes
				= dataLoaderToolConfiguration.getHealthThemes();
			for (DLHealthTheme healthTheme : healthThemes) {
				String healthThemeEntry
					= healthThemeScriptGenerator.generateScript(healthTheme);
				bufferedWriter.write(healthThemeEntry);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}

			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			bufferedWriter.write("-- Adding Denominators");
			bufferedWriter.newLine();
			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			bufferedWriter.newLine();
			
			//Processing Denominators
			DLGeographyMetaData geographyMetaData
				= dataLoaderToolConfiguration.getGeographyMetaData();
			ArrayList<DataSetConfiguration> denominators
				= dataLoaderToolConfiguration.getDenominatorDataSetConfigurations();
			for (DataSetConfiguration denominator : denominators) {
				DLHealthTheme healthTheme
					= denominator.getHealthTheme();
				bufferedWriter.write("-- Adding " + denominator.getDisplayName());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				
				String denominatorEntry
					= denominatorScriptGenerator.generateScript(
						denominator);
				bufferedWriter.write(denominatorEntry);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}

			//Processing Numerators
			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			bufferedWriter.write("-- Adding Numerators");
			bufferedWriter.newLine();
			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			bufferedWriter.newLine();
			ArrayList<DataSetConfiguration> numerators
				= dataLoaderToolConfiguration.getNumeratorDataSetConfigurations();
			for (DataSetConfiguration numerator : numerators) {
				bufferedWriter.write("-- Adding " + numerator.getDisplayName());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				
				String numeratorEntry
					= numeratorScriptGenerator.generateScript(
							geographyMetaData,
							numerator);
				bufferedWriter.write(numeratorEntry);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		
			//Processing Covariates
			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			bufferedWriter.write("-- Adding Covariates");
			bufferedWriter.newLine();
			bufferedWriter.write("-- ===============================================");
			bufferedWriter.newLine();
			bufferedWriter.newLine();
			ArrayList<DataSetConfiguration> covariateConfigurations
				= dataLoaderToolConfiguration.getCovariateDataSetConfigurations();
			for (DataSetConfiguration covariateConfiguration : covariateConfigurations) {
				bufferedWriter.write("-- Adding " + covariateConfiguration.getDisplayName());
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				
				String covariaterEntry
					= covariateScriptGenerator.generateScript(
						covariateConfiguration);
				bufferedWriter.write(covariaterEntry);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
			
			
		}
		catch(IOException ioException) {
			
			
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


