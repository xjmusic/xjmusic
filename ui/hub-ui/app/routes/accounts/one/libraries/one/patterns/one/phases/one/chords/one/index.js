// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let phase = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
    let chord = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.chords.one');
    return hash({
      phase: phase,
      chord: chord,
    });
  },

});
