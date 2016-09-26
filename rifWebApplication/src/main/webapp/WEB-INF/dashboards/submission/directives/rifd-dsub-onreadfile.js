/* 
 * DIRECTIVE to open a text file
 */
angular.module("RIF")
        .directive('onReadFile', function ($parse) {
            //http://jsfiddle.net/alexsuch/6aG4x/535/
            return {
                restrict: 'A',
                scope: false,
                link: function (scope, element, attrs) {
                    var fn = $parse(attrs.onReadFile);
                    element.on('change', function (onChangeEvent) {
                        var reader = new FileReader();

                        reader.onload = function (onLoadEvent) {
                            scope.$apply(function () {
                                fn(scope, {$fileContent: onLoadEvent.target.result});
                            });
                        };
                        reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
                    });
                }
            };
        });


