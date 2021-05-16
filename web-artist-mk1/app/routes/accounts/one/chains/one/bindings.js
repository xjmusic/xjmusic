/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Route from '@ember/routing/route';
import {get} from '@ember/object';
import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';

export default Route.extend({

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Promise}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      this.config.getConfig().then(
        () => {
          let account = this.modelFor('accounts.one');
          let chain = this.modelFor('accounts.one.chains.one');

          let chainBindings = this.store.query('chain-binding', {chainId: chain.get('id')})
            .catch((error) => {
              get(this, 'display').error(error);
              this.transitionTo('');
            });

          resolve(hash({
            chain: chain,
            chainBindings: chainBindings,
            libraries: this.store.query('library', {accountId: account.get('id')}),
            programs: this.store.query('program', {accountId: account.get('id')}),
            instruments: this.store.query('instrument', {accountId: account.get('id')}),
            bindingType: '',
            bindingTargetId: '',
            chainBindingTypes: this.config.chainBindingTypes,
          }, 'chain, chain bindings, chain binding types, config to add to chain'));

        },
        (error) => {
          reject('Failed to load config', error);
        }
      );
    });
  },

  //
  actions: {

    //
    sessionChanged: function () {
      this.modelFor('accounts.one.chains.one').reload();
      this.refresh();
    },

    removeBinding(model) {
      model.destroyRecord({}).then(
        () => {
          this.display.success('Removed Binding.');
          this.send("sessionChanged");
        },
        (error) => {
          console.error(error);
          get(this, 'display').error(error);
        });
    },

    addBinding(model) {
      let chainBinding = this.store.createRecord('chain-binding', {
        chain: model.chain,
        type: model.bindingType,
        targetId: model.bindingTargetId
      });

      chainBinding.save().then(
        () => {
          let found;
          switch (chainBinding.type) {
            case "Library":
              found = model.libraries.find(search => search.get("id") === model.bindingTargetId);
              break;

            case "Program":
              found = model.programs.find(search => search.get("id") === model.bindingTargetId);
              break;

            case "Instrument":
              found = model.instruments.find(search => search.get("id") === model.bindingTargetId);
              break;

            default:
              found = {name: "Unknown"}
              break;
          }
          get(this, 'display').success(`Added ${chainBinding.type}: ${found.get("name")}`);

          // this.transitionToRoute('chains.one.configs',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          console.error(error);
          get(this, 'display').error(error);
        });
    },

  },

});
