//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { inject as service } from '@ember/service';
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
    return this.modelFor('accounts.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let self = this;
      let confirmation = confirm("Are you sure? If there are Users or Libraries belonging to this account, destruction will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(self, 'display').success('Destroyed account ' + model.get('name') + '.');
            self.transitionTo('accounts');
          },
          (error) => {
            get(self, 'display').error(error);
            model.rollbackAttributes();
          });
      }
    },

  }

});
