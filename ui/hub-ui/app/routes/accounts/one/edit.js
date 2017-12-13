// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
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

    saveAccount(model) {
      let self = this;
      model.save().then(
        () => {
          get(self, 'display').success('Updated account ' + model.get('name') + '.');
          self.transitionTo('accounts.one');
        },
        (error) => {
          get(self, 'display').error(error);
        }
      );
    },

    destroyAccount(model) {
      let self = this;
      let confirmation = confirm("Are you sure? If there are Users or Libraries belonging to this account, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(self, 'display').success('Deleted account ' + model.get('name') + '.');
            self.transitionTo('accounts');
          },
          (error) => {
            get(self, 'display').error(error);
          });
      }
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
    }

  }

});
