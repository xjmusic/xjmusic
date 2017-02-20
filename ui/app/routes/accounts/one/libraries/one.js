// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model(params) {
    return this.store.findRecord('library', params.library_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.libraries');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', model);
  }

});
