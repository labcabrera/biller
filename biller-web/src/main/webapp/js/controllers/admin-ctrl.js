(function() {
	
	var billerModule = angular.module('billerModule');
	
	/**
	 * User activity data.
	 */
	billerModule.controller('UserActivityListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', 'dialogs', function($scope, $rootScope, $routeParams, $http, $filter, dialogs) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 20;
		$scope.reset = function() {
			$scope.searchOptions = {
				'user': $routeParams.user
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("user.id=lk=", $scope.searchOptions.user);
			predicateBuilder.append("user.name=lk=", $scope.searchOptions.name);
			predicateBuilder.append("date=ge=", $scope.searchOptions.from != null ? $filter('date')($scope.searchOptions.from, "yyyy-MM-dd") : null);
			predicateBuilder.append("date=le=", $scope.searchOptions.to != null ? $filter('date')($scope.searchOptions.to, "yyyy-MM-dd") : null);
			return 'rest/user-activity/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			$scope.currentPage = 1;
			$scope.searchMessage = "Loading...";
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
				$scope.results = data;
			});
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
		    $http.get($scope.getSearchUrl()).success(function(data) {
		    	$scope.results = data;
		    });
		};
		$scope.reset();
		$scope.search();
	}]);
	
	billerModule.controller('UserActivityDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
		$http.get('rest/user-activity/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
		});
	}]);
	
	/**
	 * Scheduler management.
	 */
	billerModule.controller('SchedulerListCtrl', ['$scope', '$rootScope', '$routeParams', '$http', '$modal', '$translate', function($scope, $rootScope, $routeParams, $http, $modal, $translate) {
		$scope.searchResults = {};
		$scope.setPage  = function(page) {
			$scope.currentPage = page;
			$http.get('rest/scheduler/find').success(function(data) {
				$scope.searchResults = data;
			});
		};
		$scope.setPage(1);
		$scope.edit = function(task) {
			$scope.open(task);
		};
		$scope.open = function(task) {
			var modalInstance = $modal.open({
				templateUrl : 'html/admin/scheduler-detail.html',
				controller : 'SchedulerDetailCtrl',
				size : 'lg',
				resolve : {
					task : function() {
						return task;
					}
				}
			});
			modalInstance.result.then(function(data) {
				$scope.message = data;
				$scope.setPage(1);
			}, function() {
			});
		};
	}]);
	
	billerModule.controller('SchedulerDetailCtrl', ['$scope', '$rootScope', '$routeParams', '$http', '$modal', '$modalInstance', '$translate', 'task', function($scope, $rootScope, $routeParams, $http, $modal, $modalInstance, $translate, task) {
		$scope.task = task;
		$scope.getNextExecutions = function() {
			$http.get('rest/scheduler/nextExecutions/' + $scope.task.id).success(function(data) {
				$scope.nextExecutions = data;
			});
		};
		$scope.save = function() {
			$http.post('rest/scheduler/merge', $scope.task).success(function(data) {
				$scope.message = data;
				if (data.code == '200') {
					$scope.task = data.payload;
					$scope.getNextExecutions();
				}
			});
		};
		$scope.cancel = function() {
			$modalInstance.dismiss('cancel');
		};
		$scope.enable = function() {
			$scope.task.disabled = false;
			$http.post('rest/scheduler/resume/' +  $scope.task.id).success(function(data) {
				if (data.code == '200') {
					$scope.task = data.payload;
					$modalInstance.close(data);
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.disable = function() {
			$scope.task.disabled = true;
			$http.post('rest/scheduler/pause/' +  $scope.task.id).success(function(data) {
				if (data.code == '200') {
					$scope.task = data.payload;
					$modalInstance.close(data);
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.execute = function() {
			$http.post('rest/scheduler/execute/' +  $scope.task.id).success(function(data) {
				$scope.message = data;
			});
		};
		$scope.getNextExecutions();
	}]);
	
	/**
	 * System alerts.
	 */
	billerModule.controller('AlertReceiverListCtrl', ['$scope', '$rootScope', '$routeParams', '$http', '$modal', '$translate', function($scope, $rootScope, $routeParams, $http, $modal, $translate) {
		$scope.searchResults = {};
		$scope.setPage  = function(page) {
			$scope.currentPage = page;
			$http.get('rest/alert-receivers/find').success(function(data) {
				$scope.results = data;
			});
		};
		$scope.setPage(1);
		$scope.edit = function(task) {
			$scope.open(task);
		};
		$scope.open = function(task) {
		};
	}]);
	
	billerModule.controller('AlertReceiverDetailCtrl', ['$scope', '$rootScope', '$routeParams', '$http', '$translate', 'messageService', function($scope, $rootScope, $routeParams, $http, $translate, messageService) {
		$scope.init = function() {
			$http.get('rest/alert-receivers/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
			});
		}
		$scope.update = function() {
			$http.post('rest/alert-receivers/merge', $scope.entity).success(function(data) {
				$scope.message = data; 
				$rootScope.isReadOnly = true;
			});
		};
		$scope.cancel = function() {
		};
		$scope.enable = function() {
		};
		$scope.disable = function() {
		};
		$scope.init();
	}]);

	billerModule.controller('AlertReceiverNewCtrl', ['$scope', '$rootScope', '$location', '$http', 'messageService', function($scope, $rootScope, $location, $http, messageService) {
		$scope.isReadOnly = false;
		$scope.entity = {
				disabled: false
		}
		$scope.update = function() {
			$http.post('rest/alert-receivers/merge/', $scope.entity).success(function(data) {
				if(data.code == '200') {
					messageService.setMessage(data);
					$location.path("admin/alert-management/id/" + data.payload.id);				
				} else {
					$scope.message = data;
				}
			});
		};
	}]);
	
	billerModule.controller('SystemInfoCtrl', ['$scope', '$rootScope', '$location', '$http', 'messageService', function($scope, $rootScope, $location, $http, messageService) {
		$scope.init = function() {
			$http.get('rest/system/info', $scope.entity).success(function(data) {
				$scope.info = data;
			});
		};
		$scope.init();
	}]);

	
})();
