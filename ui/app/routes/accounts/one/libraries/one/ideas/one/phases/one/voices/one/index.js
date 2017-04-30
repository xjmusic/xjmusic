// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
    return Ember.RSVP.hash({
      phase: phase,
      voice: voice,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.voice.get('description'),
      entity: {
        name: 'Voice',
        id: model.voice.get('id')
      }
    });
  },

});
