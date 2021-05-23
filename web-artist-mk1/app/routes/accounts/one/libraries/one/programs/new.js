/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

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
    return new EmberPromise((resolve, reject) => {
      this.config.getConfig().then(
        () => {
          let auth = this.get('auth');
          if (auth.isArtist || auth.isAdmin) {

            let program = this.store.createRecord('program', {
              type: this.config.programTypes[0],
              config: this.config.defaultProgramConfig,
              library: this.modelFor('accounts.one.libraries.one')
            });

            resolve(program);

          } else {
            this.transitionTo('accounts.one.libraries.one.programs');
          }
        },
        (error) => {
          reject('Could not instantiate new Program', error);
        }
      );
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createProgram(model) {
      let library = model.get("library");
      let account = library.get("account");
      model.save().then(
        () => {
          get(this, 'display').success('Created program ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.programs.one', account, library, model);
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

    cancel() {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.programs');
        }
      }
    }

  }
});
