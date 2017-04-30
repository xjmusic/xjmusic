// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import ApplicationAdapter from "./application";

export default ApplicationAdapter.extend({

  /**
   Chain has a custom adapter that overrides the deleteRecord() method, appending a ?destroy=true parameter

   Called by the store when a record is deleted.
   The `deleteRecord` method  makes an Ajax (HTTP DELETE) request to a URL computed by `buildURL`.
   @method deleteRecord
   @param {DS.Store} store
   @param {DS.Model} type
   @param {DS.Snapshot} snapshot
   @return {Promise} promise
   */
  deleteRecord(store, type, snapshot) {
    let id = snapshot.id;

    let url = this.buildURL(type.modelName, id, snapshot, 'deleteRecord');

    if (snapshot.adapterOptions && snapshot.adapterOptions.destroy) {
      url += '?destroy=true';
    }

    return this.ajax(url, 'DELETE');
  },

});
