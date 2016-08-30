/* SERVICE for map-table directive in comaprison and study areas
 * Used by rifd-dsub-maptable.js
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
                        {field: 'id', enableHiding: false, width: "*"},
                        {field: 'label', enableHiding: false, width: "*"},
                        {field: 'band', enableHiding: false, width: "*"}
                    ];

                    //Set-up rows to capture click and keypress events
                    function rowTemplate() {
                        return  '<div id="testdiv" tabindex="0" ng-keydown="grid.appScope.keyDown($event)" ng-keyup="grid.appScope.keyUp($event);">' +
                                '<div style="height: 100%" ng-class="{ ' +
                                'band1: row.entity.band===1,' +
                                'band2: row.entity.band===2,' +
                                'band3: row.entity.band===3,' +
                                'band4: row.entity.band===4,' +
                                'band5: row.entity.band===5,' +
                                'band6: row.entity.band===6,' +
                                'band7: row.entity.band===7,' +
                                'band8: row.entity.band===8,' +
                                'band9: row.entity.band===9' +
                                '}">' +
                                '<div ng-click="grid.appScope.rowClick(row)">' +
                                '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                                '</div>';
                    }

                    return {
                        //Populate the polygon attribute table
                        fillTable: function (data) {
                            var rowCollection = [];
                            for (var i = 0; i < data.objects['2_1_1'].geometries.length; i++) {
                                var obj = {
                                    id: data.objects['2_1_1'].geometries[i].properties.area_id,
                                    label: data.objects['2_1_1'].geometries[i].properties.name,
                                    band: data.objects['2_1_1'].geometries[i].properties.band
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
                        },
                        //used for table multiple select
                        matchRowNumber: function (visible, id) {
                            for (var i = 0; i < visible.length; i++) {
                                if (visible[i].entity.id === id) {
                                    return(i);
                                }
                            }
                        }
                    };
                });