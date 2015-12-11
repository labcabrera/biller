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
	
	billerModule.controller('UserDetailCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $filter, dialogs, messageService) {
		if(messageService.hasMessage()) {
			$scope.message = messageService.getMessage();
		}
		$scope.load = function() {
			$http.get('rest/users/id/' + $routeParams.id).success(function(data) {
				$scope.entity = data;
				$http.get('rest/users/roles').success(function(data) {
					$scope.availableRoles = data;
					var current = [];
					for(var i=0; i<$scope.entity.roles.length; i++) {
						current.push($scope.entity.roles[i].id);
					};
					console.log("current: " + current);
					for(var i = $scope.availableRoles.length -1; i >= 0 ; i--){
					    if(current.indexOf($scope.availableRoles[i].id) > -1){
					    	$scope.availableRoles.splice(i, 1);
					    }
					};
				});
			});
			$rootScope.isReadOnly = true;
		};
		$scope.update = function() {
			$http.post('rest/users/merge/', $scope.entity).success(function(data) {
				$scope.message = data;
				$rootScope.isReadOnly = true;
			});
		};
		$scope.remove = function() {
			var dlg = dialogs.confirm($filter('translate')('remove.confirmation.title') ,$filter('translate')('user.remove.confirmation'));
			dlg.result.then(function(btn){
				$http.post('rest/users/remove/' + $scope.entity.id).success(function(data) {
					if(data.code == 200) {
						$location.path("admin/users");
					} else {
						$scope.message = data;
					}
				});				
			});
		};
		$scope.addRole = function(role) {
			console.log("add role " + role)
			if($scope.entity.roles == null) {
				$scope.entity.roles = [];
			}
			$scope.entity.roles.push(role);
			console.log("index " + role)
			$scope.availableRoles.splice($scope.availableRoles.indexOf(role), 1);
		};
		$scope.removeRole = function(role) {
			console.log("remove role " + role)
			var index = $scope.entity.roles.indexOf(role);
			console.log("index " + role)
			$scope.entity.roles.splice(index, 1);
			$scope.availableRoles.push(role);
		};
		$scope.load();
	} ]);
	
	billerModule.controller('UserNewCtrl', [ '$scope', '$rootScope', '$routeParams', '$http', '$filter', 'dialogs', 'messageService', function($scope, $rootScope, $routeParams, $http, $filter, dialogs, messageService) {
		$scope.load = function() {
			$rootScope.isReadOnly = false;
		}
		$scope.load();
	} ]);	
	
})();