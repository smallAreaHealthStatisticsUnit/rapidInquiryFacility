/* 
 * CONTROLLER for disease submission save study 
 * http://bgrins.github.io/devtools-snippets/#console-save
 */
/* global URL */

angular.module("RIF")
        .controller('ModalSaveCtrl', ['$scope', 'ModelService',
            function ($scope, ModelService) {

                //get the study object
                $scope.getBlobJob = function () {
                    var data = ModelService.get_rif_job_submission_JSON();
                    var json = JSON.stringify(data);
                    var blob = new Blob([json], {type: 'text/json'});
                    var filename = "RIFstudy.json";
                    
                    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
                        window.navigator.msSaveOrOpenBlob(blob, filename);
                    } else {
                        var e = document.createEvent('MouseEvents'),
                                a = document.createElement('a');
                        a.download = filename;
                        a.href = window.URL.createObjectURL(blob);
                        a.dataset.downloadurl = ['text/json', a.download, a.href].join(':');
                        e.initEvent('click', true, false, window,
                                0, 0, 0, 0, 0, false, false, false, false, 0, null);
                        a.dispatchEvent(e);
                    }
                };
            }]);