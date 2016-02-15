// ************************************************************************
//
// GIT Header
//
// $Format:Git ID: (%h) %ci$
// $Id: 7ccec3471201c4da4d181af6faef06a362b29526 $
// Version hash: $Format:%H$
//
// Description:
//
// Rapid Enquiry Facility (RIF) - HTTP error reponse
//
// Copyright:
//
// The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
// that rapidly addresses epidemiological and public health questions using 
// routinely collected health and population data and generates standardised 
// rates and relative risks for any given health outcome, for specified age 
// and year ranges, for any given geographical area.
//
// Copyright 2014 Imperial College London, developed by the Small Area
// Health Statistics Unit. The work of the Small Area Health Statistics Unit 
// is funded by the Public Health England as part of the MRC-PHE Centre for 
// Environment and Health. Funding for this project has also been received 
// from the Centers for Disease Control and Prevention.  
//
// This file is part of the Rapid Inquiry Facility (RIF) project.
// RIF is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// RIF is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
// to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
// Boston, MA 02110-1301 USA
//
// Author:
//
// Peter Hambly, SAHSU; copied from original stdout hook by: Javier Carrillo

/*
 * Function: 	httpErrorResponse() 
 * Parameters:  File called from, line number called from, procedure called from, 
 *				rifLog object,
 *				HTTP status,
 *				HTTP request object,
 *				HTTP response object,
 * 				Message text,
 *				Error object [may be null],
 *				Internal response object [may be null]
 * Description: HTTP error reponse
 *
 * Response object - errors:
 *  
 * error: 			Error message (if present) 
 * no_files: 		Numeric, number of files    
 * field_errors: 	Number of errors in processing fields
 * file_list: 		Array file objects:
 *						file_name: File name
 * message: 		Processing messages, including debug from topoJSON               
 * fields: 			Array of fields; includes all from request plus any additional fields set as a result of processing 
 */		
httpErrorResponse=function(file, line, calling_function, rifLog, status, req, res, msg, err, g_response) {
	var l_response = {                 // Set output response    
		error: '',
		no_files: 0,    
		field_errors: 0,
		file_list: [],
		message: '',               
		fields: [] 
	};
	try {			
		if (g_response) {
			l_response.no_files = g_response.no_files;
			l_response.field_errors = g_response.field_errors;
			for (i = 0; i < l_response.no_files; i++) {	
				if (g_response.file_list[i]) { // Handle incomplete file list
					if (g_response.file_list[i].file_name) {
						l_response.file_list[i] = {
							file_name: g_response.file_list[i].file_name
						};
					}
					else {
						l_response.file_list[i] = {
							file_name: ''
						};
					}							
				}
				else {
					l_response.file_list[i] = {
						file_name: ''
					};
				}
			}
			l_response.fields = g_response.fields;
		}
		l_response.message = msg;
		if (err) { // Add error to message
			l_response.error = err.message;
		}
		rifLog.rifLog2(file, line, calling_function, msg, req, err);
		res.status(status);		
		var output = JSON.stringify(l_response);// Convert output response to JSON 		
		res.write(output);
		res.end();	

	} catch (e) {                            // Catch conversion errors
		var n_msg="Error response processing ERROR!\n\n" + msg;				  
		rifLog.rifLog(n_msg, req, e);
		res.status(501);			
		res.write(n_msg);
		res.end();				
		return;
	}
}

module.exports.httpErrorResponse = httpErrorResponse;	