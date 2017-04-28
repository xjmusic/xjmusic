// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

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
   * Route Actions
   */
  actions: {

    saveChain(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated chain ' + model.get('name') + '.');
          this.transitionTo('accounts.one.chains');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    destroyChain(model) {
      let confirmation = confirm("Are you fucking sure? If there are Ideas or Instruments belonging to this Chain, deletion will fail anyway.");
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

    willTransition(transition) {
      let model = this.controller.get('model');
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
