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
// Rapid Enquiry Facility (RIF) - Tile maker config
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

const fs = require('fs'),
      xml2js = require('xml2js');
	  
function TileMakerConfig(xmlConfigFileName, winston) {
	this.xmlConfig={xmlFileName: xmlConfigFileName};
	this.xmlConfigFileName=xmlConfigFileName;
	this.winston=winston;
	
	return this;
} // End of TileMakerConfig() object constructor
TileMakerConfig.prototype = { // Add methods
	writeConfig: function() {
		var builder = new xml2js.Builder({
			rootName: "geoDataLoader" /*,
			doctype: { // DOES NOT WORK!!!!
					'ext': "geoDataLoader.xsd"
			} */
			});
		var xmlDoc = builder.buildObject(this.xmlConfig);
			
		fs.writeFileSync(this.xmlConfig.xmlFileDir + "/" + this.xmlConfig.xmlFileName, xmlDoc);		
	}, // End of writeConfig()
	setXmlConfig: function(config, callback) {
		this.xmlConfig=config;
		// Check this.xmlConfig.xmlFileDir 
		// Check if the file exists in the current directory and is readable
		fs.access(""+this.xmlConfig.xmlFileDir, fs.constants.F_OK, (err) => { // callback
			if (err) {
				if (this.winston) {
					winston.info("XML Directory " + this.xmlConfig.xmlFileDir + " does not exist: " + err.code + "; using : " + process.cwd());
				}
				else {
					console.error("XML Directory " + this.xmlConfig.xmlFileDir + " does not exist: " + err.code + "; using : " + process.cwd());
				}
				this.xmlConfig.xmlFileDir=process.cwd();
			}// Check if the file is readable.
			fs.access(""+this.xmlConfig.xmlFileDir, fs.constants.R_OK, (err) => { // callback
				if (this.winston) {
					winston.info("XML Directory " + this.xmlConfig.xmlFileDir + ` ${err ? 'is not readable' : 'is readable'}`);
				}
				else {
					console.log("XML Directory " + this.xmlConfig.xmlFileDir + ` ${err ? 'is not readable' : 'is readable'}`);
				}
				
				if (!err) {
					if (this.winston) {
						winston.info("Parsed XML config file: " + this.xmlConfig.xmlFileDir + "/" + this.xmlConfig.xmlFileName);
					}
					else {
						console.error("Parsed XML config file: " + this.xmlConfig.xmlFileDir + "/" + this.xmlConfig.xmlFileName);
					}
				}
				callback(err);
			})
		});
	},
	parseConfig: function(callback) {
		var parser = new xml2js.Parser({async: false});
		var data;
		try {
			data=fs.readFileSync(this.xmlConfigFileName);
		}
		catch (e) {
			callback(new Error("Unable to open/read: " + xmlConfigFileName + ": " + e.message));
		}
		parser.parseString(data, function (err, result) {		
			if (err) {
				callback(new Error("Unable to parse: " + xmlConfigFileName + ": " + err.message));
			}
			
			callback(undefined, result.geoDataLoader);
		});			
	} // End of parseConfig()
}

module.exports.TileMakerConfig = TileMakerConfig;

//
// Eof