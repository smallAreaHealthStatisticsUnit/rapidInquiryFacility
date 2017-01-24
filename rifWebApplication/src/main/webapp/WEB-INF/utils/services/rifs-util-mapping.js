/*
 * SERVICE for mapping helper functions
 */

angular.module("RIF")
        .factory('MappingService',
                function (ChoroService) {
                    //Convert sex description to code
                    function sexCode(s) {
                        return ["Males", "Females", "Both"].indexOf(s) + 1;
                    }
                    //get the other of the two disease maps
                    function otherMap(id) {
                        if (id === "diseasemap2") {
                            return "diseasemap1";
                        } else {
                            return "diseasemap2";
                        }
                    }                 
                    return {
                        getSexCode: function (s) {
                            return sexCode(s);
                        },
                        getOtherMap: function (id) {
                            return otherMap(id);
                        }
                    };
                });
