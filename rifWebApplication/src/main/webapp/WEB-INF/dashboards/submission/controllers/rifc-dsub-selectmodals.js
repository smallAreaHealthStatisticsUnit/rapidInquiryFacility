/*
 * CONRTROLLERS for map and table linked area selections open from file of shp boundary modals
 */

angular.module("RIF")      
        //Open a shapefile for risk analysis
        .controller('ModalAOIShapefileInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $scope.uploadShapeFile();
                $uibModalInstance.close();
            };
        })
        //Open a list of IDs
        .controller('ModalFileListInstanceCtrl', function ($scope, $uibModalInstance) {
            $scope.close = function () {
                $uibModalInstance.dismiss();
            };
            $scope.submit = function () {
                $scope.uploadFile();
                $uibModalInstance.close();
            };
        });