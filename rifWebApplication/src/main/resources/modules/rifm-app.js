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
 */

/*
 * MODULE for the main RIF web application
 */
angular.module("RIF",
        [
            "ui.router",
            "ui.bootstrap",
            "ui.grid",
            "ui.grid.selection",
            "ui.grid.resizeColumns",
            "ui.grid.autoResize",
            "ui.grid.exporter",
            "ngAnimate",
            "ngSanitize",
            "ngNotificationsBar",
            "ui.layout",
            "ngMaterial",
            "ngPatternRestrict"
        ]
        )
        .config(['$stateProvider', '$urlRouterProvider',
            function ($stateProvider, $urlRouterProvider) {
                //Handle main page transitions in navbar, login
                $stateProvider
                        .state('state0', {
                            url: '/login',
                            templateUrl: "dashboards/login/partials/rifp-login-main.html",
                            controller: 'LoginCtrl'
                        })
                        .state('state1', {
                            url: '/submission',
                            templateUrl: "dashboards/submission/partials/rifp-dsub-main.html",
                            controller: 'SumbmissionCtrl'
                        })
                        .state('state2', {
                            url: '/viewer',
                            templateUrl: "dashboards/viewer/partials/rifp-view-main.html",
                            controller: 'ViewerCtrl'
                        })
                        .state('state3', {
                            url: '/mapping',
                            templateUrl: "dashboards/mapping/partials/rifp-dmap-main.html",
                            controller: 'MappingCtrl'
                        })
                        .state('state4', {
                            url: '/export',
                            templateUrl: "dashboards/export/partials/rifp-expt-main.html",
                            controller: 'ExportCtrl'
                        });
                $urlRouterProvider.otherwise("/login");
            }])
        .run(function ($rootScope, $state) {
            $rootScope.$state = $state;
        })
        .run(['$rootScope', '$uibModalStack', 'AlertService', 'SelectStateService',
				function ($rootScope, $uibModalStack, AlertService, SelectStateService) {
			
			var savedSelectState = {};
			
            $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
				// Save/restore SelectStateService as require
				
				if (fromState.name == 'state0' || toState.name == 'state0') {
					savedSelectState = {};
					AlertService.consoleLog("[rifm-app.js] state change from: " + (fromState.name || "NO STATE") + " to: " + toState.name +
						"; reset savedSelectState");
				}
				else if (fromState.name == 'state1' && toState.name == 'state1') {
					savedSelectState = SelectStateService.getState();
					AlertService.consoleLog("[rifm-app.js] state no change from: " + (fromState.name || "NO STATE") + " to: " + toState.name +
						"; save savedSelectState");
				}
				else if (fromState.name == 'state1' && toState.name != 'state1') {
					savedSelectState = SelectStateService.getState();
					AlertService.consoleLog("[rifm-app.js] state change from: " + (fromState.name || "NO STATE") + " to: " + toState.name +
						"; save savedSelectState");
				}
				else if (fromState.name != 'state1' && toState.name == 'state1' && savedSelectState) {
					if (savedSelectState.studySelection && savedSelectState.studyType) {
						SelectStateService.setStudySelection(savedSelectState.studySelection, savedSelectState.studyType);
						AlertService.consoleLog("[rifm-app.js] state change from: " + (fromState.name || "NO STATE") + " to: " + toState.name +
							"; restore savedSelectState");
					}
					else {
						AlertService.consoleLog("[rifm-app.js] state change from: " + (fromState.name || "NO STATE") + " to: " + toState.name +
							"; no savedSelectState to restore");
					}
				}
				else {
					AlertService.consoleLog("[rifm-app.js] state change from: " + (fromState.name || "NO STATE") + " to: " + toState.name);
				}
			});
			
            //force modal close on state change
            $rootScope.$on('$stateChangeSuccess', function (newVal, oldVal) {
                if (oldVal !== newVal) {
                    $uibModalStack.dismissAll();
                }
            });
        }]);