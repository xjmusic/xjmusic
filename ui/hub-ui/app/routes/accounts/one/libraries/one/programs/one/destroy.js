// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  // Inject auth service
  auth: service(),

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
  model() {
    return this.modelFor('accounts.one.libraries.one.programs.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let self = this;
      let confirmation = confirm("Are you sure? If there are Programs or Programs belonging to this Program, destruction will fail anyway.");
      let library = model.get('library');
      let account = library.get('account');
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(self, 'display').success('Destroyed program ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.programs', account, library);
          },
          (error) => {
            get(self, 'display').error(error);
            model.rollbackAttributes();
          });
      }
    },

    cancel() {
      history.back();
    },

  }

});
