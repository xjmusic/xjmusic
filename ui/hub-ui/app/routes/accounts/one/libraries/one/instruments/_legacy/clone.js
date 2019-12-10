// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

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
      this.config.getConfig().then(
        () => {
          resolve(self.resolvedModel());
        },
        (error) => {
          reject('Could not instantiate new Instrument', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*} hash
   */
  resolvedModel() {
    let fromInstrument = this.modelFor('accounts.one.libraries.one.instruments.editor');
    this.set('fromInstrumentId', fromInstrument.get('id'));
    let instrument = this.store.createRecord('instrument', {
      library: fromInstrument.get('library'),
      name: fromInstrument.get('name')
    });

    return hash({
      libraries: this.store.query('library', {accountId: this.modelFor('accounts.one').get('id')}),
      instrument: instrument
    }, 'libraries, instrument');
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
      let cloneInstrumentId = this.fromInstrumentId;

      model.save({
        adapterOptions: {
          query: {
            cloneId: cloneInstrumentId
          }
        }
      }).then(
        () => {
          this.display.success('Cloned instrument ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments.editor', account, library, model);
        },
        (error) => {
          this.display.error(error);
        });
    },

  },

});
