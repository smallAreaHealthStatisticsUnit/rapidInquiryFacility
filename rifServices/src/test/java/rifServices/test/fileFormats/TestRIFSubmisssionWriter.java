package rifServices.test.fileFormats;

import rifServices.fileFormats.*;
import rifServices.businessConceptLayer.RIFStudySubmission;
import rifServices.businessConceptLayer.User;
import rifServices.system.RIFServiceException;
import rifServices.dataStorageLayer.SampleTestObjectGenerator;

import org.json.*;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.*;

/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
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

public class TestRIFSubmisssionWriter {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public TestRIFSubmisssionWriter() {

	}

	@Test
	public void test1() {
		SampleTestObjectGenerator testObjectGenerator
			= new SampleTestObjectGenerator();
		User validUser = User.newInstance("kgarwood", "XXX");
		RIFStudySubmission rifStudySubmission
			= testObjectGenerator.createSampleRIFJobSubmission();
		try {
			RIFJobSubmissionXMLWriter writer = new RIFJobSubmissionXMLWriter();
			String xmlResults
				= writer.writeToString(
					validUser,
					rifStudySubmission);
			System.out.println("==== XML =======\n");
			System.out.println(xmlResults);
			System.out.println("==== XML =======\n");
			JSONObject jsonObject
				= org.json.XML.toJSONObject(xmlResults);
			System.out.println("=====JSON=====\n");
			String jsonResult
				= jsonObject.toString(4);
			System.out.println("=====JSON=====\n");
			System.out.println(jsonResult);
			System.out.println("=====JSON=====\n");
		}
		catch(RIFServiceException rifServiceException) {
			
		}
	}
	
	@Test
	public void test2() {
		SampleTestObjectGenerator testObjectGenerator
			= new SampleTestObjectGenerator();
		RIFStudySubmission studySubmission
			= testObjectGenerator.createSampleRIFJobSubmission();
		
		//RIFJobSubmissionXMLWriter writer = new RIFJobSubmissionXMLWriter();
		
		File file = new File("C://rif_scripts//result-kgarwood-17-Apr-15.xml");
		
		RIFJobSubmissionXMLReader reader = new RIFJobSubmissionXMLReader();
		RIFJobSubmissionXMLWriter writer = new RIFJobSubmissionXMLWriter();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buffer.append("<!-- Please refer to your standard procedures for handling sensitive data.-->");
		buffer.append("<rif_job_submission><submitted_by>kgarwood</submitted_by><job_submission_date>");
		buffer.append("17-Apr-2015 16:14:08:965</job_submission_date><project></project><disease_mapping_study>");
		buffer.append("<!-- Disease mapping study--><name>public health study</name>");
		buffer.append("<disease_mapping_study_area><!-- A study which examines the distribution of disease.--><geo_levels><geolevel_select>");
		buffer.append("<!-- Some intelligent explanation for what a GeoLevelSelect is--></geolevel_select><geolevel_area>");
		buffer.append("<!-- Some intelligent explanation for what a GeoLevelArea is--></geolevel_area><geolevel_view>");
		buffer.append("<!-- Some intelligent explanation for what a GeoLevelView is-->");
		buffer.append("</geolevel_view><geolevel_to_map><!-- Some intelligent explanation for what a GeoLevelToMap is--></geolevel_to_map>");
		buffer.append("</geo_levels><map_areas><map_area><id>01.011.012600.1</id><label>01.011.012600.1</label></map_area>");
		buffer.append("<map_area><id>01.011.012600.2</id><label>01.011.012600.2</label></map_area></map_areas>");
		buffer.append("<!-- The area that is compared with the study area.--><geo_levels>");
		buffer.append("<geolevel_select></geolevel_select><geolevel_area></geolevel_area><geolevel_view></geolevel_view><geolevel_to_map></geolevel_to_map></geo_levels>");
		buffer.append("<map_areas><map_area><id>01.011.012600.1</id><label>01.011.012600.1</label></map_area>");
		buffer.append("<map_area><id>01.011.012600.2</id><label>01.011.012600.2</label></map_area></map_areas>");
		buffer.append("</comparison_area><investigations><investigation id=\"9\"><title>CANCERSTUDY1</title><numerator_denominator_pair><numerator_table_name>");
		buffer.append("SAHSULAND_CANCER</numerator_table_name><numerator_table_description>Cancer cases in SAHSU land</numerator_table_description>");
		buffer.append("<denominator_table_name>SAHSULAND_POP</denominator_table_name><denominator_table_description>SAHSU land population</denominator_table_description>");
		buffer.append("</numerator_denominator_pair><health_codes></health_codes><year_range>");
		buffer.append("<!-- Tool tip for year range.--><lower_bound>1992</lower_bound><upper_bound>1997</upper_bound></year_range><year_intervals></year_intervals>");
		buffer.append("<sex>Both</sex><covariates></covariates></investigation><investigation id=\"10\"><title>BRAINCANCERSTUDY</title>");
		buffer.append("<numerator_denominator_pair><numerator_table_name>SAHSULAND_CANCER</numerator_table_name><numerator_table_description>Cancer cases in SAHSU ");
		buffer.append("land</numerator_table_description><denominator_table_name>SAHSULAND_POP</denominator_table_name><denominator_table_description>SAHSU land population</denominator_table_description>");
		buffer.append("</numerator_denominator_pair><health_codes></health_codes><year_range><lower_bound>1992</lower_bound><upper_bound>1997</upper_bound></year_range>");
		buffer.append("<year_intervals></year_intervals><sex>Both</sex><covariates></covariates></investigation></investigations></disease_mapping_study>");
		buffer.append("<calculation_methods></calculation_methods></rif_job_submission>");
				
		try {
			reader.readFileFromString(buffer.toString());
			RIFStudySubmission rifStudySubmission = reader.getStudySubmission();
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
	
	
	}

	@Test
	public void test3() {
		try {
			SampleTestObjectGenerator testObjectGenerator
				= new SampleTestObjectGenerator();
			User validUser = User.newInstance("kgarwood", "XXX");
			RIFStudySubmission rifStudySubmission
				= testObjectGenerator.createSampleRIFJobSubmission();
			RIFJobSubmissionXMLWriter writer
				 = new RIFJobSubmissionXMLWriter();
			File file = new File("C://rif_scripts//walley.xml");
			writer.write(validUser, rifStudySubmission, file);
			
			//now read it
			RIFJobSubmissionXMLReader reader1
				= new RIFJobSubmissionXMLReader();
			reader1.readFile(file);
			
			RIFStudySubmission rifStudySubmission2
				=  reader1.getStudySubmission();
			
			File file2 = new File("C://rif_scripts//walley-2.xml");
			RIFJobSubmissionXMLWriter writer2
				= new RIFJobSubmissionXMLWriter();
			writer2.write(validUser, rifStudySubmission2, file2);
			
			
			RIFJobSubmissionXMLReader reader2
				= new RIFJobSubmissionXMLReader();
			reader2.readFile(file2);
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
		
	
		
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

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
