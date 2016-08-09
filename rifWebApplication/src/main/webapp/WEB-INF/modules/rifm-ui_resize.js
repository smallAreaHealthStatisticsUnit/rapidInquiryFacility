//https://github.com/angular-ui/ui-layout/issues/34
/*
 * MODULE to handle ui-layout events
 * Used to fire container resize events
 */

angular.module('oz.components.ui-layout-events', [
    'ui.layout'
]).directive('uiLayout', function($timeout, $rootScope) {
        var methods = ['updateDisplay', 'toggleBefore', 'toggleAfter', 'mouseUpHandler', 'mouseMoveHandler'],
            timer;

        function fireEvent() {
            if(timer) $timeout.cancel(timer);
            timer = $timeout(function() {
                $rootScope.$broadcast('ui.layout.resize');
                timer = null;
            }, 50);
        }

        return {
            restrict: 'AE',
            require: '^uiLayout',
            link: function(scope, elem, attrs, uiLayoutCtrl) {
                angular.forEach(methods, function(method) {
                    var oldFn = uiLayoutCtrl[method];
                    uiLayoutCtrl[method] = function() {
                        oldFn.apply(uiLayoutCtrl, arguments);
                        fireEvent();
                    };
                });
            }
        };
    });
