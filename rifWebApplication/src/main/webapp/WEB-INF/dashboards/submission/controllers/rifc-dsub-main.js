/*
 * CONTROLLER for the main 'tree' page
 */

angular.module("RIF")
        .controller('SumbmissionCtrl', ['$scope', 'user', '$state', 'SubmissionStateService', 'StudyAreaStateService', 'CompAreaStateService', 'ParameterStateService',
            function ($scope, user, $state, SubmissionStateService, StudyAreaStateService, CompAreaStateService, ParameterStateService) {
                /*
                 * STUDY, GEOGRAPHY AND FRACTION DROP-DOWNS
                 * Calls to API returns a chain of promises
                 */

                $scope.geographies = [];

                //Get geographies
                user.getGeographies(user.currentUser).then(handleGeographies, handleError); //1ST PROMISE
                function handleGeographies(res) {
                    $scope.geographies.length = 0;
                    for (var i = 0; i < res.data[0].names.length; i++) {
                        $scope.geographies.push(res.data[0].names[i]);
                    }
                    var thisGeography = SubmissionStateService.getState().geography;
                    if ($scope.geographies.indexOf(thisGeography) !== -1) {
                        $scope.geography = thisGeography;
                    } else {
                        $scope.geography = $scope.geographies[0];
                    }
                    SubmissionStateService.getState().geography = $scope.geography;
                    //Fill health themes drop-down
                    $scope.healthThemes = [];
                    user.getHealthThemes(user.currentUser, $scope.geography).then(handleHealthThemes, handleError); //2ND PROMISE
                }
                $scope.geographyChange = function () {
                    SubmissionStateService.getState().geography = $scope.geography;
                    //reset states using geography
                    StudyAreaStateService.resetState();
                    CompAreaStateService.resetState();
                    ParameterStateService.resetState();
                    SubmissionStateService.getState().comparisonTree = false;
                    SubmissionStateService.getState().studyTree = false;
                    SubmissionStateService.getState().investigationTree = false;
                    $scope.resetState();
                };

                //Get health themes
                function handleHealthThemes(res) {
                    $scope.healthThemes.length = 0;
                    for (var i = 0; i < res.data.length; i++) {
                        $scope.healthThemes.push({name: res.data[i].name, description: res.data[i].description});
                    }
                    $scope.healthTheme = $scope.healthThemes[0];
                    SubmissionStateService.getState().healthTheme = $scope.healthTheme;
                    $scope.healthThemeChange();
                }

                //Get relevant numerators and associated denominators
                $scope.fractions = [];
                $scope.healthThemeChange = function () {
                    if ($scope.healthTheme) {
                        SubmissionStateService.getState().healthTheme = $scope.healthTheme;
                        user.getFractions(user.currentUser, $scope.geography, $scope.healthTheme.description).then(handleFractions, handleError); //3RD PROMISE
                    } else {
                        $scope.fractions.length = 0;
                    }
                };
                function handleFractions(res) {
                    $scope.fractions.length = 0;
                    for (var i = 0; i < res.data.length; i++) {
                        $scope.fractions.push(res.data[i]);
                    }
                    $scope.numerator = $scope.fractions[0];
                    $scope.denominator = $scope.fractions[0].denominatorTableName;
                    SubmissionStateService.getState().numerator = $scope.numerator;
                    SubmissionStateService.getState().denominator = $scope.fractions[0];
                }

                //sync the denominator
                $scope.numeratorChange = function () {
                    if ($scope.numerator) {
                        $scope.denominator = $scope.numerator.denominatorTableName;
                    } else {
                        $scope.denominator = "";
                    }
                    SubmissionStateService.getState().numerator = $scope.numerator;
                    SubmissionStateService.getState().denominator = $scope.fractions[0];

                    //This will have an impact on investigations year range, so reset investigation parameters
                    ParameterStateService.resetState();
                };

                function handleError(e) {
                    $scope.showError("Could not retreive your project information from the database");
                }

                /*
                 * STUDY NAME
                 */
                $scope.studyName = SubmissionStateService.getState().studyName;
                $scope.studyNameChanged = function () {
                    SubmissionStateService.getState().studyName = $scope.studyName;
                };

                /*
                 * RESET
                 */
                $scope.resetState = function () {
                    $state.go('state1').then(function () {
                        $state.reload();
                    });
                };
            }]);