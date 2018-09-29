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
 * SERVICE for stored map state: see also rifs-dmap-mappingstate.js which will be merged in time
 */

angular.module("RIF")
        .factory('CommonMappingStateService', ['AlertService',
                function (AlertService) {
                    
					var mapNames = {
						"areamap": "Study and comparison area map",
						"diseasemap1": "Disease mapping left hand map",
						"diseasemap2": "Disease mapping right hand map",
						"viewer": "Viewer map"
					};
					
					var s = {};
					
                    return {
                        getState: function (mapName) {
							var found=false;
							for (var key in mapNames) {
								if (key == mapName) {
									found=true;
								}
								
								if (s[key] == undefined) { // Initialise
									s[key] = { // All possible mapping elements
										map: undefined,
										shapes: undefined,
										drawnItems: undefined,
										info: undefined,
										areaNameList: undefined,
										selectedPolygon: [],				
										selectedPolygonObj: {},
										clearSselectedPolygon: function() { // Clear selectedPolygon list
											this.selectedPolygon.length = 0;
											this.selectedPolygonObj = {};
											AlertService.consoleDebug("[rifs-util-mapstate.js] clearSselectedPolygon(): " + 
												this.selectedPolygon.length);
											return this.selectedPolygon;
										},
										initialiseSselectedPolygon: function(arr) {	// Initialise selectedPolygon from an array arr of items	
											if (arr && arr.length > 0) {
												this.selectedPolygon.length = 0;
												this.selectedPolygonObj = {};				
												for (var i = 0; i < arr.length; i++) { // Maintain keyed list for faster checking
													this.selectedPolygonObj[arr[i].id] = arr[i];
												}
												this.selectedPolygon = arr;
												AlertService.consoleDebug("[rifs-util-mapstate.js] initialiseSselectedPolygon(): " + 
													this.selectedPolygon.length);
											}
											return this.selectedPolygon;
										},
										sortSselectedPolygon: function() { // Sort selectedPolygon list alphabetically by id 
											this.selectedPolygon.sort(function(a, b) {
												if (a.id < b.id) {
													return -1;
												}
												else if (a.id > b.id) {
													return 1;
												}
												else { // Same
													return 0;
												}
											}); // Alphabetically by id!
											return this.selectedPolygon;
										},
										addToSselectedPolygon: function(item) {	// Add item to selectedPolygon
											if (item && item.id) {
												if (this.selectedPolygonObj[item.id]) {
													throw new Error("Duplicate items: " + item.id + " in selectedPolygon list");
												}
												this.selectedPolygonObj[item.id] = item;
												this.selectedPolygon.push(item);
												AlertService.consoleDebug("[rifs-util-mapstate.js] addToSselectedPolygon(" +
													JSON.stringify(item) + "): " + 
													this.selectedPolygon.length);
											}
											else {
												throw new Error("Null item/id: " + JSON.stringify(item));
											}
											return this.selectedPolygon;
										},
										removeFromSselectedPolygon: function(id) { // Remove item from selectedPolygon
											if (id) {
												if (this.selectedPolygonObj[id]) {
													var found=false;
													for (var i = 0; i < this.selectedPolygon.length; i++) { 
														if (this.selectedPolygon[i].id == id) {
															found=true;
															AlertService.consoleDebug("[rifs-util-mapstate.js] removeFromSselectedPolygon(" +
																JSON.stringify(this.selectedPolygonObj[id]) + "): " + 
																(this.selectedPolygon.length-1));
															this.selectedPolygon.splice(i, 1);
															delete this.selectedPolygonObj[id];
															break;
														}
													}	
													if (!found) {
														throw new Error("Cannot find item: " + id + " in selectedPolygon list");
													}	
												}
												else {
													throw new Error("Cannot find item: " + id + " in selectedPolygon object");
												}
											}
											else {
												throw new Error("Null id");
											}
											return this.selectedPolygon;
										},						
										currentBand: undefined,
										possibleBands: undefined,
										description: mapNames[key]
									}
								}								
							}
							if (!found) {
								throw new Error("[rifs-util-mapstate.js] invalid map name: " + mapName);
							}
							return s[mapName];
						}
                    };
                }]);
