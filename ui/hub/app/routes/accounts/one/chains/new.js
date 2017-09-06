// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

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
    let account = this.modelFor('accounts.one');
    return Ember.RSVP.hash({
      chain: this.store.createRecord('chain', {
        account: account,
        state: config.chainStates[0],
        type: config.chainTypes[0]
      })
    });
  },

  /**
   * Headline
   */
  afterModel() {
    let account = this.modelFor('accounts.one');
    Ember.set(this, 'routeHeadline', {
      title: 'New Chain',
      entity: {
        name: 'Account',
        id: account.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createChain(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Created chain ' + model.get('name') + '.');
          this.transitionTo('accounts.one.chains.one', model);
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = this.controller.get('model.chain');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    }

  },

});
