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
        () => {
          resolve(self.resolvedModel());
        },
        (error) => {
          reject('Could not instantiate new Chain', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let chain = this.modelFor('accounts.one.chains.one');
    let account = this.modelFor('accounts.one');
    return Ember.RSVP.hash({
      chain: chain,
      ideas: this.store.query('idea', {accountId: account.id}),
      ideaToAdd: null,
      chainIdeas: this.store.query('chain-idea', {chainId: chain.id}),
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

    removeIdea(model) {
      model.destroyRecord({}).then(
        () => {
          Ember.get(this, 'display').success('Removed Idea from Chain.');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    addIdea(model) {
      let chainConfig = this.store.createRecord('chain-idea', {
        chain: model.chain,
        idea: model.ideaToAdd,
      });
      chainConfig.save().then(
        () => {
          Ember.get(this, 'display').success('Added ' + model.ideaToAdd.get('name') + ' to ' + model.chain.get('name') + '.');
          // this.transitionToRoute('chains.one.ideas',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

  },

});
