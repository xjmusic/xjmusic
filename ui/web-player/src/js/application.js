/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {Player} from "player";
import {DOM} from "dom";

/**
 Browser constants
 * @type {string}
 */
const CHROME = 'Chrome';
const EXPLORER_6 = 'Internet Explorer 6';
const EXPLORER = 'Inter et Explorer';
const FIREFOX = 'Firefox';
const OPERA = 'Opera';
const SAFARI = 'Safari';
const UNKNOWN = 'Unknown';

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
   * @type {String} browser as detected
   */
  browser = Application.detectBrowser();

  /**
   * @type {String} name of the browser
   */

  /**
   * Create new application
   * @param {String} url of application, including hash of options key-values
   */
  constructor(url) {
    switch (this.browser) {
      case EXPLORER:
      case EXPLORER_6:
        let bodyEl = document.querySelector('bodyEl');
        let statusEl = document.getElementById('status-text');
        DOM.addClass(bodyEl, 'unsupported-browser');
        DOM.setHTML(statusEl, `<strong>${this.browser}</strong><br/><small>web browser unsupported!</small>`);
        console.error(this.browser, 'web browser is not supported');
        break;
      default:
        console.info(this.browser, 'web browser is supported');
        this.initPlayer(Application.parseOptions(url));
    }
  }

  /**
   * Start the Player
   * @param {Object} options
   */
  initPlayer(options) {
    this.options = options;
    this.player = new Player(this.options);
  }

  /**
   * XSS attack protection
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

  /**
   * Detect the browser
   * @returns {string} constant of detected browser
   */
  static detectBrowser() {
    if (!!navigator.userAgent.match(/Version\/[\d.]+.*Safari/)) return SAFARI;
    if (navigator.appName === 'Microsoft Internet Explorer') return EXPLORER;
    if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1) return FIREFOX;
    if (navigator.userAgent.match(/Opera\/9.80/i)) return OPERA;
    if (!!window.chrome) return CHROME;
    return UNKNOWN;
  }

}
