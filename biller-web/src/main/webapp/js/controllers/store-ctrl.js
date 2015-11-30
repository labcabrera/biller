(function() {

	var billerModule = angular.module('billerModule');

	billerModule.controller('StoreListCtrl', [ '$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 15;
		$scope.reset = function() {
			$scope.searchOptions = {
				'name': '',
				'province': {"id": null, 'name': ''},
				'company': {"id": null, 'name': ''},
				'owner': {"id": null, 'name': ''},
				'costCenter': {"id": null, 'name': ''},
				'type': '',
				'showDeleted': false,
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("name=lk=", $scope.searchOptions.name);
			predicateBuilder.append("address.province.id==", $scope.searchOptions.province != null ? $scope.searchOptions.province.id : null);
			predicateBuilder.append("parent.id==", $scope.searchOptions.company != null ? $scope.searchOptions.company.id : null);
			predicateBuilder.append("costCenter.id==", $scope.searchOptions.costCenter != null ? $scope.searchOptions.costCenter.id : null);
			predicateBuilder.append("owner.id==", $scope.searchOptions.owner != null ? $scope.searchOptions.owner.id : null);
			predicateBuilder.append("type==", $scope.searchOptions.type);
			if(!$scope.searchOptions.showDeleted) {
				predicateBuilder.appendKey("auditData.deleted=n=");
			} else {
				predicateBuilder.appendKey("auditData.deleted=!n=");
				
			}
			return 'rest/stores/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
		    $scope.search();
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

	billerModule.controller('StoreDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, dialogs, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.load = function() {
			$http.get('rest/stores/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
				$http.get('rest/terminals/find?q=store.id==' + $routeParams.id).success(function(data) { $scope.childTerminals = data.results; });
			});
			$rootScope.isReadOnly = true;
		};
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/stores/merge/', $scope.entity).success(function(data) {
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
					$http.post('rest/stores/remove/' + $scope.entity.id).success(function(data) {
						$scope.isSaving = false;
						if(data.code == 200) {
							$location.path("stores");
						} else {
							$scope.message = data;
						}
					});
				});
			});
		};
		$scope.addTerminal = function() {
			var current = $scope.newTerminal.store != null ? $scope.newTerminal.store.name : null;
			if(current == null || ($rootScope.autoconfirm || window.confirm('El terminal esta actualmente asociado con la empresa ' + current))) {
				$scope.newTerminal.store = $scope.entity;
				$scope.isSaving = true;
				$http.post('rest/terminals/merge', $scope.newTerminal).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						$scope.load();
						$scope.newTerminal = null;
						$("#addTerminalModal").modal('hide');
					}
				});
			}
		};
		$scope.removeTerminal = function(data) {
			data.store = null;
			$scope.isSaving = true;
			$http.post('rest/terminals/merge', data).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$scope.load();
				}
			});
		};
		$scope.$watch('entity.owner', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.owner = null; } });
		$scope.$watch('entity.parent', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.parent = null; } });
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
		$scope.$watch('entity.billingModel', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.billingModel = null; } });
		$scope.load();
	} ]);

	billerModule.controller('StoreNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.reset = function() { };
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/stores/merge/', $scope.entity).success(function(data) {
				$scope.message = data;
				$scope.isSaving = false;
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("stores/id/" + data.payload.id);				
				}
			});
		};
		$scope.$watch('entity.owner', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.owner = null; } });
		$scope.$watch('entity.parent', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.parent = null; } });
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
		$scope.$watch('entity.billingModel', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.billingModel = null; } });
	} ]);

})();