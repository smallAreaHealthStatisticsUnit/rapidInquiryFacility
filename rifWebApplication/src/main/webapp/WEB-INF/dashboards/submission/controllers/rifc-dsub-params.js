/* CONTROLLER for disease submission invetigation parameters modal
 * 
 */
angular.module("RIF")
        .controller('ModalParametersCtrl', ['$scope', '$uibModal', '$http', 'SubmissionStateService',
            function ($scope, $uibModal, $http, SubmissionStateService) {

                $scope.tree = SubmissionStateService.get_state().investigationTree;
                $scope.animationsEnabled = false;

                //input params
                $scope.startYear = "StartYear";
                $scope.endYear = "EndYear";
                $scope.gender = "Gender";

                //recap table <> ICD tree link
                var providerNameSpace = "";
                var investigationCount = 1;
                var rowCollectionStore = [];
                $scope.fullICDselection = [];
                $scope.thisICDselection = [];

                //table setups ICD treeview
                $scope.gridOptionsICD = {
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    enableSorting: true,
                    enableFiltering: true,
                    showTreeExpandNoChildren: false,
                    rowTemplate: rowTemplate(),
                    columnDefs: [
                        {name: 'term_name', enableHiding: false, width: 100},
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
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                }
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

                //table setups recap table
                $scope.gridOptionsRecap = {
                    enableColumnMenus: false,
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: false,
                    enableSorting: false,
                    enableFiltering: false,
                    rowTemplate: rowTemplateRecap(),
                    columnDefs: [
                        {name: 'taxonomy', enableHiding: false, width: "10%"},
                        {name: 'health_outcomes', enableHiding: false, width: "30%"},
                        {name: 'age_groups', enableHiding: false, width: "15%"},
                        {name: 'years', enableHiding: false, width: "15%"},
                        {name: 'gender', enableHiding: false, width: "10%"},
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
                            '}">' +
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                }
                //click event for the bin icon in the recap table
                $scope.cellClickRecapRemove = function (row) {
                    if (row.entity.remove === 1) {
                        if (confirm("Remove this investigation\n\nAre you sure?")) { //TODO: use custom dialogue
                            //remove current selections
                            if (row.entity.i === investigationCount) {
                                $scope.thisICDselection = [];
                            } else {
                                //remove comitted investigation i
                                $scope.fullICDselection = $scope.fullICDselection.filter(function (item) {
                                    return (item.i !== row.entity.i);
                                });
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
                                    $scope.fullICDselection[j].n = get_oddOrEven($scope.fullICDselection[j].i);
                                }
                                investigationCount--;
                            }
                            fillRecapTable($scope.thisICDselection);
                        }
                    }
                };

                //table setups age category table
                $scope.gridOptionsAge = {
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    enableSorting: false,
                    enableFiltering: false,
                    columnDefs: [
                        {name: 'age', enableHiding: false},
                        {name: 'description', enableHiding: false}
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                    }
                };

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
                //On changing years or gender
                $scope.parametersChanged = function () {
                    fillRecapTable($scope.thisICDselection);
                };
                function fillRecapTable(ICD) {
                    var rowCollection = [];
                    rowCollectionStore = [];
                    //render existing
                    for (var i in $scope.fullICDselection) {
                        rowCollection.push($scope.fullICDselection[i]);
                    }

                    for (var i = 0; i < ICD.length; i++) {
                        var obj = {
                            taxonomy: providerNameSpace,
                            health_outcomes: get_ICDText(ICD[i]),
                            age_groups: get_ageGroups(i),
                            years: get_years(i),
                            gender: get_gender(i),
                            covariates: get_covariates(i),
                            remove: get_remove(i),
                            n: get_oddOrEven(investigationCount),
                            i: investigationCount
                        };
                        rowCollection.push(obj);
                        rowCollectionStore.push(obj);
                    }
                    $scope.gridOptionsRecap.data = rowCollection;
                }

                //format recap table rows
                function get_ICDText(ICD) {
                    for (var i = 0; i < $scope.gridOptionsICD.data.length; i++) {
                        if ($scope.gridOptionsICD.data[i].$$treeLevel === 2) {
                            if ($scope.gridOptionsICD.data[i].term_name === ICD) {
                                return ICD + " - " + $scope.gridOptionsICD.data[i].term_description;
                            }
                        }
                    }
                }
                function get_ageGroups(i) {
                    if (i === 0) {
                        return "AgeGroups";
                    }
                }
                function get_years(i) {
                    if (i === 0) {
                        return $scope.startYear + " - " + $scope.endYear;
                    }
                }
                function get_gender(i) {
                    if (i === 0) {
                        return $scope.gender;
                    }
                }
                function get_covariates(i) {
                    if (i === 0) {
                        return "covariates";
                    }
                }
                function get_remove(i) {
                    if (i === 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
                function get_oddOrEven(n) {
                    return n % 2 === 0 ? 1 : 2;
                }

                //Add investigation controls
                $scope.addInvestigation = function () {
                    //save rows of this investigation
                    for (var row in rowCollectionStore) {
                        $scope.fullICDselection.push(rowCollectionStore[row]);
                    }
                    //reset selected rows in tree
                    $scope.thisICDselection = [];
                    investigationCount++;
                };

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
                });
                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: false, //$scope.animationsEnabled,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-params.html',
                        controller: 'ModalParametersInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        keyboard: false
                    });
                    modalInstance.result.then(function () {
                        //Change tree icon colour
                        SubmissionStateService.get_state().investigationTree = true;
                        $scope.tree = true;
                    });
                };
            }])
        .controller('ModalParametersInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $uibModalInstance.close();
            }
            ;
        });