// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {Player} from "player";

/**
 Application
 */
export class Application {

  /**
   * @type {Player}
   */
  player;

  /**
   * @type {Object}
   */
  options;

  /**
   * Create new application
   * @param {String} url of application, including hash of options key-values
   */
  constructor(url) {
    this.options = Application.parseOptions(url);
    this.player = new Player(this.options);
  }

  /**
   * XSS attack proof.
   * @see https://stackoverflow.com/questions/1822598/getting-url-hash-location-and-using-it-in-jquery
   * @param {String} url of application, including hash of options key-values
   * @returns {Object} key-values of options found after hash in URL
   */
  static parseOptions(url) {
    let hashPos = url.indexOf('#');
    if (hashPos < 0) return {};
    let hashString = url.substr(hashPos + 1);
    let options = {};
    hashString.replace(/([^=&]+)=([^&]*)/gi, function (m, key, value) {
      if (value > 0 || value < 0) {
        options[key] = Number(value);
      } else {
        options[key] = value;
      }
    });
    return options;
  }

}
