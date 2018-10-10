//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: configuration service
  config: service(),

  /**
   * Route Model
   * @returns {Ember.RSVP.Promise} that returns {*} hash
   */
  model: function () {
    let self = this;
    let auth = this.get('auth');

    if (auth.isArtist || auth.isAdmin) {
      return new EmberPromise((resolve, reject) => {
        get(self, 'config').promises.config.then(
          (config) => {
            let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
            let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
            resolve(hash({
              instrument: instrument,
              audioBaseUrl: config.audioBaseUrl,
              audio: audio,
            }, 'instrument, audio, audio base URL'));
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

});
