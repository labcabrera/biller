(function() {
	
	var billerControllers = angular.module('billerControllers');
	
	/* ----------------------------------------------------------------------------
	 * USUARIOS
	 * ----------------------------------------------------------------------------
	 */
	
	billerControllers.controller('LoginCtrl', [ '$scope', '$rootScope', '$location', '$http', function($scope, $rootScope, $location, $http) {
		$scope.login = function(user, password) {
			var request = { "name": $scope.username, "password": $scope.password};
			$http.post('rest/users/login', request).success(function(data) {
				if(data.code == 200) {
					$rootScope.user = data.payload;
					$location.url("index");
				} else {
					$rootScope.loginResult = data.message;
				}
			});
		};
	} ]);
	billerControllers.controller('UserListCtrl', [ '$scope', '$http', function($scope, $http) {
		$http.get('rest/users').success(function(data) { $scope.users = data; });
		$scope.orderProp = 'name';
	} ]);
	billerControllers.controller('UserDetailCtrl', [ '$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
		$http.get('rest/users/' + $routeParams.id).success(function(data) { $scope.user = data; });
	} ]);
	
	/* ----------------------------------------------------------------------------
	 * EMPRESAS
	 * ----------------------------------------------------------------------------
	 */
	
	billerControllers.controller('CompanyListCtrl', [ '$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
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
			if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
			return 'rest/companies/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
		};
		$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
		$scope.reset();
		$scope.search();
	} ]);
	
	billerControllers.controller('CompanyDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
		if(messageService.hasMessage()) {
			$scope.displayAlert(messageService.getMessage());
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
				$rootScope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
					$rootScope.isReadOnly = true;
				}
			});
		};
		$scope.remove = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a eliminar la empresa')) {
				$scope.isSaving = true;
				$http.post('rest/companies/remove/' + $scope.entity.id).success(function(data) {
					$scope.isSaving = false;
					if(data.code == 200) { $location.path("companies"); } else { $scope.displayAlert(data); }
				});
			}
		};
		$scope.addStore = function() {
			$scope.newStore.parent = $scope.entity;
			$scope.isSaving = true;
			$http.post('rest/stores/merge', $scope.newStore).success(function(data) {
				$scope.isSaving = false;
				$rootScope.displayAlert(data);
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
	
	billerControllers.controller('CompanyNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
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
					$scope.displayAlert(data);
				}
			});
		};
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	} ]);
	
	/* ----------------------------------------------------------------------------
	 * TITULARES
	 * ----------------------------------------------------------------------------
	 */
	billerControllers.controller('OwnerListCtrl', [ '$scope', '$http', function($scope, $http) {
		$scope.currentPage = 1;
		$scope.reset = function() {
			$scope.searchOptions = {
				'name': '',
				'idCardNumber': '',
				'showDeleted': false
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			if($scope.searchOptions.name != null && $scope.searchOptions.name != '') {
				var name = $scope.searchOptions.name;
				var key = "(name=lk=" + name + ",firstSurname=lk=" + name + ",secondSurname=lk=" + name + ")";
				predicateBuilder.appendKey(key);
			}
			predicateBuilder.append("idCard.number=lk=", $scope.searchOptions.idCardNumber);
			if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
			return 'rest/owners/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.setPage = function(page) {
			$scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
		};
		$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
		$scope.reset();
		$scope.search();
	} ]);
	
	billerControllers.controller('OwnerDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
		if(messageService.hasMessage()) {
			$scope.displayAlert(messageService.getMessage());
		}
		$scope.load = function() {
			$http.get('rest/owners/id/' + $routeParams.id).success(function(data) { $scope.entity = data; });
			$rootScope.isReadOnly = true;
			$scope.setStorePage(1);
		};
		$scope.reset = function() { $scope.load(); };
		$scope.update = function() {
			$scope.isSaving = true;
			$http.post('rest/owners/merge/', $scope.entity).success(function(data) {
				$scope.isSaving = false;
				$scope.displayAlert(data);
				if(data.code == 200) {
					$rootScope.isReadOnly = true;				
					$scope.message = data.payload;
				}
			});
		};
		$scope.remove = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a eliminar el titular')) {
				$http.post('rest/owners/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) { $location.path("owners"); } else { $scope.displayAlert(data); }
				});
			}
		};
		$scope.setStorePage = function(page) {
		    $scope.currentPage = page;
		    $http.get('rest/stores/find?q=owner.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
		};
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
		$scope.load();
	} ]);
	
	billerControllers.controller('OwnerNewCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.reset = function() {};
		$scope.update = function() {
			$http.post('rest/owners/merge/', $scope.entity).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("owners/id/" + data.payload.id);				
				}
			});
		};
		$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
		$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	}]);
	
	/* ----------------------------------------------------------------------------
	 * CENTROS DE COSTE
	 * ----------------------------------------------------------------------------
	 */
	billerControllers.controller('CostCenterListCtrl', [ '$scope', '$http', function($scope, $http) {
		$scope.currentPage = 1;
		$scope.reset = function() {
			$scope.searchOptions = {
				'name': '',
				'code': '',
				'showDeleted': false
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("name=lk=", $scope.searchOptions.name);
			predicateBuilder.append("code=lk=", $scope.searchOptions.code);
			if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
			return 'rest/costcenters/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.setPage = function(page) {
			$scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
		};
		$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
		$scope.reset();
		$scope.search();
	} ]);
	
	
	billerControllers.controller('CostCenterDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
		if(messageService.hasMessage()) {
			$scope.displayAlert(messageService.getMessage());
		}
		$scope.load = function() {
			$http.get('rest/costcenters/id/' + $routeParams.id).success(function(data) { $scope.entity = data; });
			$rootScope.isReadOnly = true;
			$scope.setStorePage(1);
		};
		$scope.reset = function() { $scope.load(); };
		$scope.update = function() {
			$http.post('rest/costcenters/merge/', $scope.entity).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$rootScope.isReadOnly = true;				
					$scope.message = data.payload;
				}
			});
		};
		$scope.remove = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a eliminar el centro de coste')) {
				$http.post('rest/costcenters/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) { $location.path("costcenters"); } else { $scope.displayAlert(data); }
				});
			}
		};
		$scope.setStorePage = function(page) {
		    $scope.currentPage = page;
		    $http.get('rest/stores/find?q=costCenter.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
		};
		$scope.load();
	} ]);
	
	billerControllers.controller('CostCenterNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.update = function() {
			$http.post('rest/costcenters/merge/', $scope.entity).success(function(data) {
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("costcenters/id/" + data.payload.id);				
				} else {
					$scope.displayAlert(data);
				}
			});
		};
		$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
	} ]);
	
	/* ----------------------------------------------------------------------------
	 * TERMINALES
	 * ----------------------------------------------------------------------------
	 */
	billerControllers.controller('TerminalListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
		$scope.currentPage = 1;
		$scope.searchName = '';
		$scope.reset = function() {
			$scope.searchOptions = {
				'terminal': { 'code': ''},
				'showOrphan' : false,
				'showDeleted': false,
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("code=lk=", $scope.searchOptions.terminal.code);			
			if($scope.searchOptions.showOrphan) { predicateBuilder.appendKey("store=n="); }
			if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
			return 'rest/terminals/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
			$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
		};
		$scope.reset();
		$scope.search();
	} ]);
	
	billerControllers.controller('TerminalDetailCtrl', [ '$scope', '$rootScope', '$location', '$routeParams', '$http', 'messageService', function($scope, $rootScope, $location, $routeParams, $http, messageService) {
		if(messageService.hasMessage()) {
			$scope.displayAlert(messageService.getMessage());
		}
		$scope.load = function() {
			$http.get('rest/terminals/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
		});};
		$scope.update = function() {
			$http.post('rest/terminals/merge/', $scope.entity).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
					$rootScope.isReadOnly = true;				
				}
			});
		};
		$scope.remove = function() {
			if($rootScope.autoconfirm || window.confirm('Se va a eliminar el terminal')) {
				$http.post('rest/terminals/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) { $location.path("terminals"); } else { $scope.displayAlert(data); }
				});
			}
		};
		$scope.$watch('entity.store', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.store = null; } });
		$scope.load();
	} ]);
	
	billerControllers.controller('TerminalNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
		$scope.isReadOnly = false;
		$scope.update = function() {
			$http.post('rest/terminals/merge/', $scope.entity).success(function(data) {
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("terminals/id/" + data.payload.id);				
				} else {
					$scope.displayAlert(data);
				}
			});
		};
		$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
	} ]);
	
	/* ----------------------------------------------------------------------------
	 * CONFIGURACION DE LA APLICACION
	 * ----------------------------------------------------------------------------
	 */
	billerControllers.controller('SettingsCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
		$scope.load = function() {
			$http.get('rest/settings/id/MAIL').success(function(data) { $scope.mailSettings = data; });
			$http.get('rest/settings/id/SYSTEM').success(function(data) { $scope.systemSettings = data; });
			$http.get('rest/settings/id/BILLING').success(function(data) { $scope.billingSettings = data; });
		};
		$scope.load();
	}]);
	
	billerControllers.controller('AdminCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', 'dialogs', function($scope, $rootScope, $routeParams, $http, dialogs) {
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
		}
	}]);

})();
