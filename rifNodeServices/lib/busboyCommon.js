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
// Rapid Enquiry Facility (RIF) - Busboy common functions
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
// Peter Hambly, SAHSU

// Globals
var cf={};	// NOT exported

exports.commonFilesInit=function(rifLog, req, res, response, ofields) {
	cf.rifLog=rifLog;
	cf.req=req;
	cf.res=res;
	cf.response=response;
	cf.ofields=ofields;
}

/*
 * Function: 	req.busboy.on('partsLimit') callback function
 * Parameters:	None
 * Description:	Processor if the parts limit has been reached 
 */	
exports.commonPartsLimit=function() {
	var msg="FAIL! Parts limit reached";
	cf.response.no_files=d.no_files;			// Add number of files process to response
	response.fields=cf.ofields;				// Add return fields	
	cf.httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('partsLimit')", 
		cf.rifLog, 500, cf.req, cf.res, msg, undefined, cf.response);
	return;				
}

/*
 * Function: 	req.busboy.on('fieldsLimit') callback function
 * Parameters:	None
 * Description:	Processor if the fields limit has been reached  
 */			
exports.commonFieldsLimit=function() {
	var msg="FAIL! Fields limit reached";
	cf.response.no_files=d.no_files;			// Add number of files process to response
	response.fields=cf.ofields;				// Add return fields	
	httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('fieldsLimit')", 
		cf.rifLog, 500, cf.req, cf.res, msg, undefined, cf.response);
	return;				
}
			
/*
 * Function: 	req.busboy.on('filesLimit') callback function
 * Parameters:	None
 * Description:	Processor if the files limit has been reached  
 */	
exports.commonFilesLimit=function() {
	var msg="FAIL! Files limit reached";
	cf.response.no_files=d.no_files;			// Add number of files process to response
	cf.response.fields=cf.ofields;				// Add return fields	
	httpErrorResponse.httpErrorResponse(__file, __line, "req.busboy.on('filesLimit')", 
		cf.rifLog, 500, cf.req, cf.res, msg, undefined, cf.response);
	return;				
}

		