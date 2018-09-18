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
 * SERVICE for map-table directive in comaprison and study areas
 */
/* global d3 */

angular.module("RIF")
        .factory('ModalAreaService',
                function () {
                    //UI-Grid setup options
                    var areaTableOptions = {
                        enableFiltering: true,
                        enableRowSelection: true,
                        enableColumnResizing: true,
                        enableRowHeaderSelection: false,
                        enableHorizontalScrollbar: 0,
                        selectionRowHeaderWidth: 35,
                        rowHeight: 25,
                        multiSelect: true,
                        rowTemplate: rowTemplate()
                    };
                    var areaTableColumnDefs = [
                        {field: 'area_id', enableHiding: false, width: "*"},
                        {field: 'label', sort: { direction: 'asc', priority: 0 }, enableHiding: false, width: "*"},
                        {field: 'band', enableHiding: false, width: "*"}
                    ];

                    //Set-up rows to capture click and keypress events
                    function rowTemplate() {
                        return  '<div id="testdiv" tabindex="0" ng-keydown="grid.appScope.keyDown($event)" ng-keyup="grid.appScope.keyUp($event)";>' +
                                '<div style="height: 100%" ng-class="{ ' +
                                'band1: row.entity.band===1,' +
                                'band2: row.entity.band===2,' +
                                'band3: row.entity.band===3,' +
                                'band4: row.entity.band===4,' +
                                'band5: row.entity.band===5,' +
                                'band6: row.entity.band===6' +
                                '}">' +
                                '<div ng-click="grid.appScope.rowClick(row)">' +
                                '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                                '</div>';
                    }

                    return {
                        //Populate the polygon attribute table
                        fillTable: function (data) {
                            var rowCollection = [];
                            for (var i = 0; i < data.attributes.length; i++) {
                                var obj = {
                                    area_id: data.attributes[i].area_id,
                                    label: data.attributes[i].name,
                                    band: undefined
                                };
                                rowCollection.push(obj);
                            }
                            return rowCollection;
                        },
                        //set up table
                        getAreaTableOptions: function () {
                            return areaTableOptions;
                        },
                        getAreaTableColumnDefs: function () {
                            return areaTableColumnDefs;
                        }
                    };
                });