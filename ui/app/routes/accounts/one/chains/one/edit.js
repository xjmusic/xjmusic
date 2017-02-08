// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model() {
    let account = this.modelFor('accounts.one');
    let chain = this.modelFor('accounts.one.chains.one');
    chain.set('account', account);
    return chain;
  },

  actions: {

    saveChain(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated chain ' + model.get('name') + '.');
        this.transitionTo('accounts.one.chains', model.get('account'));
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    destroyChain(model) {
      let confirmation = confirm("Are you fucking sure? If there are Ideas or Instruments belonging to this Chain, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord().then(() => {
          Ember.get(this, 'display').success('Deleted chain ' + model.get('name') + '.');
          this.transitionTo('accounts.one.chains');
        }).catch((error) => {
          Ember.get(this, 'display').error(error);
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
