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
            function ($scope, user, $injector, $uibModal, $interval, $rootScope) {

                //The user to display
                $scope.username = user.currentUser;

                //Check for update in status every x ms Seconds
                var ms = 4000;
                var stop;
                var studies;
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
                            var check = {
								ok: [],
								running: [],
								failed: []
							};
                            for (var i = 0; i < studies.length; i++) {
                                if (studies[i].study_state === "S") { // OK
                                    check.ok.push(studies[i].study_id);
                                }
								else if (studies[i].study_state === "G" || studies[i].study_state === "F") {
									
                                    check.failed.push(studies[i].study_id);
									// G: Extract failure, extract, results or maps not created
									// F: R failure, R has caught one or more exceptions 
								}
								else if (studies[i].study_state === "C" || studies[i].study_state === "V" ||
								         studies[i].study_state === "E" || studies[i].study_state === "R") {
						/*	C: created, not verified; 
						 *	V: verified, but no other work done; [NOT USED BY MIDDLEWARE]
						 *	E: extracted imported or created, but no results or maps created; 
						 *	R: initial results population, create map table; [NOT USED BY MIDDLEWARE] design] */
                                    check.running.push(studies[i].study_id);
									
									// Detect running study
                                    if (!angular.isUndefined($scope.studyIds)) {
										var name = "";
										for (var k = 0; k < $scope.studyIds.running.length; k++) {
											if (studies[i].study_id === $scope.studyIds.running[k]) {
												name = studies[i].study_name;
												break;
											}
										}
										if (name == "") { // New study
//											console.log("getCurrentStatusAllStudies() Added new running study: " + studies[i].study_id +
//												"; state: " + studies[i].study_state + 
//												"; running: (" + check.running.length + "): " + check.running.sort());
											$scope.showSuccess("Study " + studies[i].study_id + " - " + studies[i].study_name + " is now running");
										}
									}
								}
							} // End of studies loop
							
                            if (angular.isUndefined($scope.studyIds)) {
                                $scope.studyIds = angular.copy(check);
//								console.log("getCurrentStatusAllStudies() create check OK: (" + 
//									check.ok.length + "): " + check.ok.sort() +
//									"; running: " + check.running.length + "): " + check.running.sort() +
//									"; failed: " + check.failed.length + "): " + check.failed.sort());
                            } 
							else if (check.ok.length != $scope.studyIds.ok.length) { // Something has completed
								var s = arrayDifference(check.ok, $scope.studyIds.ok); // Should only be one - checked above
								for (var j = 0; j < s.length; j++) {
									var name = "";
									var id = "";
									var study_state = "";
									for (var i = 0; i < studies.length; i++) {
										if (studies[i].study_id === s[j]) {
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
//										console.log("getCurrentStatusAllStudies() completed study: " + studies[i].study_id);
										$scope.showSuccessNoHide("Study " + id + " - " + name + " has been fully processed");

										//update study lists in other tabs
										$rootScope.$broadcast('updateStudyDropDown', {study_id: s[j], name: name});								
									}		
									else {
										$scope.showErrorNoHide("Study " + id + " - " + name + " is in an unexpected study state: " + study_state)
									}
								}
//								console.log("getCurrentStatusAllStudies() changed check of OK: (" + 
//									check.ok.length + "): " + check.ok.sort() +
//									"; running: " + check.running.length + "): " + check.running.sort() +
//									"; failed: " + check.failed.length + "): " + check.failed.sort());
								$scope.studyIds = angular.copy(check);
                            }							
							else if (check.failed.length != $scope.studyIds.failed.length) { // Something has failed
								var s = arrayDifference(check.failed, $scope.studyIds.failed); // Should only be one - checked above
								for (var j = 0; j < s.length; j++) {
									var name = "";
									var id = "";
									var study_state = "";
									for (var i = 0; i < studies.length; i++) {
										if (studies[i].study_id === s[j]) {
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
										$scope.showErrorNoHide("Study " + id + " - " + name + " failed to be extracted");
									}
									else if (study_state == "F") {
										// F: R failure, R has caught one or more exceptions 
										$scope.showErrorNoHide("Study " + id + " - " + name + " failed to complete statistical processing");
									}	
									else {
										$scope.showErrorNoHide("Study " + id + " - " + name + " is in an unexpected study state: " + study_state)
									}
								}
//								console.log("getCurrentStatusAllStudies() changed check of FAILED: (" + 
//									check.ok.length + "): " + check.ok.sort() +
//									"; running: " + check.running.length + "): " + check.running.sort() +
//									"; failed: " + check.failed.length + "): " + check.failed.sort());
								$scope.studyIds = angular.copy(check);
                            }
							else if (check.running.length != $scope.studyIds.running.length) { // Something has started
//								console.log("getCurrentStatusAllStudies() update check OK: (" + 
//									check.ok.length + "): " + check.ok.sort() +
//									"; running: " + check.running.length + "): " + check.running.sort()+
//									"; failed: " + check.failed.length + "): " + check.failed.sort());
								$scope.studyIds = angular.copy(check);
							}				
							bPoll = true;
							$scope.bPoll = angular.copy(bPoll);
                        });
                    }
                }, ms);

                function arrayDifference(source, target) {
                    return source.filter(function (current) {
                        return target.indexOf(current) === -1;
                    });
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
                    user.logout(user.currentUser).then(handleLogout, handleLogout);
                };
                function handleLogout(res) {
                    $injector.get('$state').transitionTo('state0');
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