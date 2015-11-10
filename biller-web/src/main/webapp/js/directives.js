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
				locked : '='
			},
		};
	});
	
	billerModule.directive('companyGroup', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/company-group-searchbox.html',
			controller : 'CompanyGroupCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				locked : '='
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
				locked : '='
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

	billerModule.controller('CompanyGroupCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.companies = function(name) {
			return $http.get(REST_PATH + '/groups/find?q=name=lk=' + name).then(function(response) {
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
	
	billerModule.directive('messageErrorPanel', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/message-error-panel.html',
			controller : ['$scope', '$timeout', function($scope, $timeout) {
				$scope.$watch('message', function() {
					$scope.isCollapse = false;
				});
				$scope.setCollapse = function() {
					$scope.isCollapse = true;
				};
				if ($scope.autoClose) {
					$scope.$watch('message', function(newValue, oldValue) {
						$timeout(function() {
							$scope.isCollapse = true;
						}, 5000);
					});
				}
			}],
			require : '^ngModel',
			replace : 'false',
			scope : {
				message : '=ngModel',
				autoClose : '='
			},
		};
	});
	
	billerModule.directive('messageErrorBox', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/message-error-box.html',
			controller : ['$scope', function($scope) {
				$scope.$watch('messages', function() {
					$scope.isCollapse = false;
				});
				$scope.setCollapse = function() {
					$scope.isCollapse = true;
				};
			}],
			require : '^ngModel',
			replace : 'false',
			scope : {
				messages : '=ngModel',
				messageIcon : '@',
				messageClass : '@'
			}
		};
	});

	billerModule.factory('MessageUtils', function() {
		return {
			createError : function(text) {
				var message = new Object();
				message.errors = new Array();
				message.errors[0] = new Object();
				message.errors[0].text = text;
				return message;
			}
		};
	});

	billerModule.factory('Utils', function() {
		return {
			isEmpty : function(value) {
				return value == null || value == '' || value == 'undefined';
			}
		};
	});
})();