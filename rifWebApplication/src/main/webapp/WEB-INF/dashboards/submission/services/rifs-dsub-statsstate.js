/* SERVICE to store state of comparison area modal
 * will be used eventually to load studies
 */
angular.module("RIF")
        .factory('StatsStateService',
                function () {
                    var s = {
                        checked: "1",
                        bym_c: 10,
                        het_a: 5,
                        het_b: 10,
                        car_a: 5
                    };
                    var defaults = JSON.parse(JSON.stringify(s));
                    return {
                        getState: function () {
                            return s;
                        },
                        resetState: function () {
                            s = defaults;
                        },
                        getModelStats: function () {
                            var method = {
                                "name": "",
                                "code_routine_name": "",
                                "description": "",
                                "parameters": ""
                            };
                            if (s.checked === "1") {
                                method.name = "BYM";
                                method.code_routine_name = "bym_r_proceedure";
                                method.description = "Besag, York and Mollie Model";
                                method.parameters = {"parameter": [{"name": "c", "value": s.bym_c}]};
                            } else if (s.checked === "2") {
                                method.name = "HET";
                                method.code_routine_name = "het_r_proceedure";
                                method.description = "Heterogenous Model";
                                method.parameters = {"parameter": [{"name": "a", "value": s.het_a}, {"name": "b", "value": s.het_b}]};
                            } else if (s.checked === "3") {
                                method.name = "CAR";
                                method.code_routine_name = "car_r_proceedure";
                                method.description = "Conditional autoregressive";
                                method.parameters = {"parameter": [{"name": "a", "value": s.car_a}]};
                            }
                            return method;
                        }
                    };
                });