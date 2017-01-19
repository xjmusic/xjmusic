// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";
import config from "./config/environment";

const Router = Ember.Router.extend({
  location: config.locationType
});

Router.map(function() {
  this.route('admin', function() {
    this.route('users', function() {
      this.route('edit', { path: '/:user_id' });
    });
    this.route('accounts', function() {
      this.route('edit', { path: '/:account_id' }, function() {
        this.route('users');
      });
      this.route('new');
    });
    // this.route('account', { path: '/:account_id' }, function() {
    //   this.route('users');
    // });
    // this.route('account/new');
  });
  this.route('engines', function() {});
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
  this.route('yo');
});

export default Router;
