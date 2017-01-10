// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
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
  this.route('admin', function() {
    this.route('users');
  });
});

export default Router;
