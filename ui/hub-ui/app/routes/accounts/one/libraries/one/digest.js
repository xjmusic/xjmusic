// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
  model() {
    let account = this.modelFor('accounts.one');
    let library = this.modelFor('accounts.one.libraries.one');
    library.set('account', account);
    return library;
  }

});
