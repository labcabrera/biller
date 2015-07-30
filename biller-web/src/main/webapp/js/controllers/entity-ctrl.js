(function() {
	
	var billerModule = angular.module('billerModule');
	
	/* ----------------------------------------------------------------------------
	 * EMPRESAS
	 * ----------------------------------------------------------------------------
	 */
	
	billerModule.controller('CompanyListCtrl', [ '$scope', '$http', '$routeParams', function($scope, $http, $routeParams) {
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
			if(!$scope.searchOptions.showDeleted) {
				predicateBuilder.appendKey("auditData.deleted=n=");
			} else {
				predicateBuilder.appendKey("auditData.deleted=!n=");
			}
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
	
	billerModule.controller('CompanyDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
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
	
	billerModule.controller('CompanyNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
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
	billerModule.controller('OwnerListCtrl', [ '$scope', '$http', function($scope, $http) {
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
	
	billerModule.controller('OwnerDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
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
	
	billerModule.controller('OwnerNewCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
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
	billerModule.controller('CostCenterListCtrl', [ '$scope', '$http', function($scope, $http) {
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
	
	
	billerModule.controller('CostCenterDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
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
	
	billerModule.controller('CostCenterNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
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
	billerModule.controller('TerminalListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
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
	
	billerModule.controller('TerminalDetailCtrl', [ '$scope', '$rootScope', '$location', '$routeParams', '$http', 'messageService', function($scope, $rootScope, $location, $routeParams, $http, messageService) {
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
	
	billerModule.controller('TerminalNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
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

})();
