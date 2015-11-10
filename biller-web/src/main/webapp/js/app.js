'use strict';

var billerModule = angular.module('billerModule', [ 'ngRoute', 'billerModule', 'ui.bootstrap', 'dialogs.main', 'pascalprecht.translate']);

billerModule.config([ '$routeProvider', function($routeProvider, $rootScope, $location) {
	
	$routeProvider.when('/users', { templateUrl : 'partials/user-list.html', controller : 'UserListCtrl'
	}).when('/users/:id', { templateUrl : 'partials/user-detail.html', controller : 'UserDetailCtrl'
	}).when('/groups', { templateUrl : 'partials/group-list.html', controller : 'GroupListCtrl'
	}).when('/groups/id/:id', { templateUrl : 'partials/group-detail.html', controller : 'GroupDetailCtrl'
	}).when('/groups/new', { templateUrl : 'partials/group-detail.html', controller : 'GroupNewCtrl'
	}).when('/companies', { templateUrl : 'partials/company-list.html', controller : 'CompanyListCtrl'
	}).when('/companies/id/:id', { templateUrl : 'partials/company-detail.html', controller : 'CompanyDetailCtrl'
	}).when('/companies/new', { templateUrl : 'partials/company-detail.html', controller : 'CompanyNewCtrl'
	}).when('/costcenters', { templateUrl : 'partials/costcenter-list.html', controller : 'CostCenterListCtrl'
	}).when('/costcenters/id/:id', { templateUrl : 'partials/costcenter-detail.html', controller : 'CostCenterDetailCtrl'
	}).when('/costcenters/new', { templateUrl : 'partials/costcenter-detail.html', controller : 'CostCenterNewCtrl'
	}).when('/stores', { templateUrl : 'partials/store-list.html', controller : 'StoreListCtrl'
	}).when('/stores/id/:id', { templateUrl : 'partials/store-detail.html', controller : 'StoreDetailCtrl'
	}).when('/stores/new', { templateUrl : 'partials/store-detail.html', controller : 'StoreNewCtrl'
	}).when('/owners', { templateUrl : 'partials/owner-list.html', controller : 'OwnerListCtrl'
	}).when('/owners/id/:id', { templateUrl : 'partials/owner-detail.html', controller : 'OwnerDetailCtrl'
	}).when('/owners/new', { templateUrl : 'partials/owner-detail.html', controller : 'OwnerNewCtrl'
	}).when('/terminals', { templateUrl : 'partials/terminal-list.html', controller : 'TerminalListCtrl'
	}).when('/terminals/id/:id', { templateUrl : 'partials/terminal-detail.html', controller : 'TerminalDetailCtrl'
	}).when('/terminals/new', { templateUrl : 'partials/terminal-detail.html', controller : 'TerminalNewCtrl'
	}).when('/models', { templateUrl : 'partials/model-list.html', controller : 'ModelListCtrl'
	}).when('/models/id/:id', { templateUrl : 'partials/model-detail.html', controller : 'ModelDetailCtrl'
	}).when('/models/new', { templateUrl : 'partials/model-detail.html', controller : 'ModelNewCtrl'
	}).when('/taxes', { templateUrl : 'html/taxes/taxes.html', controller : 'TaxesCtrl'
	}).when('/bills', { templateUrl : 'partials/bill-list.html', controller : 'BillListCtrl'
	}).when('/bills/id/:id', { templateUrl : 'partials/bill-detail.html', controller : 'BillDetailCtrl'
	}).when('/liquidations', { templateUrl : 'partials/liquidation-list.html', controller : 'LiquidationListCtrl'
	}).when('/liquidations/id/:id', { templateUrl : 'partials/liquidation-detail.html', controller : 'LiquidationDetailCtrl'
	}).when('/rappel/stores/', { templateUrl : 'partials/rappel-store-list.html', controller : 'RappelStoreListCtrl'
	}).when('/rappel/stores/id/:id', { templateUrl : 'partials/rappel-store-detail.html', controller : 'RappelStoreDetailCtrl'
	}).when('/reports/terminals', { templateUrl : 'html/reports/report-terminals.html', controller : 'ReportTerminalsCtrl'
	}).when('/reports/liquidations', { templateUrl : 'html/reports/report-liquidations.html', controller : 'ReportLiquidationsCtrl'
	}).when('/admin/console', { templateUrl : 'partials/admin/admin-console.html'
	}).when('/admin/settings', { templateUrl : 'partials/admin/admin-settings.html', controller: 'SettingsCtrl'
	}).when('/admin/jobs', { templateUrl : 'partials/admin/admin-jobs.html', controller: 'SettingsCtrl'
	}).when('/admin/recalculate/bill', { templateUrl : 'html/admin/recalculate-bill.html', controller: 'BillRecalculationCtrl'
	}).when('/admin/recalculate/liquidation', { templateUrl : 'html/admin/recalculate-liquidation.html', controller: 'BillRecalculationCtrl'
	}).when('/login', { templateUrl : 'partials/login.html', controller: 'LoginCtrl'
	}).when('/index', { templateUrl : 'static/index.html'
	}).when('/sequence-prefix', { templateUrl : 'static/sequence-prefix.html'
	}).when('/forbidden', { templateUrl : 'partials/403.html'
	}).when('/', { templateUrl : 'static/index.html'
	}).otherwise({ templateUrl : 'partials/404.html'
	});
} ]);

billerModule.directive('typeahead', function() {
	return {
		require : 'ngModel',
		link : function(scope, element, attrs, modelCtrl) {
			var checkObject = function(value) {
				return (value == '' || typeof value === 'string') ? null : value;
			};
			modelCtrl.$parsers.push(checkObject);
			checkObject(scope[attrs.ngModel]);
		}
	};
});

billerModule.config(['$translateProvider', function($translateProvider) {
	$translateProvider.preferredLanguage('es');
	$translateProvider.useStaticFilesLoader({
		prefix: CONTEXT_PATH + '/i18n/',
		suffix: '.json'
	});
}]);

billerModule.controller('LanguageCtrl', ['$scope', '$translate', function($scope, $translate) {
	$scope.changeLanguage = function(key) {
		$translate.use(key);
	};
	$scope.getLanguage = function() {
		return $translate.use();
	};
}]);

billerModule.run(function($rootScope, $http) {
	$rootScope.isReadOnly = true;
	$rootScope.itemsPerPage = 10;
	$rootScope.groups = function(name) { return $http.get("rest/groups/find?q=name=lk=" + name).then(function(response) { return response.data.results; }); };
	$rootScope.companies = function(name) { return $http.get("rest/companies/find?q=name=lk=" + name).then(function(response) { return response.data.results; }); };
	$rootScope.stores = function(name) { return $http.get("rest/stores/find?q=name=lk=" + name).then(function(response) { return response.data.results; }); };
	$rootScope.owners = function(name) { return $http.get("rest/owners/find?q=name=lk=" + name).then(function(response) { return response.data.results; }); };
	$rootScope.models = function(name) { return $http.get("rest/models/find?q=name=lk=" + name).then(function(response) { return response.data.results; }); };
	$rootScope.terminals = function(code) { return $http.get("rest/terminals/find?q=code=lk=" + code).then(function(response) { return response.data.results; }); };
	$rootScope.costcenters = function(name) { return $http.get("rest/costcenters/find?q=name=lk=" + name).then(function(response) { return response.data.results; }); };
	$rootScope.provinces = function(name) { return $http.get("rest/provinces/find/" + name).then(function(response) { return response.data; }); };
	$rootScope.regions = function(name, provinceId) { return $http.get("rest/regions/find/" + name + (provinceId != null ? '?province=' + provinceId : '')).then(function(response) { return response.data; }); };

	$rootScope.edit = function() {
		$rootScope.isReadOnly = false;
		$("#alertContainer").empty();
	};
	
	$rootScope.displayAlertInternal = function(message, container) {
		var t = container;
		var d = $('<div/>').addClass('alert alert-dismissable').addClass(message.code == 200 ? 'alert-info' : 'alert-warning');
		d.append($('<button/>').addClass("close").attr('type', 'button').attr('data-dismiss', 'alert').attr('aria-hiden', 'true').append('&times;'));
		d.append($('<span>').append(message.message));
		if(message.errors != null && message.errors.length > 0) {
			var u = $('<ul>').addClass('style','unstyled');
			for(var i = 0; i < message.errors.length; i++) {
				u.append($('<li>').append(message.errors[i]));
			}
			d.append(u);
		}
		t.empty();
		t.append(d);
	};
	
	$rootScope.displayAlert = function(message) {
		$rootScope.displayAlertInternal(message, $("#alertContainer"));
	};
	
	$rootScope.displayAlertModal = function(message, containerId) {
		$rootScope.displayAlertInternal(message, $("#modalAlertContainer"));
	};
	
	$rootScope.logout = function() {
		$rootScope.user = { "name": ""};
	};

	$rootScope.todo = function() {
		$rootScope.displayAlert({
			"code": 500,
			"message": "Esta funcionalidad no está implementada"
		});
	};
	
	/*
	 * Muestra elementos de debug en el front como popups con el json de las entidades
	 */
	$rootScope.debug = true;
	
	/*
	 * En caso de establecer esta variable a true no se muestran los popups de confirmacion
	 */
	$rootScope.autoconfirm = false;
	
	/*
	 * Formato de fechas de los controles
	 */
	$rootScope.dateFormat = 'dd-MM-yyyy';
});

/*
billerModule.config(['$httpProvider', function($httpProvider) {
	$httpProvider.interceptors.push('securityInterceptor');
}]);

billerModule.factory('securityInterceptor', ['$q', '$location', function($q, $location) {  
    var injector = {
		request: function(data) {
    		console.log("http request " + data.url);
            return data;
    	},
    	response: function(data) {
    		console.log("http response url " + data.config.url + ": " + data.status);
            return data;
    	},
        requestError: function(data) {
        	console.log("http request error " + data.config.url + ": " + data.statys);
            return data;
        },
        responseError: function(data) {
        	console.log("http response error url " + data.config.url + ": " + data.status);
        	return $q.reject(data);
        }
    };
    return injector;
}]);
*/

billerModule.factory('messageService', function() {
	var message = null;
    return {
        setMessage: function(data) {
        	message = data;
        },
        getMessage: function() {
        	return message;
        },
        hasMessage: function() {
        	return message != null;
        }
    };
});

/*
 * Directiva para generar inputs que solo aceptan valores numericos con una cantidad dada de decimales
 */
billerModule.directive('numberOnlyInput', function () {
    return {
        restrict: 'EA',
        template: '<input class="form-control input-sm data-amount" name="{{inputName}}" ng-model="inputValue" />',
        scope: {
            inputValue: '=',
            inputName: '='
        },
        link: function (scope) {
        	var decimals = 2;
            scope.$watch('inputValue', function(newValue, oldValue) {
                var arr = String(newValue).split("");
                if (arr.length === 0) return;
                if (arr.length === 1 && (arr[0] == '-' || arr[0] === '.' )) return;
                if (arr.length === 2 && newValue === '-.') return;
                var sp = String(newValue).split(".");
                if(sp.length === 2 && sp[1].length > decimals) {
                	console.log('No permitimos mas de dos decimales');
                	scope.inputValue = oldValue;
                }
                if (isNaN(newValue)) {
                    scope.inputValue = oldValue;
                }
            });
        }
    };
});

/*
 * Utilidad para generar consultas de FIQL
 */
function PredicateBuilder(expression) {
	this.expression = expression;
	this.build = function() {
		return this.expression;
	};
	this.append = function(key, value) {
		if(value != null && value != '') {
			if(this.expression != null && this.expression != '') {
				this.expression += ';';
			}
			this.expression += key + value;
		}
	};
	this.appendKey = function(key) {
		if(this.expression != null && this.expression != '') {
			this.expression += ';';
		}
		this.expression += key;
	};
}
