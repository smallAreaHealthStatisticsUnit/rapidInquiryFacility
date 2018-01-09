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
 * CONTROLLER for export save study 
 * just saves JSON as a text file
 */

/* global URL */

angular.module("RIF")
        .controller('ExportSaveCtrl', ['$scope', 'ModelService', 'user', 
            function ($scope, ModelService, user) {
 // http://bgrins.github.io/devtools-snippets/#console-save
                //get the study object
                $scope.getBlobJob = function () {
						$scope.extractStatus=user.getJsonFile(user.currentUser, $scope.studyID["exportmap"].study_id).then(function (res) {
						if (res && res.data && res.data.rif_job_submission) {			
												
							var json = JSON.stringify(res.data, null, 2); // JSON5
							var blob = new Blob([json], {type: 'text/json'});
							var filename = "RIFstudy_" + $scope.studyID["exportmap"].study_id + ".json";
							$scope.consoleDebug("Created JSON file: " + filename);
							
							if (window.navigator && window.navigator.msSaveOrOpenBlob) {
								window.navigator.msSaveOrOpenBlob(blob, filename);
							} 
							else {
								var e = document.createEvent('MouseEvents'),
										a = document.createElement('a');
								a.download = filename;
								a.href = window.URL.createObjectURL(blob);
								a.dataset.downloadurl = ['text/json', a.download, a.href].join(':');
								e.initEvent('click', true, false, window,
										0, 0, 0, 0, 0, false, false, false, false, 0, null);
								a.dispatchEvent(e);
							}
							
							if (res.data.rif_job_submission && 
							    res.data.rif_job_submission.taxonomy_initialise_error && 
							    res.data.rif_job_submission.taxonomy_initialise_error == true) {
								$scope.showWarning("taxonomy services has not yet been initialised; please save again in 5 minutes");
							}
							if (res.data.rif_job_submission && 
							    res.data.rif_job_submission.other_taxonomy_error) {
								$scope.showWarning("RIF services to taxonomy services link had error; please see logs");
								$scope.consoleLog("Taxonomy services error message: " + 
									res.data.rif_job_submission.other_taxonomy_error.message);
								$scope.consoleLog("Taxonomy services error stack: " + 
									res.data.rif_job_submission.other_taxonomy_error.stack_trace_text);
							}
						}

						else {			
							$scope.showError("Retrieved no study JSON; unable to create JSON file");
						}
					}, function (error) {
						$scope.showError("Could not retrieve study JSON; unable to create JSON file"); 
					});	
                };
            }]);