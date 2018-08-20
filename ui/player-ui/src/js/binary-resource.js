// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

/* global $ */
/* global ArrayBuffer */

export class BinaryResource {

  /**
   * Send request for binary data from a URL
   * @param {String} method of request
   * @param {String} url of request
   * @param {Function} onSuccess to execute on success
   * @param {Function} onError to execute on error
   */
  static send(method, url, onSuccess, onError) {
    $.ajax({
      url: url,
      dataType: 'binary',
      responseType: 'arraybuffer',
      processData: false,
      method: method,
      success: onSuccess,
      error: onError
    });
  }

}

// Only tweak jquery if NOT inside (Jest) test
if ('undefined' === typeof jest) {
// use this transport for 'binary' data type
  $.ajaxTransport('+binary', function (options, originalOptions, jqXHR) {
    // check for conditions and support for blob / arraybuffer response type
    if (window.FormData && ((options.dataType && (options.dataType === 'binary')) || (options.data && ((window.ArrayBuffer && options.data instanceof ArrayBuffer) || (window.Blob && options.data instanceof Blob))))) {
      return {
        // create new XMLHttpRequest
        send: function (headers, callback) {
          // setup all variables
          let xhr = new XMLHttpRequest(),
            url = options.url,
            type = options.type,
            async = options.async || true,
            // blob or arraybuffer. Default is blob
            dataType = options.responseType || 'blob',
            data = options.data || null,
            username = options.username || null,
            password = options.password || null;

          xhr.addEventListener('load', function () {
            let data = {};
            data[options.dataType] = xhr.response;
            // make callback and send data
            callback(xhr.status, xhr.statusText, data, xhr.getAllResponseHeaders());
          });

          xhr.open(type, url, async, username, password);

          // setup custom headers
          for (let i in headers) {
            if (headers.hasOwnProperty(i)) {
              xhr.setRequestHeader(i, headers[i]);
            }
          }

          xhr.responseType = dataType;
          xhr.send(data);
        },
        abort: function () {
          jqXHR.abort();
        }
      };
    }
  });
}
