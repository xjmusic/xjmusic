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
      libraries: this.store.query('library', {accountId: account.id}),
      libraryToAdd: null,
      chainLibraries: this.store.query('chain-library', {chainId: chain.id}),
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

    removeLibrary(model) {
      model.destroyRecord({}).then(
        () => {
          Ember.get(this, 'display').success('Removed Library from Chain.');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    addLibrary(model) {
      let chainLibrary = this.store.createRecord('chain-library', {
        chain: model.chain,
        library: model.libraryToAdd,
      });
      chainLibrary.save().then(
        () => {
          Ember.get(this, 'display').success('Added ' + model.libraryToAdd.get('name') + ' to ' + model.chain.get('name') + '.');
          // this.transitionToRoute('chains.one.libraries',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

  },

});
