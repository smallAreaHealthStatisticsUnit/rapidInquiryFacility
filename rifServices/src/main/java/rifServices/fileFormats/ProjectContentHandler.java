
package rifServices.fileFormats;

import rifServices.businessConceptLayer.Project;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;



/**
 *
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 * @version
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


final class ProjectContentHandler 
	extends AbstractRIFConceptContentHandler {


// ==========================================
// Section Constants
// ==========================================

// ==========================================
// Section Properties
// ==========================================
	/** The current project. */
	private Project currentProject;
	
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new project content handler.
     */
	public ProjectContentHandler(){
		setSingularRecordName("project");
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================

    /**
     * Gets the project.
     *
     * @return the project
     */
	public Project getProject() {
		
    	return currentProject;
    }
		
    /**
     * Write xml.
     *
     * @param project the project
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeXML(
    	final Project project) 
    	throws IOException {

    	XMLUtility xmlUtility = getXMLUtility();
		String recordName = getSingularRecordName();
		xmlUtility.writeRecordStartTag(recordName);
		xmlUtility.writeField(recordName, "name", project.getName());
		xmlUtility.writeField(recordName, "description", project.getDescription());
		xmlUtility.writeField(recordName, "start_date", project.getStartDate());
		xmlUtility.writeField(recordName, "end_date", project.getEndDate());
		xmlUtility.writeRecordEndTag(recordName);
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
    
	/* (non-Javadoc)
 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
 */
@Override
	public void startElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName,
		final Attributes attributes) 
		throws SAXException {

		if (isSingularRecordName(qualifiedName) == true) {
			currentProject = Project.newInstance();
			activate();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(
		final String nameSpaceURI,
		final String localName,
		final String qualifiedName) 
		throws SAXException {
		
		if (isSingularRecordName(qualifiedName) == true) {
			deactivate();
		}
		else if (equalsFieldName("name", qualifiedName) == true) {
			currentProject.setName(getCurrentFieldValue());
		}
		else if (equalsFieldName("description", qualifiedName) == true) {
			currentProject.setDescription(getCurrentFieldValue());						
		}
		else if (equalsFieldName("startDate", qualifiedName) == true) {
			currentProject.setStartDate(getCurrentFieldValue());
		}
		else if (equalsFieldName("endDate", qualifiedName) == true) {
			currentProject.setEndDate(getCurrentFieldValue());
		}
	}
}
