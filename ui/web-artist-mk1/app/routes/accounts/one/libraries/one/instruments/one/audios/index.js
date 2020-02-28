/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {Promise} that returns {*} hash
   */
  model: function () {
    let auth = this.get('auth');

    if (auth.isArtist || auth.isAdmin) {
      return new EmberPromise((resolve, reject) => {
        this.config.getConfig().then(
          () => {
            let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
            let audioQuery = {
              instrumentId: instrument.get('id'),
              include: 'events,chords',
            };
            let audios = this.store.query('instrument-audio', audioQuery)
              .catch((error) => {
                get(this, 'display').error(error);
                this.transitionTo('');
              });
            resolve(hash({
              instrument: instrument,
              audioBaseUrl: this.config.audioBaseUrl,
              audios: audios,
            }, 'instrument, audios, audio base URL'));
          },
          (error) => {
            reject(error);
          }
        );
      });
    } else {
      history.back();
    }
  },

  /**
   * Route Actions
   */
  actions: {

    editAudio(model) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', model.instrument.library.account, model.instrument.library, model.instrument, model);
    },
  }

});
