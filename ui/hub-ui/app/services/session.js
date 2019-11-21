//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import ESASession from "ember-simple-auth/services/session";

export default ESASession.extend({

  /**
   * Returns the the session-authenticated data
   * @returns {null}
   */
  getData: function () {
    if (this.isAuthenticated) {
      return this.data.authenticated;
    } else {
      return null;
    }
  }

});
