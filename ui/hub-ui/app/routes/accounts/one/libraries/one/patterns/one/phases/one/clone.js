// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
          reject('Could not instantiate new Phase', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let fromPhase = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
    this.set('fromPhaseId', fromPhase.get('id'));
    let phase = this.store.createRecord('phase', {
      name: fromPhase.get('name'),
      pattern: fromPhase.get('pattern'),
      offset: fromPhase.get('offset')
    });

    return hash({
      patterns: this.store.query('pattern', {}),
      phase: phase
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    clonePhase(model) {
      let pattern = model.get('pattern');
      let library = pattern.get('library');
      let account = library.get('account');
      let clonePhaseId = this.get('fromPhaseId');

      model.save({
        adapterOptions: {
          query: {
            cloneId: clonePhaseId
          }
        }
      }).then(
        () => {
          get(this, 'display').success('Cloned phase ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one', account, library, pattern, model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
