// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      let self = this;
      get(this, 'config').promises.config.then(
        () => {
          resolve(self.resolvedModel());
        },
        (error) => {
          reject('Could not instantiate new Audio', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let fromAudio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    this.set('fromAudioId', fromAudio.get('id'));
    let audio = this.store.createRecord('audio', {
      instrument: fromAudio.get('instrument'),
      name: fromAudio.get('name')
    });

    return hash({
      instruments: this.store.query('instrument', {}),
      audio: audio
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    cloneAudio(model) {
      let instrument = model.get('instrument');
      let library = instrument.get('library');
      let account = library.get('account');
      let cloneAudioId = this.get('fromAudioId');

      model.save({
        adapterOptions: {
          query: {
            cloneId: cloneAudioId
          }
        }
      }).then(
        () => {
          get(this, 'display').success('Cloned audio ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', account, library, instrument, model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
