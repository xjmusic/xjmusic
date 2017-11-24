// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get } from '@ember/object';

import { inject as service } from '@ember/service';
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
      return this.store.createRecord('audio-chord', {
        audio: this.modelFor('accounts.one.libraries.one.instruments.one.audios.one')
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createChord(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Created chord ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords');
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

    cancelCreateChord(transition)
    {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords');
        } else {
          transition.abort();
        }
      }
    }

  }
});
