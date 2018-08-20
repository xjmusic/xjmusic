//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let pattern = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
    let chord = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one.chords.one');
    return hash({
      pattern: pattern,
      chord: chord,
    });
  },

});
