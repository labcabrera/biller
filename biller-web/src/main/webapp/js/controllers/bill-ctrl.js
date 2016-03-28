(function() {
	
	var billerModule = angular.module('billerModule');

	/* ----------------------------------------------------------------------------
	 * LISTADO DE FACTURAS
	 * ----------------------------------------------------------------------------
	 */
	billerModule.controller('BillListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', 'messageService', function($scope, $rootScope, $routeParams, $http, $filter, messageService) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 20;
		$scope.reset = function() {
			$scope.searchOptions = {
				'code': $routeParams.code,
				'store': { "id": $routeParams.store, "name": '' },
				'company': { "id": $routeParams.company, "name": '' },
				'model': { "id": $routeParams.model},
				'state': $routeParams.state,
				'from': '',
				'to':  ''
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("code=lk=", $scope.searchOptions.code);
			predicateBuilder.append("sender.id==", $scope.searchOptions.store.id);
			predicateBuilder.append("receiver.id==", $scope.searchOptions.company.id);
			predicateBuilder.append("currentState.stateDefinition.id==", $scope.searchOptions.state);
			predicateBuilder.append("model.id==", $scope.searchOptions.model != null ? $scope.searchOptions.model.id : null);
			predicateBuilder.append("dateFrom=ge=", $scope.searchOptions.from != null ? $filter('date')($scope.searchOptions.from, "yyyy-MM-dd") : null);
			predicateBuilder.append("dateTo=le=", $scope.searchOptions.to != null ? $filter('date')($scope.searchOptions.to, "yyyy-MM-dd") : null);
			return 'rest/bills/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			messageService.setMessage(null);
			$scope.setPage(1);
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
		    $scope.results = null;
		    $scope.searchMessage = "Loading...";
		    $http.get($scope.getSearchUrl()).success(function(data) {
		    	$scope.results = data;
		    	$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
		    });
		};
		$scope.reset();
		$scope.search();
	} ]);

	/* ----------------------------------------------------------------------------
	 * DETALLE DE FACTURAS
	 * ----------------------------------------------------------------------------
	 */
	billerModule.controller('BillDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$location', '$http', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $location, $http, dialogs, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.load = function() {
			$http.get('rest/bills/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
				$scope.billLiquidationDetail = {
						"bill": { "id": $scope.entity.id },
						"concept": "MANUAL",
						"liquidationIncluded": true
				};
				$scope.billDetail = {
						"bill": { "id": $scope.entity.id },
						"concept": "MANUAL"
				};
			});
			$rootScope.isReadOnly = true;
		};
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/bills/merge/', $scope.entity).success(function(data) {
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
				var dlg = dialogs.confirm('Confirmacion de borrado','Desea eliminar la factura?');
				dlg.result.then(function(btn){
					$scope.isSaving = true;
					$http.post('rest/bills/remove/' + $scope.entity.id).success(function(data) {
						$scope.isSaving = false;
						if(data.code == 200) {
							$location.path("bills");
						} else {
							$scope.message = data;
						};
					});
				});
			}
		};
		$scope.editLiquidationDetail = function(id) {
			if(id != null && !(typeof id === 'undefined') ) {
				$scope.isSaving = true;
				$http.get('rest/bills/detail/liquidation/id/' + id).success(function(data) {
					$scope.isSaving = false;
					$scope.billLiquidationDetail = data;
					$scope.billLiquidationDetail.bill = { "id": $scope.entity.id };
				});	
			} else {
				var d = $scope.billLiquidationDetail;
				d.id = d.units = d.value = d.name = d.dummyType = null;
				d.liquidationIncluded = true;
			}
			$('#editBillLiquidationConceptModal').modal('show');			
		};
		$scope.editDetail = function(id) {
			$scope.billDetail.id = $scope.billDetail.value = $scope.billDetail.name = $scope.billDetail.units = null;
			if(id != null && !(typeof id === 'undefined') ) {
				$scope.isSaving = true;
				$http.get('rest/bills/detail/id/' + id).success(function(data) {
					$scope.isSaving = false;
					$scope.billDetail = data;
					$scope.billDetail.bill = { "id": $scope.entity.id };
				});	
			}
			$('#editBillConceptModal').modal('show');			
		};
		$scope.confirm = function() {
			var dlg = dialogs.confirm('Confirmacion de aceptacion','Se va a aceptar la factura');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/bills/confirm/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			});
		};
		$scope.cancel = function() {
			var dlg = dialogs.confirm('Confirmacion','Se va a cancelar la factura');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/bills/cancel/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			});
		};
		$scope.rectify = function() {
			var dlg = dialogs.confirm('Confirmacion de rectificacion','Se va a rectificar la factura');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/bills/rectify/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						messageService.setMessage(data);
						$location.path("bills/id/" + data.payload.id);
					};
				});
			});
		};
		$scope.draft = function() {
			var dlg = dialogs.confirm('Confirmacion','La factura pasara a estado borrador');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/bills/draft/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					$scope.message = data;
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			});
		};
		$scope.recalculate = function() {
			var dlg = dialogs.confirm('Confirmacion','Desea recalcular la factura? Los ajustes manuales se perderan');
			dlg.result.then(function(btn){
				$scope.isSaving = true;
				$http.post('rest/bills/recalculate/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;	
					$scope.message = data;
					if(data.code == 200) {
						$scope.entity = data.payload;
					}
				});
			});
		};
		$scope.editSendMail = function(option) {
			$scope.sendMail.option = option;
			$('#sendMailModal').modal('show');
		};
		$scope.sendMail = function() {
			$scope.isSaving = true;
			$http.post('rest/bills/send/' + $scope.sendMail.option + '/' + $scope.entity.id, $scope.sendMail.value).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				$('#sendMailModal').modal('hide');
			});
		};
		$scope.load();
	} ]);

})();