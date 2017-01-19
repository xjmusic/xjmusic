import Ember from 'ember';

export default Ember.Route.extend({
  renderTemplate: function() {
    window.location.replace("/auth/google");
  }
});
