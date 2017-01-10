// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

/**
 * XJ Auth Service
 * (wraps the custom ember-simple-auth session)
 */
export default Ember.Service.extend({
  cookies: Ember.inject.service("cookies"),
  session: Ember.inject.service("session"),
  accessToken: "",

  /**
   * On app start, authorize the current browser session with the domain server API via access token cookie.
   */
  init() {
    this._super(...arguments);
    let session = this.get('session');
    let auth = this;
    /* even if the user is believed to be authenticated,
       re-authenticate to ensure they still are. */
    session.authenticate('authenticator:xj-auth').then(function(){
      auth.parseRolesCSV(session.getData().roles);
    }, xhr => {
      console.log("auth failure", xhr);
    });
  },

  /**
   * Invalidate the current session.
   */
  invalidate() {
    this.get('session').invalidate();
  },

  /**
   * Parse a CSV of roles and set corresponding booleans
   * @param roles in CSV format
   */
  parseRolesCSV: function(roles) {
    let roleArr = roles.split(",");
    let auth = this;
    roleArr.forEach(function(role) {
      switch (role.trim().toLowerCase()) {
        case "user":
          auth.set("isUser",true);
          console.log("Role: User");
          break;
        case "admin":
          auth.set("isAdmin",true);
          console.log("Role: Admin");
          break;
      }
    });
  },

  /**
   * @returns {boolean} if session has 'user' role
   */
  isUser: false,

  /**
   * @returns {boolean} if session has 'admin' role
   */
  isAdmin: false

});
