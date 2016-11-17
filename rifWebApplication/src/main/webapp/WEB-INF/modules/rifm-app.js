/* global L, topojson */

/**
 * <hr>
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
 * <pre> 
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
 * </pre>
 * <hr>
 */

/*
 * MODULE for the main RIF web application
 * Third-party modules injected here
 * Navbar state changes and notifications handled
 */

angular.module("RIF",
        [
            "ui.router",
            "ui.bootstrap",
            "ui-leaflet",
            "ui.grid",
            "ui.grid.selection",
            "ui.grid.resizeColumns",
            "ui.grid.treeView",
            "ui.grid.edit",
            "ui.grid.autoResize",
            "ui.grid.moveColumns",
            "ngAnimate",
            "ngSanitize",
            "ngNotificationsBar",
            "ui.layout"
        ]
        )
        .config(['$stateProvider', '$urlRouterProvider',
            function ($stateProvider, $urlRouterProvider) {

                //Extend Leaflet to handle topojson
                L.TopoJSON = L.GeoJSON.extend({
                    addData: function (jsonData) {
                        if (jsonData.type === "Topology") {
                            for (var key in jsonData.objects) {
                                geojson = topojson.feature(jsonData, jsonData.objects[key]);
                                L.GeoJSON.prototype.addData.call(this, geojson);
                            }
                        } else {
                            L.GeoJSON.prototype.addData.call(this, jsonData);
                        }
                    }
                });

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
                        });
                $urlRouterProvider.otherwise("/login");  //login submission
            }])
        .run(function ($rootScope, $state) {
            $rootScope.$state = $state;
        })
        .run(function ($rootScope, $uibModalStack) {
            //force modal close on state chnage
            $rootScope.$on('$stateChangeSuccess', function (newVal, oldVal) {
                if (oldVal !== newVal) {
                    $uibModalStack.dismissAll();
                }
            });
        });