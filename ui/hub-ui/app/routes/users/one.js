//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('user', params.user_id)
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('users');
      });
  },

  /**
   * Route Actions
   */
  actions: {

    saveUser(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated user ' + model.get('name') + '.');
          this.transitionTo('users');
        },
        (error) => {
          get(this, 'display').error(error);
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

  },

});
