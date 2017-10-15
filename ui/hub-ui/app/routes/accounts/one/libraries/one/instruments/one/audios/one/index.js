// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: configuration service
  config: Ember.inject.service(),

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
            let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
            let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
            resolve(Ember.RSVP.hash({
              instrument: instrument,
              audioBaseUrl: config.audioBaseUrl,
              audio: audio,
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
      title: model.audio.get('name'),
      entity: {
        name: 'Audio',
        id: model.audio.get('id')
      }
    });
  },

});
