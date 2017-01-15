// app/routes/libraries/edit.js
import Ember from "ember";

export default Ember.Route.extend({

  flashMessages: Ember.inject.service(),

  model(params) {
    return this.store.findRecord('user', params.user_id);
  },

  actions: {

    saveUser(newUser) {
      newUser.save().then(() => {
        Ember.get(this, 'flashMessages').success('User updated.');
        this.transitionTo('admin.users');
      }).catch((adapterError) => {
        Ember.get(this, 'flashMessages').danger('Error: ' + adapterError.errors[0].detail);
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
    }
  }
});
