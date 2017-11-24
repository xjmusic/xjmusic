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
   * @returns {*|DS.Model}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
      let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
      audio.set('instrument', instrument);
      return audio;
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    saveAudio(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated audio "' + model.get('name') + '".');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyAudio(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Deleted audio "' + model.get('name') + '".');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
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
    }
  }

});
