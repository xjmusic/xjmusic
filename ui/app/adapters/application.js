// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';
import DataAdapterMixin from 'ember-simple-auth/mixins/data-adapter-mixin';

export default DS.JSONAPIAdapter.extend(DataAdapterMixin, {
  authorizer: 'authorizer:some'
});

export default DS.RESTAdapter.extend({
  namespace: 'api/1'
});

// App.ApplicationAdapter = DS.RESTAdapter.extend({
//   namespace: 'hub'
// });
