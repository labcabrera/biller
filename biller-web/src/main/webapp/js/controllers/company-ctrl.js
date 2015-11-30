(function() {
	
	var billerModule = angular.module('billerModule');
	
	billerModule.controller('CompanyListCtrl', [ '$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 15;
		$scope.reset = function() {
			$scope.searchOptions = {
				'name': '',
				'province': { "id": null, "name": ''},
				'showDeleted': false
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("name=lk=", $scope.searchOptions.name);
			predicateBuilder.append("address.province.id==", $scope.searchOptions.province != null ? $scope.searchOptions.province.id : null);
			if(!$scope.searchOptions.showDeleted) {
				predicateBuilder.appendKey("auditData.deleted=n=");
			} else {
				predicateBuilder.appendKey("auditData.deleted=!n=");
			}
			return 'rest/companies/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.results = data;
			});
		};
		$scope.search = function() {
			$scope.searchMessage = 'Loading...';
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.results = data;
				$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
			});
		};
		$scope.reset();
		$scope.search();
	} ]);
	
	billerModule.controller('CompanyDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, dialogs, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.regions = function(name) {
			var url = "rest/regions/find/" + name + (angular.isDefined($scope.entity.address.province.id) ? '?province=' + $scope.entity.address.province.id : '');
			return $http.get(url).then(function(response) { return response.data; });
		};
		$scope.load = function() {
			$http.get('rest/companies/id/' + $routeParams.id).success(function(data) {
				$rootScope.isReadOnly = true;
				$scope.entity = data;
				$scope.setStorePage(1);
			});
		};
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/companies/merge/', $scope.entity).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$scope.entity = data.payload;
					$rootScope.isReadOnly = true;
				}
			});
		};
		$scope.remove = function() {
			var dlg = dialogs.confirm('Confirmacion','Se va a eliminar la empresa');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/liquidations/confirm/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = true;
					$http.post('rest/companies/remove/' + $scope.entity.id).success(function(data) {
						$scope.isSaving = false;
						if(data.code == 200) {
							$location.path("companies");
						} else {
							$scope.message = data;
						}
					});
				});
			});
		};
		$scope.addStore = function() {
			$scope.newStore.parent = $scope.entity;
			$scope.isSaving = true;
			$http.post('rest/stores/merge', $scope.newStore).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$scope.load();
					$scope.newStore = null;
					$("#addStoreModal").modal('hide');
				}
			});
		};
		$scope.setStorePage = function(page) {
		    $scope.currentPage = page;
		    $http.get('rest/stores/find?q=parent.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
		};
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
		$scope.load();
	} ]);
	
	billerModule.controller('CompanyNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.reset = function() { };
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/companies/merge/', $scope.entity).success(function(data) {
				$scope.isSaving = false;
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("companies/id/" + data.payload.id);				
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	} ]);

})();