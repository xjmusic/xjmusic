// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*|DS.Model|Promise}
   */
  model: function () {
    return this.store.createRecord('account');
  },

  /**
   * Route Actions
   */
  actions: {

    createAccount(model) {
      model.save().then(
        () => {
          this.display.success('Created account ' + model.get('name') + '.');
          this.transitionTo('accounts.one', model);
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
    },

    cancelCreateAccount() {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts');
        }
      }
    },
  }
});
