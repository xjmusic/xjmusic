/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {later} from '@ember/runloop';
import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';
import RSVP from "rsvp";

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  // audio base url will be set by promise after config resolves
  audioBaseUrl: '',

  /**
   * Route Model
   * @returns {Promise}
   */
  model: function () {
    let auth = this.get('auth');

    if (auth.isArtist || auth.isAdmin) {
      return new RSVP.Promise((resolve, reject) => {
        this.config.getConfig().then(
          () => {
            this.audioBaseUrl = this.config.audioBaseUrl;
            resolve(this.store.createRecord('instrument-audio', {
              instrument: this.modelFor('accounts.one.libraries.one.instruments.one')
            }));
          },
          (error) => {
            reject(error);
          }
        );
      });
    } else {
      history.back();
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createAudio(model) {
      let self = this;
      model.save().then(
        () => {
          let model = self.controller.get('model');
          let instrument = model.get("instrument");
          let library = instrument.get("library");
          let account = library.get("account");
          later(() => {
            self.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', account, library, instrument, model);
          }, 2);
        },
        (error) => {
          get(this, 'display').error(['Failed to create.', error]);
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

    cancel(model) {
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          history.back();
        }
      }
    }

  },

});
