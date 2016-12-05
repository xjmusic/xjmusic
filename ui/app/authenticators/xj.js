// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.

// import Ember from 'ember';
import Base from 'ember-simple-auth/authenticators/base';

// const { service } = Ember.inject;

export default Base.extend({
  // restore(data) {},
  authenticate(type) {
    window.location.replace("/auth/" + type);
  },
  // invalidate(data) {}
});


/*
 return new RSVP.Promise((resolve, reject) => {
 const data                = { 'grant_type': 'password', username: identification, password };
 const serverTokenEndpoint = this.get('serverTokenEndpoint');
 const scopesString = Ember.makeArray(scope).join(' ');
 if (!Ember.isEmpty(scopesString)) {
 data.scope = scopesString;
 }
 this.makeRequest(serverTokenEndpoint, data).then((response) => {
 run(() => {
 const expiresAt = this._absolutizeExpirationTime(response['expires_in']);
 this._scheduleAccessTokenRefresh(response['expires_in'], expiresAt, response['refresh_token']);
 if (!isEmpty(expiresAt)) {
 response = assign(response, { 'expires_at': expiresAt });
 }
 resolve(response);
 });
 }, (xhr) => {
 run(null, reject, xhr.responseJSON || xhr.responseText);
 });
 });


 */
