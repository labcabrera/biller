(function() {

	var appModule = angular.module('billerControllers');
	
	appModule.directive('calendar', function() {
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
	
	appModule.directive('calendarReal', function() {
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
	
	appModule.directive('province', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/province-searchbox.html',
			controller : 'ProvinceCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				disabled : '@'
			},
		};
	});
	
	appModule.directive('company', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/company-searchbox.html',
			controller : 'CompanyCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				disabled : '@'
			},
		};
	});

	appModule.controller('CalendarCtrl', ['$scope', function($scope) {
		$scope.placeHolder= 'DD/MM/YYYY';
		$scope.showCalendar = false;
		$scope.loadRealCalendar = function() {
			$scope.showCalendar = true;
		};
	}]);
	
	appModule.controller('CalendarRealCtrl', ['$scope', '$timeout', function($scope, $timeout) {
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
	
	appModule.controller('ProvinceCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.provinces = function(name) {
			return $http.get(REST_PATH + '/provinces/find/' + name).then(function(response) {
				return response.data;
			});
		};
	}]);
	
	appModule.controller('CompanyCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.companies = function(name) {
			return $http.get(REST_PATH + '/companies/find?q=name=lk=' + name).then(function(response) {
				return response.data.results;
			});
		};
	}]);
	
	
})();