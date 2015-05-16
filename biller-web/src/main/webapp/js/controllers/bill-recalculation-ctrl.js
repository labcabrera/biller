(function() {
	
	var billerModule = angular.module('billerModule');
	

	billerModule.filter('offset', function() {
		return function(input, start) {
			start = parseInt(start, 10);
			return input != null ? input.slice(start) : [];
		};
	});
	
	billerModule.directive('billRecalculationInfo', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/bills/bill-recalculation-info.html',
			controller : ['$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
				$scope.init = function() {
					$scope.itemsPerPage = 10;
					$scope.pageCurrentBills = 0;
					$scope.pageNonExistingBills = 0;
				};
				$scope.setCurrentBillsPage = function(value) {
					$scope.pageCurrentBills = value;
				};
				$scope.setNonExistingBillsPage = function(value) {
					$scope.pageNonExistingBills = value;
				};
				$scope.init();
			}],
			require : '^ngModel',
			replace : 'true',
			scope : {
				entity : '=ngModel'
			}
		};
	});
	
	billerModule.controller('BillRecalculationCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
		$scope.init = function() {
			$scope.modes = [
				{ id: 'byStore', label:'Por establecimiento' },
				{ id: 'byCompany', label:'Por operador' },
				{ id: 'allBills', label:'Todos' }
			];
			$scope.months = [
				{ id: '1', label: "Enero"}, { id: '2', label: "Febrero"}, { id: '3', label: "Marzo"}, { id: '4', label: "Abril"},
				{ id: '5', label: "Mayo"}, { id: '6', label: "Junio"}, { id: '7', label: "Julio"}, { id: '8', label: "Agosto"},
				{ id: '9', label: "Septiembre"}, { id: '10', label: "Octubre"}, { id: '11', label: "Noviembre"}, { id: '12', label: "Diciembre"},
			];
			$scope.options = { mode: $scope.modes[0]};
		};
		$scope.prepareBill = function() {
			$http.get(REST_PATH + '/recalculation/prepare/bill', {
				params: {
					y: $scope.options.year,
					m: $scope.options.month != null ? $scope.options.month.id : null,
					s: $scope.options.store != null && $scope.options.mode.id == 'byStore' ? $scope.options.store.id : null,
					c: $scope.options.company != null && $scope.options.mode.id == 'byCompany' ? $scope.options.company.id : null
					}
			}).success(function(data) {
				$scope.message = data;
				$scope.prepareResult = data.payload;
			});
		};
		$scope.cancelBillRecalculation = function() {
			$scope.prepareResult = null;
		};
		$scope.executeBillRecalculation = function() {
			var dlg = dialogs.confirm('Confirmacion','Desea recalcular la factura? Los ajustes manuales se perderan');
			dlg.result.then(function(btn){
				$http.post(REST_PATH + '/recalculation/execute/bill', $scope.prepareResult
				).success(function(data) {
					$scope.message = data;
					$scope.results = data.payload;
				});
			});			
		};
		
		
		
		$scope.recalculateBill = function() {
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
		$scope.init();
	}]);
	
})();
