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
 * CONTROLLER to handle tab transitions, logout and alert on new study completion
 */
angular.module("RIF")
        .controller('TabCtrl', ['$scope', 'user', '$injector', '$uibModal', '$interval', '$rootScope', 
			'SubmissionStateService', 'StudyAreaStateService', 'CompAreaStateService', 'ExportStateService',
            'ParameterStateService', 'StatsStateService', 'ViewerStateService', 'MappingStateService',
			'SelectStateService', 'CommonMappingStateService', 
            function ($scope, user, $injector, $uibModal, $interval, $rootScope, 
                    SubmissionStateService, StudyAreaStateService, CompAreaStateService, ExportStateService,
                    ParameterStateService, StatsStateService, ViewerStateService, MappingStateService,
					SelectStateService, CommonMappingStateService) {

                //The user to display
                $scope.username = user.currentUser;

                //Check for update in status every x ms Seconds
                var ms = 4000;
				var suppressionS = 15;
				var ignoreRunningS = 1209600; // Two weeks
                var stop;
                var studies;
				var numOlderRunningStudies=0;
                $scope.studyIds;
				$scope.bPoll;
				
                //In DEBUG set false to keep Tomcat console clear
                var bPoll = true;
				if (angular.isUndefined($scope.bPoll)) {
                    $scope.bPoll = angular.copy(bPoll);
				}
                stop = $interval(function () {
                    if ($scope.bPoll) {
						bPoll = false; // Prevent function stacking
						$scope.bPoll = angular.copy(bPoll);
						/*
						 * Ignore:
						 *	C: created, not verified; 
						 *	V: verified, but no other work done; [NOT USED BY MIDDLEWARE]
						 *	E: extracted imported or created, but no results or maps created; 
						 *	R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design]
						 *	W: R warning. [NOT USED BY MIDDLEWARE]
						 * Handle:
						 *
						 *	G: Extract failure, extract, results or maps not created;
						 *	S: R success;
						 *	F: R failure, R has caught one or more exceptions [depends on the exception handler]
						 */	
                        user.getCurrentStatusAllStudies(user.currentUser).then(function (res) {
                            studies = res.data.smoothed_results;
							/* studies is an array of:
							 *
							 * {
							 *	study_id: 374,
							 *	study_name: "1002 LUNG CANCER",
							 *	study_description: "null",
							 *	study_state: S,
							 *	date: "28 MAR 2018 10:54:52",   // AWLAYS IN THIS FORMAT!!!! See: PGSQLStudyStateManager.java
							 *									// getCurrentStatusAllStudies()
							 *	message: "The study results have been computed and they are now ready to be used."
							 *	trace: undefined
							 * }
							 */
                            var check = {
								ok: [],
								running: [],
								failed: []
							};
							var olderRunningStudies="";
							var tempNumOlderRunningStudies=0;
                            for (var i = 0; i < studies.length; i++) {
								studies[i].studyDate = moment(studies[i].date, "DD MMM YYYY HH:mm:ss");
									// Parse 28 MAR 2018 10:54:52
								studies[i].msgInterval=undefined;
								studies[i].parsedDate=undefined;
								if (studies[i].studyDate) {
									studies[i].studyTime=studies[i].studyDate.valueOf(); 
									studies[i].parsedDate=studies[i].studyDate.toString();
									nowTime=new Date().getTime(); // milliseconds since Jan 1, 1970, 00:00:00.000 GMT
									studies[i].msgInterval=(Math.round((nowTime - studies[i].studyTime)/100))/10; 
										// time since status message in S to 1/10S	
									studies[i].wasRunning=false;
									studies[i].isRunning=false;
									studies[i].runningReported=false;
									
                                    if (!angular.isUndefined($scope.studyIds)) {
										for (var k = 0; k < $scope.studyIds.running.length; k++) {
											if (studies[i].study_id === $scope.studyIds.running[k].study_id) {
												studies[i].wasRunning=true;	
												studies[i].k=k;
												// Preserve isRunning flag accross status updates
												studies[i].isRunning=$scope.studyIds.running[k].isRunning;
												studies[i].runningReported=$scope.studyIds.running[k].runningReported;
												// Preserve date stamps accross status updates
												studies[i].studyTime=$scope.studyIds.running[k].studyTime;  
												studies[i].studyDate=$scope.studyIds.running[k].studyDate; 
												studies[i].parsedDate=$scope.studyIds.running[k].parsedDate;
												studies[i].msgInterval=(Math.round((nowTime - 
													studies[i].studyTime)/100))/10; 			
											}
										}
									}
									
									if (studies[i].wasRunning && studies[i].msgInterval < ignoreRunningS) { // In running list <2 weeks
										if (!studies[i].runningReported) {
											if (studies[i].isRunning) {
												$scope.consoleLog("getCurrentStatusAllStudies() actual running study: " + 
													studies[i].study_id +
													"; study_state: " + studies[i].study_state + 
													"; isRunning: " + studies[i].isRunning +
													"; date: " + studies[i].date + 
													"; studyDate: " + studies[i].studyDate + 
													"; parsed date: " + studies[i].parsedDate +
													"; msgInterval: " + studies[i].msgInterval);
											}
											else {
												$scope.consoleLog("getCurrentStatusAllStudies() potential running study: " + 
													studies[i].study_id +
													"; study_state: " + studies[i].study_state + 
													"; isRunning: " + studies[i].isRunning +
													"; date: " + studies[i].date + 
													"; studyDate: " + studies[i].studyDate + 
													"; parsed date: " + studies[i].parsedDate +
													"; msgInterval: " + studies[i].msgInterval);	
											}
											studies[i].runningReported=true;
											$scope.studyIds.running[studies[i].k].runningReported=true;											
										}											
									}									
									else if (studies[i].wasRunning) { // In running list >2 weeks (i.e. stuck)
										tempNumOlderRunningStudies++;
										olderRunningStudies+=studies[i].study_id +
											"; study_state: " + studies[i].study_state + 
											"; isRunning: " + studies[i].isRunning +
											"; date: " + studies[i].date + 
											"; studyDate: " + studies[i].studyDate + 
											"; parsed date: " + studies[i].parsedDate +
											"; msgInterval: " + studies[i].msgInterval + "\n";										
									}
									else if (studies[i].msgInterval && studies[i].msgInterval <= suppressionS) { // Print last 15S of messages
										$scope.consoleLog("getCurrentStatusAllStudies() study: " + 
											studies[i].study_id +
											"; study_state: " + studies[i].study_state + 
											"; date: " + studies[i].date + 
											"; studyDate: " + studies[i].studyDate + 
											"; parsed date: " + studies[i].parsedDate +
											"; msgInterval: " + studies[i].msgInterval);
									}
										
								}
								
                                if (studies[i].study_state === "S") { // OK
                                    check.ok.push(studies[i]);
                                }
								else if (studies[i].study_state === "G" || studies[i].study_state === "F") {
									
                                    check.failed.push(studies[i]);
									// G: Extract failure, extract, results or maps not created
									// F: R failure, R has caught one or more exceptions 
								}
								else if (studies[i].study_state === "C" || studies[i].study_state === "V" ||
								         studies[i].study_state === "E" || studies[i].study_state === "R") {
						/*	C: created, not verified; 
						 *	V: verified, but no other work done; [NOT USED BY MIDDLEWARE]
						 *	E: extracted imported or created, but no results or maps created; 
						 *	R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design] */
                                    check.running.push(studies[i]);
									
									// Detect running study
                                    if (!angular.isUndefined($scope.studyIds)) {
										var name = "";
										for (var k = 0; k < $scope.studyIds.running.length; k++) {
											if (studies[i].study_id === $scope.studyIds.running[k].study_id) {
												name = studies[i].study_name;
												break;
											}
										}
										if (name == "") { // New study
											if (studies[i].msgInterval && studies[i].msgInterval > suppressionS) {
												$scope.consoleLog("getCurrentStatusAllStudies() Previous new running study: " + studies[i].study_id +
													"; state: " + studies[i].study_state + 
													"; msgInterval: " + studies[i].msgInterval + 
													"; parsed date: " + studies[i].parsedDate +
													"; running: (" + check.running.length + "): " + getStudyIds(check.running));
											}
											else {	
												$scope.showSuccessNoHide("Study " + studies[i].study_id + " - " + studies[i].study_name + " is now running");
												studies[i].isRunning=true;
											}
										}
									}
								}
							} // End of studies loop
							
							if (olderRunningStudies.length > 0 && 
							    numOlderRunningStudies != tempNumOlderRunningStudies) {
								$scope.consoleLog("getCurrentStatusAllStudies() older potential running studies:\n" + 
									olderRunningStudies);
							}
							numOlderRunningStudies=tempNumOlderRunningStudies;
							
                            if (angular.isUndefined($scope.studyIds)) {
                                $scope.studyIds = angular.copy(check);
//								$scope.consoleLog("getCurrentStatusAllStudies() create check OK: (" + 
//									check.ok.length + "): " + getStudyIds(check.ok) +
//									"; running: " + check.running.length + "): " + getStudyIds(check.running) +
//									"; failed: " + check.failed.length + "): " + getStudyIds(check.failed));
                            } 
							else if (check.ok.length != $scope.studyIds.ok.length) { // Something has completed
								var s = arrayDifference("OK", check.ok, $scope.studyIds.ok); // Should only be one - checked above
								for (var j = 0; j < s.length; j++) {
									var name = "";
									var id = "";
									var study_state = "";
									for (var i = 0; i < studies.length; i++) {
										if (studies[i].study_id === s[j].study_id) {
											name = studies[i].study_name;
											id = studies[i].study_id;
											study_state = studies[i].study_state;
											break;
										}
									}
									if (name == "") {
										$scope.showErrorNoHide("Unable to deduce study name/id/study_state for study " + j + "/" + s.length + " : " + s[j])
									}
									else if (study_state == 'S') { // OK
// +317.9: getCurrentStatusAllStudies() completed study: 380; msgInterval: 15625103.5; parsed date: Fri Sep 29 2017 11:39:52 GMT+0100
// ignoreRunningS = 1209600
										if (!studies[i].isRunning &&
										    studies[i].msgInterval && studies[i].msgInterval > ignoreRunningS) {
											$scope.consoleLog(
												"getCurrentStatusAllStudies() completed study: " + 
												studies[i].study_id +
												"; msgInterval: " + studies[i].msgInterval +
												"; parsed date: " + studies[i].parsedDate);
										}
										else {
											$scope.showSuccessNoHide("Study " + id + " - " + name + " fully processed after " + 
												studies[i].msgInterval + " seconds");
										} 

										//update study lists in other tabs
										$rootScope.$broadcast('updateStudyDropDown', {study_id: s[j], name: name});								
									}		
									else {
										$scope.showErrorNoHide("Study " + id + " - " + name + " is in an unexpected study state: " + study_state)
									}
								}
//								$scope.consoleLog("getCurrentStatusAllStudies() changed check of OK: (" + 
//									check.ok.length + "): " + getStudyIds(check.ok) +
//									"; running: " + check.running.length + "): " + getStudyIds(check.running) +
//									"; failed: " + check.failed.length + "): " + getStudyIds(check.failed));
								$scope.studyIds = angular.copy(check);
                            }							
							else if (check.failed.length != $scope.studyIds.failed.length) { // Something has failed
								var s = arrayDifference("FAILED", check.failed, $scope.studyIds.failed); // Should only be one - checked above
								for (var j = 0; j < s.length; j++) {
									var name = "";
									var id = "";
									var study_state = "";
									for (var i = 0; i < studies.length; i++) {
										if (studies[i].study_id === s[j].study_id) {
											name = studies[i].study_name;
											id = studies[i].study_id;
											study_state = studies[i].study_state;
											break;
										}
									}
									if (name == "") {
										$scope.showErrorNoHide("Unable to deduce study name/id/study_state for study " + j + "/" + s.length + " : " + s[j])
									}
									if (study_state == "G") {
										// G: Extract failure, extract, results or maps not created
										
										if (!studies[i].isRunning &&
										    studies[i].msgInterval && studies[i].msgInterval > ignoreRunningS) {
											$scope.consoleError("Study " + id + " - " + name + 
												" failed to be extracted" +
												"; msgInterval: " + studies[i].msgInterval +
												"; parsed date: " + studies[i].parsedDate);
										}
										else {
											$scope.showErrorNoHide("Study " + id + " - " + name + 
												" failed to be extracted");
										}
									}
									else if (study_state == "F") {
										// F: R failure, R has caught one or more exceptions 
										if (!studies[i].isRunning &&
										    studies[i].msgInterval && studies[i].msgInterval > ignoreRunningS) {
											$scope.consoleError("Study " + id + " - " + name + 
												" failed to complete statistical processing" +
												"; msgInterval: " + studies[i].msgInterval +
												"; parsed date: " + studies[i].parsedDate);
										}
										else {
											$scope.showErrorNoHide("Study " + id + " - " + name + 
												" failed to complete statistical processing");		
										}
									}	
									else {
										if (!studies[i].isRunning &&
										    studies[i].msgInterval && studies[i].msgInterval > ignoreRunningS) {
											$scope.consoleError("Study " + id + " - " + name + 
												" is in an unexpected study state: " + study_state +
												"; msgInterval: " + studies[i].msgInterval +
												"; parsed date: " + studies[i].parsedDate);
										}
										else {
											$scope.showErrorNoHide("Study " + id + " - " + name + 
												" is in an unexpected study state: " + study_state);		
										}
									}
								}
//								$scope.consoleLog("getCurrentStatusAllStudies() changed check of FAILED: (" + 
//									check.ok.length + "): " + getStudyIds(check.ok) +
//									"; running: " + check.running.length + "): " + getStudyIds(check.running) +
//									"; failed: " + check.failed.length + "): " + getStudyIds(check.failed));
								$scope.studyIds = angular.copy(check);
                            }
							else if (check.running.length != $scope.studyIds.running.length) { // Something has started
//								$scope.consoleLog("getCurrentStatusAllStudies() update check OK: (" + 
//									check.ok.length + "): " + getStudyIds(check.ok) +
//									"; running: " + check.running.length + "): " + getStudyIds(check.running)+
//									"; failed: " + check.failed.length + "): " + getStudyIds(check.failed));
								$scope.studyIds = angular.copy(check);
							}				
							bPoll = true;
							$scope.bPoll = angular.copy(bPoll);
                        }, function (e) {
							bPoll = false; // Prevent function stacking
							$scope.consoleError("[rifc-util-tabctrl.js] Auto retrieve of study status failed: " + 
								JSON.stringify(e), e);
							$scope.showError("Auto retrieve of study status failed", e);
							$interval.cancel(stop);
						});
                    }
                }, ms);

				function getStudyIds(objArray) { // Get study_id's as sorted array from study object array
					var list=[];
					for (var i = 0; i < objArray.length; i++) {
						if (objArray[i].study_id) {
							list.push(objArray[i].study_id);
						}
					}
					if (list) {
						return list.sort();
					}
					else {
						return undefined;
					}
				}
				
                function arrayDifference(name, source, target) {
                    var diff=source.filter(function (currentValue) { // Compare source[current] with target
						rval=true;
						for (var i = 0; i < target.length; i++) {
							if (currentValue && currentValue.study_id == target[i].study_id) {
								rval=false;
							}
						}
                        return rval;
                    });
					
					$scope.consoleLog("getCurrentStatusAllStudies.arrayDifference(" + name + ")" +
						";\nsource: " + JSON.stringify(getStudyIds(source)) +
						";\ntarget: " + JSON.stringify(getStudyIds(target)) +
						";\ndiff: " + JSON.stringify(getStudyIds(diff)));
					return diff;
                }

                $scope.hamburger = function () {
                    var x = document.getElementById("myTopnav");
                    if (x.className === "topnav") {
                        x.className += " responsive";
                    } else {
                        x.className = "topnav";
                    }
                };

                $scope.$on('$destroy', function () {
                    if (!angular.isUndefined(stop)) {
                        $interval.cancel(stop);
                        stop = undefined;
                    }
                });

                //yes-no modal for $scope.logout
                $scope.openLogout = function () {
                    $scope.modalHeader = "Log out";
                    $scope.modalBody = "Unsaved work will be lost. Are you sure?";
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'utils/partials/rifp-util-yesno.html',
                        controller: 'ModalLogoutYesNoInstanceCtrl',
                        windowClass: 'stats-Modal',
                        backdrop: 'static',
                        scope: $scope,
                        keyboard: false
                    });
                };

                $scope.logout = function () {
                    $scope.openLogout();
                };
                $scope.doLogout = function () {
                    user.logout(user.currentUser).then(handleLogout, handleLogout /* Error method! */);
                };
                function handleLogout(res) {  
				//reset all services
					SubmissionStateService.resetState();
					StudyAreaStateService.resetState();
					CompAreaStateService.resetState();
					ParameterStateService.resetState();
					StatsStateService.resetState();
					ViewerStateService.resetState();
					MappingStateService.resetState();
					ExportStateService.resetState();
					SelectStateService.resetState();
                    CommonMappingStateService.resetState();
							
                    $injector.get('$state').transitionTo('state0'); // Logout
                }
            }])
        .controller('ModalLogoutYesNoInstanceCtrl',
                function ($scope, $uibModalInstance) {
                    $scope.close = function () {
                        $uibModalInstance.dismiss();
                    };
                    $scope.submit = function () {
                        $scope.doLogout();
                        $uibModalInstance.close();
                    };
                });