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
			$scope.currentPage = 1;
			$http.get($scope.getSearchUrl()).success(function(data) {
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
						"bill": {"id": $scope.entity.id },
						"concept": "MANUAL",
						"liquidationIncluded": true
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
		$scope.editLiquidationDetail = function(data) {
			if(data != null && !(typeof data === 'undefined') ) {
				$scope.isSaving = true;
				$http.get('rest/bills/liquidation/detail/id/' + data).success(function(data) {
					$scope.isSaving = false;
					$scope.billLiquidationDetail = data;
					$scope.billLiquidationDetail.bill = { "id": $scope.entity.id };
					$('#editBillLiquidationConceptModal').modal('show');
				});	
			} else {
				//$scope.billLiquidationDetail = $scope.billLiquidationDetail ? $scope.billLiquidationDetail : {};
				//$scope.billLiquidationDetail.id = $scope.entity.id;
				$('#editBillLiquidationConceptModal').modal('show');			
			};
		};
		$scope.mergeLiquidationDetail = function() {
			console.log("mergeLiquidationDetail");
			$scope.isSaving = true;
			$http.post('rest/bills/detail/liquidation/merge/', $scope.billLiquidationDetail).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				$("#editBillLiquidationConceptModal").modal('hide');
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		};
		$scope.editDetail = function(data) {
			if(data != null && !(typeof data === 'undefined') ) {
				$scope.isSaving = true;
				$http.get('rest/bills/detail/id/' + data).success(function(data) {
					$scope.isSaving = false;
					$scope.billDetail = data;
					$scope.billDetail.bill = { "id": $scope.entity.id };
					$('#editBillConceptModal').modal('show');
				});	
			} else {
				$scope.billDetail = { "bill" : { "id" : $scope.entity.id }, "value":"", "units":"" };
				$('#editBillConceptModal').modal('show');			
			};
		};
		$scope.mergeDetail = function(data) {
			$scope.isSaving = true;
			$http.post('rest/bills/detail/merge/', $scope.billDetail).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				$("#editBillConceptModal").modal('hide');
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		};
		$scope.removeDetail = function(data) {
			$scope.isSaving = true;
			$http.post('rest/bills/detail/remove/' + data).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
				$("#editBillConceptModal").modal('hide');
			});
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
		$scope.editSendMail = function() { $('#sendMailModal').modal('show'); };
		$scope.sendMail = function() {
			$scope.isSaving = true;
			$http.post('rest/bills/send/' + $scope.entity.id, $scope.sendMail.value).success(function(data) {
				$scope.isSaving = false;
				$scope.message = data;
				$('#sendMailModal').modal('hide');
			});
		};
		$scope.load();
	} ]);

})();