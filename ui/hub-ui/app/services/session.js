// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import ESASession from "ember-simple-auth/services/session";

export default ESASession.extend({

  /**
   * Returns the the session-authenticated data
   * @returns {null}
   */
  getData: function() {
    if (this.get('isAuthenticated')) {
      return this.get('data').authenticated;
    } else {
      return null;
    }
  }

});
