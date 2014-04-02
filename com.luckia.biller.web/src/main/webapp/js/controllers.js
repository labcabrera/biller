'use strict';

/* Controllers */

var billerControllers = angular.module('billerControllers', []);

/* ----------------------------------------------------------------------------
 * USUARIOS
 * ----------------------------------------------------------------------------
 */
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

billerControllers.controller('GroupDetailCtrl', [ '$scope', '$rootScope', '$location', '$routeParams', '$http', function($scope, $rootScope, $location, $routeParams, $http) {
	$scope.load = function() {
		$http.get('rest/groups/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
			$http.get('rest/companies/find?q=parent.id==' + $routeParams.id).success(function(data) { $scope.childs = data.results; });
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
		$http.post('rest/groups/remove/' + $scope.entity.id).success(function(data) {
			if(data.code == 200) { $location.path("groups"); } else { $scope.displayAlert(data); }
		});
	};
	$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
	$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	$scope.load();
} ]);

billerControllers.controller('GroupNewCtrl', [ '$scope', '$routeParams', '$http', '$location', function($scope, $routeParams, $http, $location) {
	$scope.isReadOnly = false;
	$scope.update = function() {
		$http.post('rest/groups/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
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

/** Detalle de empresa */
billerControllers.controller('CompanyDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', function($scope, $rootScope, $routeParams, $http, $location) {
	$scope.regions = function(name) {
		var url = "/rest/regions/find/" + name + (angular.isDefined($scope.entity.address.province.id) ? '?province=' + $scope.entity.address.province.id : '');
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
		$http.post('rest/companies/remove/' + $scope.entity.id).success(function(data) {
			if(data.code == 200) { $location.path("companies"); } else { $scope.displayAlert(data); }
		});
	};
	$scope.addStore = function() {
		$scope.newStore.parent = $scope.entity;
		$http.post('rest/stores/merge', $scope.newStore).success(function(data) {
			$rootScope.displayAlert(data);
			if(data.code == 200) {
				$scope.loadChilds();
				$scope.newStore = null;
				$("#addStoreModal").modal('hide');
			}
		});
	};
	$scope.setStorePage = function(page) {
	    $scope.currentPage = page;
	    $http.get('rest/stores/find?q=parent.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
	};
	$scope.load();
} ]);

/** Nueva empresa */
billerControllers.controller('CompanyNewCtrl', [ '$scope', '$routeParams', '$http', '$location', function($scope, $routeParams, $http, $location) {
	$scope.isReadOnly = false;
	$scope.reset = function() { };
	$scope.update = function() {
		$http.post('rest/companies/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
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
 * LOCALES
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

/** Detalle de establecimiento */
billerControllers.controller('StoreDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', function($scope, $rootScope, $routeParams, $http, $location) {
	$scope.load = function() {
		$http.get('rest/stores/id/' + $routeParams.id).success(function(data) { $scope.entity = data; });
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
		$http.post('rest/stores/remove/' + $scope.entity.id).success(function(data) {
			if(data.code == 200) { $location.path("stores"); } else { $scope.displayAlert(data); }
		});
	};
	$scope.$watch('entity.parent', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.parent = null; } });
	$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
	$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	$scope.load();
} ]);

/** Nuevo establecimiento */
billerControllers.controller('StoreNewCtrl', [ '$scope', '$routeParams', '$http', '$location', function($scope, $routeParams, $http, $location) {
	$scope.isReadOnly = false;
	$scope.reset = function() { };
	$scope.update = function() {
		$http.post('rest/stores/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
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
		console.log('[' + $scope.searchOptions.name + ']');
		if($scope.searchOptions.name != null && $scope.searchOptions.name != '') {
			var name = $scope.searchOptions.name;
			var key = "(name=lk=" + name + ",firstSurname=lk=" + name + ",secondSurname=lk=" + name + ")";
			console.log('append key!');
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

billerControllers.controller('OwnerDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
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
	$scope.setStorePage = function(page) {
	    $scope.currentPage = page;
	    $http.get('rest/stores/find?q=owner.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
	};
	$scope.$watch('entity.address.province', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.province = null; } });
	$scope.$watch('entity.address.region', function(newValue, oldValue){ if(newValue === ''){ $scope.entity.address.region = null; } });
	$scope.load();
} ]);

/** Nuevo titular */
billerControllers.controller('OwnerNewCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', function($scope, $rootScope, $routeParams, $http, $location) {
	$scope.isReadOnly = false;
	$scope.reset = function() {};
	$scope.update = function() {
		$http.post('rest/owners/merge/', $scope.entity).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
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


billerControllers.controller('CostCenterDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$location', function($scope, $rootScope, $routeParams, $http, $location) {
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
		$http.post('rest/costcenters/remove/' + $scope.entity.id).success(function(data) {
			if(data.code == 200) { $location.path("costcenters"); } else { $scope.displayAlert(data); }
		});
	};
	$scope.setStorePage = function(page) {
	    $scope.currentPage = page;
	    $http.get('rest/stores/find?q=costCenter.id==' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
	};
	$scope.load();
} ]);

billerControllers.controller('CostCenterNewCtrl', [ '$scope', '$routeParams', '$http', '$location', function($scope, $routeParams, $http, $location) {
	$scope.isReadOnly = false;
	$scope.update = function() {
		$http.post('rest/costcenters/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
				$location.path("costcenters/id/" + data.payload.id);				
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
billerControllers.controller('ModelListCtrl', [ '$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
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
billerControllers.controller('ModelDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.load = function() {
		$rootScope.isReadOnly = true;
		$http.get('rest/models/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
			$http.get('rest/stores/find?model=' + $routeParams.id).success(function(data) {
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
	$scope.setStorePage = function(page) {
	    $scope.currentPage = page;
	    $http.get('rest/stores/find?model=' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
	};
	$scope.load();
} ]);

/** Nuevo modelo de facturacion */
billerControllers.controller('ModelNewCtrl', [ '$scope', '$routeParams', '$http', '$location', function($scope, $routeParams, $http, $location) {
	$scope.isReadOnly = false;
	$scope.update = function() {
		$http.post('rest/models/merge/', $scope.entity).success(function(data) {
			if(data.code == 200) {
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

billerControllers.controller('BillListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', function($scope, $rootScope, $routeParams, $http, $filter) {
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
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.reset();
	$scope.search();
} ]);

billerControllers.controller('BillDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$location', '$http', function($scope, $rootScope, $routeParams, $location, $http) {
	$scope.load = function() {
		$http.get('rest/bills/' + $routeParams.id).success(function(data) { $scope.entity = data; });
		$rootScope.isReadOnly = true;
	};
	$scope.update = function() {
		$http.post('rest/bills/merge/', $scope.entity).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
				$rootScope.isReadOnly = true;				
				$scope.entity = data.payload;
			}
		});
	};
	$scope.editDetail = function(data) {
		$('#editBillConceptModal').modal('show');
		console.log("Editando detalle " + data);
		$scope.billDetail = { "bill" : { "id" : $scope.entity.id } };
		if(data != null && !(typeof data === 'undefined') ) {
			console.log('Recuperando informacion del detalle ' + data + ' via REST');	
			$http.get('rest/bills/detail/id/' + data).success(function(data) {
				$scope.billDetail = data;
				$scope.billDetail.bill = { "id": $scope.entity.id };
			});	
		};
	};
	$scope.mergeDetail = function(data) {
		console.log("Actualizando detalle de factura " + data);
		$http.post('rest/bills/detail/merge/', $scope.billDetail).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
				if(data.propagate) {
					alert("TODO generar detalle de liquidación");
				}
				$("#editBillConceptModal").modal('hide');
				$scope.billDetail = null;
				$scope.entity = data.payload;
			}
		});
	};
	$scope.removeDetail = function(data) {
		console.log("Eliminando detalle de factura " + data);
		$http.post('rest/bills/detail/remove/id/' + data).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
				$("#editBillConceptModal").modal('hide');
				$scope.billDetail = null;
				$scope.entity = data.payload;
			}
		});
	};
	$scope.confirm = function() {
		if($rootScope.autoconfirm || window.confirm('Se va a aceptar la factura')) {
			$http.post('rest/bills/confirm/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		}
	};
	$scope.cancel = function() {
		if($rootScope.autoconfirm || window.confirm('Se va a cancelar la factura')) {
			$http.post('rest/bills/cancel/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		}
	};
	$scope.rectify = function() {
		if($rootScope.autoconfirm || window.confirm('Se va a rectificar la factura')) {
			$http.post('rest/bills/rectify/' + $scope.entity.id).success(function(data) {
				if(data.code == 200) {
					$location.path("bills/id/" + data.payload.id);
				} else {
					$scope.displayAlert(data);
				}
			});
		}
	};
	$scope.editSendMail = function() {
		$('#sendMailModal').modal('show');
	};
	$scope.sendMail = function() {
		$http.post('rest/bills/send/' + $scope.entity.id, $scope.sendMail.value).success(function(data) {
			$scope.displayAlert(data);
			$('#sendMailModal').modal('hide');
		});
	};
	$scope.load();
} ]);

billerControllers.controller('SettingsCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.load = function() {
		$http.get('rest/settints/find/MAIL').success(function(data) { $scope.mail = data; });
	};
	$scope.load();
}]);


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
		predicateBuilder.append("receiver.id==", $scope.searchOptions.costCenter != null ? $scope.searchOptions.costCenter.id : null);
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

billerControllers.controller('LiquidationDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
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
		if($rootScope.autoconfirm || window.confirm('Se va a aceptar la liquidacion')) {
			$http.post('rest/liquidations/confirm/' + $scope.entity.id).success(function(data) {
				$scope.displayAlert(data);
				if(data.code == 200) {
					$scope.entity = data.payload;
				}
			});
		}
	};
	$scope.load();
} ]);

