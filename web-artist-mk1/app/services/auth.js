/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Service, {inject as service} from '@ember/service';
import RSVP from "rsvp";

/**
 * XJ Auth Service
 * (wraps the custom ember-simple-auth session)
 */
export default Service.extend({
  cookies: service(),
  session: service(),
  messageBus: service(),
  accessToken: "",

  // for any other action with a user auth prerequisite
  promise: {},

  /**
   * Set directly from GET /auth
   */
  userId: null,
  accountIds: null,
  roleTypes: null,

  /**
   On app start, authorize the current browser session with the domain server API via access token cookie.
   Even if the user is believed to be authenticated,
   re-authenticate to ensure they still are.

   Other routes with a user auth prerequisite can implement:
   this.get('auth').promise.then(...);

   */
  init() {
    this._super(...arguments);
    let session = this.session;
    let auth = this;
    auth.set('promise', new RSVP.Promise(
      function (resolve, reject) {

        session.authenticate('authenticator:xj').then(function () {
          auth.userId = session.getData().userId;
          auth.accountIds = session.getData().accountIds;
          auth.roleTypes = session.getData().roleTypes;
          auth.roleTypes.forEach(function (rawRole) {
            let role = auth.normalizeRole(rawRole);
            auth.set('is' + role, true);
          });

          auth.get('messageBus').publish('auth-accounts', auth.accountIds);
          console.info(`User{ id:${auth.userId}, roleTypes:[${auth.roleTypes}], accountIds[${auth.accountIds}] } Authenticated.`);
          resolve(auth);

        }, xhr => {
          console.log("auth failure", xhr);
          reject(xhr);
        });

      }));
  },

  /**
   * Invalidate the current session.
   */
  invalidate() {
    this.session.invalidate();
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

  /**
   * {boolean} if session has 'engineer' role
   */
  isEngineer: false,

});
