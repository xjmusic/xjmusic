import Ember from "ember";
import Base from "ember-simple-auth/authenticators/base";

/**
 Authenticator for server API via access token token implied by browser.

 @class Token
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
   @method init
   @private
   */
  init() {

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
    return properties;
    // TODO determine strategy for local storage of user session in frontend, and implementation of ember session restore.
  },

  /**
   Authorizes a session with the access token implied by browser cookie,
   used in a GET request to the server auth endpoint to retrieve the user info
   for this access token, if valid.

   @method authenticate
   @return {Ember.RSVP.Promise} A promise that resolves when an auth token is successfully acquired from the server and rejects otherwise
   */
  authenticate(data) {
    console.log(data);
    return new Ember.RSVP.Promise((resolve, reject) => {
      this.makeRequest().then(response => {
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
   @method makeRequest; access_token implied by browser cookie
   @private
   */
  makeRequest() {
    return Ember.$.ajax({
      url: this.serverTokenEndpoint,
      method: 'GET',
      xhrFields: {
        withCredentials: true
      }
    });
  }
});
