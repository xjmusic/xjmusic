// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';
import DataAdapterMixin from 'ember-simple-auth/mixins/data-adapter-mixin';

export default DS.JSONAPIAdapter.extend(DataAdapterMixin, {
  authorizer: 'authorizer:some'
});

// App.ApplicationAdapter = DS.RESTAdapter.extend({
//   namespace: 'hub'
// });
