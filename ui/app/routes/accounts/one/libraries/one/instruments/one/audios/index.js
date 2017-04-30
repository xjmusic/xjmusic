// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: configuration service
  config: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {Ember.RSVP.Promise}
   */
  model: function () {
    let self = this;
    let auth = this.get('auth');

    if (auth.isArtist || auth.isAdmin) {
      return new Ember.RSVP.Promise((resolve, reject) => {
        Ember.get(self, 'config').promises.config.then(
          (config) => {
            let instrument = self.modelFor('accounts.one.libraries.one.instruments.one');
            let audios = self.store.query('audio', {instrumentId: instrument.get('id')})
              .catch((error) => {
                Ember.get(self, 'display').error(error);
                self.transitionTo('');
              });
            resolve(Ember.RSVP.hash({
              instrument: instrument,
              audioBaseUrl: config.audioBaseUrl,
              audios: audios,
            }));
          },
          (error) => {
            reject(error);
          }
        );
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
    }
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.instrument.get('description') + ' ' + 'Audios',
      entity: {
        name: 'Instrument',
        id: model.instrument.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editAudio(audio) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', audio);
    },
  }

});
