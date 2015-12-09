(function() {

	var billerModule = angular.module('billerModule');

	billerModule.controller('GroupListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
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
			if(!$scope.searchOptions.showDeleted) {
				predicateBuilder.appendKey("auditData.deleted=n=");
			} else {
				predicateBuilder.appendKey("auditData.deleted=!n=");
			}
			return 'rest/groups/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			$scope.searchMessage = "Loading...";
			$scope.results = null;
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.results = data;
				$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
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
	} ]);

	billerModule.controller('GroupDetailCtrl', [ '$scope', '$rootScope', '$location', '$routeParams', '$http', '$filter', 'dialogs', 'messageService', function($scope, $rootScope, $location, $routeParams, $http, $filter, dialogs, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.load = function() {
			$http.get('rest/groups/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
			});
			$rootScope.isReadOnly = true;
			$scope.setCompanyPage(1);
		};
		$scope.update = function() {
			$http.post('rest/groups/merge/', $scope.entity).success(function(data) {
				$scope.message = data;
				$rootScope.isReadOnly = true;
			});
		};
		$scope.remove = function() {
			var dlg = dialogs.confirm($filter('translate')('remove.confirmation.title') ,$filter('translate')('companyGroup.remove.confirmation'));
			dlg.result.then(function(btn){
				$http.post('rest/groups/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) {
						$location.path("groups");
					} else {
						$scope.message = data;
					}
				});				
			});
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

	billerModule.controller('GroupNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.update = function() {
			$http.post('rest/groups/merge/', $scope.entity).success(function(data) {
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("groups/id/" + data.payload.id);				
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.provinces = function(name) { return $http.get('rest/provinces/find/' + name).then(function(response) { return response.data; }); };
	} ]);

})();