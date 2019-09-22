//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get, set} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Model
   * @param {*} params
   * @return {Model}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('instrument', params['instrument_id'], {reload: true})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries.one.instruments');
      }, 'instrument');
  },

  //
  actions: {

    /**
     * Check for dirty attributes before allowing transition
     * @param transition
     */
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

    /**
     * Save the model
     */
    commit() {
      let self = this;
      let model = self.controller.get('model');
      model.save().then(
        () => {
          self.display.success('Saved ' + model.get('name') + '.');
          set(this, 'isEditModalVisible', false);
        },
        (error) => {
          self.display.error(error);
        });
    },

    /**
     * Destroy the model
     */
    destroy() {
      let self = this;
      let model = self.controller.get('model');
      let confirmation = confirm("Are you sure!?");
      let library = model.get('library');
      let account = library.get('account');
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(self, 'display').success('Destroyed ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.instruments', account, library);
          },
          (error) => {
            get(self, 'display').error(error);
          });
      }
    },

    /**
     * Revert the model
     */
    revert() {
      let self = this;
      let model = self.controller.get('model');
      model.rollbackAttributes();
      self.display.success('Reverted ' + model.get('name') + '.');
      set(this, 'isEditModalVisible', false);
    }

  }

});
