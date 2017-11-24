// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get } from '@ember/object';

import { hash } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

let DRAFT = "draft";
let READY = "ready";
let FABRICATING = "fabricating";
let PREVIEW = "preview";

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let library = this.modelFor('accounts.one.libraries.one');
    let idea =this.modelFor('accounts.one.libraries.one.ideas.one');
    return hash({
      library: library,
      idea: idea,
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
      let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
      let account = this.modelFor('accounts.one');
      let name = 'Preview of "' + idea.get('name') + '" Idea';
      let chain = this.store.createRecord('chain', {
        account: account,
        name: name,
        state: DRAFT,
        type: PREVIEW
      });
      chain.save().then(
        () => {
          get(self, 'display').success('Created chain ' + chain.get('name') + '.');
          self.addIdea(chain);
        },
        (error) => {
          get(self, 'display').error(error);
        });
    }

  },
  // end of route actions

  /**
   * Add Idea to Chain (quick-preview, step 2)
   * @param chain
   */
  addIdea: function (chain) {
    let self = this;
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    let chainIdea = this.store.createRecord('chain-idea', {
      chain: chain,
      idea: idea,
    });
    chainIdea.save().then(
      () => {
        get(self, 'display').success('Added ' + idea.get('name') + ' to ' + chain.get('name') + '.');
        self.addLibrary(chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  },

  /**
   * Add Library to Chain (quick-preview, step 3)
   * @param chain
   */
  addLibrary: function (chain) {
    let self = this;
    let library = this.modelFor('accounts.one.libraries.one');
    let chainLibrary = this.store.createRecord('chain-library', {
      chain: chain,
      library: library,
    });
    chainLibrary.save().then(
      () => {
        get(self, 'display').success('Added ' + library.get('name') + ' to ' + chain.get('name') + '.');
        self.updateToReady(chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  },

  /**
   * Update Chain to Ready-state (quick-preview, step 4)
   * @param chain
   */
  updateToReady: function (chain) {
    let self = this;
    chain.set('state', READY);
    chain.save().then(
      () => {
        get(self, 'display').success('Advanced chain state to ' + READY + '.');
        self.updateToFabricating(chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  },

  /**
   * Update Chain to Fabricating-state (quick-preview, step 5)
   * @param chain
   */
  updateToFabricating: function (chain) {
    let self = this;
    chain.set('state', FABRICATING);
    chain.save().then(
      () => {
        get(self, 'display').success('Advanced chain state to ' + FABRICATING + '.');
        self.transitionTo('accounts.one.chains.one.links', chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  }

});
