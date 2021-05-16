/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

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
          let fromProgram = this.modelFor('accounts.one.libraries.one.programs.one');
          this.set('fromProgramId', fromProgram.get('id'));
          let program = this.store.createRecord('program', {
            library: fromProgram.get('library'),
            name: fromProgram.get('name')
          });

          resolve(hash({
            libraries: this.store.query('library', {}),
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
      let library = model.get('library');
      let account = library.get('account');
      let cloneProgramId = this.get('fromProgramId');

      model.save({
        adapterOptions: {
          query: {
            cloneId: cloneProgramId
          }
        }
      }).then(
        () => {
          get(this, 'display').success('Cloned program ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.programs.one', account, library, model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    cancel() {
      history.back();
    },

  },

});
