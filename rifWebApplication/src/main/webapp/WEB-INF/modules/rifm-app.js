angular.module("RIF",
        [
            "ui.router",
            "ui.bootstrap",
            "ui-leaflet",
            "ui.grid",
            "ui.grid.selection",
            "ui.grid.resizeColumns",
            'ui.grid.treeView',
            "ngAnimate",
            "ngNotificationsBar",
            "ui.layout"
        ]
        )
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
                //Handle main page transitions in navbar, login
                $stateProvider
                        .state('state0', {
                            url: '/login',
                            templateUrl: "dashboards/login/partials/rifp-login-main.html"
                        })
                        .state('state1', {
                            url: '/submission',
                            templateUrl: "dashboards/submission/partials/rifp-dsub-main.html",
                            controller: function ($scope, $state) {
                                $scope.$state = $state;
                            }
                        })
                        .state('state2', {
                            url: '/viewer',
                            templateUrl: "dashboards/viewer/partials/rifp-view-main.html"
                        })
                        .state('state3', {
                            url: '/mapping',
                            templateUrl: "dashboards/mapping/partials/rifp-dmap-main.html"
                        })
                        .state('state4', {
                            url: '/test',
                            templateUrl: "test/rifp-test-main.html"
                        });
                $urlRouterProvider.otherwise("/submission");  //login
            }])
        .controller('AlertCtrl', function ($scope, notifications) {
            //CONTROLLER to handle alert bars and notofications
            //This is on the main <body> scope in index
            //Subsequent contollers in data-ui-view are children
            $scope.hide = false;
            $scope.showError = function (msg) {
                notifications.showError({message: 'Error: ' + msg, hide: $scope.hide});
            };
            $scope.showWarning = function (msg) {
                notifications.showWarning({message: 'Warning: ' + msg, hide: $scope.hide});
            };
            $scope.showSuccess = function (msg) {
                notifications.showSuccess({message: 'Success: ' + msg, hide: $scope.hide});
            };
        })
        .run(function ($rootScope, $state) {
            $rootScope.$state = $state;
        });



















	