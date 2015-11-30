(function() {

	var billerModule = angular.module('billerModule');
	
	billerModule.directive('searchResultPagination', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/search-result-pagination.html',
			replace : 'false'
		};
	});
	
	billerModule.directive('calendar', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/calendar.html',
			controller : ['$scope', function($scope) {
					$scope.placeHolder= 'DD/MM/YYYY';
					$scope.showCalendar = false;
					$scope.loadRealCalendar = function() {
						if(!$scope.isReadOnly) {
							$scope.showCalendar = true;
						};
					};
			}],
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '='
			},
		};
	});
	
	billerModule.directive('calendarReal', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/calendar-real.html',
			controller : 'CalendarRealCtrl',
			controller : ['$scope', '$timeout', function($scope, $timeout) {
				$scope.format = 'dd/MM/yyyy';
				$scope.placeHolder= 'DD/MM/YYYY';
				$timeout(function() {
						$scope.opened= true;
				}, 100);
				$scope.today = function() {
					$scope.ngModel = new Date();
				};
				$scope.clear = function() {
					$scope.ngModel = null;
				};
				$scope.toggleMin = function() {
					$scope.minDate = $scope.minDate ? null : new Date();
				};
				$scope.toggleMin();
				$scope.open = function($event) {
					$event.preventDefault();
					$event.stopPropagation();
					$scope.opened = true;
				};
				$scope.dateOptions = {
					formatYear : 'yy',
					startingDay : 1
				};
			}],
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '='
			},
		};
	});
	
	billerModule.directive('province', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/province-searchbox.html',
			controller : ['$scope', '$http', function($scope, $http) {
				$scope.provinces = function(name) {
					return $http.get('rest/provinces/find/' + name).then(function(response) {
						return response.data;
					});
				};
			}],
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				locked : '='
			},
		};
	});
	
	billerModule.directive('region', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/region-searchbox.html',
			controller : 'RegionCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				provinceId : '=',
				disabled : '@'
			},
		};
	});
	
	billerModule.directive('company', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/company-searchbox.html',
			controller : 'CompanyCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				locked : '='
			},
		};
	});
	
	billerModule.directive('companyGroup', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/company-group-searchbox.html',
			controller : 'CompanyGroupCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				locked : '='
			},
		};
	});
	
	billerModule.directive('store', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/store-searchbox.html',
			controller : 'StoreCtrl',
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				locked : '='
			},
		};
	});
	
	billerModule.directive('costCenter', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/cost-center-searchbox.html',
			controller : ['$scope', '$http', function($scope, $http) {
				$scope.costCenters = function(name) {
					return $http.get(REST_PATH + '/costcenters/find?q=name=lk=' + name).then(function(response) {
						return response.data.results;
					});
				};
			}],
			require : '^ngModel',
			replace : 'true',
			scope : {
				ngModel : '=',
				isReadOnly : '=',
				locked : '='
			},
		};
	});

	billerModule.directive('selectEnum', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/select-enum.html',
			require : [ '^ngModel', '^enumName' ],
			controller : ['$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
				$http.get('rest/enumeration/' + $scope.enumName, {
					cache : true
				}).success(function(data) {
					$scope.names = data;
				});
			}],
			scope : {
				ngModel : '=',
				enumName : '@',
				isReadOnly : '=',
			}
		};
	});
	
	billerModule.controller('RegionCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.regions = function(name) {
			var provinceId = $scope.provinceId;
			console.log("Province: " + provinceId);
			return $http.get("rest/regions/find/" + name + (provinceId != null ? '?province=' + provinceId : '')).then(function(response) { return response.data; });
		};
	}]);
	
	billerModule.controller('CompanyCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.companies = function(name) {
			return $http.get(REST_PATH + '/companies/find?q=name=lk=' + name).then(function(response) {
				return response.data.results;
			});
		};
	}]);

	billerModule.controller('CompanyGroupCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.companies = function(name) {
			return $http.get(REST_PATH + '/groups/find?q=name=lk=' + name).then(function(response) {
				return response.data.results;
			});
		};
	}]);
	
	billerModule.controller('StoreCtrl', ['$scope', '$http', function($scope, $http) {
		$scope.stores = function(name) {
			return $http.get(REST_PATH + '/stores/find?q=name=lk=' + name).then(function(response) {
				return response.data.results;
			});
		};
	}]);
	
	billerModule.directive('messageErrorPanel', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/message-error-panel.html',
			controller : ['$scope', '$timeout', function($scope, $timeout) {
				$scope.$watch('message', function() {
					$scope.isCollapse = false;
				});
				$scope.setCollapse = function() {
					$scope.isCollapse = true;
				};
				if ($scope.autoClose) {
					$scope.$watch('message', function(newValue, oldValue) {
						$timeout(function() {
							$scope.isCollapse = true;
						}, 5000);
					});
				}
			}],
			require : '^ngModel',
			replace : 'false',
			scope : {
				message : '=ngModel',
				autoClose : '='
			},
		};
	});
	
	billerModule.directive('messageErrorBox', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/message-error-box.html',
			controller : ['$scope', function($scope) {
				$scope.$watch('messages', function() {
					$scope.isCollapse = false;
				});
				$scope.setCollapse = function() {
					$scope.isCollapse = true;
				};
			}],
			require : '^ngModel',
			replace : 'false',
			scope : {
				messages : '=ngModel',
				messageIcon : '@',
				messageClass : '@'
			}
		};
	});

	billerModule.factory('MessageUtils', function() {
		return {
			createError : function(text) {
				var message = new Object();
				message.errors = new Array();
				message.errors[0] = new Object();
				message.errors[0].text = text;
				return message;
			}
		};
	});

	billerModule.factory('Utils', function() {
		return {
			isEmpty : function(value) {
				return value == null || value == '' || value == 'undefined';
			}
		};
	});
	
	billerModule.directive('addBillLiquidationDetail', function() {
		return {
			restrict : 'AE',
			templateUrl : 'templates/bill-modal-liquidation-detail.html',
			controller : function($scope, $http) {
				$scope.init = function() {
					$scope.isSaving = false;
				};
				$scope.mergeLiquidationDetail = function() {
					$scope.isSaving = true;
					$http.post('rest/bills/detail/liquidation/merge/', $scope.detail).success(function(data) {
						$scope.isSaving = false;
						$scope.message = data;
						$("#editBillLiquidationConceptModal").modal('hide');
						if(data.code == 200) {
							$scope.bill = data.payload;
						}
					});
				};
				$scope.removeLiquidationDetail = function(data) {
					console.log("removeDetail() " + data);
					$scope.isSaving = true;
					$http.post('rest/bills/detail/liquidation/remove/' + data).success(function(data) {
						$scope.isSaving = false;
						$scope.message = data;
						if(data.code == 200) {
							$scope.bill = data.payload;
						}
						$("#editBillLiquidationConceptModal").modal('hide');
					});
				};
				$scope.init();
			},
			scope : {
				bill: "=",
				detail: '=',
				message: '=',
				isSaving: '='
			}
		};
	});
	
	billerModule.directive('addBillDetail', function() {
		return {
			restrict : 'AE',
			templateUrl : 'templates/bill-modal-detail.html',
			controller : function($scope, $http) {
				$scope.mergeDetail = function() {
					$scope.isSaving = true;
					$http.post('rest/bills/detail/merge/', $scope.detail).success(function(data) {
						$scope.isSaving = false;
						$scope.message = data;
						$("#editBillConceptModal").modal('hide');
						if(data.code == 200) {
							$scope.bill = data.payload;
							$scope.detail.id = $scope.detail.value = $scope.detail.name = $scope.detail.units = null;
						}
					});
					$scope.removeDetail = function(data) {
						console.log("removeDetail() " + data);
						$scope.isSaving = true;
						$http.post('rest/bills/detail/remove/' + data).success(function(data) {
							$scope.isSaving = false;
							$scope.message = data;
							if(data.code == 200) {
								$scope.bill = data.payload;
							}
							$("#editBillConceptModal").modal('hide');
						});
					};
				};
			},
			scope : {
				bill: "=",
				detail : '=',
				message: '='
			}
		};
	});
	
	billerModule.directive('addLiquidationDetail', function() {
		return {
			restrict : 'AE',
			templateUrl : 'templates/liquidation-modal-detail.html',
			controller : function($scope, $http) {
				$scope.init = function() {
					$scope.isSaving = false;
				};
				$scope.mergeDetail = function() {
					$scope.isSaving = true;
					$http.post('rest/liquidations/detail/merge/', $scope.detail).success(function(data) {
						$scope.isSaving = false;
						$scope.message = data;
						$("#editLiquidationConceptModal").modal('hide');
						if(data.code == 200) {
							$scope.liquidation = data.payload;
						}
					});
				};
				$scope.removeDetail = function(id) {
					$scope.isSaving = true;
					$http.post('rest/liquidations/detail/remove/' + id).success(function(data) {
						$scope.isSaving = false;
						$scope.message = data;
						$("#editLiquidationConceptModal").modal('hide');
						if(data.code == 200) {
							$scope.liquidation = data.payload;
						}
					});
				};
				$scope.init();
			},
			scope : {
				liquidation: "=",
				detail: '=',
				message: '=',
				isSaving: '='
			}
		};
	});
	
	billerModule.directive('liquidationDetails', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/liquidation-details.html',
			controller : function($scope, $http, $routeParams) {
				$scope.editDetail = function(data) {
					if(data != null && !(typeof data === 'undefined') ) {
						$scope.isSaving = true;
						$http.get('rest/liquidations/detail/id/' + data).success(function(data) {
							$scope.isSaving = false;
							$scope.liquidationDetail = data;
							$scope.liquidationDetail.liquidation = { "id": $scope.entity.id };
							$('#editLiquidationConceptModal').modal('show');
						});	
					} else {
						var d = $scope.liquidationDetail;
						d.id = d.units = d.value = d.name = d.dummyType = null;
						d.liquidationIncluded = true;
						$('#editLiquidationConceptModal').modal('show');
					}
				};
				$scope.setPage = function(page) {
				    $scope.currentPage = page;
				    $http.get('rest/bills/find?q=liquidation.id==' + $routeParams.id + "&n=15" + "&p=" + page).success(function(data) { $scope.childs = data; });
				};
				$scope.init = function() {
					$scope.setPage(1);
				};
				$scope.init();
			},
			scope : {
				childs: "=",
				entity: '=',
				liquidationDetail: '='
			}
		};
	});
	
	billerModule.directive('billingModelDetail', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/billing-model-details.html',
			scope : {
				entity: '=',
				isReadOnly: '='
			}
		};
	});
	
	billerModule.directive('billLiquidationDetail', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/bill-liquidation-detail.html',
			controller : function($scope, $http) {
				$scope.editLiquidationDetail = function(id) {
					if(id != null && !(typeof id === 'undefined') ) {
						$scope.isSaving = true;
						$http.get('rest/bills/detail/liquidation/id/' + id).success(function(data) {
							$scope.detail = data;
							$scope.detail.bill = { "id": $scope.entity.id };
							$scope.isSaving = false;
						});	
					} else {
						var d = $scope.detail;
						d.id = d.units = d.value = d.name = d.dummyType = null;
						d.liquidationIncluded = true;
					}
					$('#editBillLiquidationConceptModal').modal('show');			
				};
			},
			scope : {
				entity: '=',
				detail: '='
			}
		};
	});
	
	billerModule.directive('ownerTab', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/components/entity-owner-tab.html',
			controller : function($scope, $http) {
				if($scope.owner && $scope.owner.id) {
					$scope.load = function() {
						$http.get('rest/owners/id/' + $scope.owner.id).success(function(data) {
							$scope.entity = data;
						});	
					};
				};
			},
			scope : {
				owner: '='
			}
		};
	});

})();