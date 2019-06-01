// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

/**
 * @type {string} Base URL of Hub API
 */
const API_BASE_URL = '/api/1/';

/**
 * Access the Hub API at same JavaScript origin
 */
export class API {
  name = 'XJ Music® API';

  /**
   * @type {Object} to cache configuration in
   * @private
   */
  _config = null;

  /**
   * Get configuration and execute function with resulting (cached) data
   * @param {Function} thenFunc to execute with configuration data
   */
  config(thenFunc) {
    let self = this;
    if (self._config) {
      thenFunc(self._config);
    } else {
      $.getJSON(API_BASE_URL + 'config', function (data) {
        if ('config' in data) {
          self._config = data['config'];
          thenFunc(self._config);
        } else {
          console.error("Failed to load configuration from API!", data);
        }
      });
    }
  }

  /**
   * Get configuration and execute function with resulting (cached) data
   * @param identifier
   * @param {Function} thenFunc to execute with configuration data
   */
  chain(identifier, thenFunc) {
    $.getJSON(API_BASE_URL + 'chains/' + identifier, function (data) {
      if ('chain' in data) {
        thenFunc(data['chain']);
      } else {
        console.error("Failed to load chain #" + identifier + " from API!", data);
      }
    });
  }

  /**
   * Get configuration and execute function with resulting (cached) data
   * @param identifier
   * @param {Function} thenFunc to execute with configuration data
   */
  segments(identifier, thenFunc) {
    $.getJSON(API_BASE_URL + 'segments?chainId=' + identifier, function (data) {
      if ('segments' in data) {
        thenFunc(data['segments']);
      } else {
        console.error("Failed to load segments for chain #" + identifier + " from API!", data);
      }
    });
  }

}
