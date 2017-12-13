// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let phase = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
    let voice = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.voices.one');
    return hash({
      phase: phase,
      voice: voice,
    });
  },

});
