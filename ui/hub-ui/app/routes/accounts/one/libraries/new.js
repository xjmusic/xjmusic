// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

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
    let account = this.modelFor('accounts.one');
    return this.store.createRecord('library', {
      account: account
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createLibrary(model) {
      let self = this;
      model.save().then(
        (savedModel) => {
          get(self, 'display').success('Created library ' + savedModel.get('name') + '.');
          self.transitionTo('accounts.one.libraries.one', savedModel);
        }, (error) => {
          get(self, 'display').error(error);
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

    cancelCreateLibrary() {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries');
        }
      }
    }


  }
});
