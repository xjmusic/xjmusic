//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Route from '@ember/routing/route';
import {get} from '@ember/object';
import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import {v4 as uuid} from "ember-uuid";

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
      let self = this;
      this.config.getConfig().then(
        () => {
          let account = self.modelFor('accounts.one');
          resolve(hash({
            account: account,
            chain: self.modelFor('accounts.one.chains.one'),
            libraries: this.store.query('library', {accountId: account.get('id')}),
          }, 'chain, all libraries in account'));

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

    /**
     * Remove a chainBinding record
     * @param chainBinding record to remove
     */
    removeBinding(chainBinding) {
      let self = this;
      let chain = this.modelFor('accounts.one.chains.one');
      this.store.deleteRecord(chainBinding);
      chain.save().then(
        () => {
          self.display.success('Removed Binding from Chain.');
          self.send("sessionChanged");
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },

    /**
     * Add a new Binding
     * @param targetClass of new binding
     * @param targetId of new binding
     */
    addBinding(targetClass, targetId) {
      let self = this;
      let chain = this.modelFor('accounts.one.chains.one');
      let addedBinding = this.store.push({
        data: {
          id: uuid(),
          type: 'chain-binding',
          attributes: {
            targetClass: targetClass,
            targetId: targetId
          },
          relationships: {
            chain: {
              data: {
                id: chain.get('id'),
                type: 'chain'
              }
            }
          }
        }
      });
      chain.save().then(
        () => {
          get(self, 'display').success(`Bound ${addedBinding.get('targetClass')} #${addedBinding.get('targetId')}`);
        },
        (error) => {
          chain.rollbackAttributes();
          chain.reload().then(() => {
            get(self, 'display').error(error);
          });
        });
    },

  },


});
