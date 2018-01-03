// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let pattern = this.modelFor('accounts.one.libraries.one.patterns.one');
    let voice = this.modelFor('accounts.one.libraries.one.patterns.one.voices.one');
    return hash({
      pattern: pattern,
      voice: voice,
    });
  },

});
