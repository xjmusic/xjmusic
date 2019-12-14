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
    return this.modelFor('accounts.one.chains.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let self = this;
      let account = model.get('account');
      let confirmation = confirm("Are you sure?");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(self, 'display').success('Destroyed Chain ' + model.get('name') + '.');
            self.transitionTo('accounts.one.chains', account);
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
