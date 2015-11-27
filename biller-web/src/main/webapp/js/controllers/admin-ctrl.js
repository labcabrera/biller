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
	
	billerModule.controller('UserActivityListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', 'dialogs', function($scope, $rootScope, $routeParams, $http, $filter, dialogs) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 20;
		$scope.reset = function() {
			$scope.searchOptions = {
				'user': $routeParams.user
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("user.name=lk=", $scope.searchOptions.user);
			predicateBuilder.append("date=ge=", $scope.searchOptions.from != null ? $filter('date')($scope.searchOptions.from, "yyyy-MM-dd") : null);
			predicateBuilder.append("date=le=", $scope.searchOptions.to != null ? $filter('date')($scope.searchOptions.to, "yyyy-MM-dd") : null);
			return 'rest/user-activity/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
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
	}]);
	
	billerModule.controller('UserActivityCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
		$http.get('rest/user-activity/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
		});
	}]);
	
	billerModule.controller('SchedulerListCtrl', ['$scope', '$rootScope', '$routeParams', '$http', '$modal', '$translate', function($scope, $rootScope, $routeParams, $http, $modal, $translate) {
		$scope.searchResults = {};
		$scope.setPage  = function(page) {
			$scope.currentPage = page;
			$http.get('rest/scheduler/find').success(function(data) {
				$scope.searchResults = data;
			});
		};
		$scope.setPage(1);
		$scope.edit = function(task) {
			$scope.open(task);
		};
		$scope.open = function(task) {
			var modalInstance = $modal.open({
				templateUrl : 'html/admin/scheduler-detail.html',
				controller : 'SchedulerDetailCtrl',
				size : 'lg',
				resolve : {
					task : function() {
						return task;
					}
				}
			});
			modalInstance.result.then(function(data) {
				$scope.message = data;
				$scope.setPage(1);
			}, function() {
			});
		};
	}]);
	
	billerModule.controller('SchedulerDetailCtrl', ['$scope', '$rootScope', '$routeParams', '$http', '$modal', '$modalInstance', '$translate', 'task', function($scope, $rootScope, $routeParams, $http, $modal, $modalInstance, $translate, task) {
		$scope.task = task;
		$scope.getNextExecutions = function() {
			$http.get('rest/scheduler/nextExecutions/' + $scope.task.id).success(function(data) {
				$scope.nextExecutions = data;
			});
		};
		$scope.save = function() {
			$http.post('rest/scheduler/merge', $scope.task).success(function(data) {
				if (data.code == '200') {
					$scope.task = data.payload;
					$scope.getNextExecutions();
					// $modalInstance.close(data);
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.cancel = function() {
			$modalInstance.dismiss('cancel');
		};
		$scope.enable = function() {
			$scope.task.disabled = false;
			$http.post('rest/scheduler/resume/' +  $scope.task.id).success(function(data) {
				if (data.code == '200') {
					$scope.task = data.payload;
					$modalInstance.close(data);
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.disable = function() {
			$scope.task.disabled = true;
			$http.post('rest/scheduler/pause/' +  $scope.task.id).success(function(data) {
				if (data.code == '200') {
					$scope.task = data.payload;
					$modalInstance.close(data);
				} else {
					$scope.message = data;
				}
			});
		};
		$scope.execute = function() {
			$http.post('rest/scheduler/execute/' +  $scope.task.id).success(function(data) {
				$scope.message = data;
			});
		};
		$scope.getNextExecutions();
	}]);
	
	
})();
