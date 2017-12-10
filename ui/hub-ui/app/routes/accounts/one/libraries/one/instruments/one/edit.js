// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get } from '@ember/object';

import { Promise as EmberPromise } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: flash message service
  display: service(),

  // Inject: configuration service
  config: service(),

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
          reject('Could not instantiate Instrument model', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let library = this.modelFor('accounts.one.libraries.one');
      let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
      instrument.set('library', library);
      return instrument;
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments');
    }
  },

  /**
   * Route actions
   */
  actions: {

    saveInstrument(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated instrument.');
          history.back();
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyInstrument(model) {
      let confirmation = confirm("Are you sure? If there are Instruments or Instruments belonging to this Instrument, deletion will fail anyway.");
      let library = model.get('library');
      let account = library.get('account');
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(this, 'display').success('Deleted instrument ' + model.get('description') + '.');
            this.transitionTo('accounts.one.libraries.one.instruments', account, library);
          },
          (error) => {
            get(this, 'display').error(error);
          });
      }
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
    }

  }

});
