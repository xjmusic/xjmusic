//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {hash} from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*} hash
   */
  model() {
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    return hash({
      sequence: sequence,
      patternToAdd: null,
      sequencePatterns: this.store.query('pattern', {sequenceId: sequence.id}),
    }, 'sequence, sequence patterns, pattern to add to sequence');
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

  },

});
