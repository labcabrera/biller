'use strict';

var billerModule = angular.module('billerModule', [ 'ngRoute', 'ngCookies', 'billerModule', 'ui.bootstrap', 'dialogs.main', 'tc.chartjs', 'pascalprecht.translate']);

billerModule.config([ '$routeProvider', function($routeProvider, $rootScope, $location) {
	
	$routeProvider.when('/users', { templateUrl : 'html/user-list.html', controller : 'UserListCtrl'
	}).when('/users/:id', { templateUrl : 'html/user-detail.html', controller : 'UserDetailCtrl'
	}).when('/groups', { templateUrl : 'html/company-group-list.html', controller : 'GroupListCtrl'
	}).when('/groups/id/:id', { templateUrl : 'html/company-group-detail.html', controller : 'GroupDetailCtrl'
	}).when('/groups/new', { templateUrl : 'html/company-group-detail.html', controller : 'GroupNewCtrl'
	}).when('/companies', { templateUrl : 'html/company-list.html', controller : 'CompanyListCtrl'
	}).when('/companies/id/:id', { templateUrl : 'html/company-detail.html', controller : 'CompanyDetailCtrl'
	}).when('/companies/new', { templateUrl : 'html/company-detail.html', controller : 'CompanyNewCtrl'
	}).when('/costcenters', { templateUrl : 'html/cost-center-list.html', controller : 'CostCenterListCtrl'
	}).when('/costcenters/id/:id', { templateUrl : 'html/cost-center-detail.html', controller : 'CostCenterDetailCtrl'
	}).when('/costcenters/new', { templateUrl : 'html/cost-center-detail.html', controller : 'CostCenterNewCtrl'
	}).when('/stores', { templateUrl : 'html/store-list.html', controller : 'StoreListCtrl'
	}).when('/stores/id/:id', { templateUrl : 'html/store-detail.html', controller : 'StoreDetailCtrl'
	}).when('/stores/new', { templateUrl : 'html/store-detail.html', controller : 'StoreNewCtrl'
	}).when('/owners', { templateUrl : 'html/owner-list.html', controller : 'OwnerListCtrl'
	}).when('/owners/id/:id', { templateUrl : 'html/owner-detail.html', controller : 'OwnerDetailCtrl'
	}).when('/owners/new', { templateUrl : 'html/owner-detail.html', controller : 'OwnerNewCtrl'
	}).when('/terminals', { templateUrl : 'html/terminal-list.html', controller : 'TerminalListCtrl'
	}).when('/terminals/id/:id', { templateUrl : 'html/terminal-detail.html', controller : 'TerminalDetailCtrl'
	}).when('/terminals/new', { templateUrl : 'html/terminal-detail.html', controller : 'TerminalNewCtrl'
	}).when('/models', { templateUrl : 'html/billing-model-list.html', controller : 'ModelListCtrl'
	}).when('/models/id/:id', { templateUrl : 'html/billing-model-detail.html', controller : 'ModelDetailCtrl'
	}).when('/models/new', { templateUrl : 'html/billing-model-detail.html', controller : 'ModelNewCtrl'
	}).when('/taxes', { templateUrl : 'html/taxes/taxes.html', controller : 'TaxesCtrl'
	}).when('/bills', { templateUrl : 'html/bill-list.html', controller : 'BillListCtrl'
	}).when('/bills/id/:id', { templateUrl : 'html/bill-detail.html', controller : 'BillDetailCtrl'
	}).when('/liquidations', { templateUrl : 'html/liquidation-list.html', controller : 'LiquidationListCtrl'
	}).when('/liquidations/id/:id', { templateUrl : 'html/liquidation-detail.html', controller : 'LiquidationDetailCtrl'
	}).when('/adjustments/stores', { templateUrl : 'html/adjustment-store-list.html', controller : 'AdjustmentStoreListCtrl'
	}).when('/adjustments/liquidations', { templateUrl : 'html/adjustment-liquidation-list.html', controller : 'AdjustmentLiquidationListCtrl'
	}).when('/rappel/stores/', { templateUrl : 'html/rappel-store-list.html', controller : 'RappelStoreListCtrl'
	}).when('/rappel/stores/id/:id', { templateUrl : 'html/rappel-store-detail.html', controller : 'RappelStoreDetailCtrl'
	}).when('/reports/terminals', { templateUrl : 'html/reports/report-terminals.html', controller : 'ReportTerminalsCtrl'
	}).when('/reports/liquidations', { templateUrl : 'html/reports/report-liquidations.html', controller : 'ReportLiquidationsCtrl'
	}).when('/reports/liquidations-summary', { templateUrl : 'html/reports/report-liquidations-summary.html', controller : 'ReportLiquidationsSummaryCtrl'
	}).when('/reports/adjustments', { templateUrl : 'html/reports/report-adjustments.html', controller : 'ReportAdjustmentsCtrl'
	}).when('/admin/console', { templateUrl : 'html/admin/admin-console.html'
	}).when('/admin/settings', { templateUrl : 'html/admin/admin-settings.html', controller: 'SettingsCtrl'
	}).when('/admin/jobs', { templateUrl : 'html/admin/admin-jobs.html', controller: 'SettingsCtrl'
	}).when('/admin/recalculate/bill', { templateUrl : 'html/admin/recalculate-bill.html', controller: 'BillRecalculationCtrl'
	}).when('/admin/user-activity', { templateUrl : 'html/admin/user-activity/user-activity-list.html', controller: 'UserActivityListCtrl'
	}).when('/admin/user-activity/id/:id', { templateUrl : 'html/admin/user-activity/user-activity-detail.html', controller: 'UserActivityDetailCtrl'
	}).when('/admin/users', { templateUrl : 'html/admin/user-list.html', controller: 'UserListCtrl'
	}).when('/admin/users/id/:id', { templateUrl : 'html/admin/user-detail.html', controller: 'UserDetailCtrl'
	}).when('/admin/users/new', { templateUrl : 'html/admin/user-new.html', controller: 'UserNewCtrl'
	}).when('/admin/alert-management', { templateUrl : 'html/admin/alert-receiver/alert-receiver-list.html', controller: 'AlertReceiverListCtrl'
	}).when('/admin/alert-management/id/:id', { templateUrl : 'html/admin/alert-receiver/alert-receiver-detail.html', controller: 'AlertReceiverDetailCtrl'
	}).when('/admin/alert-management/new', { templateUrl : 'html/admin/alert-receiver/alert-receiver-detail.html', controller: 'AlertReceiverNewCtrl'
	}).when('/admin/scheduler', { templateUrl : 'html/admin/scheduler-list.html', controller: 'SchedulerListCtrl'
	}).when('/dashboard/companies', { templateUrl : 'html/dashboard/dashboard-panel.html', controller : 'DashboardCtrl'
	}).when('/login', { templateUrl : 'html/login.html', controller: 'LoginCtrl'
	}).when('/index', { templateUrl : 'html/index.html'
	}).when('/sequence-prefix', { templateUrl : 'html/sequence-prefix.html'
	}).when('/forbidden', { templateUrl : 'html/403.html'
	}).when('/not-found', { templateUrl : 'html/404.html'
	}).when('/204', { templateUrl : 'html/204.html'
	}).when('/', { templateUrl : 'html/index.html'
	}).otherwise({ templateUrl : 'html/404.html'
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
		prefix: 'i18n/',
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

billerModule.run(function($rootScope, $http, $cookies, $location) {
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
	try {
		$rootScope.user = JSON.parse($cookies.get('biller.user'));
	}catch(e) {
	}

	$rootScope.edit = function() {
		$rootScope.isReadOnly = false;
		$("#alertContainer").empty();
	};
	
	$rootScope.checkRole = function(names) {
		var result = false;
		if($rootScope.user != null && $rootScope.user.roles != null) {
			for(var i=0; i<$rootScope.user.roles.length; i++) {
				var check = $rootScope.user.roles[i].code;
				var index = names.indexOf(check);
				if(index > -1) {
					result = true;
					break;
				};
			};
		};
		return result;
	};
	
	$rootScope.logout = function() {
		$cookies.remove('biller.sessionid');
		$cookies.remove('biller.user');
		$rootScope.user = null;
		$location.url('login');
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

/**
* SECURITY INTERCEPTOR
*/
billerModule.factory('authInterceptor', ['$rootScope', '$q', '$location', '$cookies', function ($rootScope, $q, $location, $cookies) {
	return {
		request: function (config) {
			config.headers = config.headers || {};
			config.headers.sessionid = $cookies.get("biller.sessionid");
			if (config.method == 'POST' && config.data == null) {
				config.data = '{}';
			}
			return config;
		},
		requestError: function (rejection) {
			return $q.reject(rejection);
		},
		response: function (response) {
			if (response.status === 404) {
				$location.url("not-found");
			}else if (response.status === 401 || response.status === 403) {
				$location.url("forbidden");
			}
			return response || $q.when(response);
		},
		responseError: function (rejection) {
			if(rejection.status === 403) {
				$location.url("forbidden");
				return;
			}
			return $q.reject(rejection);
		}
};}]);

billerModule.config(['$httpProvider', function ($httpProvider) {
	$httpProvider.interceptors.push('authInterceptor');
}]);


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
        template: '<input class="form-control data-amount" name="{{inputName}}" ng-model="inputValue" />',
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
};

billerModule.filter('abs', function () {
	return function(val) {
		return Math.abs(val);
	};
});
