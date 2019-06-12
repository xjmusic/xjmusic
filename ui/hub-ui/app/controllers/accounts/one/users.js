//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {inject as service} from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({

  display: service(),

  actions: {

    /**
     * Ember power-select uses this as onChange to set value
     * @param user
     * @returns {*}
     */
    setUserToAdd(user) {
      this.set('model.userToAdd', user);
      return user;
    },

    destroyAccountUser(model) {
      let name = model.get('user').get('name');
      model.destroyRecord().then(() => {
        this.display.success(`Removed ${name} from Account.`);
      }).catch((error) => {
        this.display.error(error);
      });
    },

    addUserToAccount(model) {
      let accountUser = this.store.createRecord('account-user', {
        account: model.account,
        user: model.userToAdd,
      });
      accountUser.save().then(() => {
        this.display.success(`Added ${model.userToAdd.get('name')} to ${model.account.get('name')}.`);
        // this.transitionToRoute('accounts.one.users',model.account);
        this.send("sessionChanged");
      }).catch((error) => {
        this.display.error(error);
      });
    },

  }

});
