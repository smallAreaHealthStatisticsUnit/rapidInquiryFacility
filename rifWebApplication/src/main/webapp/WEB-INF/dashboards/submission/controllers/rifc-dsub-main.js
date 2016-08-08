/*
 * 
 */
angular.module("RIF")
        .controller('SumbmissionCtrl', ['$scope', 'SubmissionStateService', 'user',
            function ($scope, SubmissionStateService, user) {

                //Study name
                $scope.studyName = SubmissionStateService.getState().studyName;
                $scope.studyNameChanged = function () {
                    SubmissionStateService.getState().studyName = $scope.studyName;
                };

                //TODO: do these calls only on start-up then save in a service

                //Initialise the Taxonomy service
                user.initialiseService().then(handleInitialise, handleInitialise);
                function handleInitialise(res) {
    //                console.log("taxonomy initialised: " + res.data);
                }

                //Get geographies
                $scope.geographies = [];
                var geogs = {"names": ["EW01", "SAHSU", "UK91"]};
                $scope.geographies = geogs.names;
                $scope.geography = $scope.geographies[1];
                SubmissionStateService.getState().geography = $scope.geography;

                //Fill health themes drop-down
                $scope.healthThemes = [];
                $scope.fillHealthThemes = function () {
                    user.getHealthThemes(user.currentUser, $scope.geography).then(handleHealthThemes, handleHealthThemes);
                }();
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
