// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  model(params) {
    return this.store.findRecord('voice_event', params.event_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.get("note") + '(' + model.get("inflection") + ')' + '@' + model.get("position")
    });
  }

});
