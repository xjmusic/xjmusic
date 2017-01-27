// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";
import config from "./config/environment";

const Router = Ember.Router.extend({
  location: config.locationType
});

Router.map(function() {
  // Access
  this.route('access', function() {
    this.route('users', function() {
      this.route('edit', { path: '/:user_id' });
    });
    this.route('accounts', function() {
      this.route('edit', { path: '/:account_id' }, function() {
        this.route('users');
      });
      this.route('new');
    });
  });

  // Library
  this.route('library', function() {
    this.route('edit', { path: '/:library_id' }, function() {
    });
    this.route('new');
  });

  // Me
  this.route('yo');

  // Authentication
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
});

export default Router;
