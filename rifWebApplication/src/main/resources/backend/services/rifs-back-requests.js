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
 
 * David Morley
 * @author dmorley
 */

/*
 * SERVICE for all requests to the middleware
 */
angular.module("RIF")
        .service('user', ['$http', 'servicesConfig', 
            function ($http, servicesConfig) {

                var self = this;
                self.currentUser = "";

                //identify specific middleware calls in the interceptor
                var config = {
                    headers: {
                        "rifUser": "rif"
                    }
                };

//				console.log("servicesConfig: " + JSON.stringify(servicesConfig));
					
                //submit a study               
                self.submitStudy = function (username, jsonObj) {
                    var blob = new Blob([JSON.stringify(jsonObj)], {
                        type: "text/plain"
                    });

                    var formData = new FormData();
                    formData.append("userID", username);
                    formData.append("fileField", blob, "submissionFile.txt");
                    formData.append("fileFormat", "JSON");

                    return $http.post(servicesConfig.studySubmissionURL + "submitStudy/", formData, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    });
                };

                //Note in the example URLs below either pg/ or ms/ needs to be added before the first paramter

                //login
                self.login = function (username, password) {
                    //http://localhost:8080/rifServices/studySubmission/login?userID=kgarwood&password=xyz
                    //[{"result":"User kgarwood logged in."}]
                    self.currentUser = username;
                    return $http.get(servicesConfig.studySubmissionURL + 'login?userID=' + username + '&password=' + password);
                };
                self.logout = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/logout?userID=kgarwood
                    //[{"result":"User kgarwood logged out."}]
                    self.currentUser = "";
                    return $http.get(servicesConfig.studySubmissionURL + 'logout?userID=' + username);
                };
                self.isLoggedIn = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/isLoggedIn?userID=kgarwood
                    //[{"result":"true"}]
                    return $http.get(servicesConfig.studySubmissionURL + 'isLoggedIn?userID=' + username);
                };
                self.getFrontEndParameters = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/ms/getFrontEndParameters?userID=peter
                    //JSON
                    return $http.get(servicesConfig.studySubmissionURL + 'getFrontEndParameters?userID=' + username);
                };
                //Taxonomy services              
                self.initialiseService = function () {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/initialiseService
                    //true
                    return $http.get(servicesConfig.taxonomyServicesURL + 'initialiseService', config);
                };
                self.getTaxonomyServiceProviders = function () {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getTaxonomyServiceProviders
                    //[{"identifier":"icd10","name":"ICD Taxonomy Service","description":"ICD 10 is a classification of diseases."}]
                    return $http.get(servicesConfig.taxonomyServicesURL + 'getTaxonomyServiceProviders', config);
                };
                self.getMatchingTerms = function (taxonomy, text) {
                    //http://localhost:8080/taxonomyServices/taxonomyServices/getMatchingTerms?taxonomy_id=icd10&search_text=asthma&is_case_sensitive=false
                    return $http.get(servicesConfig.taxonomyServicesURL + 'getMatchingTerms?taxonomy_id=' + taxonomy + '&search_text=' + text + '&is_case_sensitive=false', config);
                };
                //Project info
                self.getProjects = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getProjects?userID=kgarwood
                    //[{"name":"TEST","description":null}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getProjects?userID=' + username, config);
                };
                self.getProjectDescription = function (username, projectName) {
                    //http://localhost:8080/rifServices/studySubmission/getProjectDescription?userID=kgarwood&projectName=TEST
                    //[{"result":"Test Project. Will be disabled when in production."}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getProjectDescription?userID=' + username + '&projectName=' + projectName, config);
                };
                self.getGeographies = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getGeographies?userID=kgarwood
                    //[{"names":["EW01","SAHSU","UK91"]}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getGeographies?userID=' + username, config);
                };
                self.getHealthThemes = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getHealthThemes?userID=kgarwood&geographyName=SAHSU
                    //[{"name":"SAHSULAND","description":"SAHSU land cancer incidence example data"}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getHealthThemes?userID=' + username + '&geographyName=' + geography, config);
                };
                self.getNumerator = function (username, geography, healthThemeDescription) {
                    //http://localhost:8080/rifServices/studySubmission/getNumerator?userID=kgarwood&geographyName=SAHSU&healthThemeDescription=SAHSU%20land%20cancer%20incidence%20example%20data
                    //[{"numeratorTableName":"SAHSULAND_CANCER","numeratorTableDescription":"Cancer cases in SAHSU land","denominatorTableName":"SAHSULAND_POP","denominatorTableDescription":"SAHSU land population"}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getNumerator?userID=' + username + '&geographyName=' + geography + "&healthThemeDescription=" + healthThemeDescription, config);
                };
                //Investigation parameters
                self.getYearRange = function (username, geography, numeratorTableName) {
                    //http://localhost:8080/rifServices/studySubmission/getYearRange?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    //[{"lowerBound":"1989","upperBound":"1996"}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getYearRange?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numeratorTableName, config);
                };
                self.getSexes = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getSexes?userID=kgarwood
                    //[{"names":["Males","Females","Both"]}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getSexes?userID=' + username, config);
                };
                self.getCovariates = function (username, geography, geoLevel) {
                    //http://localhost:8080/rifServices/studySubmission/getCovariates?userID=kgarwood&geographyName=SAHSU&geoLevelToMapName=LEVEL4
                    return $http.get(servicesConfig.studySubmissionURL + 'getCovariates?userID=' + username + '&geographyName=' + geography + '&geoLevelToMapName=' + geoLevel, config);
                };
                self.getAgeGroups = function (username, geography, numerator) {
                    //http://localhost:8080/rifServices/studySubmission/getAgeGroups?userID=kgarwood&geographyName=SAHSU&numeratorTableName=SAHSULAND_CANCER
                    return $http.get(servicesConfig.studySubmissionURL + 'getAgeGroups?userID=' + username + '&geographyName=' + geography + '&numeratorTableName=' + numerator, config);
                };
                //geography info
                self.getDefaultGeoLevelSelectValue = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getDefaultGeoLevelSelectValue?userID=kgarwood&geographyName=SAHSU
                    //[{"names":["LEVEL2"]}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getDefaultGeoLevelSelectValue?userID=' + username + '&geographyName=' + geography, config);
                };
                self.getGeoLevelViews = function (username, geography, geoLevelSelectName) {
                    //http://localhost:8080/rifServices/studySubmission/getGeoLevelViews?userID=kgarwood&geographyName=SAHSU&geoLevelSelectName=LEVEL2
                    //[{"names":["LEVEL2","LEVEL3","LEVEL4"]}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getGeoLevelViews?userID=' + username + '&geographyName=' + geography +
                            '&geoLevelSelectName=' + geoLevelSelectName, config);
                };
                self.getGeoLevelSelectValues = function (username, geography) {
                    //http://localhost:8080/rifServices/studySubmission/getGeoLevelSelectValues?userID=kgarwood&geographyName=SAHSU
                    //[{"names":["LEVEL1","LEVEL2","LEVEL3","LEVEL4"]}]
                    return $http.get(servicesConfig.studySubmissionURL + 'getGeoLevelSelectValues?userID=' + username + '&geographyName=' + geography, config);
                };
                //statistical methods
                self.getAvailableCalculationMethods = function (username) {
                    //http://localhost:8080/rifServices/studySubmission/getAvailableCalculationMethods?userID=kgarwood
                    //[{"codeRoutineName":"car_r_procedure","prior":"Standard deviation","description":"Applies ...
                    return $http.get(servicesConfig.studySubmissionURL + 'getAvailableCalculationMethods?userID=' + username, config);
                };
                //status
                self.getCurrentStatusAllStudies = function (username) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getCurrentStatusAllStudies?userID=kgarwood              
                    //{"smoothed_results_header":["study_id","study_name","study_description","study_state","message","date"]
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getCurrentStatusAllStudies?userID=' + username, config);
                };
                //results for viewer
                self.getSmoothedResults = function (username, studyID, sex) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSmoothedResults?userID=kgarwood&studyID=1&sex=1
                    //{"smoothed_results_header":["area_id","band_id","genders","observed","...
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getSmoothedResults?userID=' + username + '&studyID=' + studyID + '&sex=' + sex, config);
                };
                self.getAllPopulationPyramidData = function (username, studyID, year) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getAllPopulationPyramidData?userID=kgarwood&studyID=1&year=1990
                    //{smoothed_results_header:{population_label,males,females}smoothed_results:{{population_la...
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getAllPopulationPyramidData?userID=' + username + '&studyID=' + studyID + '&year=' + year, config);
                };
                //get info on a completed study
                self.getYearsForStudy = function (username, studyID, leaflet) {
                    config.leaflet = leaflet;
                    //http://localhost:8080/rifServices/studyResultRetrieval/getYearsForStudy?userID=kgarwood&study_id=1
                    //{"years{":["1989","1990","1991","1992","1993","1994","1995","1996"]}
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getYearsForStudy?userID=' + username + '&study_id=' + studyID, config);
                };
                self.getSexesForStudy = function (username, studyID, leaflet) {
                    config.leaflet = leaflet;
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSexesForStudy?userID=kgarwood&study_id=1
                    //[{"names":["Males","Females","Both"]}]
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getSexesForStudy?userID=' + username + '&study_id=' + studyID, config);
                };
                self.getGeographyAndLevelForStudy = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getGeographyAndLevelForStudy?userID=kgarwood&studyID=239
                    //[["SAHSU","LEVEL3"]]
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getGeographyAndLevelForStudy?userID=' + username + '&studyID=' + studyID);
                };
                //get the map tiles from Tile-Maker
                //returns a string not a promise, is resolved in Leaflet GridLayer
                self.getTileMakerTiles = function (username, geography, geoLevel, tileType) {
					if (tileType && tileType != "geojson" && tileType != "topojson" && tileType != "png") {
						throw new Error("Invalid tileType: " + tileType);
					}
                    //'http://localhost:8080/rifServices/studyResultRetrieval/getTileMakerTiles?userID=kgarwood&geographyName=SAHSU&geoLevelSelectName=LEVEL2&zoomlevel={z}&x={x}&y={y}';
                    return (servicesConfig.studyResultRetrievalURL + 'getTileMakerTiles?userID=' + username + '&geographyName=' + geography + '&geoLevelSelectName=' + geoLevel +
                            '&zoomlevel={z}&x={x}&y={y}&tileType=' + (tileType || 'topojson')); // String 
                };
                //get 'global' geography for attribute table (DO NOT USE THE TILE DATA AT HIGH RESOLUTIONS; SMALL AREAS WILL HAVE BEEN OPTIMISED OUT
				// self.getTileMakerAttributes is a replacement

                self.getTileMakerTilesAttributes = function (username, geography, geoLevel) {
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getTileMakerTiles?userID=' + username + 
							'&geographyName=' + geography + '&geoLevelSelectName=' + geoLevel +
                            '&zoomlevel=1&x=0&y=0&tileType=topojson', config);
                };
                //get 'global' geography attribute table
				/* Instead of the topoJSON tile returned by getTileMakerTiles... it returns:
					data {
						attributes: [{
							area_id,
							name,
							band
						}, ...
						]
					}
				 */				
                self.getTileMakerAttributes = function (username, geography, geoLevel) {
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getTileMakerAttributes?userID=' + username + 
							'&geographyName=' + geography + '&geoLevelSelectName=' + geoLevel, config);
                };				
                //get all the centroids for the current geolevel
                self.getTileMakerCentroids = function (username, geography, geoLevel) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getTileMakerCentroids?userID=dwmorley&geographyName=SAHSULAND&geoLevelSelectName=SAHSU_GRD_LEVEL4
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getTileMakerCentroids?userID=' + username + '&geographyName=' + geography + '&geoLevelSelectName=' + geoLevel);
                };
				
                //check postcode
                self.getPostalCodes = function (username, geography, postcode) {
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getPostalCodes?userID=' + username + '&geographyName=' + geography + '&postcode=' + postcode);
                };                //check postcode
				
                self.getPostalCodeCapabilities = function (username, geography) {
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getPostalCodeCapabilities?userID=' + username + '&geographyName=' + geography);
                };					// Get postal coding capabilities of database (and srid)
				
                //get areas used in a completed study (recycled faulty KG method)
                self.getStudySubmission = function (username, studyID) {
                    //http://localhost:8080/rifServices/studySubmission/getStudySubmission?userID=kgarwood&studyID=274
                    return $http.get(servicesConfig.studySubmissionURL + 'getStudySubmission?userID=' + username + '&studyID=' + studyID);
                };
                //get details of a completed study
                self.getDetailsForProcessedStudy = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getDetailsForProcessedStudy?userID=dwmorley&studyID=35
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getDetailsForProcessedStudy?userID=' + username + '&studyID=' + studyID);
                };
                //get health codes used in a completed study
                self.getHealthCodesForProcessedStudy = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getHealthCodesForProcessedStudy?userID=dwmorley&studyID=35
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getHealthCodesForProcessedStudy?userID=' + username + '&studyID=' + studyID);
                };
                //get map or extract table preview of a completed study
                self.getStudyTableForProcessedStudy = function (username, studyID, type, stt, stp) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getStudyTableForProcessedStudy?userID=dwmorley&studyID=35&type=extract&stt=2&stp=100
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getStudyTableForProcessedStudy?userID=' + username + '&studyID=' + studyID +
                            '&type=' + type + '&stt=' + stt + '&stp=' + stp);
                };

                //Save study tables to Zip file
                self.createZipFile = function (username, studyID, zoomLevel, getTimeout) {
                    //http://localhost:8080/rifServices/studySubmission/pg/createZipFile?userID=dwmorley&studyID=46
                    return $http.get(servicesConfig.studySubmissionURL + 'createZipFile?userID=' + username + '&studyID=' + studyID + "&zoomLevel=" + zoomLevel,
						{timeout: getTimeout /* in mS: 360s */});
                };                
				//Fetch Zip file
               self.getZipFileURL = function (username, studyID, zoomLevel) {
                    //http://localhost:8080/rifServices/studySubmission/pg/getZipFile?userID=dwmorley&studyID=46
                    return servicesConfig.studySubmissionURL + 'getZipFile?userID=' + username + '&studyID=' + studyID + "&zoomLevel=" + zoomLevel;
                };         
				//Get Extract Status - can Zip file be created/fetched
               self.getExtractStatus = function (username, studyID) {
                    //http://localhost:8080/rifServices/studySubmission/pg/getExtractStatus?userID=dwmorley&studyID=46
                    return $http.get(servicesConfig.studySubmissionURL + 'getExtractStatus?userID=' + username + '&studyID=' + studyID);
                };
				// Get JSON study setup file
				self.getJsonFile = function (username, studyID) {
                    //http://localhost:8080/rifServices/studySubmission/pg/getJsonFile?userID=dwmorley&studyID=46
                    return $http.get(servicesConfig.studySubmissionURL + 'getJsonFile?userID=' + username + '&studyID=' + studyID);
                };     

				// Get map background
				self.getMapBackground = function (username, geography) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getMapBackground?userID=dwmorley&geography=sahsuland
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getMapBackground?userID=' + username + '&geographyName=' + geography);
                };   		
				// Get JSON study select state
				self.getSelectState = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getSelectState?userID=dwmorley&studyID=46
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getSelectState?userID=' + username + '&studyID=' + studyID);
                };  		
				// Get the rif40_homogeneity data for a study
				self.getHomogeneity = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getHomogeneity?userID=peter&studyID=55
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getHomogeneity?userID=' + username + '&studyID=' + studyID);
                };   		
				// Get Covariate Loss Report
				self.getCovariateLossReport = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getCovariateLossReport?userID=dwmorley&studyID=46
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getCovariateLossReport?userID=' + username + '&studyID=' + studyID);
                };  		
				// Get Risk Graph
				self.getRiskGraph = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getRiskGraph?userID=dwmorley&studyID=46
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getRiskGraph?userID=' + username + '&studyID=' + studyID);
                };   		
				// Get JSON study print state
				self.getPrintState = function (username, studyID) {
                    //http://localhost:8080/rifServices/studyResultRetrieval/getPrintState?userID=dwmorley&studyID=46
                    return $http.get(servicesConfig.studyResultRetrievalURL + 'getPrintState?userID=' + username + '&studyID=' + studyID);
                }; 
				// Set JSON study print state
				self.setPrintState = function (username, studyID, printState) {
					var blob = new Blob([JSON.stringify(printState)], {
                        type: "text/plain"
                    });

                    var formData = new FormData();
                    formData.append("userID", username);
                    formData.append("studyID", studyID);
                    formData.append("fileField", blob, "printState.txt");
                    formData.append("fileFormat", "JSON");

                    return $http.post(servicesConfig.studyResultRetrievalURL + "setPrintState/", formData, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    });
                };  				
				
				//Middleware logger for front end
               self.rifFrontEndLogger = function (
					username,
					messageType,
					browserType,
					message, 
					errorMessage,
					errorStack,
					actualTime,
					relativeTime) {
                    //http://localhost:8080/rifServices/studySubmission/pg/rifFrontEndLogger?userID=dwmorley&messageType=INFO&browserType=XXX&message=Hello
					var uri=undefined;
					try {
						uri=servicesConfig.studySubmissionURL + 'rifFrontEndLogger?userID=' + username +
							'&messageType=' + messageType + 
							'&browserType=' + encodeURIComponent(browserType) + 
							'&message=' + encodeURIComponent((message || "NO MESSAGE!"));
						if (errorMessage) {
							uri+='&errorMessage=' + encodeURIComponent(errorMessage);
						}
						if (errorStack) {
							uri+='&errorStack=' + encodeURIComponent(errorStack);
						}
						uri+='&actualTime=' + encodeURIComponent(actualTime) + 
							'&relativeTime=' + encodeURIComponent(relativeTime);
						return $http.get(uri);
					}
					catch(e) {
						if (window.console && console && console.log && typeof console.log == "function") { // IE safe
							if (isIE()) {
								if (window.__IE_DEVTOOLBAR_CONSOLE_COMMAND_LINE) {
									console.log("[rifs-back-requests.js] rifFrontEndLogger error: " + JSON.stringify(e) + // IE safe
										"\n" + message); 
								}
							}
							else {
								console.log("[rifs-back-requests.js] rifFrontEndLogger error: " + JSON.stringify(e) + // IE safe
									"\n" + message); 
							}
						}  						
						return undefined;
					}
                };
            }]);
