import Ember from 'ember';

export default Ember.Service.extend({
  cookies: Ember.inject.service("cookies"),
  session: Ember.inject.service("session"),
  accessToken: "",

  /**
   * On app start, authorize the current browser session with the domain server API via access token cookie.
   */
  init() {
    this._super(...arguments);
    this.get('session').authenticate('authenticator:xj-auth', this.accessToken);
  },

  invalidate() {
    this.get('session').invalidate();
  }
});
