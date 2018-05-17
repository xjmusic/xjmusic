// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    let voice = this.modelFor('accounts.one.libraries.one.sequences.one.voices.one');
    return hash({
      sequence: sequence,
      voice: voice,
    });
  },

});
