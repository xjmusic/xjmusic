// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    let chord = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.chords.one');
    return Ember.RSVP.hash({
      phase: phase,
      chord: chord,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.chord.getTitle(),
      entity: {
        name: 'Chord',
        id: model.chord.get('id')
      }
    });
  },

});
