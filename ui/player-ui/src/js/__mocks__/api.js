// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
'use strict';

export class API {

  /**
   * @type {object}
   */
  options = null;

  /**
   * Instantiate a new API
   */
  constructor() {
    console.debug('Mock API instantiated.');
  }

  /**
   *
   * @param id
   * @param callback
   */
  chain(id, callback) {
    console.log("Mock API called", id);
  }

}
