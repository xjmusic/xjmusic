/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {hash} from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chord = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one.chords.one');
    return hash({
      audio: audio,
      chord: chord,
    }, 'audio, chord');
  },

});
