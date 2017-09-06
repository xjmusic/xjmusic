// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: configuration service
  config: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    return new Ember.RSVP.Promise((resolve, reject) => {
      let self = this;
      Ember.get(this, 'config').promises.config.then(
        (config) => {
          resolve(self.resolvedModel(config));
        },
        (error) => {
          reject('Could not instantiate new Chain', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @param config
   * @returns {*}
   */
  resolvedModel(config) {
    let chain = this.modelFor('accounts.one.chains.one');
    return Ember.RSVP.hash({
      chain: chain,
      chainConfigToAdd: this.store.createRecord('chain-config', {
        chain: chain,
        type: ''
      }),
      chainConfigTypes: config.chainConfigTypes,
      chainConfigs: this.store.query('chain-config', {chainId: chain.id}),
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      // title in breadcrumb
      detail: {
        startAt: model.chain.get('startAt'),
        stopAt: model.chain.get('stopAt')
      },
      entity: {
        name: 'Chain',
        id: model.chain.get('id'),
        state: model.chain.get('state')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    removeConfig(model) {
      model.destroyRecord({}).then(
        () => {
          Ember.get(this, 'display').success('Removed Config from Chain.');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    addConfig(model) {
      let self = this;
      let chainConfig = model.chainConfigToAdd;
      chainConfig.save().then(
        (saved) => {
          Ember.get(self, 'display').success('Added ' + saved.get('type') + '=' + saved.get('value'));
          // this.transitionToRoute('chains.one.configs',model.chain);
          self.send("sessionChanged");
        },
        (error) => {
          Ember.get(self, 'display').error(error);
        });
    },

  },

});
