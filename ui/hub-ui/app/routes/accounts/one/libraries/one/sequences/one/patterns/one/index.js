//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*} hash
   */
  model: function() {
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    let pattern =this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
    return hash({
      sequence: sequence,
      pattern: pattern,
    }, 'pattern and its parent sequence');
  },

});
