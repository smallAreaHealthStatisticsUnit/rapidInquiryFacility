package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.PublishWorkflowConfiguration;
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

public final class PublishWorkflowConfigurationHandler 
	extends AbstractWorkflowConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private PublishWorkflowConfiguration publishWorkflowConfiguration;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public PublishWorkflowConfigurationHandler() {
		publishWorkflowConfiguration 
			= PublishWorkflowConfiguration.newInstance();
		setPluralRecordName("publish");
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public PublishWorkflowConfiguration getPublishWorkflowConfiguration() {
		return publishWorkflowConfiguration;		
	}
	
	public void writeXML(
		final PublishWorkflowConfiguration publishWorkflowConfiguration) 
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag("publish");
		xmlUtility.writeField(
			"publish", 
			"rif_user_role", 
			publishWorkflowConfiguration.getRIFUserRoleName());
		
		xmlUtility.writeRecordEndTag("publish");
		
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

	}
	
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {

			if (isPluralRecordName(qualifiedName)) {
				deactivate();
			}
			else if (equalsFieldName("rif_user_role", qualifiedName)) {
				publishWorkflowConfiguration.setRIFUserRoleName(getCurrentFieldValue());
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


