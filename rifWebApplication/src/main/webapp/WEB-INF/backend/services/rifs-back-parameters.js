/**
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2016 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
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

 * Peter Hambly
 * @author phambly
 */

/* 
 * SERVICE to store frnt end parameters (so they can be set in the middleware)
 */
angular.module("RIF")
        .factory('ParametersService',
                function () {
                    //this is used for all front end parameters. They originare from the Middleware (when implemented)
                    var defaultParameters = {
						usePouchDBCache: 	false,	// DO NOT Use PouchDB caching in TopoJSONGridLayer.js; it interacts with the diseasemap sync;
						debugEnabled:		false,	// Disable front end debugging
						mappingDefaults: 	{					
							'diseasemap1': {},
							'diseasemap2': {},
							'viewermap': {}
						}
					};               
                    return {
                        getParameters: function () {
                            return defaultParameters;
                        },
                        setParameters: function (params) {
							if (params) {
								defaultParameters = params;
							}
                        }
                    };
                });