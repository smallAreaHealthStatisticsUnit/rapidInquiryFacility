/* CONTROLLER for disease submission invetigation parameters modal
 * 
 * TODO: Study copy button
 * TODO: Top level terms
 */
angular.module("RIF")
        .controller('ModalParametersCtrl', ['$scope', '$uibModal', '$http', 'SubmissionStateService', 'ParameterStateService', 'user',
            function ($scope, $uibModal, $http, SubmissionStateService, ParameterStateService, user) {

                $scope.tree = SubmissionStateService.getState().investigationTree;
                $scope.animationsEnabled = true;

                //input params
                $scope.title = "Investigation";
                $scope.gender = "Both";

                //Fill years drop-downs
                $scope.years = [];
                $scope.intervals = [];

                $scope.fillYears = function () {
                    user.getYears(user.currentUser, 'SAHSU', 'SAHSULAND_CANCER').then(handleYears, handleYearsError); //TODO: is hard typed
                }();
                function handleYears(res) {
                    //TODO: check numeric and direction
                    $scope.years.length = 0;
                    for (var i = res.data[0].lowerBound; i <= res.data[0].upperBound; i++) {
                        $scope.years.push(Number(i));
                    }
                    $scope.startYear = Number(res.data[0].lowerBound);
                    $scope.endYear = Number(res.data[0].upperBound);
                    $scope.yearsChanged();
                }
                function handleYearsError(res) {
                    console.log("years error");
                }

                $scope.yearsChanged = function () {
                    $scope.intervals.length = 0;
                    //ensure that start year is before end year
                    if ($scope.endYear < $scope.startYear) {
                        $scope.endYear = [$scope.startYear, $scope.startYear = $scope.endYear][0];
                    }
                    //calculate possible intervals
                    var diff = $scope.endYear - $scope.startYear + 1;
                    for (var i = 1; i <= diff; i++) {
                        if (diff % i === 0) {
                            $scope.intervals.push(i);
                        }
                    }
                    $scope.yearInterval = 1;
                    $scope.parametersChanged();
                };

                //recap table <-> ICD tree link
                var providerNameSpace = "";
                $scope.investigationCount = 1;
                var rowCollectionStore = [];
                $scope.fullICDselection = ParameterStateService.getState().rows;
                $scope.thisICDselection = [];
                
                 //if the user has changed the Health theme, reset form
                var thisTheme = SubmissionStateService.getState().healthTheme.name;
                var activeTheme = ParameterStateService.getActiveHealthTheme();
                ParameterStateService.setActiveHealthTheme(thisTheme);
                if (thisTheme !== activeTheme) {
                    $scope.fullICDselection.length = 0;
                } 

                /*
                 * TABLE SET UP (1)
                 * age category table 
                 */
                $scope.gridOptionsAge = {
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    enableSorting: false,
                    enableFiltering: false,
                    columnDefs: [
                        {name: 'age', enableHiding: false, width: "50%"},
                        {name: 'description', enableHiding: false}
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };

                /*
                 * TABLE SET UP (2)
                 * ICD treeview
                 */
                $scope.gridOptionsICD = {
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    enableSorting: true,
                    enableFiltering: true,
                    showTreeExpandNoChildren: false,
                    rowTemplate: rowTemplate(),
                    columnDefs: [
                        {name: 'term_name', enableHiding: false, width: "20%"},
                        {name: 'term_description', enableHiding: false}
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };
                function rowTemplate() {
                    return  '<div id="testdiv">' +
                            '<div style="height: 100%" ng-class="{ ' +
                            'tree0: row.entity.$$treeLevel===0,' +
                            'tree1: row.entity.$$treeLevel===1,' +
                            'tree2: row.entity.$$treeLevel===2 && row.entity.selected===0,' +
                            'tree2selected: row.entity.$$treeLevel===2 && row.entity.selected===1' +
                            '}">' +
                            '<div ng-click="grid.appScope.rowClickICD(row)">' +
                            '<div ng-dblclick="grid.appScope.rowDoubleClickICD(row)">' +
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                }
                //Tree level 2 click (e.g. J458 Mixed Asthma)
                $scope.rowClickICD = function (row) {
                    if (row.treeLevel === 2) {
                        var thisIndex = $scope.thisICDselection.indexOf(row.entity.term_name);
                        if (thisIndex === -1) {
                            $scope.thisICDselection.push(row.entity.term_name);
                        } else {
                            $scope.thisICDselection.splice(thisIndex, 1);
                        }
                    }
                };
                //Tree level 1 double-click (e.g. J45 Asthma)
                $scope.rowDoubleClickICD = function (row) {
                    if (row.treeLevel === 1) {
                        var selCount = 0;
                        for (var i = 0; i < row.treeNode.children.length; i++) {
                            if (row.treeNode.children[i].row.entity.selected === 1) {
                                selCount++;
                            }
                        }
                        for (var i = 0; i < row.treeNode.children.length; i++) {
                            var thisIndex = $scope.thisICDselection.indexOf(row.treeNode.children[i].row.entity.term_name);
                            if (selCount === row.treeNode.children.length) {
                                //if all selected, deselect all
                                $scope.thisICDselection.splice(thisIndex, 1);
                            } else {
                                //select all in branch not already selected
                                if (thisIndex === -1) {
                                    $scope.thisICDselection.push(row.treeNode.children[i].row.entity.term_name);
                                }
                            }
                        }
                    }
                };

                /*
                 * TABLE SET UP (3)
                 * table setups recap table
                 */
                $scope.gridOptionsRecap = {
                    enableColumnMenus: false,
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    enableSorting: false,
                    enableFiltering: false,
                    rowTemplate: rowTemplateRecap(),
                    columnDefs: [
                        {name: 'title', enableHiding: false, enableCellEditOnFocus: true, width: "10%"},
                        {name: 'taxonomy', enableHiding: false, width: "7%"},
                        {name: 'health_outcomes', enableHiding: false, width: "26%"},
                        {name: 'age_groups', enableHiding: false, width: "15%"},
                        {name: 'years', enableHiding: false, width: "15%"},
                        {name: 'gender', enableHiding: false, width: "7%"},
                        {name: 'covariates', enableHiding: false, width: "15%"},
                        {name: '\t', enableHiding: false,
                            cellTemplate:
                                    '<div class="removebtn" ng-click="grid.appScope.cellClickRecapRemove(row)">' +
                                    '<div style="height: 100%" ng-class="{ ' +
                                    'binOdd: row.entity.remove===1 && row.entity.n ===2,' +
                                    'binEven: row.entity.remove===1 && row.entity.n ===1,' +
                                    '}">' +
                                    '</div>'
                        }
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };
                function rowTemplateRecap() {
                    return  '<div id="testdiv">' +
                            '<div style="height: 100%" ng-class="{ ' +
                            'recapOdd: row.entity.n===1,' +
                            'recapEven: row.entity.n===2,' +
                            'recapToAdd: row.entity.i===grid.appScope.investigationCount,' +
                            '}">' +
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                }
                //click event for the bin icon in the recap table
                $scope.cellClickRecapRemove = function (row) {
                    if (row.entity.remove === 1) {
                        if (confirm("Remove this investigation\n\nAre you sure?")) { //TODO: use custom dialogue
                            //remove current selections
                            if (row.entity.i === $scope.investigationCount && $scope.thisICDselection.length !== 0) {
                                $scope.thisICDselection.length = 0;
                            } else {
                                //remove comitted investigation i
                                for (var i = $scope.fullICDselection.length - 1; i >= 0; i--) {
                                    if ($scope.fullICDselection[i].i === row.entity.i) {
                                        $scope.fullICDselection.splice(i, 1);
                                    }
                                }
                                //reorder investigation numbers for alternating row colours                        
                                var indx = [];
                                for (var j = 0; j < $scope.fullICDselection.length; j++) {
                                    indx.push($scope.fullICDselection[j].i);
                                }
                                indx = indx.reverse().filter(function (e, i, indx) {
                                    return indx.indexOf(e, i + 1) === -1;
                                }).reverse();
                                for (var j = 0; j < $scope.fullICDselection.length; j++) {
                                    $scope.fullICDselection[j].i = indx.indexOf($scope.fullICDselection[j].i) + 1;
                                    $scope.fullICDselection[j].n = getOddOrEven($scope.fullICDselection[j].i);
                                }
                                $scope.investigationCount--;
                            }
                            fillRecapTable($scope.thisICDselection);
                        }
                    }
                };
                /*
                 * TABLE SET UP (4)
                 * table setups covariate table
                 */
                $scope.gridOptionsCovariates = {
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    enableSorting: false,
                    enableFiltering: false,
                    enableRowSelection: true,
                    enableSelectAll: true,
                    columnDefs: [
                        {name: 'variable', enableHiding: false}
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };
                /*
                 * TABLE SET-UP FINISHED
                 */


                //read XML
                $http.get("test/ExampleICD10Codes.xml").success(function (xml, status) {
                    var x2js = new X2JS();
                    var data = x2js.xml_str2json(xml);
                    //Taxonomy code for recap table col#1
                    providerNameSpace = data.rif_health_code_provider.provider_information.provider_name_space;
                    data = data.rif_health_code_provider.terms.term;
                    data = "[" + JSON.stringify(data) + "]";
                    data = JSON.parse(data);
                    var myData = data[0];
                    var myICD = [];
                    //format JSON data for ui-grid treeview
                    for (i = 0; i < myData.length; i++) {
                        myICD.push({term_name: myData[i].term_name, term_description: myData[i].term_description, $$treeLevel: 0, selected: 0});
                        for (j = 0; j < myData[i].term.length; j++) {
                            myICD.push({term_name: myData[i].term[j].term_name, term_description: myData[i].term[j].term_description, $$treeLevel: 1, selected: 0});
                            for (k = 0; k < myData[i].term[j].term.length; k++) {
                                myICD.push({term_name: myData[i].term[j].term[k].term_name, term_description: myData[i].term[j].term[k].term_description, $$treeLevel: 2, selected: 0});
                            }
                        }
                    }
                    $scope.gridOptionsICD.data = myICD;

                    //fill recap table with any stored selection
                    var rowCollection = [];
                    rowCollectionStore.length = 0;
                    for (var i in $scope.fullICDselection) {
                        rowCollection.push($scope.fullICDselection[i]);
                    }
                    if (rowCollection.length === 0) {
                        $scope.investigationCount = 1;
                    } else {
                        $scope.investigationCount = rowCollection[rowCollection.length - 1].i + 1;
                    }
                    $scope.gridOptionsRecap.data = rowCollection;
                });

                /*
                 * Table synchoronistation
                 */
                $scope.$watchCollection('thisICDselection', function (newNames, oldNames) {
                    if (newNames === oldNames) {
                        return;
                    }
                    for (var i = 0; i < $scope.gridOptionsICD.data.length; i++) {
                        if ($scope.gridOptionsICD.data[i].$$treeLevel === 2) {
                            if (newNames.indexOf($scope.gridOptionsICD.data[i].term_name) !== -1) {
                                $scope.gridOptionsICD.data[i].selected = 1;
                            } else {
                                $scope.gridOptionsICD.data[i].selected = 0;
                            }
                        }
                    }
                    fillRecapTable($scope.thisICDselection);
                });
                //On changing years or gender etc
                $scope.parametersChanged = function () {
                    fillRecapTable($scope.thisICDselection);
                };
                function fillRecapTable(ICD) {
                    var rowCollection = [];
                    rowCollectionStore.length = 0;
                    //render existing
                    for (var i in $scope.fullICDselection) {
                        rowCollection.push($scope.fullICDselection[i]);
                    }
                    for (var i = 0; i < ICD.length; i++) {
                        var obj = {
                            title: getTitle(i),
                            taxonomy: providerNameSpace,
                            health_outcomes: getICDText(ICD[i]),
                            age_groups: getAgeGroups(i),
                            years: getYears(i),
                            gender: getGender(i),
                            covariates: getCovariates(i),
                            remove: getRemove(i),
                            n: getOddOrEven($scope.investigationCount),
                            i: $scope.investigationCount
                        };
                        rowCollection.push(obj);
                        rowCollectionStore.push(obj);
                    }
                    $scope.gridOptionsRecap.data = rowCollection;
                }

                //format recap table rows
                function getICDText(ICD) {
                    for (var i = 0; i < $scope.gridOptionsICD.data.length; i++) {
                        if ($scope.gridOptionsICD.data[i].$$treeLevel === 2) {
                            if ($scope.gridOptionsICD.data[i].term_name === ICD) {
                                return ICD + " - " + $scope.gridOptionsICD.data[i].term_description;
                            }
                        }
                    }
                }
                function getTitle(i) {
                    if (i === 0) {
                        return $scope.title;
                    }
                }
                function getAgeGroups(i) {
                    if (i === 0) {
                        return "AgeGroups";
                    }
                }
                function getYears(i) {
                    if (i === 0) {
                        return $scope.startYear + " - " + $scope.endYear + " [" + $scope.yearInterval + "]";
                    }
                }
                function getGender(i) {
                    if (i === 0) {
                        return $scope.gender;
                    }
                }
                function getCovariates(i) {
                    if (i === 0) {
                        return "covariates";
                    }
                }
                function getRemove(i) {
                    if (i === 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
                function getOddOrEven(n) {
                    return n % 2 === 0 ? 1 : 2;
                }

                //Add investigation controls
                $scope.addInvestigation = function () {
                    //save rows of this investigation
                    for (var row in rowCollectionStore) {
                        $scope.fullICDselection.push(rowCollectionStore[row]);
                    }
                    //reset selected rows in tree
                    $scope.thisICDselection.length = 0;
                    $scope.investigationCount++;
                };

                //Clear all from recap table
                $scope.clearInvestigations = function () {
                    $scope.fullICDselection.length = 0;
                    $scope.thisICDselection.length = 0;
                    $scope.investigationCount = 1;
                    fillRecapTable($scope.thisICDselection);
                };

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: false, //$scope.animationsEnabled,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-params.html',
                        controller: 'ModalParametersInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.opened.then(function () {

                    });
                    modalInstance.result.then(function () {
                        ParameterStateService.getState().rows = $scope.fullICDselection;                     
                        //Change tree icon colour
                        if ($scope.fullICDselection.length === 0) {
                            SubmissionStateService.getState().investigationTree = false;
                            $scope.tree = false;
                        } else {
                            SubmissionStateService.getState().investigationTree = true;
                            $scope.tree = true;
                        }
                    });
                };
            }
        ])
        .controller('ModalParametersInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            }
            ;
        });