/*jslint plusplus: true, vars: true*/
/*global angular, window, console, btoa */

(function (undefined) {
  'use strict';

  var module = angular.module('services');

  var ApiService = function (configService, $http, Upload) {

    /**
     * Receive some data from backend
     * @param collection/resource i. e. boards or tasks
     * @param parameters to specify collection
     * @returns list of collection
     */
    this.query = function (collection, parameters) {
      parameters = parameters || {};
      var uri = configService.baseUrl + collection;
      return $http(
        {method: "GET", url: uri, params: parameters, cache: false}
      );
    };

    /**
     * Query the specified object
     * @param collection i. e. board
     * @param id
     * @param parameters
     * @returns collection item
     */
    this.queryById = function (collection, id, parameters) {
      parameters = parameters || {};
      var uri = configService.baseUrl + collection + '/' + id;
      return $http({method: "GET", url: uri, params: parameters, cache: false});
    };

    /**
     * Create / Save Object
     * @param collection
     * @param object
     * @param attachments files to upload
     * @returns
     */
    this.createObject = function (collection, object, attachments) {
      var uri = configService.baseUrl + collection;
      var method = "POST";
      var headers = {};

      // Attachments as form data with Uploader
      if(attachments){
        return Upload.upload({
          url: uri,
          headers: headers,
          method: method,
          data: {file: attachments, info: Upload.jsonBlob(object)},
          cache: false
        });
      }else{
        var data = JSON.stringify(object);
        // Request without attachments
        return $http({method: method, url: uri, data:data,
          headers: headers, cache: false
        });
      }
    };

    /**
     * Update the Object
     * @param collection for example friends, products
     * @param object
     * @returns true/false
     */
    this.updateObject = function (collection, object, attachments, subResource) {
      var uri = configService.baseUrl + collection + "/" + object.id;
      if(subResource){
        uri = uri + "/" + subResource;
      }
      var method = "PUT";
      var headers = {};

      // files as form data
      if(attachments){
        return Upload.upload({
          url: uri,
          headers: headers,
          method: method,
          data: {file: attachments, info: Upload.jsonBlob(object)},
          cache: false
        });
      }else{
        var data = JSON.stringify(object);
        // Request without attachments
        return $http({method: method, url: uri, data:data,
          headers: headers, cache: false
        });
      }
    };

    /**
     * Delete the specified object
     * @param collection
     * @param object
     * @returns {*}
     */
    this.deleteObject = function (collection, object) {
      var uri = configService.baseUrl + collection + "/" +object.id;
      return $http({method: "DELETE", url: uri, cache: false, data: JSON.stringify(object)});
    };

    /**
     * Returns the server url
     * @returns {*|string}
       */
    this.getPath = function(){
        return configService.baseUrl;
    }

  };

  ApiService.$inject = ['ConfigService', '$http', 'Upload'];

  module.service('ApiService', ApiService);
})();
