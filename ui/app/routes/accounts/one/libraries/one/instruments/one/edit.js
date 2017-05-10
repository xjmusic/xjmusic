// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  // Inject: configuration service
  config: Ember.inject.service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    return new Ember.RSVP.Promise((resolve, reject) => {
      let self = this;
      Ember.get(this, 'config').promises.config.then(
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
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: 'Edit ' + model.get('description'),
      entity: {
        name: 'Instrument',
        id: model.get('id')
      }
    });
  },

  /**
   * Route actions
   */
  actions: {

    saveInstrument(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated instrument.');
          this.transitionTo('accounts.one.libraries.one.instruments.one');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    destroyInstrument(model) {
      let confirmation = confirm("Are you sure? If there are Instruments or Instruments belonging to this Instrument, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            Ember.get(this, 'display').success('Deleted instrument ' + model.get('description') + '.');
            this.transitionTo('accounts.one.libraries.one.instruments');
          },
          (error) => {
            Ember.get(this, 'display').error(error);
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
