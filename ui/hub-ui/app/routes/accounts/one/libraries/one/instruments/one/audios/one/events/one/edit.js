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
   * @returns {*|DS.Model}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
      let event = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one.events.one');
      event.set('instrumentAudio', audio);
      return event;
    } else {
      history.back();
    }
  },

  /**
   * Route Actions
   */
  actions: {

    saveEvent(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated event "' + model.get('name') + '" event in ' + model.get('note') + '.');
          history.back();
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
    },

  }

});
