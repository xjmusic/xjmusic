// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get } from '@ember/object';

import { Promise as EmberPromise } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

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
        (config) => {
          resolve(self.resolvedModel(config));
        },
        (error) => {
          reject('Could not instantiate new Instrument', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @param config
   * @returns {*}
   */
  resolvedModel(config) {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {

      let instrument = this.store.createRecord('instrument', {
        type: config.instrumentTypes[0],
        library: this.modelFor('accounts.one.libraries.one')
      });

      // resolves the user *after* closure
      this.store.findRecord('user', this.get('auth').userId).then((record) => {
        instrument.set('user', record);
      });

      return instrument;
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createInstrument(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Created instrument ' + model.get('description') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments.one', model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    },

    cancelCreateInstrument(transition)
    {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.instruments');
        } else {
          transition.abort();
        }
      }
    }

  }
});
