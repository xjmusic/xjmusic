// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    let phase =this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    return hash({
      idea: idea,
      phase: phase,
    });
  },

});
