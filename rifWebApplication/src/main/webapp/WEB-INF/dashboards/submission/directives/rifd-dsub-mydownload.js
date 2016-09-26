/* 
 * DIRECTIVE to download JSON to TXT file
 * https://angularchat.co/blog/angularjs-client-side-downloads
 */
angular.module("RIF")
        .directive('myDownload', function ($compile) {
            return {
                restrict: 'E',
                scope: {getUrlData: '&getData'},
                link: function (scope, elm, attrs) {
                    var url = URL.createObjectURL(scope.getUrlData());
                    elm.append($compile(
                            '<a class="myButton" id="exportParameters" download="RIFstudy.json"' +
                            'href="' + url + '">' + 'EXPORT FILE' + '</a>'
                            )(scope));
                }
            };
        });