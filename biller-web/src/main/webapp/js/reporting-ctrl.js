(function() {

	var billerControllers = angular.module('billerControllers');
	
	billerControllers.controller('ReportTerminalsCtrl', ['$scope', '$location', '$window', function($scope, $location, $window) {
		$scope.generate = function() {
			var url = REST_PATH + '/report/terminals';
			$window.open(url, '_blank');
		};
	}]);
	
	billerControllers.controller('ReportLiquidationsCtrl', ['$scope', '$location', '$window', function($scope, $location, $window) {
		$scope.generate = function() {
			var url = REST_PATH + '/report/liquidations';
			$window.open(url, '_blank');
		};
	}]);
	
	
	
})();