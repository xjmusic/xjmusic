// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
  model: function () {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      return this.store.createRecord('audio-event', {
        audio: this.modelFor('accounts.one.libraries.one.instruments.one.audios.one')
      });
    } else {
      history.back();
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createEvent(model) {
      let audio = model.get("audio");
      let instrument = audio.get("instrument");
      let library = instrument.get("library");
      let account = library.get("account");
      model.save().then(
        () => {
          get(this, 'display').success('Created "' + model.get('inflection') + '" event in ' + model.get('note') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.events', account, library, instrument, audio);
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

    cancelCreateEvent() {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.events', model.audio.instrument.library.account, model.audio.instrument.library, model.audio.instrument, model.audio, model);
        }
      }
    }

  }
});
