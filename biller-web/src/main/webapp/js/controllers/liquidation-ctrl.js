(function() {
	
	var billerModule = angular.module('billerModule');

	/* ----------------------------------------------------------------------------
	 * LISTADO DE LIQUIDACIONES
	 * ----------------------------------------------------------------------------
	 */
	billerModule.controller('LiquidationListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', function($scope, $rootScope, $routeParams, $http, $filter) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 15;
		$scope.reset = function() {
			$scope.searchOptions = {
				'code': $routeParams.code,
				'company': { "id": $routeParams.company, "name": '' },
				'costCenter': { "id": $routeParams.costcenter, "name": '' },
				'state': $routeParams.state,
				'from': '',
				'to':  ''
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("code==", $scope.searchOptions.code);
			predicateBuilder.append("sender.id==", $scope.searchOptions.company != null ? $scope.searchOptions.company.id : null);
			predicateBuilder.append("receiver.id==", $scope.searchOptions.trader != null ? $scope.searchOptions.trader.id : null);
			predicateBuilder.append("dateFrom=ge=", $scope.searchOptions.from != null ? $filter('date')($scope.searchOptions.from, "yyyy-MM-dd") : null);
			predicateBuilder.append("dateTo=le=", $scope.searchOptions.to != null ? $filter('date')($scope.searchOptions.to, "yyyy-MM-dd") : null);
			predicateBuilder.append("currentState.stateDefinition.id==", $scope.searchOptions.state);
			return 'rest/liquidations/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
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
		    $scope.search();
		};
		$scope.reset();
		$scope.search();
	} ]);

	/* ----------------------------------------------------------------------------
	 * DETALLE DE LIQUIDACION
	 * ----------------------------------------------------------------------------
	 */
	billerModule.controller('LiquidationDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', '$filter', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, $filter, dialogs, messageService) {
		$scope.load = function() {
			$http.get('rest/liquidations/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
				$http.get('rest/bills/find?q=liquidation.id==' + $routeParams.id + "&n=15").success(function(data) {
					$scope.childs = data;
				});
				$scope.liquidationDetail = {
					"liquidation": { "id": $scope.entity.id },
					"concept": "MANUAL",
					"liquidationIncluded": true
				};
			});
			$rootScope.isReadOnly = true;
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
		    $http.get('rest/bills/find?q=liquidation.id==' + $routeParams.id + "&n=15" + "&p=" + page).success(function(data) {
		    	$scope.childs = data;
		    });
		};
		$scope.confirm = function() {
			var dlg = dialogs.confirm('Confirmacion','Se va a aceptar la liquidacion');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/liquidations/confirm/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			});
		};
		$scope.confirmPendingBills = function() {
			var dlg = dialogs.confirm('Confirmacion','Se van a aceptar todas las facturas pendientes de la liquidacion');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/liquidations/bills/confirm/' + $scope.entity.id).success(function(data) {
					$scope.message= data;
					if(data.code == 200) {
						$http.get('rest/bills/find?q=liquidation.id==' + $routeParams.id + "&n=15").success(function(data) {
							$scope.childs = data;
						});
					}
					$scope.isSaving = false;
				});
			});
		};
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/liquidations/merge/', $scope.entity).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$rootScope.isReadOnly = true;				
					$scope.entity = data.payload;
				}
			});
		};
		$scope.remove = function() {
			if(!$rootScope.isReadOnly) {
				var dlg = dialogs.confirm('Confirmacion de borrado','Desea eliminar la liquidacion?');
				dlg.result.then(function(btn){
					$scope.isSaving = true;
					$http.post('rest/liquidations/remove/' + $scope.entity.id).success(function(data) {
						$scope.isSaving = false;
						if(data.code == 200) {
							$location.path("liquidations");
						} else {
							$scope.message = data;
						}
					});
				});
			}
		};
		$scope.editDetail = function(data) {
			if(data != null && !(typeof data === 'undefined') ) {
				$scope.isSaving = true;
				$http.get('rest/liquidations/detail/id/' + data).success(function(data) {
					$scope.isSaving = false;
					$scope.liquidationDetail = data;
					$scope.liquidationDetail.liquidation = { "id": $scope.entity.id };
				});	
			} else {
				var d = $scope.liquidationDetail;
				d.id = d.units = d.value = d.name = d.dummyType = null;
				d.liquidationIncluded = true;
			}
			$('#editLiquidationConceptModal').modal('show');
		};
		$scope.recalculate = function() {
			var dlg = dialogs.confirm('Confirmacion','Desea recalcular la liquidacion? Los ajustes manuales se perderan');
			dlg.result.then(function(btn){
				$scope.message = {code: 200, info: [ $filter('translate')('recalculatingLiquidation') ]};
				$scope.isSaving = true;
				$http.post('rest/liquidations/recalculate/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						messageService.setMessage(data);
						$location.path("liquidations/id/" + data.payload.id);
					}
				});
			});
		};
		$scope.recreatePDF = function() {
			var dlg = dialogs.confirm('Confirmacion','Desea regenerar el PDF asociado a la liquidacion?');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/liquidations/pdf/recreate/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			});
		};
		$scope.draft = function() {
			var dlg = dialogs.confirm('Confirmacion','La liquidacion volvera a estado borrador');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/liquidations/draft/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			});
		};
		$scope.editSendMail = function() { $('#sendMailModal').modal('show'); };
		$scope.sendMail = function() {
			$('#sendMailModal').modal('hide');
			$scope.message = {code: 200, info: [ $filter('translate')('sendingMail') ]};
			$http.post('rest/liquidations/send/' + $scope.entity.id, $scope.sendMail.value).success(function(data) {
				$scope.message = data;
			});
		};
		$scope.load();
	} ]);
	
})();