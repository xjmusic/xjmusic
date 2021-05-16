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
   * Model is a promise because it depends on promised configs
   * @returns {Promise}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      this.config.getConfig().then(
        () => {
          let fromInstrument = this.modelFor('accounts.one.libraries.one.instruments.one');
          this.set('fromInstrumentId', fromInstrument.get('id'));
          let instrument = this.store.createRecord('instrument', {
            library: fromInstrument.get('library'),
            name: fromInstrument.get('name')
          });

          resolve(hash({
            libraries: this.store.query('library', {}),
            instrument: instrument
          }, 'libraries, instrument'));

        },
        (error) => {
          reject('Could not instantiate new Instrument', error);
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

    cloneInstrument(model) {
      let library = model.get('library');
      let account = library.get('account');
      let cloneInstrumentId = this.get('fromInstrumentId');

      model.save({
        adapterOptions: {
          query: {
            cloneId: cloneInstrumentId
          }
        }
      }).then(
        () => {
          get(this, 'display').success('Cloned instrument ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments.one', account, library, model);
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
