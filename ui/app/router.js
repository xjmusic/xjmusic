// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";
import config from "./config/environment";

const Router = Ember.Router.extend({
  location: config.locationType
});

Router.map(function () {
  this.route('users', {path: '/u'}, users);
  this.route('accounts', {path: '/a'}, accounts);
  this.route('login');
  this.route('logout');
  this.route('unauthorized');
});

function users() {
  this.route('one', {path: '/:user_id'});
}

function accounts() {
  this.route('new'); // New Account
  this.route('one', {path: '/:account_id'}, account); // One Account
}

function account() {
  this.route('edit'); // Edit Account
  this.route('users', {path: '/u'}); // Users in Account
  this.route('libraries', {path: '/lib'}, accountLibraries); // Libraries in Account
}

function accountLibraries() {
  this.route('new'); // New Library
  this.route('one', {path: '/:library_id'}, accountLibrary); // One Library
}

function accountLibrary() {
  this.route('edit'); // Edit Library
  this.route('ideas', accountLibraryIdeas); // Ideas in Library
}

function accountLibraryIdeas() {
  this.route('new'); // New Idea
  this.route('one', {path: '/:idea_id'}, accountLibraryIdea); // One Idea
}

function accountLibraryIdea() {
  this.route('edit'); // Edit Idea
  this.route('memes'); // Memes in Idea
  this.route('phases', accountLibraryIdeaPhases); // Phases in Idea
}

function accountLibraryIdeaPhases() {
  this.route('new'); // New Phase
  this.route('one', {path: '/:phase_id'}, accountLibraryIdeaPhase); // One Phase
}

function accountLibraryIdeaPhase() {
  this.route('edit'); // Edit Phase
  // this.route('memes'); // Memes in Phase
}

export default Router;
