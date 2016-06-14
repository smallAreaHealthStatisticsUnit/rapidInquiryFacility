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
                        {field: 'id', enableHiding: false},
                        {field: 'name', enableHiding: false},
                        {field: 'band', enableHiding: false}
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
                            for (var i = 0; i < data.objects.lad.geometries.length; i++) {
                                var obj = {
                                    id: data.objects.lad.geometries[i].properties.LAD13CD,
                                    name: data.objects.lad.geometries[i].properties.LAD13NM,
                                    band: 0
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
                        //set colour of selected map polygons
                        getColor: function (index, band) {
                            if (index === -1) {
                                return '#F5F5F5'; //whitesmoke
                            } else {
                                //colorbrewer 9-class set1
                                var cb = ['#e41a1c','#377eb8','#4daf4a','#984ea3','#ff7f00','#ffff33','#a65628','#f781bf','#999999'];
                                return cb[band - 1];
                            }
                        },
                        //used for table multiple select
                        matchRowNumber: function (visible, name) {
                            for (var i = 0; i < visible.length; i++) {
                                if (visible[i].entity.name === name) {
                                    return(i);
                                }
                            }
                        }
                    };
                });