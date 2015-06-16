package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.OptimiseWorkflowConfiguration;
import rifDataLoaderTool.businessConceptLayer.OptimiseWorkflowFieldConfiguration;
import rifServices.fileFormats.XMLUtility;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
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

public final class OptimiseWorkflowConfigurationHandler 
	extends AbstractWorkflowConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private OptimiseWorkflowConfiguration optimiseWorkflowConfiguration;
	private OptimiseWorkflowFieldConfiguration currentFieldConfiguration;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public OptimiseWorkflowConfigurationHandler() {
		optimiseWorkflowConfiguration 
			= OptimiseWorkflowConfiguration.newInstance();
		setPluralRecordName("optimise");
		setSingularRecordName("optional_indexed_field");
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public OptimiseWorkflowConfiguration getOptimiseWorkflowConfiguration() {
		return optimiseWorkflowConfiguration;		
	}
	
	public void writeXML(
		final OptimiseWorkflowConfiguration optimiseWorkflowConfiguration) 
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag("optimise");
		
		ArrayList<OptimiseWorkflowFieldConfiguration> fieldConfigurations
			= optimiseWorkflowConfiguration.getFieldConfigurations();
		
		for (OptimiseWorkflowFieldConfiguration fieldConfiguration : fieldConfigurations) {
		
			xmlUtility.writeField(
				"optimise", 
				"optional_indexed_field", 
				fieldConfiguration.getFieldName());
		}
		
		xmlUtility.writeRecordEndTag("optimise");
		
	}
	
	
	@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isPluralRecordName(qualifiedName)) {
			activate();
		}
		else if (isSingularRecordName(qualifiedName)) {
			System.out.println("OWFC - startElement 1 qualifiedName=="+qualifiedName+"==");
			currentFieldConfiguration = OptimiseWorkflowFieldConfiguration.newInstance();
		}
	}
	
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

			if (isPluralRecordName(qualifiedName)) {
				deactivate();
			}
			else if (isSingularRecordName(qualifiedName)) {
				System.out.println("OWFC - endElement 1 qualifiedName=="+qualifiedName+"==" + getCurrentFieldValue()+"==");
				
				currentFieldConfiguration.setFieldName(getCurrentFieldValue());				
				optimiseWorkflowConfiguration.addFieldConfiguration(currentFieldConfiguration);
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


