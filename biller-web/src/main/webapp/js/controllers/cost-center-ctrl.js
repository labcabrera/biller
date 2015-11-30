(function() {
	
	var billerModule = angular.module('billerModule');

	billerModule.controller('CostCenterListCtrl', [ '$scope', '$http', function($scope, $http) {
		$scope.currentPage = 1;
		$scope.reset = function() {
			$scope.searchOptions = {
				'name': '',
				'code': '',
				'showDeleted': false
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("name=lk=", $scope.searchOptions.name);
			predicateBuilder.append("code=lk=", $scope.searchOptions.code);
			if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
			return 'rest/costcenters/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.setPage = function(page) {
			$scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
		};
		$scope.search = function() {
			$scope.searchMessage = "Loading...";
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.results = data;
				$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
			});
		};
		$scope.reset();
		$scope.search();
	} ]);
	
	
	billerModule.controller('CostCenterDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.load = function() {
			$http.get('rest/costcenters/id/' + $routeParams.id).success(function(data) { $scope.entity = data; });
			$rootScope.isReadOnly = true;
			$scope.setStorePage(1);
		};
		$scope.reset = function() { $scope.load(); };
		$scope.update = function() {
			$http.post('rest/costcenters/merge/', $scope.entity).success(function(data) {
				$scope.message = data;
				if(data.code == 200) {
					$rootScope.isReadOnly = true;				
					$scope.message = data.payload;
				}
			});
		};
		$scope.remove = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a eliminar el centro de coste')) {
				$http.post('rest/costcenters/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) {
						$location.path("costcenters");
					} else {
						$scope.message = data;
					}
				});
			}
		};
		$scope.setStorePage = function(page) {
		    $scope.currentPage = page;
		    $http.get('rest/stores/find?q=costCenter.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
		};
		$scope.load();
	} ]);
	
	billerModule.controller('CostCenterNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.update = function() {
			$http.post('rest/costcenters/merge/', $scope.entity).success(function(data) {
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("costcenters/id/" + data.payload.id);				
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
	} ]);

})();