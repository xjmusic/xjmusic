/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
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
    return this.modelFor('accounts.one');
  },

  /**
   * Route Actions
   */
  actions: {

    saveAccount(model) {
      let self = this;
      model.save().then(
        () => {
          get(self, 'display').success('Updated account ' + model.get('name') + '.');
          history.back();
        },
        (error) => {
          get(self, 'display').error(error);
        }
      );
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
      history.back();
    },

  }

});
