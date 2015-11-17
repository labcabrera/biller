(function() {
	
	var billerModule = angular.module('billerModule');
	
	/**
	 * Configuracion de la aplicación.
	 */
	billerModule.controller('SettingsCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
		$scope.load = function() {
			$http.get('rest/settings/id/MAIL').success(function(data) { $scope.mailSettings = data; });
			$http.get('rest/settings/id/SYSTEM').success(function(data) { $scope.systemSettings = data; });
			$http.get('rest/settings/id/BILLING').success(function(data) { $scope.billingSettings = data; });
		};
		$scope.load();
	}]);
	
	billerModule.controller('AdminCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
		$scope.recalculateBill = function() {
			var dlg = dialogs.confirm('Confirmacion','Desea recalcular la factura? Los ajustes manuales se perderan');
			dlg.result.then(function(btn){
				var billYear = $scope.billYear;
				var billMonth = $scope.billMonth;
				var billStore = $scope.billStore != null ? $scope.billStore.id : '';
				$scope.displayAlert({ 'code': 200, 'message': 'Recalculando facturas... El proceso puede durar varios minutos'});
				$http.post('rest/admin/recalculate/bill/' + billStore + '/' + billYear + "/" + billMonth).success(function(data) {
					$scope.displayAlert(data);
				});
			});
		};
		$scope.recalculateAllBills = function() {
			var dlg = dialogs.confirm('Confirmacion','Desea recalcular todas las facturas? Los ajustes manuales se perderan');
			dlg.result.then(function(btn){
				var billYear = $scope.billYear;
				var billMonth = $scope.billMonth;
				$scope.displayAlert({ 'code': 200, 'message': 'Recalculando facturas... El proceso puede durar varios minutos'});
				$http.post('rest/admin/recalculate/bills/' + billYear + "/" + billMonth).success(function(data) {
					$scope.displayAlert(data);
				});
			});
		};
		$scope.recalculateLiquidation = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a recalcular la liquidacion')) {
				var liquidationYear = $scope.liquidationYear;
				var liquidationMonth = $scope.liquidationMonth;
				var liquidationCompany = $scope.liquidationCompany != null ? $scope.liquidationCompany.id : '';
				$scope.displayAlert({ 'code': 200, 'message': 'Recalculando facturas... El proceso puede durar varios minutos'});
				$http.post('rest/admin/recalculate/liquidation/' + liquidationCompany + '/' + liquidationYear + "/" + liquidationMonth).success(function(data) {
					$scope.displayAlert(data);
				});
			}
		};
		$scope.recalculateAllLiquidations = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a recalcular todas las liquidaciones')) {
				var liquidationYear = $scope.liquidationYear;
				var liquidationMonth = $scope.liquidationMonth;
				var liquidationCompany = $scope.liquidationCompany != null ? $scope.liquidationCompany.id : '';
				$scope.displayAlert({ 'code': 200, 'message': 'Recalculando facturas... El proceso puede durar varios minutos'});
				$http.post('rest/admin/recalculate/liquidations/' + liquidationYear + "/" + liquidationMonth).success(function(data) {
					$scope.displayAlert(data);
				});
			}
		};
	}]);
	
	billerModule.controller('UserActivityListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 20;
		$scope.reset = function() {
			$scope.searchOptions = {
//				'code': $routeParams.code,
//				'store': { "id": $routeParams.store, "name": '' },
//				'company': { "id": $routeParams.company, "name": '' },
//				'model': { "id": $routeParams.model},
//				'state': $routeParams.state,
//				'from': '',
//				'to':  ''
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
//			predicateBuilder.append("code=lk=", $scope.searchOptions.code);
//			predicateBuilder.append("sender.id==", $scope.searchOptions.store.id);
//			predicateBuilder.append("receiver.id==", $scope.searchOptions.company.id);
//			predicateBuilder.append("currentState.stateDefinition.id==", $scope.searchOptions.state);
//			predicateBuilder.append("model.id==", $scope.searchOptions.model != null ? $scope.searchOptions.model.id : null);
//			predicateBuilder.append("dateFrom=ge=", $scope.searchOptions.from != null ? $filter('date')($scope.searchOptions.from, "yyyy-MM-dd") : null);
//			predicateBuilder.append("dateTo=le=", $scope.searchOptions.to != null ? $filter('date')($scope.searchOptions.to, "yyyy-MM-dd") : null);
			return 'rest/user-activity/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			$scope.currentPage = 1;
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.results = data;
			});
		};
		$scope.reset();
		$scope.search();
	}]);
	
	billerModule.controller('UserActivityCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
		$http.get('rest/user-activity/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
		});
	}]);
	
	
	
})();
