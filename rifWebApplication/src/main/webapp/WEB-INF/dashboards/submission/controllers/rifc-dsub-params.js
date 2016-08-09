/* CONTROLLER for disease submission invetigation parameters modal
 * 
 * TODO: Study copy button
 * TODO: Top level terms
 */
angular.module("RIF")
        .controller('ModalParametersCtrl', ['$scope', '$uibModal', 'SubmissionStateService', 'ParameterStateService', 'user',
            function ($scope, $uibModal, SubmissionStateService, ParameterStateService, user) {
                $scope.tree = SubmissionStateService.getState().investigationTree;
                $scope.animationsEnabled = true;

                $scope.fillContents = function () {
                    //TODO: check geography and level selected
                    var thisGeography = SubmissionStateService.getState().geography;
                    var thisNumerator = SubmissionStateService.getState().numerator.numeratorTableName;
                    var thisGeoLevel = "LEVEL4";

                    $scope.title = "Investigation";


                    //Fill drop-downs
                    $scope.years = [];
                    $scope.intervals = [];
                    $scope.sexes = [];
                    $scope.covariates = [];
                    $scope.selectedAges = [];
                    $scope.ages = [];
                    $scope.taxonomyServices = [];
                    //          $scope.taxonomyScheme = $scope.taxonomyServices[0];

                    //taxonomy services
                    user.getTaxonomyServiceProviders().then(handleTaxonomyServiceProviders, handleParameterError);
                    //start and end years
                    user.getYears(user.currentUser, thisGeography, thisNumerator).then(handleYears, handleParameterError);
                    //sex
                    user.getSexes(user.currentUser).then(handleSexes, handleParameterError);
                    //covariates
                    user.getCovariates(user.currentUser, thisGeography, thisGeoLevel).then(handleCovariates, handleParameterError);
                    //age ranges
                    user.getAgeGroups(user.currentUser, thisGeography, thisNumerator).then(handleAgeGroups, handleParameterError);

                    //if the user has changed the Health theme, reset form
                    var thisTheme = SubmissionStateService.getState().healthTheme.name;
                    var activeTheme = ParameterStateService.getActiveHealthTheme();
                    ParameterStateService.setActiveHealthTheme(thisTheme);
                    if (thisTheme !== activeTheme) {
                        $scope.fullICDselection.length = 0;
                    }
                };

                //Callback error handler
                function handleParameterError(res) {
                    console.log("error");
                }

                //handle taxonomy services
                function handleTaxonomyServiceProviders(res) {
                    $scope.taxonomyServices.length = 0;
                    for (var i = 0; i < res.data.length; i++) {
                        $scope.taxonomyServices.push(res.data[i].identifier);
                    }
                    $scope.taxonomyScheme = $scope.taxonomyServices[0];
                }

                //handle years
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

                //handle sex
                function handleSexes(res) {
                    $scope.sexes.length = 0;
                    for (var i = 0; i < res.data[0].names.length; i++) {
                        $scope.sexes.push(res.data[0].names[i]);
                    }
                    $scope.sex = res.data[0].names[0];
                }

                //handle covariates
                function handleCovariates(res) {
                    $scope.covariates.length = 0;
                    var tmp = [];
                    for (var i = 0; i < res.data.length; i++) {
                        $scope.covariates.push({variable: res.data[i].name});
                        tmp.push({name: res.data[i].name, minimum_value: res.data[i].minimumValue,
                            maximum_value: res.data[i].maximumValue, covariate_type: res.data[i].covariateType});
                    }
                    ParameterStateService.setPossibleCovariates(tmp); //TODO: move this to 1st tab open to avoid repeated calls
                }

                //handle ages
                function handleAgeGroups(res) {
                    $scope.ages.length = 0;
                    $scope.agesSelected = 0;
                    var tmp = [];
                    for (var i = 0; i < res.data[0].name.length; i++) {
                        $scope.ages.push({age: res.data[1].lowerAgeLimit[i] + " - " + res.data[2].upperAgeLimit[i], description: res.data[0].name[i]});
                        tmp.push({id: i, name: res.data[0].name[i], lower_limit: res.data[1].lowerAgeLimit[i], upper_limit: res.data[2].upperAgeLimit[i]});
                    }
                    ParameterStateService.setPossibleAges(tmp);
                }
                function ageRowChanged(row) {
                    var indx = $scope.ages.indexOf(row.entity);
                    if ($scope.selectedAges.indexOf(indx) === -1) {
                        $scope.selectedAges.push(indx);
                    } else {
                        $scope.selectedAges.splice($scope.selectedAges.indexOf(indx), 1);
                    }
                    //make contiguous
                    var min = Math.min.apply(null, $scope.selectedAges);
                    var max = Math.max.apply(null, $scope.selectedAges);
                    for (var i = min; i <= max; i++) {
                        $scope.gridApi1.selection.selectRow($scope.ages[i]);
                    }
                    $scope.parametersChanged();
                }
                $scope.clearAgeSelection = function () {
                    $scope.gridApi1.selection.clearSelectedRows();
                    $scope.selectedAges.length = 0;
                };


                /*
                 * TABLE SET UP (1)
                 * age category table 
                 */

                $scope.gridOptionsAge = {
                    enableHorizontalScrollbar: 0,
                    enableRowHeaderSelection: false,
                    enableColumnResizing: true,
                    enableSorting: false,
                    enableFiltering: false,
                    rowHeight: 20,
                    columnDefs: [
                        {name: 'age', enableHiding: false, width: "50%"},
                        {name: 'description', enableHiding: false}
                    ],
                    data: $scope.ages,
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi1 = gridApi;
                        $scope.gridApi1.selection.on.rowSelectionChanged($scope, ageRowChanged);
                    }
                };

                /*
                 * TABLE SET UP (2)
                 * ICD code list
                 */
                $scope.gridOptionsICD = {
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    rowTemplate: rowTemplate(),
                    columnDefs: [
                        {name: 'term_name', enableHiding: false, width: 100},
                        {name: 'term_description', enableHiding: false}
                    ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi2 = gridApi;
                    }
                };
                function rowTemplate() {
                    return  '<div id="testdiv">' +
                            '<div style="height: 100%" ng-class="{ ' +
                            'ICDNotSelected: row.entity.selected===0,' +
                            'ICDSelected: row.entity.selected===1' +
                            '}">' +
                            '<div ng-click="grid.appScope.rowClickICD(row)">' +
                            '<div ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div>' +
                            '</div>';
                }

                $scope.rowClickICD = function (row) {
                    var idToCheck = [];
                    for (var i = 0; i < $scope.thisICDselection.length; i++) {
                        idToCheck.push($scope.thisICDselection[i][0]);
                    }
                    var thisIndex = idToCheck.indexOf(row.entity.identifier);
                    if (thisIndex === -1) {
                        $scope.thisICDselection.push([row.entity.identifier, row.entity.term_description]);
                    } else {
                        $scope.thisICDselection.splice(thisIndex, 1);
                    }
                };

                /*
                 * TABLE SET UP (3)
                 * table setups recap table
                 */

                //recap table <-> ICD tree link
                $scope.investigationCount = 1;
                var rowCollectionStore = [];
                $scope.fullICDselection = ParameterStateService.getState().rows;
                $scope.thisICDselection = [];

                $scope.gridOptionsRecap = {
                    enableColumnMenus: false,
                    enableHorizontalScrollbar: 0,
                    enableColumnResizing: true,
                    enableSorting: false,
                    enableFiltering: false,
                    rowTemplate: rowTemplateRecap(),
                    columnDefs: [
                        {name: 'title', enableHiding: false, enableCellEditOnFocus: true, width: "10%"},
                        {name: 'identifier', enableHiding: false, width: "7%"},
                        {name: 'health_outcomes', enableHiding: false, width: "26%"},
                        {name: 'age_groups', enableHiding: false, width: "15%"},
                        {name: 'years', enableHiding: false, width: "15%"},
                        {name: 'sex', enableHiding: false, width: "7%"},
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
                        $scope.gridApi3 = gridApi;
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

                //Restore past state in recap table
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
                    data: $scope.covariates,
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi4 = gridApi;
                        $scope.gridApi4.selection.on.rowSelectionChanged($scope, $scope.parametersChanged);
                    }
                };

                /*
                 * TABLE SET-UP FINISHED
                 */


                /*
                 * TAXONOMY SERVICE
                 */
                $scope.taxonomySchemeChanged = function () {
                    console.log("scheme changed");
                };

                $scope.searchCode = "";
                $scope.searchText = "";
                $scope.hitsCount = "";

                //search on return key from text box
                $scope.textKeyPress = function (event) {
                    if (event.charCode === 13) {
                        $scope.searchDescription();
                    }
                };
                $scope.codeKeyPress = function (event) {
                    if (event.charCode === 13) {
                        $scope.searchCode();
                    }
                };

                //Get search terms
                $scope.searchDescription = function () {
                    user.getMatchingTerms($scope.taxonomyScheme.toLowerCase(), $scope.searchText).then(handleTextSearch, handleTextSearch);
                };
                $scope.searchLabels = function () {
                    //Not yet implemented
                    //user.getCodeSearch('icd10', $scope.searchCode).then(handleTextSearch, handleTextSearch);
                };
                function handleTextSearch(res) {
                    var myICD = [];
                    if (res.data.length !== 0) {
                        for (var i = 0; i < res.data.length; i++) {
                            myICD.push({term_name: res.data[i].label, term_description: res.data[i].description, identifier: res.data[i].identifier, selected: 0});
                        }
                    } else {
                        $scope.showWarning("no terms found (not needed here proof of concept $scope)");
                    }
                    $scope.hitsCount = res.data.length + " terms returned";
                    $scope.gridOptionsICD.data = myICD;
                }
                $scope.resetCodeSearch = function () {
                    $scope.searchCode = "";
                    $scope.hitsCount = "";
                };
                $scope.resetTextSearch = function () {
                    $scope.searchText = "";
                    $scope.hitsCount = "";
                };

                /*
                 * TABLE SYNCHRONISATION
                 */
                $scope.$watchCollection('thisICDselection', function (newNames, oldNames) {
                    if (newNames === oldNames) {
                        return;
                    }
                    var idToCheck = [];
                    for (var i = 0; i < newNames.length; i++) {
                        idToCheck.push(newNames[i][0]);
                    }
                    for (var i = 0; i < $scope.gridOptionsICD.data.length; i++) {
                        if (idToCheck.indexOf($scope.gridOptionsICD.data[i].identifier) !== -1) {
                            $scope.gridOptionsICD.data[i].selected = 1;
                        } else {
                            $scope.gridOptionsICD.data[i].selected = 0;
                        }
                    }
                    fillRecapTable($scope.thisICDselection);
                });

                //On changing years or sex etc
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
                            identifier: ICD[i][0],
                            health_outcomes: ICD[i][1],
                            age_groups: getAgeGroups(i),
                            years: getYears(i),
                            sex: getSex(i),
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
                function getTitle(i) {
                    if (i === 0) {
                        return $scope.title;
                    }
                }
                function getAgeGroups(i) {
                    if (i === 0) {
                        //get min and max of selected range
                        if ($scope.selectedAges.length !== 0) {
                            var min = Math.min.apply(null, $scope.selectedAges);
                            var max = Math.max.apply(null, $scope.selectedAges);
                            var tmp = "LWR: " + ParameterStateService.getPossibleAges()[min].name + ", UPR: "
                                    + ParameterStateService.getPossibleAges()[max].name;
                            return tmp;
                        }
                    }
                }
                function getYears(i) {
                    if (i === 0) {
                        return $scope.startYear + " - " + $scope.endYear + " [" + $scope.yearInterval + "]";
                    }
                }
                function getSex(i) {
                    if (i === 0) {
                        return $scope.sex;
                    }
                }
                function getCovariates(i) {
                    if (i === 0) {
                        var covariatesSelected = $scope.gridApi4.selection.getSelectedRows();
                        var tmp = "";
                        for (var j = 0; j < covariatesSelected.length; j++) {
                            tmp += covariatesSelected[j].variable;
                            if (j !== covariatesSelected.length - 1) {
                                tmp += "; ";
                            }
                        }
                        return tmp;
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
                    if (rowCollectionStore.length === 0) {
                        return;
                    }
                    //check age is filled                    
                    if (!rowCollectionStore[0].age_groups) {
                        $scope.showWarning("Age groups not defined");
                        return;
                    }
                    //save rows of this investigation
                    for (var row in rowCollectionStore) {
                        $scope.fullICDselection.push(rowCollectionStore[row]);
                    }
                    //reset selected rows in tree
                    resetTable();
                    $scope.investigationCount++;
                };

                //Clear all from recap table
                $scope.clearInvestigations = function () {
                    $scope.fullICDselection.length = 0;
                    resetTable();
                    $scope.investigationCount = 1;
                    fillRecapTable($scope.thisICDselection);
                };

                resetTable = function () {
                    $scope.thisICDselection.length = 0;
                    $scope.selectedAges.length = 0;
                    $scope.gridApi1.selection.clearSelectedRows();
                    $scope.gridApi4.selection.clearSelectedRows();
                };

                $scope.open = function () {
                    var modalInstance = $uibModal.open({
                        animation: false, //$scope.animationsEnabled,
                        templateUrl: 'dashboards/submission/partials/rifp-dsub-params.html',
                        controller: 'ModalParametersInstanceCtrl',
                        windowClass: 'modal-fit',
                        backdrop: 'static',
                        scope: $scope, //reference parent scope in modal
                        keyboard: false
                    });
                    modalInstance.opened.then(function () {
                        //fill all user controls
                        $scope.fillContents();
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
            };
        });