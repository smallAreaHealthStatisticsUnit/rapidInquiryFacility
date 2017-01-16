
//DEPRECIATED


angular.module("RIF")
        .factory('MappingService', [
            function () {
                //Convert sex description to code
                function sexCode(s) {
                    return ["Males", "Females", "Both"].indexOf(s) + 1;
                }
                
                
                
                return {
                    getSexCode: function (s) {
                        return sexCode(s);
                    }
                };
            }]);

