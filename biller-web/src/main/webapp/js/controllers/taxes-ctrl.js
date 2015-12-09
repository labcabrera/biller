(function() {

	var billerModule = angular.module('billerModule');

	billerModule.controller('TaxesCtrl', [ '$scope', '$routeParams', '$http', '$modal', function($scope, $routeParams, $http, $modal) {
		$scope.currentPage = 1;
		$scope.searchName = '';
		$scope.reset = function() {
			$scope.searchOptions = { };
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			if($scope.searchOptions.province != null) {
				predicateBuilder.appendKey("province.id==" + $scope.searchOptions.province.id);
			};
			return 'rest/provinceTaxes/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			$scope.searchMessage = "Loading...";
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.results = data;
				$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
			});
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
		};
		$scope.edit = function(task) {
			$scope.open(task);
		};
		$scope.open = function(entity) {
			var modalInstance = $modal.open({
				templateUrl : 'html/taxes/taxes-detail.html',
				controller : 'TaxesDetailCtrl',
				size : 'lg',
				resolve : {
					entity : function() {
						return entity;
					}
				}
			});
			modalInstance.result.then(function(data) {
				$scope.message = data;
				$scope.setPage(1);
			}, function() {
			});
		};
		$scope.reset();
		$scope.search();
	}]);
	
	billerModule.controller('TaxesDetailCtrl', [ '$scope', '$routeParams', '$http', '$modalInstance', 'entity', function($scope, $routeParams, $http, $modalInstance, entity) {
		$scope.entity = entity;
		$scope.cancel = function() {
			$modalInstance.dismiss('cancel');
		};
		$scope.save = function() {
			$http.post('rest/provinceTaxes/merge', $scope.entity).success(function(data) {
				if (data.code == '200') {
					$modalInstance.close(data);
				} else {
					$scope.message = data;					
				};
			});
		};
	}]);

})();