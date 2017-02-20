// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    let chord = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.chords.one');
    return Ember.RSVP.hash({
      phase: phase,
      chord: chord,
    });
  },

});
