// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let pattern = this.modelFor('accounts.one.libraries.one.patterns.one');
    let phase =this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
    return hash({
      pattern: pattern,
      phase: phase,
    });
  },

});
