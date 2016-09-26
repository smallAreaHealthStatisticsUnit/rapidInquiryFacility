/* 
 * SERVICE to store state of stats modal
 */
angular.module("RIF")
        .factory('StatsStateService',
                function () {
                    var s = {
                        checked: -2,
                        model: [],
                        methods: []
                    };
                    var defaults = angular.copy(JSON.parse(JSON.stringify(s)));
                    return {
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = angular.copy(defaults);
                        },
                        getModelStats: function () {
                            if (s.checked < 0) {
                                return {};
                            }
                            var method = {
                                "name": "",
                                "code_routine_name": "",
                                "description": "",
                                "parameters": ""
                            };                           
                            method.name = s.methods[s.checked].codeRoutineName;
                            method.code_routine_name = s.methods[s.checked].codeRoutineName;
                            method.description = s.methods[s.checked].description;                          
                            var tmp = [];
                            for (var i = 0; i < s.model[s.checked].length; i++) {
                                tmp.push({"name": s.methods[s.checked].parameterProxies[i].name, "value": s.model[s.checked][i]});
                            }                           
                            method.parameters = {"parameter": tmp};                        
                            return method;
                        }
                    };
                });
               