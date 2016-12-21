/* 
 * DIRECTIVE to handle single and double clicks in the same element
 * http://stackoverflow.com/questions/20444409/handling-ng-click-and-ng-dblclick-on-the-same-element-with-angularjs
 */

angular.module("RIF")
        .directive('sglclick', ['$parse', function ($parse) {
                return {
                    restrict: 'A',
                    link: function (scope, element, attr) {
                        var fn = $parse(attr['sglclick']);
                        var delay = 300, clicks = 0, timer = null;
                        element.on('click', function (event) {
                            clicks++;  //count clicks
                            if (clicks === 1) {
                                timer = setTimeout(function () {
                                    scope.$apply(function () {
                                        fn(scope, {$event: event});
                                    });
                                    clicks = 0;             //after action performed, reset counter
                                }, delay);
                            } else {
                                clearTimeout(timer);    //prevent single-click action
                                clicks = 0;             //after action performed, reset counter
                            }
                        });
                    }
                };
            }]);
