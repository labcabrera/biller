'use strict';

/* Services */

var billerServices = angular.module('billerServices', [ 'ngResource' ]);

billerServices.factory('CompanyGroup', [ '$resource', function($resource) {
	return $resource('rest/groups/:id', {}, {
		query : {
			method : 'GET',
			params : {
				id : 'find'
			},
			isArray : true
		}
	});
} ]);

billerServices.factory('Company', [ '$resource', function($resource) {
	return $resource('rest/companies/:id', {}, {
		query : {
			method : 'GET',
			params : {
				id : 'find'
			},
			isArray : true
		}
	});
} ]);

billerServices.factory('Province', [ '$resource', function($resource) {
	return $resource('rest/provinces/find/:expression', {}, {
		query : {
			method : 'GET',
			params : {
				expression : 'Ma'
			},
			isArray : true
		}
	});
} ]);
