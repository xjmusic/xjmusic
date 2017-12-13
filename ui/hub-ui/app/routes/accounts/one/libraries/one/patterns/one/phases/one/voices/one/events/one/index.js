// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let voice = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.voices.one');
    let event = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.voices.one.events.one');
    return hash({
      voice: voice,
      event: event,
    });
  },

});
