angular.module("RIF")
        .factory('ChoroService',
                function () {
                    var features = [];
                    var viewMap = {
                        brewerName: "LightGreen",
                        brewer: ["#9BCD9B"],
                        intervals: 1,
                        feature: "",
                        invert: false,
                        method: "quantile"
                    };
                    
                    return {
                        setFeaturesToMap: function (attrs) {
                            features = attrs;
                        },
                        getFeaturesToMap: function () {
                            return features;
                        },
                        setViewMap: function (modal) {
                     
                        },
                        getViewMap: function () {
                            return viewMap;
                        }
                    };
                });