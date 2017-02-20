// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

/**
 * XJ Auth Service
 * (wraps the custom ember-simple-auth session)
 */
export default Ember.Service.extend({
  cookies: Ember.inject.service(),
  session: Ember.inject.service(),
  messageBus: Ember.inject.service(),
  accessToken: "",

  /**
   * Set directly from GET /api/1/auth
   */
  userId: null,
  accounts: null,
  roles: null,

  /**
   * On app start, authorize the current browser session with the domain server API via access token cookie.
   */
  init() {
    this._super(...arguments);
    let session = this.get('session');
    let auth = this;
    /* even if the user is believed to be authenticated,
     re-authenticate to ensure they still are. */
    session.authenticate('authenticator:xj').then(function () {
      auth.parseRolesCSV(session.getData().roles);
      auth.userId = session.getData().userId;
      auth.accounts = session.getData().accounts;
      auth.roles = session.getData().roles;
      auth.get('messageBus').publish('auth-accounts', auth.accounts);
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
  parseRolesCSV: function (roles) {
    let roleArr = roles.split(",");
    let auth = this;
    roleArr.forEach(function (rawRole) {
      let role = auth.normalizeRole(rawRole);
      auth.set('is' + role, true);
    });
  },

  /**
   * Returns first letter capital, all else lower, trimmed spaces.
   * @param string
   * @returns {string}
   */
  normalizeRole(string) {
    return string.trim().toLowerCase().charAt(0).toUpperCase() + string.slice(1);
  },

  /**
   * {boolean} if session has 'user' role
   */
  isUser: false,

  /**
   * {boolean} if session has 'admin' role
   */
  isAdmin: false,

  /**
   * {boolean} if session has 'artist' role
   */
  isArtist: false,

});
