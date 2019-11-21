//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

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
    return this.modelFor('accounts.one.libraries.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let confirmation = confirm("Are you sure? If there are Sequences or Instruments belonging to this Library, destruction will fail anyway.");
      let account = model.get("account");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            this.display.success('Destroyed library ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries', account);
          },
          (error) => {
            this.display.error(error);
            model.rollbackAttributes();
          });
      }
    },

  }

});
