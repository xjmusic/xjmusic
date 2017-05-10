// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  // Inject: configuration service
  config: Ember.inject.service(),

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
          reject('Could not instantiate Chain model', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let account = this.modelFor('accounts.one');
    let chain = this.modelFor('accounts.one.chains.one');
    chain.set('account', account);
    return Ember.RSVP.hash({
      chain: chain,
      chainFromState: chain.get('state')
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: 'Edit Chain',
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

    saveChain(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated chain.');
          this.transitionTo('accounts.one.chains.one');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    deleteChain(model) {
      let confirmation = confirm("Are you sure? If there are Ideas or Instruments belonging to this Chain, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            Ember.get(this, 'display').success('Deleted chain ' + model.get('name') + '.');
            this.transitionTo('accounts.one.chains');
          },
          (error) => {
            Ember.get(this, 'display').error(error);
          });
      }
    },

    destroyChain(model) {
      let confirmation = confirm("Are you fucking sure??");
      if (confirmation) {
        // Chain has a custom adapter to append query params
        model.destroyRecord({
          adapterOptions: {
            destroy: true
          }
        }).then(
          () => {
            Ember.get(this, 'display').success('Destroyed chain ' + model.get('name') + '.');
            this.transitionTo('accounts.one.chains');
          },
          (error) => {
            Ember.get(this, 'display').error(error);
          });
      }
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

  }

});
