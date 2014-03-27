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
	$scope.searchOptions = {
		'showDeleted': false,
		'name': '',
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
	$scope.searchName = '';
	$scope.searchProvince = null;
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("name=lk=", $scope.searchName);
		predicateBuilder.append("address.province.id==", $scope.searchProvince != null ? $scope.searchProvince.id : null);	
		return 'rest/companies/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
		$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.search();
} ]);

billerControllers.controller('CompanyDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.loadChilds = function() {
		$http.get('rest/companies/id/' + $routeParams.id).success(function(data) {
			$scope.entity = data;
			$http.get('rest/stores/find?q=parent.id==' + $routeParams.id).success(function(data) { $scope.childs = data; });
		});	
	};
	$scope.regions = function(name) {
		var url = "/rest/regions/find/" + name + (angular.isDefined($scope.entity.address.province.id) ? '?province=' + $scope.entity.address.province.id : '');
		return $http.get(url).then(function(response) { return response.data; });
	};
	$scope.load = function() {
		$http.get('rest/companies/id/' + $routeParams.id).success(function(data) {
			$rootScope.isReadOnly = true;
			$scope.entity = data;
			$http.get('rest/stores/find?q=parent.id==' + $routeParams.id).success(function(data) {
				$scope.childs = data;
			});
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
	$scope.setPage = function(page) {
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
} ]);
/* ----------------------------------------------------------------------------
 * LOCALES
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('StoreListCtrl', [ '$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
	$scope.currentPage = 1;
	$scope.searchName = '';
	$scope.itemsPerPage = 15;
	$scope.searchProvince = null;
	$scope.searchCompany = { "id": $routeParams.parent, "name": '' };
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("name=lk=", $scope.searchName);
		predicateBuilder.append("address.province.id==", $scope.searchProvince != null ? $scope.searchProvince.id : null);
		predicateBuilder.append("parent.id==", $scope.searchCompany != null ? $scope.searchCompany.id : null);
		predicateBuilder.append("owner.id==", angular.isDefined($routeParams.owner) ? $routeParams.owner : null);		
		return 'rest/stores/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.search();
} ]);

billerControllers.controller('StoreDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.load = function() {
		$http.get('rest/stores/id/' + $routeParams.id).success(function(data) { $scope.entity = data; });
		$rootScope.isReadOnly = true;
	};
	$scope.update = function() {
		$http.post('rest/stores/merge/', $scope.entity).success(function(data) {
			$scope.message = "Establecimiento actualizado";
		});
		$scope.isReadOnly = true;
	};
	$scope.regions = function(name) {
		var url = "/rest/regions/find/" + name;
		if(angular.isDefined($scope.entity.address.province.id)) {
			url += '?province=' + $scope.entity.address.province.id;
		}
		return $http.get(url).then(function(response) { return response.data; });
	};
	$scope.load();
} ]);
/* ----------------------------------------------------------------------------
 * TITULARES
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('OwnerListCtrl', [ '$scope', '$http', function($scope, $http) {
	$scope.currentPage = 1;
	$scope.searchName = '';	
	$scope.getSearchUrl = function() {
		var url = 'rest/owners/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&name=" + $scope.searchName;
		return url;
	};
	$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	$scope.setPage = function(page) {
		$scope.currentPage = page;
		$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
} ]);

billerControllers.controller('OwnerDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.load = function() {
		$http.get('rest/owners/id/' + $routeParams.id).success(function(data) { $scope.entity = data; });
		$rootScope.isReadOnly = true;
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
	$scope.load();
} ]);

billerControllers.controller('OwnerNewCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
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
	$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
	$scope.regions = function(name) { return $http.get("/rest/regions/find/" + name).then(function(response) { return response.data; }); };
}]);

/* ----------------------------------------------------------------------------
 * CENTROS DE COSTE
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('CostCenterListCtrl', [ '$scope', '$http', function($scope, $http) {
	$scope.currentPage = 1;
	$scope.searchName = '';	
	$scope.getSearchUrl = function() {
		var url = 'rest/costcenters/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&name=" + $scope.searchName;
		return url;
	};
	$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	$scope.setPage = function(page) {
		$scope.currentPage = page;
		$http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; });
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
} ]);

billerControllers.controller('CostCenterDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.load = function() {
		$http.get('rest/costcenters/id/' + $routeParams.id).success(function(data) { $scope.entity = data; });
		$rootScope.isReadOnly = true;
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
	$scope.load();
} ]);

/* ----------------------------------------------------------------------------
 * MODELOS DE FACTURACION
 * ----------------------------------------------------------------------------
 */
billerControllers.controller('ModelListCtrl', [ '$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
	$scope.currentPage = 1;
	$scope.searchName = '';
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("name=lk=", $scope.searchName);
		return 'rest/models/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.search();
} ]);


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
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $http.get('rest/stores/find?model=' + $routeParams.id + "&n=10" + "&p=" + page).success(function(data) { $scope.childs = data; });
	};
	$scope.load();
} ]);

/* ----------------------------------------------------------------------------
 * FACTURAS
 * ----------------------------------------------------------------------------
 */

billerControllers.controller('BillListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', function($scope, $rootScope, $routeParams, $http, $filter) {
	$scope.currentPage = 1;
	$scope.itemsPerPage = 15;
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("code=lk=", $scope.searchOptions.code);
		predicateBuilder.append("sender.id==", $scope.searchOptions.store.id);
		predicateBuilder.append("receiver.id==", $scope.searchOptions.company.id);
		predicateBuilder.append("currentState.stateDefinition.id==", $scope.searchOptions.state);
		predicateBuilder.append("dateFrom=ge=", $scope.searchOptions.from != null ? $filter('date')($scope.searchOptions.from, "yyyy-MM-dd") : null);
		predicateBuilder.append("dateTo=le=", $scope.searchOptions.to != null ? $filter('date')($scope.searchOptions.to, "yyyy-MM-dd") : null);
		return 'rest/bills/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
	$scope.reset = function() {
		$scope.searchOptions = {
			'code': $routeParams.code,
			'store': { "id": $routeParams.store, "name": '' },
			'company': { "id": $routeParams.company, "name": '' },
			'state': '',
			'from': '',
			'to':  ''
		};
	};
	$scope.reset();
	$scope.search();
//	$('.selectpicker').selectpicker();
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
		var values = [ $scope.sendMail.value ];
		$http.post('rest/bills/send/' + $scope.entity.id, values).success(function(data) {
			$scope.displayAlert(data);
			if(data.code == 200) {
				$scope.entity = data.payload;
				$('#sendMailModal').modal('hide');
				$scope.sendMail = null;
			}
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

billerControllers.controller('LiquidationListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
	$scope.currentPage = 1;
	$scope.searchCode = $routeParams.code;
	$scope.searchState = $routeParams.state;
	$scope.itemsPerPage = 15;
	$scope.searchStore = { "id": $routeParams.store, "name": '' };
	$scope.searchCompany = { "id": $routeParams.company, "name": '' };
	$scope.getSearchUrl = function() {
		var predicateBuilder = new PredicateBuilder('');
		predicateBuilder.append("code==", $scope.searchCode);
		predicateBuilder.append("sender.id==", $scope.searchStore.id);
		predicateBuilder.append("receiver.id==", $scope.searchCompany.id);
		predicateBuilder.append("currentState.stateDefinition.id==", $scope.searchState);
		return 'rest/liquidations/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
	};
	$scope.search = function() { $http.get($scope.getSearchUrl()).success(function(data) { $scope.results = data; }); };
	$scope.setPage = function(page) {
	    $scope.currentPage = page;
	    $scope.search();
	};
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
	$scope.load();
} ]);

