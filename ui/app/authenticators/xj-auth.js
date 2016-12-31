import Ember from "ember";
import Base from "ember-simple-auth/authenticators/base";

// const ACCESS_TOKEN_NAME="access_token";

/**
 Authenticator that works with token-based authentication like JWT.

 _The factory for this authenticator is registered as
 `'authenticator:token'` in Ember's container._

 @class Token
 @namespace SimpleAuth.Authenticators
 @module ember-simple-auth-token/authenticators/token
 @extends Base
 */
export default Base.extend({
  /**
   The endpoint on the server the authenticator acquires authorization info from..

   @property serverTokenEndpoint
   @type String
   @default '/auth'
   */
  serverTokenEndpoint: '/auth',

  /**
   The access token used for authorization.

   @property tokenPropertyName
   @type String
   @default 'token'
   */
  accessToken: 'token',

  /**
   @method init
   @private
   */
  init() {
    console.log("INIT xj-auth authenticator");
    // TODO remove this log
  },

  /**
   Restores the session from a set of session properties; __will return a
   resolving promise when there's a non-empty `token` in the
   `properties`__ and a rejecting promise otherwise.

   @method restore
   @param {Object} properties The properties to restore the session from
   @return {Ember.RSVP.Promise} A promise that when it resolves results in the session being authenticated
   */
  restore(properties) {
    const propertiesObject = Ember.Object.create(properties);

    return new Ember.RSVP.Promise((resolve, reject) => {
      if (!Ember.isEmpty(propertiesObject.get(this.tokenPropertyName))) {
        resolve(properties);
      } else {
        reject();
      }
    });
  },

  /**
   Authorizes a session with the specified `accessToken`; the access token cookie
   is used in a GET request to the server auth endpoint to retrieve the user info
   for this access token, if valid.

   @method authenticate
   @param {Object} accessToken to authenticate the session with
   @return {Ember.RSVP.Promise} A promise that resolves when an auth token is successfully acquired from the server and rejects otherwise
   */
  authenticate(accessToken) {
    return new Ember.RSVP.Promise((resolve, reject) => {
      this.makeRequest(accessToken).then(response => {
        Ember.run(() => {
          resolve(this.getResponseData(response));
        });
      }, xhr => {
        Ember.run(() => { reject(xhr.responseJSON || xhr.responseText); });
      });
    });
  },

  /**
   Returns an object with properties the `authenticate` promise will resolve,
   be saved in and accessible via the session.

   @method getResponseData
   @return {object} An object with properties for the session.
   */
  getResponseData(response) {
    return response;
  },

  /**
   Does nothing

   @method invalidate
   @return {Ember.RSVP.Promise} A resolving promise
   */
  invalidate() {
    return Ember.RSVP.resolve();
  },

  /**
   @method makeRequest
   @param {Object} accessToken to send with request
   @private
   */
  makeRequest(accesstoken) {
    console.log("NOTHING TO DO WITH", accesstoken);
    return Ember.$.ajax({
      url: this.serverTokenEndpoint,
      method: 'GET',
      xhrFields: {
        withCredentials: true
      }
    });
  }
});
