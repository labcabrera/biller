(function() {
	
	var billerModule = angular.module('billerModule');
	
	billerModule.controller('AdjustmentStoreListCtrl', [ '$scope', '$rootScope', '$routeParams', '$location', '$http', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $location, $http, dialogs, messageService) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 20;
		$scope.reset = function() {
			$scope.searchOptions = {
				'company': { "id": $routeParams.company, "name": '' },
				'model': { "id": $routeParams.model},
				'state': $routeParams.state,
				'from': '',
				'to':  ''
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			predicateBuilder.append("concept=lk=", 'MANUAL');
			return 'rest/adjustments/store/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			messageService.setMessage(null);
			$scope.setPage(1);
		};
		$scope.setPage = function(page) {
			console.log("set page >> " + page);
		    $scope.currentPage = page;
		    $scope.results = null;
		    $scope.searchMessage = "Loading...";
		    $http.get($scope.getSearchUrl()).success(function(data) {
		    	$scope.results = data;
		    	$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
		    });
		};
		$scope.reset();
		$scope.search();
	} ]);
	
	billerModule.controller('AdjustmentLiquidationListCtrl', [ '$scope', '$rootScope', '$routeParams', '$location', '$http', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $location, $http, dialogs, messageService) {
		$scope.currentPage = 1;
		$scope.itemsPerPage = 20;
		$scope.reset = function() {
			$scope.searchOptions = {
				'company': { "id": $routeParams.company, "name": '' },
				'model': { "id": $routeParams.model},
				'state': $routeParams.state,
				'from': '',
				'to':  ''
			};
		};
		$scope.getSearchUrl = function() {
			var predicateBuilder = new PredicateBuilder('');
			return 'rest/adjustments/liquidation/find?p=' + $scope.currentPage + '&n=' + $scope.itemsPerPage + "&q=" + predicateBuilder.build();
		};
		$scope.search = function() {
			messageService.setMessage(null);
			$scope.setPage(1);
		};
		$scope.setPage = function(page) {
			console.log("set page >> " + page);
		    $scope.currentPage = page;
		    $scope.results = null;
		    $scope.searchMessage = "Loading...";
		    $http.get($scope.getSearchUrl()).success(function(data) {
		    	$scope.results = data;
		    	$scope.searchMessage = "(" + data.totalItems + " en " + data.ms + " ms)";
		    });
		};
		$scope.reset();
		$scope.search();
	} ]);

})();