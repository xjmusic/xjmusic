// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function () {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    let chords = this.store.query('phase-chord', {phaseId: phase.get('id')})
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('');
      });
    return Ember.RSVP.hash({
      phase: phase,
      chords: chords,
    });
  },

  actions: {

    editChord(chord) {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords.one', chord);
    },

  }
});
