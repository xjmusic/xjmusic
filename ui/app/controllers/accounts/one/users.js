// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({

  display: Ember.inject.service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param user
     * @returns {*}
     */
    setUserToAdd(user){
      this.set('model.userToAdd', user);
      return user;
    },

    destroyAccountUser(model) {
      model.destroyRecord().then(() => {
        Ember.get(this, 'display').success('Removed User from Account.');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    addUserToAccount(model) {
      let accountUser = this.store.createRecord('account-user', {
        account: model.account,
        user: model.userToAdd,
      });
      accountUser.save().then(() => {
        Ember.get(this, 'display').success('Added ' + model.userToAdd.get('name') + ' to ' + model.account.get('name') + '.');
        // this.transitionToRoute('accounts.one.users',model.account);
        this.send("sessionChanged");
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

  }

});
