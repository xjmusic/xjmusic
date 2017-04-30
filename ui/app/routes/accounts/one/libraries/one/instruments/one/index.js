// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

let DRAFT = "draft";
let READY = "ready";
let FABRICATING = "fabricating";
let PREVIEW = "preview";

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let library = this.modelFor('accounts.one.libraries.one');
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    return Ember.RSVP.hash({
      library: library,
      instrument: instrument,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.instrument.get('description'),
      entity: {
        name: 'Instrument',
        id: model.instrument.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    /**
     * Create a quick-preview chain (step 1)
     */
    quickPreview() {
      let self = this;
      let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
      let account = this.modelFor('accounts.one');
      let name = 'Preview of "' + instrument.get('description') + '" Instrument';
      let chain = this.store.createRecord('chain', {
        account: account,
        name: name,
        state: DRAFT,
        type: PREVIEW
      });
      chain.save().then(
        () => {
          Ember.get(self, 'display').success('Created chain ' + chain.get('name') + '.');
          self.addInstrument(chain);
        },
        (error) => {
          Ember.get(self, 'display').error(error);
        });
    }
  },

  /**
   * Add Instrument to Chain (quick-preview, step 2)
   * @param chain
   */
  addInstrument: function (chain) {
    let self = this;
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    let chainInstrument = this.store.createRecord('chain-instrument', {
      chain: chain,
      instrument: instrument,
    });
    chainInstrument.save().then(
      () => {
        Ember.get(self, 'display').success('Added ' + instrument.get('description') + ' to ' + chain.get('name') + '.');
        self.updateToReady(chain);
      },
      (error) => {
        Ember.get(self, 'display').error(error);
      });
  },

  /**
   * Update Chain to Ready-state (quick-preview, step 3)
   * @param chain
   */
  updateToReady: function (chain) {
    let self = this;
    chain.set('state', READY);
    chain.save().then(
      () => {
        Ember.get(self, 'display').success('Advanced chain state to ' + READY + '.');
        self.updateToFabricating(chain);
      },
      (error) => {
        Ember.get(self, 'display').error(error);
      });
  },

  /**
   * Update Chain to Fabricating-state (quick-preview, step 4)
   * @param chain
   */
  updateToFabricating: function (chain) {
    let self = this;
    chain.set('state', FABRICATING);
    chain.save().then(
      () => {
        Ember.get(self, 'display').success('Advanced chain state to ' + FABRICATING + '.');
        self.transitionTo('accounts.one.chains.one', chain);
      },
      (error) => {
        Ember.get(self, 'display').error(error);
      });
  }

});
