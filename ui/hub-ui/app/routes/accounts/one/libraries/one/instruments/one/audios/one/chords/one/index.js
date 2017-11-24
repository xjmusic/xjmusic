// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.

import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chord = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one.chords.one');
    return hash({
      audio: audio,
      chord: chord,
    });
  },

});
