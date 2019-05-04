//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {get} from '@ember/object';
import fetch from 'fetch';
import Service, {inject as service} from '@ember/service';
import RSVP from "rsvp";

export default Service.extend({

  /**
   * Stores promises;
   * Actual values will be set on the main config-service object
   */
  promises: {},

  /**
   * Sets configuration properties, each to its own Promise.
   */
  init() {

    // main promise
    this.promises.config = this.newMainPromise();

    // subsequent promises
    this.promises.apiBaseUrl = this.newSubPromise("apiBaseUrl");
    this.promises.audioBaseUrl = this.newSubPromise("audioBaseUrl");
    this.promises.baseUrl = this.newSubPromise("baseUrl");
    this.promises.chainConfigTypes = this.newSubPromise("chainConfigTypes");
    this.promises.chainStates = this.newSubPromise("chainStates");
    this.promises.chainTypes = this.newSubPromise("chainTypes");
    this.promises.choiceTypes = this.newSubPromise("choiceTypes");
    this.promises.sequenceTypes = this.newSubPromise("sequenceTypes");
    this.promises.patternTypes = this.newSubPromise("patternTypes");
    this.promises.patternDetailTypes = this.newSubPromise("patternDetailTypes");
    this.promises.instrumentTypes = this.newSubPromise("instrumentTypes");
    this.promises.segmentStates = this.newSubPromise("segmentStates");
    this.promises.segmentBaseUrl = this.newSubPromise("segmentBaseUrl");
    this.promises.voiceTypes = this.newSubPromise("voiceTypes");
  },

  /**
   * Returns a promise of the configuration value for a given key
   * @param key
   */
  newSubPromise(key) {
    let self = this;
    return new RSVP.Promise(function (resolve, reject) {
      self.promises.config.then(
        (data) => {
          if (data.hasOwnProperty(key)) {
            // set the resulting value on this key of the main config-service object
            self[key] = data[key];
            // send the resulting value to the promise resolver
            resolve(data[key]);
          } else {
            reject('Platform configuration is missing "' + key + '"');
          }
        },
        (error) => {
          reject('Failed to get "' + key + '" from platform configuration: ' + error);
        }
      );
    });
  },

  /**
   * Get the platform configuration from the backend
   * as a promise, such that any subsequent getters are blocked by the API call
   * and any subsequent implementors of those getters are blocked
   * and their subsequent actions are blocked
   * until everything is ready
   */
  newMainPromise() {
    let self = this;

    return new RSVP.Promise(
      function (resolve, reject) {

        fetch('/api/1/config', {
          headers: {
            'Content-Type': 'application/json'
          },
        }).then(function (response) {
          return response.json();
        }).then(
          (data) => {
            if (data.hasOwnProperty('config')) {
              resolve(data['config']);
            } else {
              self.sendRejection(reject, 'Platform configuration is invalid.');
            }
          },
          (error) => {
            self.sendRejection(reject, 'Failed to get platform configuration: ' + error);
          });
      });
  },

  /**
   * Display a flash message and reject with the same message
   * @param reject
   * @param message
   */
  sendRejection(reject, message) {
    get(this, 'display').error(message);
    reject(message);
  },

  // Inject: Flash messages
  display: service()

});
