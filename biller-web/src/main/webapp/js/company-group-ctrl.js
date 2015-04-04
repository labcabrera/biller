(function() {

	var billerControllers = angular.module('billerControllers');

	billerControllers.controller('GroupListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
		$scope.currentPage = 1;
		$scope.searchName = '';
		$scope.reset = function() {
			$scope.searchOptions = {
					'name': '',
					'showDeleted': false,
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("name=lk=", $scope.searchOptions.name);			
			if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
			return REST_PATH + '/groups/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
		};
		$scope.reset();
		$scope.search();
	} ]);

	billerControllers.controller('GroupDetailCtrl', [ '$scope', '$rootScope', '$location', '$routeParams', '$http', 'messageService', function($scope, $rootScope, $location, $routeParams, $http, messageService) {
		if(messageService.hasMessage()) {
			$scope.displayAlert(messageService.getMessage());
		}
		$scope.load = function() {
			$http.get(REST_PATH + '/groups/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
			});
			$rootScope.isReadOnly = true;
			$scope.setCompanyPage(1);
		};
		$scope.update = function() {
			$http.post(REST_PATH + '/groups/merge/', $scope.entity).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
					$rootScope.isReadOnly = true;				
				}
			});
		};
		$scope.remove = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a eliminar el grupo')) {
				$http.post(REST_PATH + '/groups/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) { $location.path("groups"); } else { $scope.displayAlert(data); }
				});
			}
		};
		$scope.setCompanyPage = function(page) {
		    $scope.currentPage = page;
		    $http.get('rest/companies/find?q=parent.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) {
		    	$scope.childs = data;
		    });
		};
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
		$scope.load();
	} ]);

	billerControllers.controller('GroupNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.update = function() {
			$http.post(REST_PATH + '/groups/merge/', $scope.entity).success(function(data) {
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("groups/id/" + data.payload.id);				
				} else {
					$scope.displayAlert(data);
				}
			});
		};
		$scope.provinces = function(name) { return $http.get(REST_PATH + "/provinces/find/" + name).then(function(response) { return response.data; }); };
	} ]);

})();