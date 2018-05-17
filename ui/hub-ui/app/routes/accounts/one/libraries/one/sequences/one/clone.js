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
          reject('Could not instantiate new Sequence', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let fromSequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    this.set('fromSequenceId', fromSequence.get('id'));
    let sequence = this.store.createRecord('sequence', {
      user: fromSequence.get('user'),
      type: fromSequence.get('type'),
      library: fromSequence.get('library'),
      name: fromSequence.get('name')
    });

    return hash({
      libraries: this.store.query('library', {}),
      sequence: sequence
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    cloneSequence(model) {
      let library = model.get('library');
      let account = library.get('account');
      let cloneSequenceId = this.get('fromSequenceId');

      model.save({
        adapterOptions: {
          query: {
            cloneId: cloneSequenceId
          }
        }
      }).then(
        () => {
          get(this, 'display').success('Cloned sequence ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.sequences.one', account, library, model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
