// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let pattern = this.modelFor('accounts.one.libraries.one.patterns.one');
    return hash({
      pattern: pattern,
      phaseToAdd: null,
      patternPhases: this.store.query('phase', { patternId: pattern.id }),
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
