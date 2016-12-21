/* 
 * CONTROLLER for disease submission retrieve study 
 */
/* global URL */

angular.module("RIF")
        .controller('ModalRetrieveCtrl', ['$scope', 'ModelService',
            function ($scope, ModelService) {

                //get the study object
                $scope.getBlobJob = function () {
                    var data = ModelService.get_rif_job_submission_JSON();
                    var json = JSON.stringify(data);
                    var theBlob = new Blob([json], {type: "application/json"});
                    saveAs(theBlob, "RIFstudy.json");
                };
            }]);