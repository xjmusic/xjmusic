// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import Component from '@ember/component';
import {computed} from '@ember/object';

const LinkToLibraryComponent = Component.extend({
  library: computed('libraries', 'libraryId', function () {
    let library = '';
    this.libraries.forEach(search => {
      if (Number(search.get('id')) === Number(this.libraryId)) {
        library = search;
      }
    });
    return library;
  }),
});

/**
 * Usage (e.g, in Handlebars):
 *
 *   {{link-to-library <libraryId>}}
 */
LinkToLibraryComponent.reopenClass({
  positionalParams: ['libraries', 'libraryId']
});

export default LinkToLibraryComponent;

