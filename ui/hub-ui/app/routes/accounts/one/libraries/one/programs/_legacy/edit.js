// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Promise}
   */
  model() {
    if (!(this.auth.isArtist || this.auth.isAdmin))
      return this.transitionTo('accounts.one.libraries.one.programs');

    return new EmberPromise((resolve, reject) => {
      this.config.getConfig().then(
        () => {
          let library = this.modelFor('accounts.one.libraries.one');
          let program = this.modelFor('accounts.one.libraries.one.programs.editor');
          program.set('library', library);
          resolve(program);

        },
        (error) => {
          reject('Could not instantiate Sequence model', error);
        }
      );
    });
  },

  /**
   * Route Actions
   */
  actions: {

    saveSequence(model) {
      model.save().then(
        () => {
          this.display.success('Updated sequence ' + model.get('name') + '.');
          history.back();
        },
        (error) => {
          this.display.error(error);
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
