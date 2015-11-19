(function() {

	var appModule = angular.module('billerModule');

	appModule.controller('DashboardCtrl', [ '$scope', function($scope) {
	
	}]);

	appModule.factory('colorChartService', function() {
		var highlight = 20;
		var red = "#bf616a",
			blue = "#5B90BF",
			orange = "#d08770",
			yellow = "#ebcb8b",
			green = "#a3be8c",
			teal = "#96b5b4",
			pale_blue = "#8fa1b3",
			purple = "#b48ead",
			brown = "#ab7967";
			colorList = [red, blue, orange, yellow, green, teal, pale_blue, purple, brown];
		
		function Colour(col, amt) {
			var usePound = false;
			if (col[0] == "#") {
				col = col.slice(1);
				usePound = true;
			}
			var num = parseInt(col,16);
			var r = (num >> 16) + amt;
			if (r > 255) r = 255;
			else if  (r < 0) r = 0;
			var b = ((num >> 8) & 0x00FF) + amt;
			if (b > 255) b = 255;
			else if  (b < 0) b = 0;
			var g = (num & 0x0000FF) + amt;
			if (g > 255) g = 255;
			else if (g < 0) g = 0;
			return (usePound?"#":"") + (g | (b << 8) | (r << 16)).toString(16);
		}
		
        return {
            getColor: function(index) {
            	return colorList[index%colorList.length];
            },
            getColorHighlight: function(index) {
            	return Colour(colorList[index%colorList.length], highlight);
            }
        };
		
	});

	appModule.directive('pieChart', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/dashboard/dashboard-pie.html',
			controller : [ '$scope', '$rootScope', '$http', '$filter', 'colorChartService', function($scope, $rootScope, $http, $filter, colorChartService) {
				$scope.date = new Date();
				$scope.loadData = function() {
					$http.get($scope.restPath, {
						params : {
							year : $scope.year
						}
					}).success(function(records) {
						$scope.records = records;
						for (var i = 0; i < $scope.records.length; i++) {
							$scope.records[i].color = colorChartService.getColor(i);
							$scope.records[i].highlight = colorChartService.getColorHighlight(i);
							$scope.records[i].label = $filter('translate')($scope.records[i].label);
						}
						$scope.hasData = $scope.records.length != 0;
						$scope.myData = $scope.records;
						$scope.myOptions = {
						 	legendTemplate : '<ul class="tc-chart-js-legend"><% for (var i=0; i<segments.length; i++){%><li><span style="background-color:<%=segments[i].fillColor%>"></span><%if(segments[i].label){%><%=segments[i].label%><%}%></li><%}%></ul>'
						};
					});
				};
				$scope.$watch('year', function() {
					$scope.loadData();
				});
			}],
			scope : {
				restPath : '@',
				panelName : '@'
			},
		};
	});
	
	appModule.directive('barLineChart', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/dashboard/dashboard-line.html',
			controller : [ '$scope', '$rootScope', '$http', '$translate', function($scope, $rootScope, $http, $translate) {
				$scope.loadData = function() {
					$http.get($scope.restPath, {
						params : {
							year : $scope.year
						}
					}).success(function(records) {
						var labels = new Array();
						var dataRecords = new Array();
						for(var i = 0; i < records.length; i++) {
							labels[i] = records[i].label;
							dataRecords[i] = records[i].value;
						}
						$scope.hasData = dataRecords.length != 0;
						$scope.myData = {
						    labels: labels,
						    datasets: [
						        {
						            fillColor: "rgba(220,220,220,0.2)",
						            strokeColor: "rgba(220,220,220,1)",
						            pointColor: "rgba(220,220,220,1)",
						            pointStrokeColor: "#fff",
						            pointHighlightFill: "#fff",
						            pointHighlightStroke: "rgba(220,220,220,1)",
						            data: dataRecords
						        }
						    ]
						};
					});
				};
				$scope.$watch('year', function() {
					$scope.loadData();
				});
			} ],
			scope : {
				restPath : '@',
				chartType : '@',
				panelName : '@'
			},
		};
	});

	appModule.directive('chartYear', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/dashboard/chart-year.html',
			controller : ['$scope', function($scope) {
				$scope.currentYear = new Date().getFullYear();
			}],
			scope : {
				ngModel : '=',
			}
		};
	});


	appModule.directive('dashboardPendingLiquidations', function() {
		return {
			restrict : 'AE',
			templateUrl : 'html/dashboard/dashboard-pending-liquidations.html',
			controller : 'DashboardPendingLiquidationsCtrl',
			scope : {
			}
		};
	});
	
	appModule.controller('DashboardPendingLiquidationsCtrl', ['$scope', '$rootScope', '$routeParams', '$http', '$modal', '$translate', function($scope, $rootScope, $routeParams, $http, $modal, $translate) {
		$scope.searchResults = {};
		$scope.setPage  = function(page) {
			$scope.currentPage = page;
			$http.get('rest/dashboard/liquidation/pending', {
				params : {
					p : $scope.currentPage,
					n : 4
				}
			}).success(function(data) {
				$scope.searchResults = data;
			});
		};
		$scope.setPage(1);
	}]);

})();