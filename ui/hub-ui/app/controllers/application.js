// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Controller from '@ember/controller';

export default Controller.extend({

  // Depends on application
  needs: ['application'],

  actions: {
    goBack: function () {
      history.back();
    }
  }

});
