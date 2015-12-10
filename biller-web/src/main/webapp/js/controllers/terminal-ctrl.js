(function() {
	
	var billerModule = angular.module('billerModule');
	
	billerModule.controller('TerminalListCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', function($scope, $rootScope, $routeParams, $http) {
		$scope.currentPage = 1;
		$scope.searchName = '';
		$scope.reset = function() {
			$scope.searchOptions = {
				'terminal': '',
				'showOrphan' : false,
				'showDeleted': false,
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("code=lk=", $scope.searchOptions.terminal);			
			if($scope.searchOptions.showOrphan) { predicateBuilder.appendKey("store=n="); }
			if(!$scope.searchOptions.showDeleted) { predicateBuilder.appendKey("auditData.deleted=n="); }
			return 'rest/terminals/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			$scope.searchMessage = "Loading...";
			$scope.results = null;
			$http.get($scope.getSearchUrl()).success(function(data) {
				$scope.results = data;
				$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
			});
		};
		$scope.setPage = function(page) {
		    $scope.currentPage = page;
		    $scope.search();
		};
		$scope.reset();
		$scope.search();
	} ]);
	
	billerModule.controller('TerminalDetailCtrl', [ '$scope', '$rootScope', '$location', '$routeParams', '$http', '$filter', 'dialogs', 'messageService', function($scope, $rootScope, $location, $routeParams, $http, $filter, dialogs, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.load = function() {
			$http.get('rest/terminals/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
		});};
		$scope.update = function() {
			$http.post('rest/terminals/merge/', $scope.entity).success(function(data) {
				$scope.message = data;
				if(data.code == 200) {
					$scope.entity = data.payload;
					$rootScope.isReadOnly = true;				
				}
			});
		};
		$scope.remove = function() {
			var dlg = dialogs.confirm($filter('translate')('remove.confirmation.title') ,$filter('translate')('terminal.remove.confirmation'));
			dlg.result.then(function(btn){
				$http.post('rest/terminals/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) {
						$location.path("terminals");
					} else {
						$scope.message = data;
					}
				});				
			});
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
					$scope.message = data;
				}
			});
		};
		$scope.provinces = function(name) { return $http.get("/rest/provinces/find/" + name).then(function(response) { return response.data; }); };
	} ]);

})();
