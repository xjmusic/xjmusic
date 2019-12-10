// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  // Inject auth service
  auth: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    return this.modelFor('accounts.one.libraries.one.programs.editor');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let confirmation = confirm("Are you sure?");
      let library = model.get('library');
      let account = library.get('account');

      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            this.display.success('Destroyed sequence ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.programs', account, library);
          },
          (error) => {
            this.display.error(error);
            model.rollbackAttributes();
          });
      }
    },

  }

});
