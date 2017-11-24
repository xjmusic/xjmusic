// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    return hash({
      idea: idea,
      phaseToAdd: null,
      ideaPhases: this.store.query('phase', { ideaId: idea.id }),
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
