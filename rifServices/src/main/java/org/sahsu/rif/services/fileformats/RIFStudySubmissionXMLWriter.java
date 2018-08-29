package org.sahsu.rif.services.fileformats;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.fileformats.XMLCommentInjector;
import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.system.RIFServiceExceptionFactory;
import org.sahsu.rif.services.concepts.RIFStudySubmission;
import org.sahsu.rif.services.concepts.StudyType;
import org.sahsu.rif.services.system.RIFServiceMessages;

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
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public final class RIFStudySubmissionXMLWriter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new RIF job submission xml writer.
	 */
	public RIFStudySubmissionXMLWriter() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	/**
	 * Write html.
	 */
	public String writeToString(
		final User user,
		final RIFStudySubmission rifStudySubmission)
		throws RIFServiceException {
		
		try {
			RIFStudySubmissionContentHandler rifStudySubmissionContentHandler
				= new RIFStudySubmissionContentHandler();
			ByteArrayOutputStream outputStream
				= new ByteArrayOutputStream();
			XMLCommentInjector commentInjector = new XMLCommentInjector();			
			rifStudySubmissionContentHandler.initialise(
				outputStream, 
				commentInjector);
			rifStudySubmissionContentHandler.writeXML(user, rifStudySubmission);
    		String result 
				= new String(outputStream.toByteArray(), "UTF-8");	
    		outputStream.close();			
    		return result;
		}
		catch(Exception exception) {
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileWritingProblemException("");
		}
		
	}

	public String write(
		final User user,
		final RIFStudySubmission rifStudySubmission,
		final File file) 
		throws RIFServiceException {
			
		try {
			FileOutputStream fileOutputStream
				= new FileOutputStream(file);
			
			RIFStudySubmissionContentHandler rifStudySubmissionContentHandler
				= new RIFStudySubmissionContentHandler();
			ByteArrayOutputStream outputStream
				= new ByteArrayOutputStream();
			XMLCommentInjector commentInjector 
				= createXMLCommentInjector();
			
			rifStudySubmissionContentHandler.initialise(
				fileOutputStream, 
				commentInjector);
			rifStudySubmissionContentHandler.writeXML(user, rifStudySubmission);
	    	String result 
				= new String(outputStream.toByteArray(), "UTF-8");	
	    	outputStream.close();			
	    	return result;
		}
		catch(Exception exception) {
			RIFServiceExceptionFactory exceptionFactory
				= new RIFServiceExceptionFactory();
			throw exceptionFactory.createFileWritingProblemException("");
		}
	}	
	
	private XMLCommentInjector createXMLCommentInjector() {
		XMLCommentInjector commentInjector
			= new XMLCommentInjector();
		
		String introductionMessage
			= RIFServiceMessages.getMessage("general.dataSharingComment1");
		commentInjector.addIntroductionComment(introductionMessage);
	
		commentInjector.setRecordToolTipMessage(
			"user", 
			"user.toolTip");
		commentInjector.setRecordToolTipMessage(
			"calculation_method", 
			"calculationMethod.toolTip");
		commentInjector.setRecordToolTipMessage(
			"parameter", 
			"parameter.toolTip");
		commentInjector.setRecordToolTipMessage(
			"rif_output_option", 
			"rifOutputOption.toolTip");
		commentInjector.setRecordToolTipMessage(
				StudyType.DISEASE_MAPPING.type(),
				"diseaseMappingStudy.label");
		commentInjector.setRecordToolTipMessage(
			"comparison_area", 
			"comparisonArea.toolTip");
		commentInjector.setRecordToolTipMessage(
			"disease_mapping_study_area", 
			"diseaseMappingStudyArea.toolTip");
		commentInjector.setRecordToolTipMessage(
			"age_group",
			"ageGroup.toolTip");
		commentInjector.setRecordToolTipMessage(
			"health_code", 
			"healthCode.toolTip");
		commentInjector.setRecordToolTipMessage(
			"exposure_covariate", 
			"exposureCovariate.toolTip");
		commentInjector.setRecordToolTipMessage(
			"adjustable_covariate", 
			"adjustableCovariate.toolTip");		
		commentInjector.setRecordToolTipMessage(
			"sex", 
			"sex.toolTip");
		commentInjector.setRecordToolTipMessage(
			"year_range", 
			"yearRange.toolTip");
		commentInjector.setRecordToolTipMessage(
			"year_interval", 
			"yearInterval.toolTip");
		commentInjector.setRecordToolTipMessage(
			"geolevel_select", 
			"geoLevelSelect.toolTip");
		commentInjector.setRecordToolTipMessage(
			"geolevel_area", 
			"geoLevelArea.toolTip");
		commentInjector.setRecordToolTipMessage(
			"geolevel_view", 
			"geoLevelView.toolTip");
		commentInjector.setRecordToolTipMessage(
			"geolevel_to_map", 
			"geoLevelToMap.toolTip");
		commentInjector.setFieldToolTipMessage(
			"health_code", 
			"code", 
			"healthCode.code.toolTip");
		
		return commentInjector;
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
