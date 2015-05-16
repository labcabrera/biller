(function() {
	
	var billerModule = angular.module('billerModule');

	/* ----------------------------------------------------------------------------
	 * USUARIOS
	 * ----------------------------------------------------------------------------
	 */
	
	billerModule.controller('LoginCtrl', [ '$scope', '$rootScope', '$location', '$http', function($scope, $rootScope, $location, $http) {
		$scope.login = function(user, password) {
			var request = { "name": $scope.username, "password": $scope.password};
			$http.post('rest/users/login', request).success(function(data) {
				if(data.code == 200) {
					$rootScope.user = data.payload;
					$location.url("index");
				} else {
					$rootScope.loginResult = data.message;
				}
			});
		};
	} ]);
	
	billerModule.controller('UserListCtrl', [ '$scope', '$http', function($scope, $http) {
		$http.get('rest/users').success(function(data) { $scope.users = data; });
		$scope.orderProp = 'name';
	} ]);
	
	billerModule.controller('UserDetailCtrl', [ '$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
		$http.get('rest/users/' + $routeParams.id).success(function(data) { $scope.user = data; });
	} ]);
	
})();