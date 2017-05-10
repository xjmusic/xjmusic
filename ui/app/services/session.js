// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
// import Ember from 'ember';
// import DS from "ember-data";
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
