/*
 * 
 */
angular.module("RIF")
        .controller('SumbmissionCtrl', ['$scope', 'user', '$state',
            'SubmissionStateService', 'StudyAreaStateService', 'CompAreaStateService', 'ParameterStateService', 'StatsStateService',
            function ($scope, user, $state,
                    SubmissionStateService, StudyAreaStateService, CompAreaStateService, ParameterStateService, StatsStateService) {

                //Initialise the Taxonomy service
                user.initialiseService().then(handleInitialise, handleInitialise);
                function handleInitialise(res) {
                    //console.log("taxonomy initialised: " + res.data);
                }

                //called from reset button.
                $scope.resetToDefaults = function () {

                    //close the reset modal
                    $scope.submit();

                    //Reset services
                    SubmissionStateService.resetState();
                    StudyAreaStateService.resetState();
                    CompAreaStateService.resetState();
                    ParameterStateService.resetState();
                    StatsStateService.resetState();

                    //Reload submission (state1)
                    $state.go('state1').then(function () {
                        $state.reload();
                    });
                };
       
                //Study name
                $scope.studyName = SubmissionStateService.getState().studyName;
                $scope.studyNameChanged = function () {
                    SubmissionStateService.getState().studyName = $scope.studyName;
                };

                //TODO: do these calls only on start-up then save in a service

                function handleError(e) {
                    console.log(e);
                }

                //Get geographies and health themes
                $scope.geographies = [];
                user.getGeographies(user.currentUser).then(handleGeographies, handleGeographies);

                function handleGeographies(res) {
                    $scope.geographies.length = 0;
                    for (var i = 0; i < res.data[0].names.length; i++) {
                        $scope.geographies.push(res.data[0].names[i]);
                    }
                    //TODO: default hardtyped
                    if ($scope.geographies.indexOf("SAHSU") !== -1) {
                        $scope.geography = "SAHSU";
                    } else {
                        $scope.geography = $scope.geographies[0];
                    }
                    SubmissionStateService.getState().geography = $scope.geography;
                    //Fill health themes drop-down
                    $scope.healthThemes = [];
                    user.getHealthThemes(user.currentUser, $scope.geography).then(handleHealthThemes, handleHealthThemes);
                }
                $scope.geographyChange = function () {
                    SubmissionStateService.getState().geography = $scope.geography;

                    //TODO: reset trees and area states

                };

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
                        user.getFractions(user.currentUser, $scope.geography, $scope.healthTheme.description).then(handleFractions, handleFractions);
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
                };
            }]);
