(function() {
	
	var billerModule = angular.module('billerModule');

	/* ----------------------------------------------------------------------------
	 * USUARIOS
	 * ----------------------------------------------------------------------------
	 */
	
	billerModule.controller('LoginCtrl', [ '$scope', '$rootScope', '$location', '$http', function($scope, $rootScope, $location, $http) {
		$scope.login = function(user, password) {
			var request = { "user": $scope.username, "password": $scope.password};
			$http.post('rest/security/login', request).success(function(data) {
				switch(data.code) {
				case '200':
					//billerModule.run(function($http) {
						  $http.defaults.headers.common.sessionId = data.payload.session;
					//});
					$rootScope.user = data.payload;
					$location.url("index");
					break;
				case '404':
					$rootScope.loginResult = "Usuario no válido";
					break;
				case '401':
					$rootScope.loginResult = "Password incorrecta";
					break;
				};
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