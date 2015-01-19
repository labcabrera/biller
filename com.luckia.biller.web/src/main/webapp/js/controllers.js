'use strict';

/* Controllers */

var billerControllers = angular.module('billerControllers', []);

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
 * GRUPOS DE EMPRESAS
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('GroupListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.currentPage = 1;
	$scope.searchName = '';
	$scope.reset = function() {
		$scope.searchOptions = {
				'name': '',
				'showDeleted': false,
		};
	};
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("name=lk=", $scope.searchOptions.name);			
		if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
		return 'rest/groups/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
		$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	};
	$scope.reset();
	$scope.search();
} ]);

billerControllers.controller('GroupDetailCtrl', [ '$scope', '$rootScope', '$location', '$routeParams', '$http', 'messageService', function($scope, $rootScope, $location, $routeParams, $http, messageService) {
	if(messageService.hasMessage()) {
		$scope.displayAlert(messageService.getMessage());
	}
	$scope.load = function() {
		$http.get('rest/groups/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
			$http.get('rest/companies/find?q=parent.id==' + $routeParams.id).success(function(data) { $scope.childs = data.results; });
		$rootScope.isReadOnly = true;
	});};
	$scope.update = function() {
		$http.post('rest/groups/merge/', $scope.entity).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
				$scope.entity = data.payload;
				$rootScope.isReadOnly = true;				
			}
		});
	};
	$scope.remove = function() {
		if($rootScope.autoconfirm || window.confirm('Se va a eliminar el grupo')) {
			$http.post('rest/groups/remove/' + $scope.entity.id).success(function(data) {
				if(data.code == 200) { $location.path("groups"); } else { $scope.displayAlert(data); }
			});
		}
	};
	$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
	$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	$scope.load();
} ]);

billerControllers.controller('GroupNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
	$scope.isReadOnly = false;
	$scope.update = function() {
		$http.post('rest/groups/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
				messageService.setMessage(data);
				$location.path("groups/id/" + data.payload.id);				
			} else {
				$scope.displayAlert(data);
			}
		});
	};
	$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
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
		$http.post('rest/companies/merge/', $scope.entity).success(function(data) {
			$rootScope.displayAlert(data);
			if(data.code == 200) {
				$scope.entity = data.payload;
				$rootScope.isReadOnly = true;
			}
		});
	};
	$scope.remove = function() {
		if($rootScope.autoconfirm || window.confirm('Se va a eliminar la empresa')) {
			$http.post('rest/companies/remove/' + $scope.entity.id).success(function(data) {
				if(data.code == 200) { $location.path("companies"); } else { $scope.displayAlert(data); }
			});
		}
	};
	$scope.addStore = function() {
		$scope.newStore.parent = $scope.entity;
		$http.post('rest/stores/merge', $scope.newStore).success(function(data) {
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
		$http.post('rest/companies/merge/', $scope.entity).success(function(data) {
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
 * ESTABLECIMIENTOS
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('StoreListCtrl', [ '$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
	$scope.currentPage = 1;
	$scope.itemsPerPage = 15;
	$scope.reset = function() {
		$scope.searchOptions = {
			'name': '',
			'province': {"id": null, 'name': ''},
			'company': {"id": null, 'name': ''},
			'owner': {"id": null, 'name': ''},
			'costCenter': {"id": null, 'name': ''},
			'type': '',
			'showDeleted': false,
		};
	};
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("name=lk=", $scope.searchOptions.name);
		predicateBuilder.append("address.province.id==", $scope.searchOptions.province != null ? $scope.searchOptions.province.id : null);
		predicateBuilder.append("parent.id==", $scope.searchOptions.company != null ? $scope.searchOptions.company.id : null);
		predicateBuilder.append("costCenter.id==", $scope.searchOptions.costCenter != null ? $scope.searchOptions.costCenter.id : null);
		predicateBuilder.append("owner.id==", $scope.searchOptions.owner != null ? $scope.searchOptions.owner.id : null);
		predicateBuilder.append("type==", $scope.searchOptions.type);
		if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
		return 'rest/stores/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.reset();
	$scope.search();
} ]);

billerControllers.controller('StoreDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, messageService) {
	if(messageService.hasMessage()) {
		$scope.displayAlert(messageService.getMessage());
	}
	$scope.load = function() {
		$http.get('rest/stores/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
			$http.get('rest/terminals/find?q=store.id==' + $routeParams.id).success(function(data) { $scope.childTerminals = data.results; });
		});
		$rootScope.isReadOnly = true;
	};
	$scope.update = function() {
		$http.post('rest/stores/merge/', $scope.entity).success(function(data) {
			$rootScope.displayAlert(data);
			if(data.code == 200) {
				$scope.entity = data.payload;
				$rootScope.isReadOnly = true;
			}
		});
	};
	$scope.remove = function() {
		if($rootScope.autoconfirm || window.confirm('Se va a eliminar el establecimiento')) {
			$http.post('rest/stores/remove/' + $scope.entity.id).success(function(data) {
				if(data.code == 200) { $location.path("stores"); } else { $scope.displayAlert(data); }
			});
		}
	};
	$scope.addTerminal = function() {
		var current = $scope.newTerminal.store != null ? $scope.newTerminal.store.name : null;
		if(current == null || ($rootScope.autoconfirm || window.confirm('El terminal esta actualmente asociado con la empresa ' + current))) {
			$scope.newTerminal.store = $scope.entity;
			$http.post('rest/terminals/merge', $scope.newTerminal).success(function(data) {
				$rootScope.displayAlert(data);
				if(data.code == 200) {
					$scope.load();
					$scope.newTerminal = null;
					$("#addTerminalModal").modal('hide');
				}
			});
		}
	};
	$scope.removeTerminal = function(data) {
		data.store = null;
		$http.post('rest/terminals/merge', data).success(function(data) {
			$rootScope.displayAlert(data);
			if(data.code == 200) {
				$scope.load();
			}
		});
	};
	$scope.$watch('entity.owner', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.owner = null; } });
	$scope.$watch('entity.parent', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.parent = null; } });
	$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
	$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	$scope.$watch('entity.billingModel', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.billingModel = null; } });
	$scope.load();
} ]);

billerControllers.controller('StoreNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
	$scope.isReadOnly = false;
	$scope.reset = function() { };
	$scope.update = function() {
		$http.post('rest/stores/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
				messageService.setMessage(data);
				$location.path("stores/id/" + data.payload.id);				
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
		$http.post('rest/owners/merge/', $scope.entity).success(function(data) {
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
 * MODELOS DE FACTURACION
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('ModelListCtrl', [ '$scope', '$rootScope', '$http', 'messageService', function($scope, $rootScope, $http, messageService) {
	if(messageService.hasMessage()) {
		$scope.displayAlert(messageService.getMessage());
	}
	$scope.currentPage = 1;
	$scope.reset = function() {
		$scope.searchOptions = {
			'name': '',
			'showDeleted': false
		};
	};
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("name=lk=", $scope.searchOptions.name);
		if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
		return 'rest/models/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.reset();
	$scope.search();
} ]);

/** Detalle de modelo de facturacion */
billerControllers.controller('ModelDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location','dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, dialogs, messageService) {
	if(messageService.hasMessage()) {
		$scope.displayAlert(messageService.getMessage());
	}
	$scope.load = function() {
		$rootScope.isReadOnly = true;
		$http.get('rest/models/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
			$http.get('rest/stores/find?q=billingModel.id==' + $routeParams.id).success(function(data) {
				$scope.childs = data;
			});
		});
	};
	$scope.update = function() {
		$http.post('rest/models/merge', $scope.entity).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
				$rootScope.isReadOnly = true;
				$scope.entity = data.payload;
			}
		});
	};
	$scope.remove = function() {
		var dlg = dialogs.confirm('Confirmacion de borrado','Desea eliminar el modelo de facturacion?');
		dlg.result.then(function(btn){
			$http.post('rest/models/remove/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("models");
				}
			});
		});
	};
	$scope.setStorePage = function(page) {
	    $scope.currentPage = page;
	    $http.get('rest/stores/find?model=' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
	};
	$scope.mergeRappel = function() {
		$http.post('rest/models/rappel/merge', $scope.newRappel).success(function(data) {
			if(data.code == 200) {
				$scope.displayAlert(data);
				$rootScope.isReadOnly = true;
				$scope.entity = data.payload;
				$('#addRappelModal').modal('hide');
			} else {
				$scope.displayAlertModal(data);
			}
		});
	};
	$scope.removeRappel = function() {
		$http.post('rest/models/rappel/remove/' + $scope.newRappel.id).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
				$rootScope.isReadOnly = true;
				$scope.entity = data.payload;
				$('#addRappelModal').modal('hide');
			}
		});
		
	};
	$scope.editRappel = function(rappelId) {
		if(rappelId > 0) {
			$http.get('rest/models/rappel/id/' + rappelId).success(function(data) {
				$scope.newRappel = data;
				$scope.newRappel.model = { 'id': $scope.entity.id};
			});
		} else {
			$scope.newRappel = { amount: '0', bonusAmount: '0', model: { 'id': $scope.entity.id } };
		}
		$('#addRappelModal').modal('show');
	};
	$scope.load();
} ]);

/** Nuevo modelo de facturacion */
billerControllers.controller('ModelNewCtrl', [ '$scope', '$routeParams', '$http', '$location', 'messageService', function($scope, $routeParams, $http, $location, messageService) {
	$scope.isReadOnly = false;
	$scope.update = function() {
		$http.post('rest/models/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
				messageService.setMessage(data);
				$location.path("models/id/" + data.payload.id);				
			} else {
				$scope.displayAlert(data);
			}
		});
	};
	$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
} ]);

/* ----------------------------------------------------------------------------
 * FACTURAS
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('BillListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', 'messageService', function($scope, $rootScope, $routeParams, $http, $filter, messageService) {
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
		$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	};
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	};
	$scope.reset();
	$scope.search();
} ]);

billerControllers.controller('BillDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$location', '$http', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $location, $http, dialogs, messageService) {
	if(messageService.hasMessage()) {
		$scope.displayAlert(messageService.getMessage());
	}
	$scope.load = function() {
		$http.get('rest/bills/' + $routeParams.id).success(function(data) { $scope.entity = data; });
		$rootScope.isReadOnly = true;
	};
	$scope.update = function() {
		$scope.isSaving = true;
		$http.post('rest/bills/merge/', $scope.entity).success(function(data) {
			$scope.isSaving = false;
			$scope.displayAlert(data);
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
						$scope.displayAlert(data);
					}
				});
			});
		}
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
			$scope.displayAlert(data);
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
			if(data.code == 200) {
				$scope.entity = data.payload;
			}
			$scope.displayAlert(data);
			$("#editBillConceptModal").modal('hide');
		});
	};
	$scope.confirm = function() {
		var dlg = dialogs.confirm('Confirmacion de aceptacion','Se va a aceptar la factura');
		dlg.result.then(function(btn){
			$scope.isSaving = true;
			$http.post('rest/bills/confirm/' + $scope.entity.id).success(function(data) {
				$scope.isSaving = false;
				$scope.displayAlert(data);
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
				$scope.displayAlert(data);
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
				if(data.code == 200) {
					messageService.setMessage(data);
					$location.path("bills/id/" + data.payload.id);
				} else {
					$scope.displayAlert(data);
				}
			});
		});
	};
	$scope.draft = function() {
		var dlg = dialogs.confirm('Confirmacion','La factura pasara a estado borrador');
		dlg.result.then(function(btn){
			$scope.isSaving = true;
			$http.post('rest/bills/draft/' + $scope.entity.id).success(function(data) {
				$scope.isSaving = false;
				$scope.displayAlert(data);
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
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		});
	};
	$scope.editSendMail = function() { $('#sendMailModal').modal('show'); };
	$scope.sendMail = function() {
		$http.post('rest/bills/send/' + $scope.entity.id, $scope.sendMail.value).success(function(data) {
			$scope.displayAlert(data);
			$('#sendMailModal').modal('hide');
		});
	};
	$scope.load();
} ]);

/* ----------------------------------------------------------------------------
 * LIQUIDACIONES
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('LiquidationListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', function($scope, $rootScope, $routeParams, $http, $filter) {
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
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.reset();
	$scope.search();
} ]);

billerControllers.controller('LiquidationDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $location, dialogs, messageService) {
	$scope.load = function() {
		$http.get('rest/liquidations/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
			$http.get('rest/bills/find?q=liquidation.id==' + $routeParams.id + "&n=15").success(function(data) {
				$scope.childs = data;
			});
		});
		$rootScope.isReadOnly = true;
	};
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $http.get('rest/bills/find?q=liquidation.id==' + $routeParams.id + "&n=15" + "&p=" + page).success(function(data) { $scope.childs = data; });
	};
	$scope.confirm = function() {
		var dlg = dialogs.confirm('Confirmacion','Se va a aceptar la liquidacion');
		dlg.result.then(function(btn){
			$http.post('rest/liquidations/confirm/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		});
	};
	$scope.update = function() {
		$http.post('rest/liquidations/merge/', $scope.entity).success(function(data) {
			$scope.displayAlert(data);
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
				$http.post('rest/liquidations/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) { $location.path("liquidations"); } else { $scope.displayAlert(data); }
				});
			});
		}
	};
	$scope.editDetail = function(data) {
		if(data != null && !(typeof data === 'undefined') ) {
			$http.get('rest/liquidations/detail/id/' + data).success(function(data) {
				$scope.liquidationDetail = data;
				$scope.liquidationDetail.liquidation = { "id": $scope.entity.id };
				$('#editLiquidationConceptModal').modal('show');
			});	
		} else {
			$scope.liquidationDetail = { "liquidation" : { "id" : $scope.entity.id }, "units":"", value: "" };			
			$('#editLiquidationConceptModal').modal('show');
		}
	};
	$scope.mergeDetail = function(data) {
		$http.post('rest/liquidations/detail/merge/', $scope.liquidationDetail).success(function(data) {
			$scope.displayAlert(data);
			$("#editLiquidationConceptModal").modal('hide');
			if(data.code == 200) {
				$scope.entity = data.payload;
			}
		});
	};
	$scope.removeDetail = function(data) {
		$http.post('rest/liquidations/detail/remove/' + $scope.liquidationDetail.id).success(function(data) {
			$scope.displayAlert(data);
			$("#editLiquidationConceptModal").modal('hide');
			if(data.code == 200) {
				$scope.entity = data.payload;
			}
		});
	};
	$scope.recalculate = function() {
		var dlg = dialogs.confirm('Confirmacion','Desea recalcular la liquidacion? Los ajustes manuales se perderan');
		dlg.result.then(function(btn){
			$http.post('rest/liquidations/recalculate/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
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
			$http.post('rest/liquidations/pdf/recreate/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		});
	};
	$scope.draft = function() {
		var dlg = dialogs.confirm('Confirmacion','La liquidacion volvera a estado borrador');
		dlg.result.then(function(btn){
			$http.post('rest/liquidations/draft/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		});
	};
	$scope.editSendMail = function() { $('#sendMailModal').modal('show'); };
	$scope.sendMail = function() {
		$http.post('rest/liquidations/send/' + $scope.entity.id, $scope.sendMail.value).success(function(data) {
			$scope.displayAlert(data);
			$('#sendMailModal').modal('hide');
		});
	};
	$scope.load();
} ]);

/* ----------------------------------------------------------------------------
 * RAPPEL DE ESTABLECIMIENTOS
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('RappelStoreListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', function($scope, $rootScope, $routeParams, $http, $filter) {
	$scope.currentPage = 1;
	$scope.itemsPerPage = 15;
	$scope.reset = function() {
		$scope.searchOptions = {
		};
	};
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		return 'rest/rappel/stores/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.reset();
	$scope.search();
} ]);

billerControllers.controller('RappelStoreDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.load = function() {
		$http.get('rest/rappel/stores/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
		});
		$rootScope.isReadOnly = true;
	};
	$scope.confirm = function() {
		if($rootScope.autoconfirm || window.confirm('Se va a aceptar el rappel')) {
			$http.post('rest/rappel/stores/confirm/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		}
	};
	$scope.load();
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
}]);

