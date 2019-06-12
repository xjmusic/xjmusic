//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {get} from '@ember/object';
import {dasherize} from '@ember/string';
import {pluralize} from 'ember-inflector';

import JSONAPIAdapter from '@ember-data/adapter/json-api';

const dontFetch = [
  "audio",
  "audio-chord",
  "audio-event",
  "instrument-meme",
  "pattern",
  "pattern-event",
  "program-meme",
  "sequence",
  "sequence-binding",
  "sequence-chord",
  "sequence-meme",
  "voice"
];

export default JSONAPIAdapter.extend({

  // root URL of API
  namespace: 'api/1',

  /**
   Path for a type of entity
   * @param type
   * @returns {*}
   */
  pathForType(type) {
    let dashed = dasherize(type);
    return pluralize(dashed);
  },

  /**
   Custom URL for findRecord

   Cribbed from https://github.com/emberjs/data/issues/3596#issuecomment-126604014
   This makes it possible to send query parameters with a findRecord():

   this.get('store').findRecord('chain', chain.get('id'), {
      adapterOptions: {
        query: {
          include: "segments"
        }
      }
    }).then(...);

   * @param id
   * @param modelName
   * @param snapshot
   * @returns {*}
   */
  urlForFindRecord(id, modelName, snapshot) {
    if (dontFetch.includes(modelName)) console.warn("Front-end should not attempt to fetch", modelName);

    let url = this._super(...arguments);
    let query = get(snapshot, 'adapterOptions.query');
    if (query) {
      url += '?' + this.serializeParams(query); // assumes no query params are present already
    }
    return url;
  },


  /**
   Custom URL for createRecord

   Cribbed from https://github.com/emberjs/data/issues/3596#issuecomment-126604014
   This makes it possible to send query parameters with a createRecord():

   this.get('store').createRecord(chain, {
      adapterOptions: {
        query: {
          cloneId: "87983"
        }
      }
    }).then(...);

   * @param modelName
   * @param snapshot
   * @returns {*}
   */
  urlForCreateRecord(modelName, snapshot) {
    let url = this._super(...arguments);
    let query = get(snapshot, 'adapterOptions.query');
    if (query) {
      url += '?' + this.serializeParams(query); // assumes no query params are present already
    }
    return url;
  },

  /**
   * Serialize parameters into query string
   * @param query parameters to serialize
   * @return {string} query parameter string
   */
  serializeParams(obj) {
    return Object.entries(obj).map(([key, val]) => `${key}=${val}`).join('&');
  }
});
