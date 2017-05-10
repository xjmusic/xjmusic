// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';
import DS from 'ember-data';
import DataAdapterMixin from 'ember-simple-auth/mixins/data-adapter-mixin';

export default DS.JSONAPIAdapter.extend(DataAdapterMixin, {
  authorizer: 'authorizer:some'
});

export default DS.RESTAdapter.extend({
  namespace: 'api/1',
  pathForType(type) {
    var dashed = Ember.String.dasherize(type);
    return Ember.String.pluralize(dashed);
  }
});

// App.ApplicationAdapter = DS.RESTAdapter.extend({
//   namespace: 'hub'
// });
