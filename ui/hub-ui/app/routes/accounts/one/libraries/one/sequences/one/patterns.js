// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    return hash({
      sequence: sequence,
      patternToAdd: null,
      sequencePatterns: this.store.query('pattern', { sequenceId: sequence.id }),
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function() {
      this.refresh();
    },

  },

});
