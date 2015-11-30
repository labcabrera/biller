(function() {
	
	var billerModule = angular.module('billerModule');
	
	/* ----------------------------------------------------------------------------
	 * MODELOS DE FACTURACION
	 * ----------------------------------------------------------------------------
	 */
	billerModule.controller('ModelListCtrl', [ '$scope', '$rootScope', '$http', 'messageService', function($scope, $rootScope, $http, messageService) {
		if(messageService.hasMessage()) {
			$scope.displayAlert(messageService.getMessage());
		}
		$scope.currentPage = 1;
		$scope.reset = function() {
			$scope.searchOptions = {
				'name': '',
				'showDeleted': false
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
			return 'rest/models/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
		    $scope.search();
		};
		$scope.reset();
		$scope.search();
	} ]);
	
	/** Detalle de modelo de facturacion */
	billerModule.controller('ModelDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location','dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, dialogs, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.load = function() {
			$rootScope.isReadOnly = true;
			$http.get('rest/models/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
				$http.get('rest/stores/find?q=billingModel.id==' + $routeParams.id).success(function(data) {
					$scope.childs = data;
				});
			});
		};
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/models/merge', $scope.entity).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$rootScope.isReadOnly = true;
					$scope.entity = data.payload;
				}
			});
		};
		$scope.remove = function() {
			var dlg = dialogs.confirm('Confirmacion de borrado','Desea eliminar el modelo de facturacion?');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/models/remove/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.displayAlert(data);
					if(data.code == 200) {
						messageService.setMessage(data);
						$location.path("models");
					}
				});
			});
		};
		$scope.setStorePage = function(page) {
		    $scope.currentPage = page;
		    $http.get('rest/stores/find?q=billingModel.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
		};
		$scope.mergeRappel = function() {
			$scope.isSaving = true;
			$http.post('rest/models/rappel/merge', $scope.newRappel).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$rootScope.isReadOnly = true;
					$scope.entity = data.payload;
					$('#addRappelModal').modal('hide');
				}
			});
		};
		$scope.removeRappel = function() {
			$scope.isSaving = true;
			$http.post('rest/models/rappel/remove/' + $scope.newRappel.id).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$rootScope.isReadOnly = true;
					$scope.entity = data.payload;
					$('#addRappelModal').modal('hide');
				}
			});
			
		};
		$scope.editRappel = function(rappelId) {
			if(rappelId > 0) {
				$scope.isSaving = true;
				$http.get('rest/models/rappel/id/' + rappelId).success(function(data) {
					$scope.isSaving = false;
					$scope.newRappel = data;
					$scope.newRappel.model = { 'id': $scope.entity.id};
				});
			} else {
				$scope.newRappel = { amount: '0', bonusAmount: '0', model: { 'id': $scope.entity.id } };
			}
			$('#addRappelModal').modal('show');
		};
		$scope.load();
	} ]);
	
	/**
	 * Nuevo modelo de facturacion.
	 */
	billerModule.controller('ModelNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/models/merge/', $scope.entity).success(function(data) {
				$scope.isSaving = false;
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("models/id/" + data.payload.id);				
				} else {
					$scope.displayAlert(data);
				}
			});
		};
		$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
	} ]);
	
	/* ----------------------------------------------------------------------------
	 * RAPPEL DE ESTABLECIMIENTOS
	 * ----------------------------------------------------------------------------
	 */
	billerModule.controller('RappelStoreListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', function($scope, $rootScope, $routeParams, $http, $filter) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 15;
		$scope.reset = function() {
			$scope.searchOptions = {
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			return 'rest/rappel/stores/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
		    $scope.search();
		};
		$scope.reset();
		$scope.search();
	} ]);
	
	billerModule.controller('RappelStoreDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
		$scope.load = function() {
			$http.get('rest/rappel/stores/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
			});
			$rootScope.isReadOnly = true;
		};
		$scope.confirm = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a aceptar el rappel')) {
				$http.post('rest/rappel/stores/confirm/' + $scope.entity.id).success(function(data) {
					$scope.displayAlert(data);
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			}
		};
		$scope.load();
	} ]);

})();