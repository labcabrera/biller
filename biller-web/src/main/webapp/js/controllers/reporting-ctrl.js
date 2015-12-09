(function() {

	var billerModule = angular.module('billerModule');

	billerModule.controller('ReportTerminalsCtrl', [ '$scope', '$location', '$window', function($scope, $location, $window) {
		$scope.generate = function() {
			var url = 'rest/report/terminals';
			if($scope.reportData != null && $scope.reportData.company != null && $scope.reportData.company.id != null) {
				url += '?companyId=' + $scope.reportData.company.id;
			}
			$window.open(url, '_blank');
		};
	}]);

	billerModule.controller('ReportLiquidationsCtrl', [ '$scope', '$location', '$window', '$filter', function($scope, $location, $window, $filter) {
		$scope.reportData = {};
		$scope.generate = function() {
			var ids = '';
			if($scope.reportData != null && $scope.reportData.company != null) {
				ids = $scope.reportData.company.id;
			}
			var from = $scope.reportData.from != null ? $filter('date')($scope.reportData.from, "yyyy-MM-dd") : '';
			var to = $scope.reportData.to != null ? $filter('date')($scope.reportData.to, "yyyy-MM-dd") : '';
			var url = 'rest/report/liquidations?ids=' + ids + '&from=' + from + '&to=' + to;
			$window.open(url, '_blank');
		};
	} ]);
	
	billerModule.controller('ReportLiquidationsSummaryCtrl', [ '$scope', '$location', '$window', '$filter', function($scope, $location, $window, $filter) {
		$scope.reportData = {};
		$scope.generate = function() {
			var from = $scope.reportData.from != null ? $filter('date')($scope.reportData.from, "yyyy-MM-dd") : '';
			var to = $scope.reportData.to != null ? $filter('date')($scope.reportData.to, "yyyy-MM-dd") : '';
			var companyId = $scope.reportData.company != null ? $scope.reportData.company.id : "";
			var costCenterId = $scope.reportData.costCenter != null ? $scope.reportData.costCenter.id : "";
			var url = 'rest/report/liquidation-summary?from=' + from + '&to=' + to + "&companyId=" + companyId + "&costCenterId=" + costCenterId;
			$window.open(url, '_blank');
		};
	}]);

})();