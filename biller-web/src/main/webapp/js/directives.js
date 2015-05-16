(function() {

	var billerModule = angular.module('billerModule');
	
	billerModule.directive('searchResultPagination', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/search-result-pagination.html',
			replace : 'false'
		};
	});
	
	billerModule.directive('calendar', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/calendar.html',
			controller : 'CalendarCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				disabled : '@'
			},
		};
	});
	
	billerModule.directive('calendarReal', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/calendar-real.html',
			controller : 'CalendarRealCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				disabled : '@'
			},
		};
	});
	
	billerModule.directive('province', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/province-searchbox.html',
			controller : 'ProvinceCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				locked : '='
			},
		};
	});
	
	billerModule.directive('region', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/region-searchbox.html',
			controller : 'RegionCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				provinceId : '=',
				disabled : '@'
			},
		};
	});
	
	billerModule.directive('company', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/company-searchbox.html',
			controller : 'CompanyCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				disabled : '@'
			},
		};
	});
	
	billerModule.directive('store', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/store-searchbox.html',
			controller : 'StoreCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				disabled : '@'
			},
		};
	});

	billerModule.controller('CalendarCtrl', ['$scope', function($scope) {
		$scope.placeHolder= 'DD/MM/YYYY';
		$scope.showCalendar = false;
		$scope.loadRealCalendar = function() {
			$scope.showCalendar = true;
		};
	}]);
	
	billerModule.controller('CalendarRealCtrl', ['$scope', '$timeout', function($scope, $timeout) {
		//$scope.format = 'mediumDate';
		$scope.format = 'dd/MM/yyyy';
		$scope.placeHolder= 'DD/MM/YYYY';
		$timeout(function() {
				$scope.opened= true;
		}, 100);
		$scope.today = function() {
			$scope.ngModel = new Date();
		};
		$scope.clear = function() {
			$scope.ngModel = null;
		};
		$scope.toggleMin = function() {
			$scope.minDate = $scope.minDate ? null : new Date();
		};
		$scope.toggleMin();
		$scope.open = function($event) {
			$event.preventDefault();
			$event.stopPropagation();
			$scope.opened = true;
		};
		$scope.dateOptions = {
			formatYear : 'yy',
			startingDay : 1
		};
	}]);
	
	billerModule.controller('ProvinceCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.provinces = function(name) {
			return $http.get(REST_PATH + '/provinces/find/' + name).then(function(response) {
				return response.data;
			});
		};
	}]);
	
	billerModule.controller('RegionCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.regions = function(name) {
			var provinceId = $scope.provinceId;
			console.log("Province: " + provinceId);
			return $http.get("rest/regions/find/" + name + (provinceId != null ? '?province=' + provinceId : '')).then(function(response) { return response.data; });
		};
	}]);
	
	billerModule.controller('CompanyCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.companies = function(name) {
			return $http.get(REST_PATH + '/companies/find?q=name=lk=' + name).then(function(response) {
				return response.data.results;
			});
		};
	}]);
	
	billerModule.controller('StoreCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.stores = function(name) {
			return $http.get(REST_PATH + '/stores/find?q=name=lk=' + name).then(function(response) {
				return response.data.results;
			});
		};
	}]);
	
	
})();