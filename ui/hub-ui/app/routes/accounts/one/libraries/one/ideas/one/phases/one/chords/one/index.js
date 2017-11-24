// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    let chord = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.chords.one');
    return hash({
      phase: phase,
      chord: chord,
    });
  },

});
