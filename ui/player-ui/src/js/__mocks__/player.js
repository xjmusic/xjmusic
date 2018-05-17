// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
'use strict';

export class Player {

  /**
   * @type {object}
   */
  options = null;

  /**
   * Instantiate a new Player
   * @param {Object} options
   */
  constructor(options) {
    console.debug('Mock player instantiated with options', options);
    this.options = options;
  }
}
