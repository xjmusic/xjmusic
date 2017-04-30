// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  model(params) {
    let self = this;
    return this.store.findRecord('audio', params.audio_id)
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries.one.instruments.one.audios');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.get("name")
    });
  }

});
