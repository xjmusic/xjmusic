/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      this.config.getConfig().then(
        () => {
          let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
          let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
          audio.set('instrument', instrument);

          resolve(hash({
            instruments: this.store.query('instrument', {}),
            audio: audio
          }, 'instruments, audio'));
        },
        (error) => {
          reject('Could not instantiate new Audio', error);
        }
      );
    });
  },


  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    moveAudio(model) {
      let instrument = model.get('instrument');
      let library = instrument.get('library');
      let account = library.get('account');

      model.save().then(
        () => {
          get(this, 'display').success('Moved audio "' + model.get('name') + '" to instrument "' + instrument.get('name') + '"');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', account, library, instrument, model);
        },
        (error) => {
          get(this, 'display').error(error);
        });

    },

    cancel() {
      history.back();
    },

  },

});
