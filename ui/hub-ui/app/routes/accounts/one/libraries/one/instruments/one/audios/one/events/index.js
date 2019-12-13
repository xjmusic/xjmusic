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
    let events = this.store.query('instrument-audio-event', {instrumentAudioId: audio.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      audio: audio,
      events: events,
    }, 'audio, events');
  },

  /**
   * Route Actions
   */
  actions: {

    editEvent(model) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.events.one', model.audio.instrument.library.account, model.audio.instrument.library, model.audio.instrument, model.audio, model);
    },

  }
});
