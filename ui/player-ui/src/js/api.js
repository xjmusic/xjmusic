// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

/**
 * @type {string} Base URL of Hub API
 */
const API_BASE_URL = '/api/1/';

/**
 * Access the Hub API at same JavaScript origin
 */
export class API {
  name = 'XJ Music™ API';

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
      fetch(API_BASE_URL + 'config')
        .then((resp) => resp.json())
        .then(function (payload) {
          if (payload.hasOwnProperty('data')) {
            thenFunc(payload.data.attributes);
          } else {
            console.error("Failed to load chain #" + identifier + " from API!", payload);
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
    fetch(API_BASE_URL + 'chains/' + identifier)
      .then((resp) => resp.json())
      .then(function (payload) {
        if (payload.hasOwnProperty('data') && payload.data.hasOwnProperty('type') && 'chains' === payload.data.type) {
          let chain = payload.data.attributes;
          chain['id'] = payload.data.id;
          thenFunc(chain);
        } else {
          console.error("Failed to load chain #" + identifier + " from API!", payload);
        }
      });
  }

  /**
   * Get configuration and execute function with resulting (cached) data
   * @param identifier
   * @param {Function} thenFunc to execute with configuration data
   */
  segments(identifier, thenFunc) {
    fetch(API_BASE_URL + 'segments?chainId=' + identifier)
      .then((resp) => resp.json())
      .then(function (payload) {
        if (payload.hasOwnProperty('data') && 0 < payload.data.length && payload.data[0].hasOwnProperty('type') && 'segments' === payload.data[0].type) {
          let segments = [];
          payload.data.forEach(data => {
            let segment = data.attributes;
            segment['id'] = data.id;
            segments.push(segment);
          });
          thenFunc(segments);
        } else {
          console.error("Failed to load segments for chain #" + identifier + " from API!", payload);
        }
      });
  }

}
