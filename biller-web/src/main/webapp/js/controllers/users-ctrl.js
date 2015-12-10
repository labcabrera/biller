(function() {
	
	var billerModule = angular.module('billerModule');

	billerModule.controller('LoginCtrl', [ '$scope', '$rootScope', '$location', '$http', '$window', '$cookies', function($scope, $rootScope, $location, $http, $window, $cookies) {
		$scope.login = function(user, password) {
			var request = { "user": $scope.username, "password": $scope.password};
			$http.post('rest/security/login', request).success(function(data) {
				switch(data.code) {
				case '200':
					var sessionid = data.payload.session;
					$cookies.put('biller.sessionid', sessionid);
					$cookies.put('biller.user', JSON.stringify(data.payload));
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
		$http.get('rest/users/find').success(function(data) {
			$scope.results = data;
		});
		$scope.orderProp = 'name';
	} ]);
	
	billerModule.controller('UserDetailCtrl', [ '$scope', '$routeParams', '$http', function($scope, $routeParams, $http) {
		$http.get('rest/users/' + $routeParams.id).success(function(data) { $scope.user = data; });
	} ]);
	
})();