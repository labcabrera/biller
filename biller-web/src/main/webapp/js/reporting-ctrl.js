(function() {

	var billerControllers = angular.module('billerControllers');

	billerControllers.controller('ReportTerminalsCtrl', [ '$scope', '$location', '$window', function($scope, $location, $window) {
		$scope.generate = function() {
			var url = REST_PATH + '/report/terminals';
			$window.open(url, '_blank');
		};
	} ]);

	billerControllers.controller('ReportLiquidationsCtrl', [ '$scope', '$location', '$window', '$filter', function($scope, $location, $window, $filter) {
		$scope.generate = function() {
			var ids = '';
			if($scope.reportData != null && $scope.reportData.company != null) {
				ids = $scope.reportData.company.id;
			}
			var from = $filter('date')($scope.reportData.from, "yyyy-MM-dd");
			var to = $filter('date')($scope.reportData.to, "yyyy-MM-dd");
			var url = REST_PATH + '/report/liquidations?ids=' + ids + '&from=' + from + '&to=' + to;
			$window.open(url, '_blank');
		};
	} ]);

})();