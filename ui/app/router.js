import Ember from 'ember';
import config from './config/environment';

const Router = Ember.Router.extend({
  location: config.locationType
});

Router.map(function() {
  this.route('engines', function() {
  });

  this.route('login');

  this.route('welcome');
  this.route('unauthorized');
});

export default Router;
