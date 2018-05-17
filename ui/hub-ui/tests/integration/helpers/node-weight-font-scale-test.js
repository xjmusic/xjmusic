
// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { moduleForComponent, test } from 'ember-qunit';
import hbs from 'htmlbars-inline-precompile';

moduleForComponent('node-weight-font-scale', 'helper:node-weight-font-scale', {
  integration: true
});

// Replace this with your real tests.
test('it renders', function(assert) {
  this.set('inputValue', '1234');

  this.render(hbs`{{node-weight-font-scale inputValue}}`);

  assert.equal(this.$().text().trim(), 'weight-1234');
});

