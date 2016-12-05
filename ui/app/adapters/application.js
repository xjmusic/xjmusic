// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';
import DataAdapterMixin from 'ember-simple-auth/mixins/data-adapter-mixin';

export default DS.JSONAPIAdapter.extend(DataAdapterMixin, {
  authorizer: 'authorizer:some'
});
