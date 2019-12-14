// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
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
   * @returns {*|DS.Model|Promise}
   */
  model: function () {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      return this.store.createRecord('instrument-audio-chord', {
        instrument: this.modelFor('accounts.one.libraries.one.instruments.one'),
        instrumentAudio: this.modelFor('accounts.one.libraries.one.instruments.one.audios.one')
      });
    } else {
      history.back();
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createChord(model) {
      let audio = model.get("instrumentAudio");
      let instrument = audio.get("instrument");
      let library = instrument.get("library");
      let account = library.get("account");
      model.save().then(
        () => {
          get(this, 'display').success('Created chord ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords', account, library, instrument, audio);
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

    cancel(model) {
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          history.back();
        }
      }
    }

  }
});
