// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.

import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
    let event = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events.one');
    return hash({
      voice: voice,
      event: event,
    });
  },

});
