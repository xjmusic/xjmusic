// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {Player} from "player";

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
 * HTML and class to add to body of page when browser is unsupported
 */
const UNSUPPORTED_BROWSER_BODY_CLASS = 'unsupported-browser';
const UNSUPPORTED_BROWSER_PRE_HTML = '<strong>';
const UNSUPPORTED_BROWSER_POST_HTML = '</strong><br/><small>web browser unsupported!</small>';

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
      case SAFARI:
      case EXPLORER:
      case EXPLORER_6:
        let body = $('body');
        let statusText = $('div#status-text');
            body.addClass(UNSUPPORTED_BROWSER_BODY_CLASS);
        statusText.html(UNSUPPORTED_BROWSER_PRE_HTML + this.browser + UNSUPPORTED_BROWSER_POST_HTML);
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

  /**
   * Detect the browser
   * @returns {string} constant of detected browser
   */
  static detectBrowser() {
    if (!!navigator.userAgent.match(/Version\/[\d\.]+.*Safari/)) return SAFARI;
    if (navigator.appName == 'Microsoft Internet Explorer') return EXPLORER;
    if (navigator.userAgent.toLowerCase().indexOf('firefox') > -1) return FIREFOX;
    if (navigator.userAgent.match(/Opera\/9.80/i)) return OPERA;
    if (!!window.chrome) return CHROME;
    return UNKNOWN;
  }

}
