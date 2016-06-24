/*
 * 
 */
angular.module("RIF")
        .controller('SumbmissionCtrl', ['$scope', 'SubmissionStateService',
            function ($scope, SubmissionStateService) {
                $scope.studyName = SubmissionStateService.get_state().studyName;
                $scope.healthTheme = SubmissionStateService.get_state().healthTheme;
                $scope.numerator = SubmissionStateService.get_state().numerator;
                $scope.denominator = SubmissionStateService.get_state().denominator;
            }]);
