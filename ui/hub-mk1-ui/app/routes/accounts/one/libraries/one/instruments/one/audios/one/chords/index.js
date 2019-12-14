// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {hash} from 'rsvp';

import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let self = this;
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chords = this.store.query('instrument-audio-chord', {instrumentAudioId: audio.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      audio: audio,
      chords: chords,
    }, 'audio, chords');
  },

  /**
   * Route Actions
   */
  actions: {

    editChord(model) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords.one', model.instrumentAudio.instrument.library.account, model.instrumentAudio.instrument.library, model.instrumentAudio.instrument, model.instrumentAudio, model);
    },
  }

});
