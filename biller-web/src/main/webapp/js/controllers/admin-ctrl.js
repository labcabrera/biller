(function() {
	
	var billerModule = angular.module('billerModule');
	
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
			predicateBuilder.append("user.name=lk=", $scope.searchOptions.user);
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
	
	billerModule.controller('UserActivityCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
		$http.get('rest/user-activity/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
		});
	}]);
	
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
	
	
})();
