// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { inject as service } from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({

  display: service(),

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
        get(this, 'display').success('Removed User from Account.');
      }).catch((error) => {
        get(this, 'display').error(error);
      });
    },

    addUserToAccount(model) {
      let accountUser = this.store.createRecord('account-user', {
        account: model.account,
        user: model.userToAdd,
      });
      accountUser.save().then(() => {
        get(this, 'display').success('Added ' + model.userToAdd.get('name') + ' to ' + model.account.get('name') + '.');
        // this.transitionToRoute('accounts.one.users',model.account);
        this.send("sessionChanged");
      }).catch((error) => {
        get(this, 'display').error(error);
      });
    },

  }

});
