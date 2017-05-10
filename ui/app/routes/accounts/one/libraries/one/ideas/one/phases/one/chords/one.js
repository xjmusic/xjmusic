// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  model(params) {
    let self = this;
    return this.store.findRecord('phase-chord', params.chord_id)
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.getTitle()
    });
  }

});
