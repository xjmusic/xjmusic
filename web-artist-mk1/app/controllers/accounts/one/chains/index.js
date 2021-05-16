/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Controller from '@ember/controller';

export default Controller.extend({

  /**
   * Actions
   */
  actions: {
    chainsUpdated(/*ev*/) {
      this.send("sessionChanged");
    }
  }

});
