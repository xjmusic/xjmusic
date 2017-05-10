// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return new Ember.RSVP.Promise((resolve, reject) => {
      Ember.$.ajax({
        url: '/api/1/docs/' + params.doc_key,
        method: 'GET',
        xhrFields: {
          withCredentials: true
        }
      }).then(
        (result) => {
          resolve(result.doc);
        },
        (error) => {
          self.transitionTo('docs');
          reject('could not load doc', error);
        }
      );
    });
  },

  /**
   * Route Breadcrumb
   * @param model
   */
  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.name
    });
    Ember.set(this, 'routeHeadline', {
      doc: true,
      // title: model.name NOT included because these markdown files start with a big H1
    });
  }

});
