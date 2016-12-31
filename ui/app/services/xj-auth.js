import Ember from 'ember';

const ACCESS_TOKEN_NAME="access_token";

export default Ember.Service.extend({
  cookies: Ember.inject.service("cookies"),
  session: Ember.inject.service("session"),
  accessToken: "",

  /**
   * On app start, authorize the current browser session with the domain server API via access token cookie.
   */
  init() {
    this._super(...arguments);
    if (this.readAccessToken()) {
      this.get('session').authenticate('authenticator:xj-auth', this.accessToken);
      console.log("U HAZ ACCESS TOKEN", this.accessToken);
    } else {
      console.log("NO ACCESS TOKEN");
      // TODO: auth state change for no access token
    }
    console.log("\n");
  },

  /**
   * Read the access token cookie from browser.
   * @returns {boolean} if an access token was found and cached.
   */
  readAccessToken() {
    let cookieService=this.get("cookies");
    if (cookieService==null) { return false; }

    let cookies=cookieService.read();
    if (cookies==null) { return false; }

    if (!(ACCESS_TOKEN_NAME in cookies)) { return false;}
    this.accessToken = cookies[ACCESS_TOKEN_NAME];
    return true;
  }
});
