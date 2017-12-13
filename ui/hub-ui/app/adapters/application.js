// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import $ from 'jquery';

import { get } from '@ember/object';
import { dasherize } from '@ember/string';
import { pluralize} from 'ember-inflector';
import DS from "ember-data";

/*
import DataAdapterMixin from "ember-simple-auth/mixins/data-adapter-mixin";
export default DS.JSONAPIAdapter.extend(DataAdapterMixin, {
  authorizer: 'authorizer:some'
});
*/

export default DS.RESTAdapter.extend({

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
          include: "links"
        }
      }
    }).then(...);

   * @param id
   * @param modelName
   * @param snapshot
   * @returns {*}
   */
  urlForFindRecord(id, modelName, snapshot) {
    let url = this._super(...arguments);
    let query = get(snapshot, 'adapterOptions.query');
    if (query) {
      url += '?' + $.param(query); // assumes no query params are present already
    }
    return url;
  }
});
