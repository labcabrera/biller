(function() {
	
	var billerModule = angular.module('billerModule');
	
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
