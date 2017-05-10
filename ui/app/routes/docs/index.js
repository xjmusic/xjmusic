// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {Promise.<T>}
   */
  model: function () {
    return new Ember.RSVP.Promise((resolve, reject) => {
      Ember.$.ajax({
        url: '/api/1/docs',
        method: 'GET',
        xhrFields: {
          withCredentials: true
        }
      }).then(
        (docs) => {
          resolve(docs);
        },
        (error) => {
          reject('could not load docs index', error);
        }
      );
    });
  },

  /**
   * Headline
   */
  afterModel() {
    Ember.set(this, 'routeHeadline', {
      doc: true,
      title: 'Docs'
    });
  },

  /**
   * Route Actions
   */
  actions: {}

});
