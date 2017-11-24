// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { hash } from 'rsvp';

import { get } from '@ember/object';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let self = this;
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chords = this.store.query('audio-chord', {audioId: audio.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      audio: audio,
      chords: chords,
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editChord(model) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords.one', model);
    },
  }

});
