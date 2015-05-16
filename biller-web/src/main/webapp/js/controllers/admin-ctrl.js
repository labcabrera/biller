(function() {
	
	var billerModule = angular.module('billerModule');
	
	/* ----------------------------------------------------------------------------
	 * CONFIGURACION DE LA APLICACION
	 * ----------------------------------------------------------------------------
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
		$scope.load = function() {
		};
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
	
})();
