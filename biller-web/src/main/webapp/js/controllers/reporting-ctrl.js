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
			$scope.message = null;
			var from = $scope.reportData.from != null ? $filter('date')($scope.reportData.from, "yyyy-MM-dd") : null;
			var to = $scope.reportData.to != null ? $filter('date')($scope.reportData.to, "yyyy-MM-dd") : null;
			var companyId = $scope.reportData.company != null ? $scope.reportData.company.id : "";
			var costCenterId = $scope.reportData.costCenter != null ? $scope.reportData.costCenter.id : "";
			var companyGroupId = $scope.reportData.companyGroup != null ? $scope.reportData.companyGroup.id : "";
			if(from == null || to == null) {
				$scope.message = {code: 200, warnings: [ 'Es necesario establecer las fechas' ]};
				return;
			}
			var url = 'rest/report/liquidation-summary?from=' + from;
			url += '&to=' + to;
			url += "&companyId=" + companyId;
			url += "&costCenterId=" + costCenterId;
			url += "&companyGroupId=" + companyGroupId;
			$window.open(url, '_blank');
		};
	}]);

})();