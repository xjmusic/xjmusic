//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
import JSONAPISerializer from 'ember-data/serializers/json-api';

/**
 [#167276586] JSON API facilitates complex transactions
 */
export default JSONAPISerializer.extend({

  /**
   * Don't dasherize attribute keys
   * @param attr to not dasherize
   * @return {*} original
   */
  keyForAttribute(attr /*, method*/) {
    return attr;
  },

  /**
   * Don't dasherize relationship keys
   * @param attr to not dasherize
   * @return {*} original
   */
  keyForRelationship(attr /*, method*/) {
    return attr;
  },

  /**
   * Custom tweaks to JSON API payload
   * @param snapshot to tweak
   * @param options for serialization
   */
  serialize(snapshot, options) {
    let self = this;
    let json = this._super(snapshot, options);

    snapshot.eachRelationship(function (name, relationship) {
      let key = relationship.meta.name;
      if ("hasMany" === relationship.meta.kind && self.shouldSerializeHasMany(snapshot, key, "hasMany")) {
        self.includeMany(json, snapshot, key);
      }
    });

    if (snapshot.hasOwnProperty('id')) {
      json.data.id = snapshot.id;
    }

    return json;
  },

  /**
   * Include all records for a given relationship
   * @param json output we are serializing onto
   * @param snapshot of record
   * @param key of relationship
   */
  includeMany(json, snapshot, key) {
    let items = snapshot.hasMany(key);
    if (items && 0 < items.length) {
      for (let i = 0; i < items.length; i++) {
        this.includeOne(json, items[i]);
      }
    }
  },

  /**
   * Include one item
   * @param json output we are serializing onto
   * @param item to include
   */
  includeOne(json, item) {
    if (!json.hasOwnProperty("included")) {
      json["included"] = [];
    }

    json.included.push(this.serialize(item).data);
  }
});
