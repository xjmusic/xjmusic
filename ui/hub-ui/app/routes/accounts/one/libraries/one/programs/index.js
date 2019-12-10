// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {hash, Promise as EmberPromise} from 'rsvp';
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
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let self = this;
    let library = self.modelFor('accounts.one.libraries.one');

    let programs = self.store.query('program', {libraryId: library.get('id')})
      .catch((error) => {
        self.display.error(error);
        self.transitionTo('');
      });

    return new EmberPromise((resolve, reject) => {
      self.config.getConfig().then(function () {
        self.store.findRecord('user', self.user_auth.userId).then(function (user) {
            // resolves the user *after* closure
            let newProgram = self.store.createRecord('program', {
              user: user,
              type: self.config.programTypes[0],
              state: self.config.programStates[0],
              library: self.modelFor('accounts.one.libraries.one')
            });
            resolve(hash({
              library: library,
              programs: programs,
              newProgram: newProgram
            }, 'library, programs, new Program'));
          },

          (error) => {
            reject(error);
          }
        );
      });
    });
  },

});
