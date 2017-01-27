import Ember from 'ember';

export default Ember.Service.extend({

  display: Ember.inject.service(),

  messageBus: Ember.inject.service(),

  /**
   * Internal storage of an array of all accounts available.
   */
  accounts: null,

  /**
   * Initialize the Account Service
   */
  init() {
    this.get('messageBus').subscribe("auth-accounts", this, this.onAuthAccounts);
  },

  /**
   * Subscribed to to the 'auth-accounts' event on the message bus
   * @param accountCSV
   */
  onAuthAccounts(accountCSV) {
    this.parseAccountsCSV(accountCSV);
  },

  /**
   * Parse a CSV of accounts and set corresponding booleans
   * "current account" switch to accounts[0] by default
   * @param accounts in CSV format
   */
  parseAccountsCSV: function (accounts) {
    this.accounts = accounts.split(",");
    if (this.accounts.length > 0) {
      this.set('isPresent', true);
      this.set('currentAccountId', this.accounts[0]);
    }
    if (this.accounts.length > 1) {
      this.set('isMultiAccount', true);
    }
  },

  /**
   * "current account" switch to an account
   * @param account
   */
  switchTo(account) {
    this.set('currentAccountId', account.id);
    Ember.get(this, 'display').success('Switched to ' + account.get('name'));
  },

  /**
   * {boolean} if account(s) present
   */
  isPresent: false,

  /**
   * {boolean} if multiple accounts present
   */
  isMultiAccount: false,

  /**
   * {int} id of current account
   */
  currentAccountId: null,

});
