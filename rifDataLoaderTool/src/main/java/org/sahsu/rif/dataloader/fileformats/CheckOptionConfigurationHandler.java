package org.sahsu.rif.dataloader.fileformats;

import java.io.IOException;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.CheckOption;
import org.sahsu.rif.generic.fileformats.XMLUtility;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

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

final class CheckOptionConfigurationHandler 
	extends AbstractDataLoaderConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<CheckOption> checkOptions;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CheckOptionConfigurationHandler() {
		checkOptions = new ArrayList<CheckOption>();
		
		setPluralRecordName("check_options");
		setSingularRecordName("check_option");

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public ArrayList<CheckOption> getCheckOptions() {
		return checkOptions;
	}

	public void writeXML(
		final ArrayList<CheckOption> checkOptions)
		throws IOException {
		
		XMLUtility xmlUtility = getXMLUtility();
		xmlUtility.writeRecordStartTag(getPluralRecordName());

		for (CheckOption checkOption : checkOptions) {
			xmlUtility.writeRecordStartTag(getSingularRecordName());
			xmlUtility.writeValue(checkOption.getCode());
			xmlUtility.writeRecordEndTag(getSingularRecordName());
		}
		xmlUtility.writeRecordEndTag(getPluralRecordName());

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
			checkOptions = new ArrayList<CheckOption>();
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
			String checkOptionName
				= getCurrentFieldValue();
			CheckOption rifCheckOption
				= CheckOption.getOptionFromCode(checkOptionName);
			checkOptions.add(rifCheckOption);
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


