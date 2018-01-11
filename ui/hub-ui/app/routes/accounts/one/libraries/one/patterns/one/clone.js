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
          reject('Could not instantiate new Pattern', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let fromPattern = this.modelFor('accounts.one.libraries.one.patterns.one');
    this.set('fromPatternId', fromPattern.get('id'));
    let pattern = this.store.createRecord('pattern', {
      user: fromPattern.get('user'),
      type: fromPattern.get('type'),
      library: fromPattern.get('library'),
      name: fromPattern.get('name')
    });

    return hash({
      libraries: this.store.query('library', {}),
      pattern: pattern
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    clonePattern(model) {
      let library = model.get('library');
      let account = library.get('account');
      let clonePatternId = this.get('fromPatternId');

      model.save({
        adapterOptions: {
          query: {
            cloneId: clonePatternId
          }
        }
      }).then(
        () => {
          get(this, 'display').success('Cloned pattern ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.patterns.one', account, library, model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
