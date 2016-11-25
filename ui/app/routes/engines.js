import Ember from 'ember';

var pollIntervalMilliseconds = 1000;

export default Ember.Route.extend({

  model: function() {
    return this.store.findAll('engine');
  },

  setupController: function(controller, model) {
    this.poll();
    // this.get('engine').poll();
  },

  poll: function() {
    var _this = this;
    Ember.run.later( function() {
      // _this.reload();
      _this.refresh();
      _this.poll();
    }, pollIntervalMilliseconds);
  }.observes('didLoad')

  // deactivate: function() {
  //   this.get('pollster').stop();
  // }

});
