//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  fromProgramId: null,

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
      let self = this;
      this.config.getConfig().then(
        () => {
          let fromProgram = self.modelFor('accounts.one.libraries.one.programs.editor');
          self.set('fromProgramId', fromProgram.get('id'));
          let program = self.store.createRecord('program', {
            user: fromProgram.get('user'),
            type: fromProgram.get('type'),
            library: fromProgram.get('library'),
            name: fromProgram.get('name')
          });
          resolve(hash({
            libraries: self.store.query('library', {accountId: self.modelFor('accounts.one').get('id')}),
            program: program
          }, 'libraries, program'));

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

    sessionChanged: function () {
      this.refresh();
    },

    cloneProgram(model) {
      let cloneProgramId = this.fromProgramId;

      model.save({
        adapterOptions: {
          query: {
            cloneId: cloneProgramId
          }
        }
      }).then(
        () => {
          this.display.success('Cloned program ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.programs.editor', model.library.account, model.library, model);
        },
        (error) => {
          this.display.error(error);
        });
    },

  },

});
