/*
 * 
 */
angular.module("RIF")
        .controller('SumbmissionCtrl', ['$scope', 'SubmissionStateService',
            function ($scope, SubmissionStateService) {
                $scope.studyName = SubmissionStateService.getState().studyName;
                $scope.healthTheme = SubmissionStateService.getState().healthTheme;
                $scope.numerator = SubmissionStateService.getState().numerator;
                $scope.denominator = SubmissionStateService.getState().denominator;
            }]);
