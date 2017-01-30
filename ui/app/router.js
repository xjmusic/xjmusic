// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";
import config from "./config/environment";

const Router = Ember.Router.extend({
  location: config.locationType
});

Router.map(function () {
  // Users
  this.route('users', {path: '/u'}, function () {

    // User
    this.route('one', {path: '/:user_id'});
  });

  // Accounts
  this.route('accounts', {path: '/a'}, function () {

    // New Account
    this.route('new');

    // Account
    this.route('one', {path: '/:account_id'}, function () {

      // Edit Account
      this.route('edit');

      // Users (in Account)
      this.route('users', {path: '/u'});

      // Libraries (in Account)
      this.route('libraries', {path: '/lib'}, function () {

        // New Library
        this.route('new');

        // Library (in Account)
        this.route('one', {path: '/:library_id'}, function () {

          // Edit Library
          this.route('edit');

        });
      });
    });
  });

  // Me
  this.route('yo');

  // Authentication
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
});

export default Router;
