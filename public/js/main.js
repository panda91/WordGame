/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
    'angular': ['../lib/angularjs/angular'],
    'angular-route': ['../lib/angularjs/angular-route'],
    'angular-animate': ['../lib/angularjs/angular-animate'],
    'angular-sanitize': ['../lib/angularjs/angular-sanitize'],
    'angularBootstrap' : ['../lib/angular-ui-bootstrap/ui-bootstrap-tpls']
  },
  shim: {
    'angular': {
      exports : 'angular'
    },
    'angular-route': {
      deps: ['angular'],
      exports : 'angular'
    },
    'angular-animate':{
    	deps: ['angular'],
        exports : 'angular'
    },
    'angular-sanitize':{
    	deps: ['angular'],
        exports : 'angular'
    },
    'angularBootstrap': {
        deps: ['angular','angular-animate','angular-sanitize'],
        exports : 'angularBootstrap'
    },
  }
});

require(['angular', './controllers', './directives', './filters', './services', 'angular-route', 'angular-animate', 'angular-sanitize', 'angularBootstrap'],
  function(angular, controllers) {

    // Declare app level module which depends on filters, and services

    angular.module('myApp', ['myApp.filters', 'myApp.services', 'myApp.directives', 'ngRoute', 'ngAnimate', 'ngSanitize', 'ui.bootstrap']).
      config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/play', {templateUrl: 'partials/partial1.html', controller: controllers.AppController});
        $routeProvider.when('/withdrawn', {templateUrl: 'partials/partial2.html', controller: controllers.WithDrawnCtrl});
        $routeProvider.otherwise({redirectTo: '/play'});
      }]);

    angular.bootstrap(document, ['myApp']);

});
