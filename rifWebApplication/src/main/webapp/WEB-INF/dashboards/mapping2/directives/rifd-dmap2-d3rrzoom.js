/* 
 * 
 */

//http://bl.ocks.org/mbostock/34f08d5e11952a80609169b7917d4172

angular.module("RIF")
        .directive('rrZoom', function (MappingStateService) { //rr-zoom
            var directiveDefinitionObject = {
                restrict: 'E',
                replace: false,
                scope: {
                    data: '=chartData',
                    opt: '=options',
                    width: '=width',
                    height: '=height'
                },
                link: function (scope, element, attrs) {

                    scope.$watch(function () {
                        if (angular.isUndefined(scope.data)) {
                            return;
                        }
                        
                        scope.renderBase();
                    });

                    scope.renderBase = function () {

                        var margin = {top: 30, right: 20, bottom: 50, left: 40};
                        var xHeight = scope.height - margin.top - margin.bottom;
                        var xWidth = scope.width - margin.left - margin.right - 4;





                      
                    };
                }
            };
            return directiveDefinitionObject;
        });


